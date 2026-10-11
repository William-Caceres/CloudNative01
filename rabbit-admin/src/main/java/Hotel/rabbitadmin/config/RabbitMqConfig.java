package Hotel.rabbitadmin.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia centralizada: 3 colas con DLQ, exchange topic, exchange direct y DLX.
 * Los nombres vienen de application.yml.
 */
@Configuration
public class RabbitMqConfig {

    // Retencion de las DLQ: 24 h (politica de retencion de la guia 2.2.3)
    private static final long DLQ_TTL_MS = 24L * 60 * 60 * 1000;
    private static final String ARG_TTL = "x-message-ttl";

    @Value("${hotel.mensajeria.exchanges.topic}")
    private String exchangeTopic;

    @Value("${hotel.mensajeria.exchanges.direct}")
    private String exchangeDirect;

    @Value("${hotel.mensajeria.exchanges.dlx}")
    private String exchangeDlx;

    @Value("${hotel.mensajeria.colas.notificaciones}")
    private String colaNotificaciones;

    @Value("${hotel.mensajeria.colas.tickets}")
    private String colaTickets;

    @Value("${hotel.mensajeria.colas.documentos}")
    private String colaDocumentos;

    @Value("${hotel.mensajeria.dlq.notificaciones}")
    private String dlqNotificaciones;

    @Value("${hotel.mensajeria.dlq.tickets}")
    private String dlqTickets;

    @Value("${hotel.mensajeria.dlq.documentos}")
    private String dlqDocumentos;

    @Value("${hotel.mensajeria.routing.reservaTodos}")
    private String routingReservaTodos;

    @Value("${hotel.mensajeria.routing.reservaCreada}")
    private String routingReservaCreada;

    @Value("${hotel.mensajeria.routing.servicioTodos}")
    private String routingServicioTodos;

    @Value("${hotel.mensajeria.routing.ticketDirecto}")
    private String routingTicketDirecto;

    // Los eventos viajan como JSON y se convierten a Map en los @Payload
    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // Administrador: declara y elimina recursos sobre el broker
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // Declara la topologia al arrancar (el dashboard la muestra sin trafico)
    @Bean
    @ConditionalOnProperty(name = "hotel.mensajeria.declarar-al-inicio", havingValue = "true", matchIfMissing = true)
    public ApplicationRunner declararTopologiaAlInicio(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }

    // Exchanges
    @Bean
    public TopicExchange exchangeTopic() {
        return ExchangeBuilder.topicExchange(exchangeTopic).durable(true).build();
    }

    @Bean
    public DirectExchange exchangeDirect() {
        return ExchangeBuilder.directExchange(exchangeDirect).durable(true).build();
    }

    @Bean
    public DirectExchange exchangeDlx() {
        return ExchangeBuilder.directExchange(exchangeDlx).durable(true).build();
    }

    // Colas de dominio: un mensaje rechazado se deriva a su DLQ por el DLX
    @Bean
    public Queue colaNotificaciones() {
        return colaConDlq(colaNotificaciones, dlqNotificaciones);
    }

    @Bean
    public Queue colaTickets() {
        return colaConDlq(colaTickets, dlqTickets);
    }

    @Bean
    public Queue colaDocumentos() {
        return colaConDlq(colaDocumentos, dlqDocumentos);
    }

    // DLQs
    @Bean
    public Queue dlqNotificaciones() {
        return QueueBuilder.durable(dlqNotificaciones).withArgument(ARG_TTL, DLQ_TTL_MS).build();
    }

    @Bean
    public Queue dlqTickets() {
        return QueueBuilder.durable(dlqTickets).withArgument(ARG_TTL, DLQ_TTL_MS).build();
    }

    @Bean
    public Queue dlqDocumentos() {
        return QueueBuilder.durable(dlqDocumentos).withArgument(ARG_TTL, DLQ_TTL_MS).build();
    }

    private Queue colaConDlq(String cola, String dlq) {
        return QueueBuilder.durable(cola)
                .withArgument("x-dead-letter-exchange", exchangeDlx)
                .withArgument("x-dead-letter-routing-key", dlq)
                .build();
    }

    // Bindings del exchange topic (eventos del dominio)
    @Bean
    public Binding notificacionesEscuchaReservas(TopicExchange exchange, Queue colaNotificaciones) {
        return BindingBuilder.bind(colaNotificaciones).to(exchange).with(routingReservaTodos);
    }

    @Bean
    public Binding ticketsEscuchaReservaCreada(TopicExchange exchange, Queue colaTickets) {
        return BindingBuilder.bind(colaTickets).to(exchange).with(routingReservaCreada);
    }

    @Bean
    public Binding documentosEscuchaServicios(TopicExchange exchange, Queue colaDocumentos) {
        return BindingBuilder.bind(colaDocumentos).to(exchange).with(routingServicioTodos);
    }

    // Binding del exchange direct (tarea puntual)
    @Bean
    public Binding ticketsPorTareaDirecta(@Qualifier("exchangeDirect") DirectExchange exchange, Queue colaTickets) {
        return BindingBuilder.bind(colaTickets).to(exchange).with(routingTicketDirecto);
    }

    // Bindings del DLX hacia cada DLQ
    @Bean
    public Binding dlxHaciaDlqNotificaciones(@Qualifier("exchangeDlx") DirectExchange exchange, Queue dlqNotificaciones) {
        return BindingBuilder.bind(dlqNotificaciones).to(exchange).with(dlqNotificaciones.getName());
    }

    @Bean
    public Binding dlxHaciaDlqTickets(@Qualifier("exchangeDlx") DirectExchange exchange, Queue dlqTickets) {
        return BindingBuilder.bind(dlqTickets).to(exchange).with(dlqTickets.getName());
    }

    @Bean
    public Binding dlxHaciaDlqDocumentos(@Qualifier("exchangeDlx") DirectExchange exchange, Queue dlqDocumentos) {
        return BindingBuilder.bind(dlqDocumentos).to(exchange).with(dlqDocumentos.getName());
    }
}
