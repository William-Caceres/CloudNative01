package Hotel.brokeradmin.kafka;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import Hotel.brokeradmin.kafka.dto.PagoEvent;
import Hotel.brokeradmin.kafka.dto.ReservaEvent;
import Hotel.brokeradmin.kafka.listener.AnalyticsKafkaListener;
import Hotel.brokeradmin.kafka.listener.AuditKafkaListener;
import Hotel.brokeradmin.kafka.processor.PagoEventProcessor;
import Hotel.brokeradmin.kafka.processor.ReservaEventProcessor;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class KafkaListenerTest {

    private final ReservaEventProcessor reservaProcessor = mock(ReservaEventProcessor.class);
    private final PagoEventProcessor pagoProcessor = mock(PagoEventProcessor.class);
    private final AnalyticsKafkaListener analytics =
            new AnalyticsKafkaListener(reservaProcessor, pagoProcessor);
    private final AuditKafkaListener audit =
            new AuditKafkaListener(reservaProcessor, pagoProcessor);

    @Test
    void analyticsProcesaReservaYMetadata() {
        ReservaEvent evento = reserva();

        analytics.reserva(evento, "reservas", 2, 10L, "7");

        verify(reservaProcessor).procesar(evento);
    }

    @Test
    void analyticsProcesaReservaNula() {
        analytics.reserva(null, "reservas", 2, 10L, null);

        verify(reservaProcessor).procesar(null);
    }

    @Test
    void analyticsProcesaPagoYMetadata() {
        PagoEvent evento = pago();

        analytics.pago(evento, "pagos", 1, 11L, "7");

        verify(pagoProcessor).procesar(evento);
    }

    @Test
    void analyticsProcesaPagoNulo() {
        analytics.pago(null, "pagos", 1, 11L, null);

        verify(pagoProcessor).procesar(null);
    }

    @Test
    void auditProcesaReservaYMetadata() {
        ReservaEvent evento = reserva();

        audit.reserva(evento, "reservas", 0, 12L, "7");

        verify(reservaProcessor).procesar(evento);
    }

    @Test
    void auditProcesaReservaNula() {
        audit.reserva(null, "reservas", 0, 12L, null);

        verify(reservaProcessor).procesar(null);
    }

    @Test
    void auditProcesaPagoYMetadata() {
        PagoEvent evento = pago();

        audit.pago(evento, "pagos", 2, 13L, "7");

        verify(pagoProcessor).procesar(evento);
    }

    @Test
    void auditProcesaPagoNulo() {
        audit.pago(null, "pagos", 2, 13L, null);

        verify(pagoProcessor).procesar(null);
    }

    private ReservaEvent reserva() {
        return new ReservaEvent("evt-r", "RESERVA_CREADA", Instant.now().toString(), 1,
                7, 9, "2026-11-01", "2026-11-05", "Doble", 2, 100000, "PENDIENTE", null);
    }

    private PagoEvent pago() {
        return new PagoEvent("evt-p", "PAGO_CONFIRMADO", Instant.now().toString(), 1,
                "pago-1", 7, 9, 100000, "TARJETA", "CONFIRMADO");
    }
}
