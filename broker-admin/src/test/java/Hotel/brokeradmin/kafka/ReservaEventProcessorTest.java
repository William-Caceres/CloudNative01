package Hotel.brokeradmin.kafka;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import Hotel.brokeradmin.kafka.dto.ReservaEvent;
import Hotel.brokeradmin.kafka.metrics.KafkaMetrics;
import Hotel.brokeradmin.kafka.processor.ReservaEventProcessor;
import Hotel.brokeradmin.listener.MensajeInvalidoException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ReservaEventProcessorTest {

    private final ReservaEventProcessor processor = new ReservaEventProcessor(new KafkaMetrics());

    @Test
    void eventoValidoSeProcesa() {
        ReservaEvent evento = new ReservaEvent("evt-1", "RESERVA_CREADA", Instant.now().toString(), 1,
                7, 9, "2026-11-01", "2026-11-05", "Doble", 2, 100000, "PENDIENTE", null);

        assertDoesNotThrow(() -> processor.procesar(evento));
    }

    @Test
    void eventoSinReservaIdSeRechaza() {
        ReservaEvent evento = new ReservaEvent("evt-1", "RESERVA_CREADA", Instant.now().toString(), 1,
                null, 9, "2026-11-01", "2026-11-05", "Doble", 2, 100000, "PENDIENTE", null);

        assertThrows(MensajeInvalidoException.class, () -> processor.procesar(evento));
    }

    @Test
    void eventoSinEventIdSeRechaza() {
        ReservaEvent evento = new ReservaEvent(null, "RESERVA_CREADA", Instant.now().toString(), 1,
                7, 9, "2026-11-01", "2026-11-05", "Doble", 2, 100000, "PENDIENTE", null);

        assertThrows(MensajeInvalidoException.class, () -> processor.procesar(evento));
    }
}
