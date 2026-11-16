package Hotel.reserva.messaging;

import Hotel.reserva.dto.PagoEvent;
import Hotel.reserva.dto.ReservaEvent;
import Hotel.reserva.entity.Reserva;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaEventPublisher {

    private static final String EVENTO_RESERVA_CREADA = "RESERVA_CREADA";
    private static final String EVENTO_PAGO_CONFIRMADO = "PAGO_CONFIRMADO";
    private static final int VERSION_EVENTO = 1;

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String topicReservas;
    private final String topicPagos;

    public KafkaEventPublisher(KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${hotel.kafka.topics.reservas}") String topicReservas,
            @Value("${hotel.kafka.topics.pagos}") String topicPagos) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicReservas = topicReservas;
        this.topicPagos = topicPagos;
    }

    public void publicarReservaCreada(Reserva reserva) {
        publicar(topicReservas, String.valueOf(reserva.getId()), reservaEvento(reserva));
    }

    public void publicarPago(Reserva reserva) {
        publicar(topicPagos, String.valueOf(reserva.getId()), pagoEvento(reserva));
    }

    public void enviarReservaCreada(Reserva reserva) {
        enviar(topicReservas, String.valueOf(reserva.getId()), reservaEvento(reserva));
    }

    public void enviarPago(Reserva reserva) {
        enviar(topicPagos, String.valueOf(reserva.getId()), pagoEvento(reserva));
    }

    private ReservaEvent reservaEvento(Reserva reserva) {
        return new ReservaEvent(
                UUID.randomUUID().toString(),
                EVENTO_RESERVA_CREADA,
                Instant.now().toString(),
                VERSION_EVENTO,
                reserva.getId(),
                reserva.getIdUsuario(),
                reserva.getFechaReserva(),
                reserva.getFechaTermino(),
                reserva.getTipoReserva(),
                reserva.getCantidadPersonas(),
                reserva.getValorFinal(),
                estadoPago(reserva),
                reserva.getReferenciaPago());
    }

    private PagoEvent pagoEvento(Reserva reserva) {
        return new PagoEvent(
                UUID.randomUUID().toString(),
                EVENTO_PAGO_CONFIRMADO,
                Instant.now().toString(),
                VERSION_EVENTO,
                reserva.getReferenciaPago(),
                reserva.getId(),
                reserva.getIdUsuario(),
                reserva.getValorFinal(),
                reserva.getMetodoPago(),
                Reserva.ESTADO_PAGO_CONFIRMADO);
    }

    private void enviar(String topic, String key, Object evento) {
        try {
            var resultado = kafkaTemplate.send(topic, key, evento).get(5, TimeUnit.SECONDS);
            log.info("Evento Kafka publicado topic={} key={} partition={} offset={}",
                    topic, key, resultado.getRecordMetadata().partition(),
                    resultado.getRecordMetadata().offset());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo publicar el evento Kafka en " + topic, e);
        }
    }

    private void publicar(String topic, String key, Object evento) {
        try {
            kafkaTemplate.send(topic, key, evento).whenComplete((resultado, error) -> {
                if (error != null) {
                    log.error("No se pudo publicar el evento Kafka en {} con key {}: {}",
                            topic, key, error.getMessage());
                    return;
                }
                if (resultado != null) {
                    log.info("Evento Kafka publicado topic={} key={} partition={} offset={}",
                            topic, key, resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("No se pudo enviar el evento Kafka a {} con key {}: {}", topic, key, e.getMessage());
        }
    }

    private String estadoPago(Reserva reserva) {
        return reserva.getEstadoPago() == null || reserva.getEstadoPago().isBlank()
                ? Reserva.ESTADO_PAGO_PENDIENTE
                : reserva.getEstadoPago();
    }
}
