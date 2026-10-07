package Hotel.usuario.messaging.consumer.notificacion;

import Hotel.usuario.messaging.event.ReservaEvento;
import Hotel.usuario.messaging.exception.MensajeInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Lógica de negocio del dominio "notificacion": notificar al usuario. No sabe nada de RabbitMQ. */
@Service
public class NotificacionProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificacionProcessor.class);

    public void procesar(ReservaEvento evento) {
        // Validación -> si falla, el mensaje es irrecuperable (NACK sin requeue => DLQ)
        if (evento == null || evento.idReserva() == null || evento.idUsuario() == null) {
            throw new MensajeInvalidoException("Evento sin idReserva o idUsuario");
        }

        // TODO: reemplazar por la lógica real (enviar correo, generar PDF, guardar ticket, etc.).
        // Cualquier excepción que lance (BD caída, servicio externo, ...) se trata como
        // RECUPERABLE: NACK con requeue, y tras app.rabbit.delivery-limit intentos va a la DLQ.
        log.info("[NOTIFICACION] Procesado evento {} de la reserva {} (usuario {})",
                evento.tipoEvento(), evento.idReserva(), evento.idUsuario());
    }
}
