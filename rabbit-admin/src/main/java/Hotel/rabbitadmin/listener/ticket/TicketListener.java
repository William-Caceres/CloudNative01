package Hotel.rabbitadmin.listener.ticket;

import Hotel.rabbitadmin.listener.MensajeInvalidoException;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumidor del dominio tickets (reserva.creada). Politica de confirmacion:
 * exito -> ACK, MensajeInvalidoException -> NACK sin reencolar (DLQ directa),
 * fallo recuperable -> NACK reencolando UNA vez y, si vuelve a fallar, NACK sin
 * reencolar (DLQ).
 */
@Slf4j
@Component
// En perfil test no se registra: los tests de contexto no deben abrir conexiones al broker
@Profile("!test")
public class TicketListener {

    private final TicketProcessor processor;

    public TicketListener(TicketProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = "${hotel.mensajeria.colas.tickets}")
    public void procesarTicket(@Payload Map<String, Object> evento,
                               Channel channel,
                               Message mensaje,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            processor.procesar(evento);
            channel.basicAck(deliveryTag, false);
        } catch (MensajeInvalidoException e) {
            log.error("Ticket invalido, se deriva a DLQ: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            if (mensaje.getMessageProperties().isRedelivered()) {
                log.error("Reintento agotado, el ticket se deriva a DLQ: {}", e.getMessage());
                channel.basicNack(deliveryTag, false, false);
            } else {
                log.warn("Fallo recuperable, se reintenta una vez el ticket: {}", e.getMessage());
                channel.basicNack(deliveryTag, false, true);
            }
        }
    }
}
