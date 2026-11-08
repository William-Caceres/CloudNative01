package Hotel.brokeradmin.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Hotel.brokeradmin.kafka.dto.PagoEvent;
import Hotel.brokeradmin.kafka.dto.ReservaEvent;
import Hotel.brokeradmin.kafka.metrics.KafkaMetrics;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class KafkaMetricsTest {

    @Test
    void cuentaReservasYPagosYRegistraElUltimoEvento() {
        KafkaMetrics metrics = new KafkaMetrics();
        metrics.registrarReserva(new ReservaEvent("evt-r", "RESERVA_CREADA",
                Instant.now().toString(), 1, 7, 9, "2026-11-01", "2026-11-05",
                "Doble", 2, 100000, "PENDIENTE", null), "kafka:analytics-group:reservas");
        metrics.registrarPago(new PagoEvent("evt-p", "PAGO_CONFIRMADO",
                Instant.now().toString(), 1, "pago-1", 7, 9, 100000,
                "TARJETA", "CONFIRMADO"), "kafka:analytics-group:pagos");

        assertEquals(1, metrics.reservasProcesadas());
        assertEquals(1, metrics.pagosProcesados());
        assertEquals("pago:evt-p", metrics.ultimoEvento());
        assertNotNull(metrics.ultimoEventoEn());
        assertEquals(2, metrics.detalleConsumidores().size());
        assertTrue(metrics.detalleConsumidores().stream()
                .anyMatch(d -> d.consumidor().equals("kafka:analytics-group:reservas") && d.reservasProcesadas() == 1));
        assertTrue(metrics.detalleConsumidores().stream()
                .anyMatch(d -> d.consumidor().equals("kafka:analytics-group:pagos") && d.pagosProcesados() == 1));
    }
}
