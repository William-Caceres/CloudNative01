package Hotel.brokeradmin.kafka.listener;

import Hotel.brokeradmin.kafka.dto.PagoEvent;
import Hotel.brokeradmin.kafka.dto.ReservaEvent;
import Hotel.brokeradmin.idempotencia.ProcesamientoIdempotente;
import Hotel.brokeradmin.kafka.processor.PagoEventProcessor;
import Hotel.brokeradmin.kafka.processor.ReservaEventProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "hotel.kafka.consumidores-activos", havingValue = "true", matchIfMissing = true)
public class AnalyticsKafkaListener {

    private static final String CONSUMIDOR_RESERVAS = "kafka:analytics-group:reservas";
    private static final String CONSUMIDOR_PAGOS = "kafka:analytics-group:pagos";

    private final ReservaEventProcessor reservaProcessor;
    private final PagoEventProcessor pagoProcessor;
    private final ProcesamientoIdempotente idempotente;

    public AnalyticsKafkaListener(ReservaEventProcessor reservaProcessor,
            PagoEventProcessor pagoProcessor,
            ProcesamientoIdempotente idempotente) {
        this.reservaProcessor = reservaProcessor;
        this.pagoProcessor = pagoProcessor;
        this.idempotente = idempotente;
    }

    @KafkaListener(
            topics = "${hotel.kafka.topics.reservas}",
            groupId = "analytics-group",
            containerFactory = "reservaKafkaListenerContainerFactory")
    public void reserva(ReservaEvent evento,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.info("Kafka analytics reservas topic={} partition={} offset={} key={} eventId={}",
                topic, partition, offset, key, evento == null ? null : evento.eventId());
        procesarReserva(evento);
    }

    @KafkaListener(
            topics = "${hotel.kafka.topics.pagos}",
            groupId = "analytics-group",
            containerFactory = "pagoKafkaListenerContainerFactory")
    public void pago(PagoEvent evento,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.info("Kafka analytics pagos topic={} partition={} offset={} key={} eventId={}",
                topic, partition, offset, key, evento == null ? null : evento.eventId());
        procesarPago(evento);
    }

    private void procesarReserva(ReservaEvent evento) {
        if (evento == null || evento.eventId() == null) {
            reservaProcessor.procesar(evento, CONSUMIDOR_RESERVAS);
            return;
        }
        if (!idempotente.reclamar(CONSUMIDOR_RESERVAS, evento.eventId())) {
            log.info("Kafka analytics reservas duplicado ignorado eventId={}", evento.eventId());
            return;
        }
        try {
            reservaProcessor.procesar(evento, CONSUMIDOR_RESERVAS);
        } catch (RuntimeException e) {
            idempotente.liberar(CONSUMIDOR_RESERVAS, evento.eventId());
            throw e;
        }
    }

    private void procesarPago(PagoEvent evento) {
        if (evento == null || evento.eventId() == null) {
            pagoProcessor.procesar(evento, CONSUMIDOR_PAGOS);
            return;
        }
        if (!idempotente.reclamar(CONSUMIDOR_PAGOS, evento.eventId())) {
            log.info("Kafka analytics pagos duplicado ignorado eventId={}", evento.eventId());
            return;
        }
        try {
            pagoProcessor.procesar(evento, CONSUMIDOR_PAGOS);
        } catch (RuntimeException e) {
            idempotente.liberar(CONSUMIDOR_PAGOS, evento.eventId());
            throw e;
        }
    }
}
