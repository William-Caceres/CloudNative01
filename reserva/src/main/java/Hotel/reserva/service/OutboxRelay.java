package Hotel.reserva.service;

import Hotel.reserva.entity.OutboxEvent;
import Hotel.reserva.entity.Reserva;
import Hotel.reserva.messaging.KafkaEventPublisher;
import Hotel.reserva.messaging.ReservaEventPublisher;
import Hotel.reserva.repository.OutboxEventRepository;
import Hotel.reserva.repository.ReservaRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private static final int MAX_INTENTOS = 10;
    private static final long BASE_BACKOFF_SEGUNDOS = 2;

    private final OutboxEventRepository outbox;
    private final ReservaRepository reservas;
    private final ReservaEventPublisher rabbitPublisher;
    private final KafkaEventPublisher kafkaPublisher;

    @Scheduled(
            fixedDelayString = "${hotel.outbox.fixed-delay-ms:2000}",
            initialDelayString = "${hotel.outbox.initial-delay-ms:2000}")
    public void procesar() {
        List<OutboxEvent> pendientes = outbox
                .findTop50ByEstadoInAndProximoIntentoLessThanEqualOrderByIdAsc(
                        List.of(OutboxEvent.PENDIENTE, OutboxEvent.FALLIDO), LocalDateTime.now());
        for (OutboxEvent evento : pendientes) {
            procesar(evento);
        }
    }

    private void procesar(OutboxEvent evento) {
        try {
            Reserva reserva = reservas.findById(evento.getReservaId()).orElse(null);
            if (reserva == null) {
                agotar(evento, "La reserva ya no existe");
                return;
            }
            if (OutboxEvent.RESERVA_CREADA.equals(evento.getTipo())) {
                rabbitPublisher.enviarReservaCreada(reserva);
                kafkaPublisher.enviarReservaCreada(reserva);
            } else if (OutboxEvent.PAGO_CONFIRMADO.equals(evento.getTipo())) {
                kafkaPublisher.enviarPago(reserva);
            } else {
                agotar(evento, "Tipo de evento desconocido");
                return;
            }
            evento.setEstado(OutboxEvent.ENVIADO);
            evento.setEnviadoEn(LocalDateTime.now());
            outbox.save(evento);
        } catch (Exception e) {
            evento.setIntentos(evento.getIntentos() + 1);
            evento.setEstado(evento.getIntentos() >= MAX_INTENTOS
                    ? OutboxEvent.AGOTADO
                    : OutboxEvent.FALLIDO);
            long espera = Math.min(60, BASE_BACKOFF_SEGUNDOS * (1L << Math.min(evento.getIntentos(), 5)));
            evento.setProximoIntento(LocalDateTime.now().plusSeconds(espera));
            outbox.save(evento);
            log.warn("Outbox evento id={} tipo={} fallo intento {}/{}: {}",
                    evento.getId(), evento.getTipo(), evento.getIntentos(), MAX_INTENTOS, e.getMessage());
        }
    }

    private void agotar(OutboxEvent evento, String motivo) {
        evento.setEstado(OutboxEvent.AGOTADO);
        outbox.save(evento);
        log.error("Outbox evento id={} agotado: {}", evento.getId(), motivo);
    }
}
