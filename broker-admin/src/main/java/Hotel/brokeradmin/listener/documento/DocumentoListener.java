package Hotel.brokeradmin.listener.documento;

import Hotel.brokeradmin.idempotencia.ProcesamientoIdempotente;
import Hotel.brokeradmin.listener.MensajeInvalidoException;
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
 * Consumidor del dominio documentos (eventos servicio.*). Politica de confirmacion:
 * exito -> ACK, MensajeInvalidoException -> NACK sin reencolar (DLQ directa),
 * fallo recuperable -> NACK reencolando UNA vez y, si vuelve a fallar, NACK sin
 * reencolar (DLQ).
 */
@Slf4j
@Component
@Profile("!test")
public class DocumentoListener {

    private static final String CONSUMIDOR = "rabbit:documentos.queue";

    private final DocumentoProcessor processor;
    private final ProcesamientoIdempotente idempotente;

    public DocumentoListener(DocumentoProcessor processor,
            ProcesamientoIdempotente idempotente) {
        this.processor = processor;
        this.idempotente = idempotente;
    }

    @RabbitListener(queues = "${hotel.mensajeria.colas.documentos}")
    public void procesarDocumento(@Payload Map<String, Object> evento,
                                  Channel channel,
                                  Message mensaje,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        String clave = evento.get("servicioId") == null ? null : String.valueOf(evento.get("servicioId"));
        if (clave != null && !idempotente.reclamar(CONSUMIDOR, clave)) {
            log.info("Documento duplicado ignorado servicioId={}", clave);
            channel.basicAck(deliveryTag, false);
            return;
        }
        try {
            processor.procesar(evento);
            channel.basicAck(deliveryTag, false);
        } catch (MensajeInvalidoException e) {
            liberar(clave);
            log.error("Documento invalido, se deriva a DLQ: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            liberar(clave);
            if (mensaje.getMessageProperties().isRedelivered()) {
                log.error("Reintento agotado, el documento se deriva a DLQ: {}", e.getMessage());
                channel.basicNack(deliveryTag, false, false);
            } else {
                log.warn("Fallo recuperable, se reintenta una vez el documento: {}", e.getMessage());
                channel.basicNack(deliveryTag, false, true);
            }
        }
    }

    private void liberar(String clave) {
        if (clave != null) {
            idempotente.liberar(CONSUMIDOR, clave);
        }
    }
}
