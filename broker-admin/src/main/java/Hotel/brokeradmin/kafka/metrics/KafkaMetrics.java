package Hotel.brokeradmin.kafka.metrics;

import Hotel.brokeradmin.kafka.dto.PagoEvent;
import Hotel.brokeradmin.kafka.dto.ReservaEvent;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class KafkaMetrics {

    private final AtomicLong reservasProcesadas = new AtomicLong();
    private final AtomicLong pagosProcesados = new AtomicLong();
    private final AtomicReference<String> ultimoEvento = new AtomicReference<>();
    private final AtomicReference<String> ultimoEventoEn = new AtomicReference<>();

    public void registrarReserva(ReservaEvent evento) {
        reservasProcesadas.incrementAndGet();
        registrarUltimo("reserva:" + evento.eventId());
    }

    public void registrarPago(PagoEvent evento) {
        pagosProcesados.incrementAndGet();
        registrarUltimo("pago:" + evento.eventId());
    }

    public long reservasProcesadas() {
        return reservasProcesadas.get();
    }

    public long pagosProcesados() {
        return pagosProcesados.get();
    }

    public String ultimoEvento() {
        return ultimoEvento.get();
    }

    public String ultimoEventoEn() {
        return ultimoEventoEn.get();
    }

    private void registrarUltimo(String evento) {
        ultimoEvento.set(evento);
        ultimoEventoEn.set(Instant.now().toString());
    }
}
