package Hotel.rabbitadmin.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import Hotel.rabbitadmin.kafka.dto.PagoEvent;
import Hotel.rabbitadmin.kafka.dto.ReservaEvent;
import Hotel.rabbitadmin.kafka.metrics.KafkaMetrics;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class KafkaMetricsTest {

    @Test
    void cuentaReservasYPagosYRegistraElUltimoEvento() {
        KafkaMetrics metrics = new KafkaMetrics();
        metrics.registrarReserva(new ReservaEvent("evt-r", "RESERVA_CREADA",
                Instant.now().toString(), 1, 7, 9, "2026-11-01", "2026-11-05",
                "Doble", 2, 100000, "PENDIENTE", null));
        metrics.registrarPago(new PagoEvent("evt-p", "PAGO_CONFIRMADO",
                Instant.now().toString(), 1, "pago-1", 7, 9, 100000,
                "TARJETA", "CONFIRMADO"));

        assertEquals(1, metrics.reservasProcesadas());
        assertEquals(1, metrics.pagosProcesados());
        assertEquals("pago:evt-p", metrics.ultimoEvento());
        assertNotNull(metrics.ultimoEventoEn());
    }
}
