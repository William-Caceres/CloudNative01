package Hotel.reserva.messaging.consumer.ticket;

import Hotel.reserva.messaging.event.ReservaEvento;
import Hotel.reserva.messaging.exception.MensajeInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Lógica de negocio del dominio "ticket": generar el ticket de la reserva. No sabe nada de RabbitMQ. */
@Service
public class TicketProcessor {

    private static final Logger log = LoggerFactory.getLogger(TicketProcessor.class);

    public void procesar(ReservaEvento evento) {
        // Validación -> si falla, el mensaje es irrecuperable (NACK sin requeue => DLQ)
        if (evento == null || evento.idReserva() == null || evento.idUsuario() == null) {
            throw new MensajeInvalidoException("Evento sin idReserva o idUsuario");
        }

        // TODO: reemplazar por la lógica real (enviar correo, generar PDF, guardar ticket, etc.).
        // Cualquier excepción que lance (BD caída, servicio externo, ...) se trata como
        // RECUPERABLE: NACK con requeue, y tras app.rabbit.delivery-limit intentos va a la DLQ.
        log.info("[TICKET] Procesado evento {} de la reserva {} (usuario {})",
                evento.tipoEvento(), evento.idReserva(), evento.idUsuario());
    }
}
