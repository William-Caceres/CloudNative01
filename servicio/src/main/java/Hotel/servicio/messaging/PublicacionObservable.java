package Hotel.servicio.messaging;

import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Observa confirms y returns del productor: registra si el broker acepto el
 * mensaje y los devueltos sin cola destino. Los contadores alimentan las pruebas.
 */
@Slf4j
@Component
public class PublicacionObservable implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    private final AtomicLong confirmadas = new AtomicLong();
    private final AtomicLong rechazadas = new AtomicLong();
    private final AtomicLong devueltas = new AtomicLong();

    @Override
    public void confirm(CorrelationData correlacion, boolean ack, String causa) {
        if (ack) {
            confirmadas.incrementAndGet();
            log.info("Publicacion confirmada por el broker ({} en total)", confirmadas.get());
        } else {
            rechazadas.incrementAndGet();
            log.error("El broker NO confirmo la publicacion: {}", causa);
        }
    }

    @Override
    public void returnedMessage(ReturnedMessage retorno) {
        devueltas.incrementAndGet();
        log.error("Mensaje devuelto sin cola destino: exchange={} routingKey={} causa={}",
                retorno.getExchange(), retorno.getRoutingKey(), retorno.getReplyText());
    }

    public long confirmadas() {
        return confirmadas.get();
    }

    public long rechazadas() {
        return rechazadas.get();
    }

    public long devueltas() {
        return devueltas.get();
    }
}
