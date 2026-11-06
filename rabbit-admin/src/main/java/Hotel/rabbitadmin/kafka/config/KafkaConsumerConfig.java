package Hotel.rabbitadmin.kafka.config;

import Hotel.rabbitadmin.kafka.dto.PagoEvent;
import Hotel.rabbitadmin.kafka.dto.ReservaEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(name = "hotel.kafka.consumidores-activos", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerConfig {

    private static final long INTERVALO_REINTENTO_MS = 1000L;
    private static final long REINTENTOS = 2L;
    private static final int CONCURRENCIA = 3;
    private static final String SUFIJO_DLT = ".DLT";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Bean
    public ConsumerFactory<String, ReservaEvent> reservaConsumerFactory() {
        return consumerFactory(ReservaEvent.class);
    }

    @Bean
    public ConsumerFactory<String, PagoEvent> pagoConsumerFactory() {
        return consumerFactory(PagoEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReservaEvent> reservaKafkaListenerContainerFactory(
            ConsumerFactory<String, ReservaEvent> consumerFactory,
            KafkaTemplate<Object, Object> kafkaTemplate) {
        return containerFactory(consumerFactory, kafkaTemplate);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PagoEvent> pagoKafkaListenerContainerFactory(
            ConsumerFactory<String, PagoEvent> consumerFactory,
            KafkaTemplate<Object, Object> kafkaTemplate) {
        return containerFactory(consumerFactory, kafkaTemplate);
    }

    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> tipo) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(tipo, false)));
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> containerFactory(
            ConsumerFactory<String, T> consumerFactory,
            KafkaTemplate<Object, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(CONCURRENCIA);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + SUFIJO_DLT, record.partition()));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer, new FixedBackOff(INTERVALO_REINTENTO_MS, REINTENTOS));
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
