package Hotel.reserva.config;

import Hotel.reserva.config.RabbitNamesProperties.QueueDef;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ del sistema hotelero. Es IDÉNTICA en los 3 micros: declarar
 * exchanges/colas/bindings es idempotente, así el sistema arranca en cualquier orden.
 *
 * Rutas de mensajería:
 *  - Notificaciones: hotel.direct --(notificacion.enviar)--> q.notificaciones  (DLQ: q.notificaciones.dlq)
 *  - Tickets:        hotel.topic  --(reserva.ticket.*)-----> q.tickets         (DLQ: q.tickets.dlq)
 *  - Documentos:     hotel.topic  --(reserva.documento.*)--> q.documentos      (DLQ: q.documentos.dlq)
 *  - Toda cola mal procesada se dead-letterea a hotel.dlx con la routing key = nombre de su DLQ.
 */
@Configuration
@EnableConfigurationProperties(RabbitNamesProperties.class)
public class RabbitConfig {

    private final RabbitNamesProperties props;

    public RabbitConfig(RabbitNamesProperties props) {
        this.props = props;
    }

    // ---------- Conversor JSON ----------
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // ---------- Exchanges ----------
    @Bean
    public DirectExchange hotelDirectExchange() {
        return new DirectExchange(props.exchanges().direct(), true, false);
    }

    @Bean
    public TopicExchange hotelTopicExchange() {
        return new TopicExchange(props.exchanges().topic(), true, false);
    }

    @Bean
    public DirectExchange hotelDlxExchange() {
        return new DirectExchange(props.exchanges().dlx(), true, false);
    }

    // ---------- Caso de uso 1: notificaciones ----------
    @Bean
    public Queue notificacionesQueue() {
        return mainQueue(props.queues().notificaciones());
    }

    @Bean
    public Queue notificacionesDlq() {
        return dlq(props.queues().notificaciones());
    }

    @Bean
    public Binding notificacionesBinding() {
        return BindingBuilder.bind(notificacionesQueue()).to(hotelDirectExchange())
                .with(props.queues().notificaciones().routingKey());
    }

    @Bean
    public Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq()).to(hotelDlxExchange())
                .with(props.queues().notificaciones().dlq());
    }

    // ---------- Caso de uso 2: tickets ----------
    @Bean
    public Queue ticketsQueue() {
        return mainQueue(props.queues().tickets());
    }

    @Bean
    public Queue ticketsDlq() {
        return dlq(props.queues().tickets());
    }

    @Bean
    public Binding ticketsBinding() {
        return BindingBuilder.bind(ticketsQueue()).to(hotelTopicExchange())
                .with(props.queues().tickets().routingKey());
    }

    @Bean
    public Binding ticketsDlqBinding() {
        return BindingBuilder.bind(ticketsDlq()).to(hotelDlxExchange())
                .with(props.queues().tickets().dlq());
    }

    // ---------- Caso de uso 3: documentos ----------
    @Bean
    public Queue documentosQueue() {
        return mainQueue(props.queues().documentos());
    }

    @Bean
    public Queue documentosDlq() {
        return dlq(props.queues().documentos());
    }

    @Bean
    public Binding documentosBinding() {
        return BindingBuilder.bind(documentosQueue()).to(hotelTopicExchange())
                .with(props.queues().documentos().routingKey());
    }

    @Bean
    public Binding documentosDlqBinding() {
        return BindingBuilder.bind(documentosDlq()).to(hotelDlxExchange())
                .with(props.queues().documentos().dlq());
    }

    // ---------- Helpers ----------
    /** Cola quorum (replicable en el clúster) con DLX y límite de reentregas. */
    private Queue mainQueue(QueueDef def) {
        return QueueBuilder.durable(def.name())
                .quorum()
                .deadLetterExchange(props.exchanges().dlx())
                .deadLetterRoutingKey(def.dlq())
                .deliveryLimit(props.deliveryLimit())
                .build();
    }

    private Queue dlq(QueueDef def) {
        return QueueBuilder.durable(def.dlq()).quorum().build();
    }
}
