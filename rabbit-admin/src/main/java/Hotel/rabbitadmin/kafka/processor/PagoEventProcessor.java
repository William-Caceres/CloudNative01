package Hotel.rabbitadmin.kafka.processor;

import Hotel.rabbitadmin.kafka.dto.PagoEvent;
import Hotel.rabbitadmin.kafka.metrics.KafkaMetrics;
import Hotel.rabbitadmin.listener.MensajeInvalidoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoEventProcessor {

    private final KafkaMetrics metrics;

    public void procesar(PagoEvent evento) {
        if (evento == null || evento.eventId() == null || evento.reservaId() == null
                || evento.pagoId() == null) {
            throw new MensajeInvalidoException("Evento de pago invalido");
        }
        metrics.registrarPago(evento);
        log.info("Evento de pago procesado eventId={} reservaId={} pagoId={} estadoPago={}",
                evento.eventId(), evento.reservaId(), evento.pagoId(), evento.estadoPago());
    }
}
