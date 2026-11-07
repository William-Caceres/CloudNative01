package Hotel.brokeradmin.kafka;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import Hotel.brokeradmin.kafka.config.KafkaConsumerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaConsumerConfigTest {

    @Test
    void construyeFactoriesYContainerFactoriesSinConectar() {
        KafkaConsumerConfig config = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(config, "autoOffsetReset", "earliest");
        KafkaTemplate<Object, Object> template = mock(KafkaTemplate.class);

        assertNotNull(config.reservaConsumerFactory());
        assertNotNull(config.pagoConsumerFactory());
        assertNotNull(config.reservaKafkaListenerContainerFactory(
                config.reservaConsumerFactory(), template));
        assertNotNull(config.pagoKafkaListenerContainerFactory(
                config.pagoConsumerFactory(), template));
    }
}
