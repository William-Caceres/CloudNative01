package Hotel.usuario.messaging.consumer.notificacion;

import com.rabbitmq.client.Channel;
import Hotel.usuario.messaging.event.ReservaEvento;
import Hotel.usuario.messaging.exception.MensajeInvalidoException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumidor del dominio "notificacion" (cola configurada en app.rabbit.queues.notificaciones.name).
 *
 * Política de confirmación (ACK manual):
 *  - Éxito                      -> basicAck.
 *  - MensajeInvalidoException   -> basicNack(requeue=false): NO recuperable, directo a la DLQ.
 *  - Cualquier otro error       -> basicNack(requeue=true): recuperable, se reintenta; al superar
 *                                  el delivery-limit de la cola quorum RabbitMQ lo manda a la DLQ.
 *  Todo caso queda registrado en logs.
 */
@Component
public class NotificacionConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionConsumer.class);

    private final NotificacionProcessor processor;

    public NotificacionConsumer(NotificacionProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = "${app.rabbit.queues.notificaciones.name}")
    public void consumir(ReservaEvento evento,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                         @Header(name = "x-delivery-count", required = false) Long entregas) throws IOException {
        try {
            processor.procesar(evento);
            channel.basicAck(tag, false);
        } catch (MensajeInvalidoException e) {
            log.error("[NOTIFICACION] Mensaje inválido, se envía a DLQ: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        } catch (Exception e) {
            log.warn("[NOTIFICACION] Error recuperable (entrega n°{}), se reencola: {}",
                    entregas == null ? 1 : entregas + 1, e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
}
