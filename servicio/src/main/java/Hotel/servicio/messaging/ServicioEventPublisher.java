package Hotel.servicio.messaging;

import Hotel.servicio.entity.Servicio;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publica servicio.actualizado hacia hotel.topic (llega a documentos).
 * Fail-safe: si RabbitMQ falla se registra el error, el servicio no se pierde.
 */
@Slf4j
@Component
public class ServicioEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeTopic;
    private final String routingServicioActualizado;

    public ServicioEventPublisher(RabbitTemplate rabbitTemplate,
            @Value("${hotel.mensajeria.exchanges.topic}") String exchangeTopic,
            @Value("${hotel.mensajeria.routing.servicioActualizado}") String routingServicioActualizado) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeTopic = exchangeTopic;
        this.routingServicioActualizado = routingServicioActualizado;
    }

    public void publicarServicioActualizado(Servicio servicio) {
        Map<String, Object> evento = new LinkedHashMap<>();
        evento.put("servicioId", servicio.getId());
        try {
            rabbitTemplate.convertAndSend(exchangeTopic, routingServicioActualizado, evento);
            log.info("Evento servicio.actualizado publicado para el servicio {}", servicio.getId());
        } catch (Exception e) {
            log.error("No se pudo publicar el evento del servicio {}: {}", servicio.getId(), e.getMessage());
        }
    }
}
