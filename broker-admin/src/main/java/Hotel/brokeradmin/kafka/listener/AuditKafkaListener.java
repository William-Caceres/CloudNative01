package Hotel.brokeradmin.kafka.listener;

import Hotel.brokeradmin.kafka.dto.PagoEvent;
import Hotel.brokeradmin.kafka.dto.ReservaEvent;
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
public class AuditKafkaListener {

    private final ReservaEventProcessor reservaProcessor;
    private final PagoEventProcessor pagoProcessor;

    public AuditKafkaListener(ReservaEventProcessor reservaProcessor,
            PagoEventProcessor pagoProcessor) {
        this.reservaProcessor = reservaProcessor;
        this.pagoProcessor = pagoProcessor;
    }

    @KafkaListener(
            topics = "${hotel.kafka.topics.reservas}",
            groupId = "audit-group",
            containerFactory = "reservaKafkaListenerContainerFactory")
    public void reserva(ReservaEvent evento,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.info("Kafka audit reservas topic={} partition={} offset={} key={} eventId={}",
                topic, partition, offset, key, evento == null ? null : evento.eventId());
        reservaProcessor.procesar(evento);
    }

    @KafkaListener(
            topics = "${hotel.kafka.topics.pagos}",
            groupId = "audit-group",
            containerFactory = "pagoKafkaListenerContainerFactory")
    public void pago(PagoEvent evento,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.info("Kafka audit pagos topic={} partition={} offset={} key={} eventId={}",
                topic, partition, offset, key, evento == null ? null : evento.eventId());
        pagoProcessor.procesar(evento);
    }
}
