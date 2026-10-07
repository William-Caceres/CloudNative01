package Hotel.reserva.messaging.producer;

import Hotel.reserva.config.RabbitNamesProperties;
import Hotel.reserva.entity.Reserva;
import Hotel.reserva.messaging.event.ReservaEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Productor: publica los eventos de una reserva creada. Si RabbitMQ falla, se registra el error
 * pero la reserva NO se pierde (la mensajería no debe afectar la lógica existente).
 */
@Component
public class ReservaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReservaEventPublisher.class);
    private static final String RESERVA_CREADA = "RESERVA_CREADA";

    private final RabbitTemplate rabbitTemplate;
    private final RabbitNamesProperties props;

    public ReservaEventPublisher(RabbitTemplate rabbitTemplate, RabbitNamesProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publicarReservaCreada(Reserva r) {
        ReservaEvento evento = new ReservaEvento(RESERVA_CREADA, r.getId(), r.getIdUsuario(),
                r.getTipoReserva(), r.getFechaReserva(), r.getFechaTermino(),
                r.getCantidadPersonas(), r.getValorFinal());
        try {
            // 1) Notificación -> exchange direct
            rabbitTemplate.convertAndSend(props.exchanges().direct(),
                    props.queues().notificaciones().routingKey(), evento);
            // 2) Ticket -> exchange topic
            rabbitTemplate.convertAndSend(props.exchanges().topic(),
                    props.routingKeys().ticketGenerar(), evento);
            // 3) Documento -> exchange topic
            rabbitTemplate.convertAndSend(props.exchanges().topic(),
                    props.routingKeys().documentoGenerar(), evento);
            log.info("Eventos de la reserva {} publicados en RabbitMQ", r.getId());
        } catch (Exception e) {
            log.error("No se pudo publicar los eventos de la reserva {}: {}", r.getId(), e.getMessage());
        }
    }
}
