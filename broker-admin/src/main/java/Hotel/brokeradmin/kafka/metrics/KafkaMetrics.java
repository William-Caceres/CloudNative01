package Hotel.brokeradmin.kafka.metrics;

import Hotel.brokeradmin.kafka.dto.KafkaConsumidorResumen;
import Hotel.brokeradmin.kafka.dto.PagoEvent;
import Hotel.brokeradmin.kafka.dto.ReservaEvent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class KafkaMetrics {

    private final AtomicLong reservasProcesadas = new AtomicLong();
    private final AtomicLong pagosProcesados = new AtomicLong();
    private final AtomicReference<String> ultimoEvento = new AtomicReference<>();
    private final AtomicReference<String> ultimoEventoEn = new AtomicReference<>();
    private final Map<String, Contadores> porConsumidor = new ConcurrentHashMap<>();

    public void registrarReserva(ReservaEvent evento, String consumidor) {
        reservasProcesadas.incrementAndGet();
        registrarUltimo("reserva:" + evento.eventId());
        Contadores contadores = porConsumidor.computeIfAbsent(consumidor, clave -> new Contadores());
        contadores.reservas.incrementAndGet();
        contadores.ultimoEvento.set("reserva:" + evento.eventId());
        contadores.ultimoEventoEn.set(Instant.now().toString());
    }

    public void registrarPago(PagoEvent evento, String consumidor) {
        pagosProcesados.incrementAndGet();
        registrarUltimo("pago:" + evento.eventId());
        Contadores contadores = porConsumidor.computeIfAbsent(consumidor, clave -> new Contadores());
        contadores.pagos.incrementAndGet();
        contadores.ultimoEvento.set("pago:" + evento.eventId());
        contadores.ultimoEventoEn.set(Instant.now().toString());
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

    public List<KafkaConsumidorResumen> detalleConsumidores() {
        return porConsumidor.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entrada -> {
                    Contadores contadores = entrada.getValue();
                    return new KafkaConsumidorResumen(entrada.getKey(),
                            contadores.reservas.get(), contadores.pagos.get(),
                            contadores.ultimoEvento.get(), contadores.ultimoEventoEn.get());
                })
                .toList();
    }

    private void registrarUltimo(String evento) {
        ultimoEvento.set(evento);
        ultimoEventoEn.set(Instant.now().toString());
    }

    private static final class Contadores {
        private final AtomicLong reservas = new AtomicLong();
        private final AtomicLong pagos = new AtomicLong();
        private final AtomicReference<String> ultimoEvento = new AtomicReference<>();
        private final AtomicReference<String> ultimoEventoEn = new AtomicReference<>();
    }
}
