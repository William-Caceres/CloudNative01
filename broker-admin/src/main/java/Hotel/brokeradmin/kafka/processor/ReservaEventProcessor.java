package Hotel.brokeradmin.kafka.processor;

import Hotel.brokeradmin.kafka.dto.ReservaEvent;
import Hotel.brokeradmin.kafka.metrics.KafkaMetrics;
import Hotel.brokeradmin.listener.MensajeInvalidoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaEventProcessor {

    private final KafkaMetrics metrics;

    public void procesar(ReservaEvent evento, String consumidor) {
        if (evento == null || evento.eventId() == null || evento.reservaId() == null) {
            throw new MensajeInvalidoException("Evento de reserva invalido");
        }
        metrics.registrarReserva(evento, consumidor);
        log.info("Evento de reserva procesado eventId={} reservaId={} estadoPago={}",
                evento.eventId(), evento.reservaId(), evento.estadoPago());
    }
}
