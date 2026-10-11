package Hotel.rabbitadmin.listener.notificacion;

import Hotel.rabbitadmin.listener.MensajeInvalidoException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Logica de negocio del dominio notificaciones: genera la notificacion del cliente.
 * No conoce RabbitMQ.
 */
@Slf4j
@Service
public class NotificacionProcessor {

    public void procesar(Map<String, Object> evento) {
        Object reservaId = evento.get("reservaId");
        if (reservaId == null) {
            throw new MensajeInvalidoException("Evento sin reservaId");
        }
        log.info("Notificacion enviada para la reserva {}", reservaId);
    }
}
