package Hotel.reserva.messaging;

import Hotel.reserva.entity.Reserva;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publica reserva.creada hacia hotel.topic (llega a notificaciones y tickets).
 * Fail-safe: si RabbitMQ falla se registra el error, la reserva no se pierde.
 */
@Slf4j
@Component
public class ReservaEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeTopic;
    private final String routingReservaCreada;

    public ReservaEventPublisher(RabbitTemplate rabbitTemplate,
            @Value("${hotel.mensajeria.exchanges.topic}") String exchangeTopic,
            @Value("${hotel.mensajeria.routing.reservaCreada}") String routingReservaCreada) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeTopic = exchangeTopic;
        this.routingReservaCreada = routingReservaCreada;
    }

    public void publicarReservaCreada(Reserva reserva) {
        Map<String, Object> evento = new LinkedHashMap<>();
        evento.put("reservaId", reserva.getId());
        evento.put("idUsuario", reserva.getIdUsuario());
        try {
            rabbitTemplate.convertAndSend(exchangeTopic, routingReservaCreada, evento);
            log.info("Evento reserva.creada publicado para la reserva {}", reserva.getId());
        } catch (Exception e) {
            log.error("No se pudo publicar el evento de la reserva {}: {}", reserva.getId(), e.getMessage());
        }
    }
}
