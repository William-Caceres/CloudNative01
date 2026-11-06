package Hotel.rabbitadmin.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "hotel.kafka.crear-topicos", havingValue = "true", matchIfMissing = true)
public class KafkaTopicsConfig {

    private static final int PARTICIONES = 3;
    private static final int REPLICAS = 3;
    private static final String MIN_ISR = "2";
    private static final long RETENCION_RESERVAS_MS = 7L * 24 * 60 * 60 * 1000;
    private static final long RETENCION_PAGOS_MS = 30L * 24 * 60 * 60 * 1000;
    private static final long RETENCION_DLT_MS = 7L * 24 * 60 * 60 * 1000;

    private final String topicReservas;
    private final String topicPagos;
    private final String topicReservasDlt;
    private final String topicPagosDlt;

    public KafkaTopicsConfig(
            @Value("${hotel.kafka.topics.reservas}") String topicReservas,
            @Value("${hotel.kafka.topics.pagos}") String topicPagos,
            @Value("${hotel.kafka.topics.reservas-dlt}") String topicReservasDlt,
            @Value("${hotel.kafka.topics.pagos-dlt}") String topicPagosDlt) {
        this.topicReservas = topicReservas;
        this.topicPagos = topicPagos;
        this.topicReservasDlt = topicReservasDlt;
        this.topicPagosDlt = topicPagosDlt;
    }

    @Bean
    public NewTopic reservasTopic() {
        return topic(topicReservas, RETENCION_RESERVAS_MS);
    }

    @Bean
    public NewTopic pagosTopic() {
        return topic(topicPagos, RETENCION_PAGOS_MS);
    }

    @Bean
    public NewTopic reservasDltTopic() {
        return topic(topicReservasDlt, RETENCION_DLT_MS);
    }

    @Bean
    public NewTopic pagosDltTopic() {
        return topic(topicPagosDlt, RETENCION_DLT_MS);
    }

    private NewTopic topic(String nombre, long retencionMs) {
        return TopicBuilder.name(nombre)
                .partitions(PARTICIONES)
                .replicas(REPLICAS)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, MIN_ISR)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.RETENTION_MS_CONFIG, Long.toString(retencionMs))
                .build();
    }
}
