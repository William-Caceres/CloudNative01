package Hotel.rabbitadmin.kafka;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import Hotel.rabbitadmin.kafka.dto.PagoEvent;
import Hotel.rabbitadmin.kafka.metrics.KafkaMetrics;
import Hotel.rabbitadmin.kafka.processor.PagoEventProcessor;
import Hotel.rabbitadmin.listener.MensajeInvalidoException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PagoEventProcessorTest {

    private final PagoEventProcessor processor = new PagoEventProcessor(new KafkaMetrics());

    @Test
    void eventoValidoSeProcesa() {
        PagoEvent evento = new PagoEvent("evt-2", "PAGO_CONFIRMADO", Instant.now().toString(), 1,
                "pago-123", 7, 9, 100000, "TARJETA", "CONFIRMADO");

        assertDoesNotThrow(() -> processor.procesar(evento));
    }

    @Test
    void eventoSinPagoIdSeRechaza() {
        PagoEvent evento = new PagoEvent("evt-2", "PAGO_CONFIRMADO", Instant.now().toString(), 1,
                null, 7, 9, 100000, "TARJETA", "CONFIRMADO");

        assertThrows(MensajeInvalidoException.class, () -> processor.procesar(evento));
    }

    @Test
    void eventoSinReservaIdSeRechaza() {
        PagoEvent evento = new PagoEvent("evt-2", "PAGO_CONFIRMADO", Instant.now().toString(), 1,
                "pago-123", null, 9, 100000, "TARJETA", "CONFIRMADO");

        assertThrows(MensajeInvalidoException.class, () -> processor.procesar(evento));
    }
}
