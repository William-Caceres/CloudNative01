package Hotel.brokeradmin.kafka.processor;

import Hotel.brokeradmin.kafka.dto.PagoEvent;
import Hotel.brokeradmin.kafka.metrics.KafkaMetrics;
import Hotel.brokeradmin.listener.MensajeInvalidoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoEventProcessor {

    private final KafkaMetrics metrics;

    public void procesar(PagoEvent evento, String consumidor) {
        if (evento == null || evento.eventId() == null || evento.reservaId() == null
                || evento.pagoId() == null) {
            throw new MensajeInvalidoException("Evento de pago invalido");
        }
        metrics.registrarPago(evento, consumidor);
        log.info("Evento de pago procesado eventId={} reservaId={} pagoId={} estadoPago={}",
                evento.eventId(), evento.reservaId(), evento.pagoId(), evento.estadoPago());
    }
}
