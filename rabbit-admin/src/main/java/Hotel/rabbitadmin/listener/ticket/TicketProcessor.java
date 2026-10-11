package Hotel.rabbitadmin.listener.ticket;

import Hotel.rabbitadmin.listener.MensajeInvalidoException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Logica de negocio del dominio tickets: genera el ticket de la reserva.
 * No conoce RabbitMQ.
 */
@Slf4j
@Service
public class TicketProcessor {

    public void procesar(Map<String, Object> evento) {
        Object reservaId = evento.get("reservaId");
        if (reservaId == null) {
            throw new MensajeInvalidoException("Evento sin reservaId");
        }
        log.info("Ticket generado para la reserva {}", reservaId);
    }
}
