package Hotel.brokeradmin.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;

import Hotel.brokeradmin.kafka.config.KafkaTopicsConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Test;

class KafkaTopicsConfigTest {

    private static final String RESERVAS = "reservas";
    private static final String PAGOS = "pagos";
    private static final String RESERVAS_DLT = "reservas.DLT";
    private static final String PAGOS_DLT = "pagos.DLT";

    private final KafkaTopicsConfig config =
            new KafkaTopicsConfig(RESERVAS, PAGOS, RESERVAS_DLT, PAGOS_DLT);

    @Test
    void reservasTieneTresParticionesTresReplicasYRetencionDeSieteDias() {
        assertTopic(config.reservasTopic(), RESERVAS, "604800000");
    }

    @Test
    void pagosTieneTresParticionesTresReplicasYRetencionDeTreintaDias() {
        assertTopic(config.pagosTopic(), PAGOS, "2592000000");
    }

    @Test
    void reservasDltTieneTresParticionesTresReplicasYRetencionDeSieteDias() {
        assertTopic(config.reservasDltTopic(), RESERVAS_DLT, "604800000");
    }

    @Test
    void pagosDltTieneTresParticionesTresReplicasYRetencionDeSieteDias() {
        assertTopic(config.pagosDltTopic(), PAGOS_DLT, "604800000");
    }

    private void assertTopic(NewTopic topic, String nombre, String retencionMs) {
        assertEquals(nombre, topic.name());
        assertEquals("3", String.valueOf(topic.numPartitions()));
        assertEquals("3", String.valueOf(topic.replicationFactor()));
        assertEquals("2", topic.configs().get(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG));
        assertEquals(TopicConfig.CLEANUP_POLICY_DELETE,
                topic.configs().get(TopicConfig.CLEANUP_POLICY_CONFIG));
        assertEquals(retencionMs, topic.configs().get(TopicConfig.RETENTION_MS_CONFIG));
    }
}
