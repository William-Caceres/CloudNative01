package Hotel.rabbitadmin.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import Hotel.rabbitadmin.kafka.dto.KafkaConfigRequest;
import Hotel.rabbitadmin.kafka.dto.KafkaResumen;
import Hotel.rabbitadmin.kafka.dto.KafkaTopicInfo;
import Hotel.rabbitadmin.kafka.dto.KafkaTopicRequest;
import Hotel.rabbitadmin.kafka.metrics.KafkaMetrics;
import Hotel.rabbitadmin.kafka.service.KafkaAdminService;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Test;

class KafkaAdminServiceTest {

    private static final int PUERTO_KAFKA = 9092;
    private static final long ESPERA_MAXIMA_MS = 10000;

    @Test
    void creaDescribeAlteraYEliminaUnTopicLocal() throws Exception {
        assumeTrue(puertoAbierto("localhost", PUERTO_KAFKA),
                "Kafka local no disponible en " + PUERTO_KAFKA + " (se omite esta prueba)");
        String nombre = "prueba.admin." + UUID.randomUUID().toString().replace("-", "");
        AdminClient admin = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + PUERTO_KAFKA));
        KafkaAdminService service = new KafkaAdminService(admin, new KafkaMetrics());
        try {
            service.crearTopic(new KafkaTopicRequest(nombre, 1, 3,
                    Map.of(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")));

            assertTrue(service.listarTopics().contains(nombre));
            KafkaTopicInfo creado = service.describirTopic(nombre);
            assertEquals(1, creado.particiones());
            assertEquals(3, creado.replicas());
            assertEquals("2", creado.configuraciones().get(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG));

            service.alterarConfiguraciones(nombre,
                    new KafkaConfigRequest(Map.of(TopicConfig.RETENTION_MS_CONFIG, "120000")));
            KafkaTopicInfo alterado = service.describirTopic(nombre);
            assertEquals("120000", alterado.configuraciones().get(TopicConfig.RETENTION_MS_CONFIG));

            KafkaResumen resumen = service.resumen();
            assertEquals(3, resumen.brokers());
            assertTrue(resumen.topics() >= 1);
        } finally {
            try {
                service.eliminarTopic(nombre);
            } catch (RuntimeException ignored) {
                // El topic pudo no haberse creado.
            }
            try {
                esperarSinTopic(service, nombre);
            } finally {
                admin.close();
            }
        }
    }

    private void esperarSinTopic(KafkaAdminService service, String nombre) throws InterruptedException {
        long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
        while (System.currentTimeMillis() < limite && service.listarTopics().contains(nombre)) {
            Thread.sleep(250);
        }
        assertFalse(service.listarTopics().contains(nombre));
    }

    private static boolean puertoAbierto(String host, int puerto) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, puerto), 1000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
