package Hotel.brokeradmin.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import Hotel.brokeradmin.exception.MensajeriaException;
import Hotel.brokeradmin.kafka.metrics.KafkaMetrics;
import Hotel.brokeradmin.kafka.service.KafkaAdminService;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class KafkaAdminServiceErrorTest {

    private final AdminClient admin = mock(AdminClient.class);
    private final KafkaAdminService service = new KafkaAdminService(admin, new KafkaMetrics());

    @Test
    void topicInexistenteDevuelveNotFound() {
        DescribeTopicsResult resultado = mock(DescribeTopicsResult.class);
        when(admin.describeTopics(List.of("no.existe"))).thenReturn(resultado);
        when(resultado.topicNameValues()).thenReturn(Map.of());

        MensajeriaException ex = assertThrows(MensajeriaException.class,
                () -> service.describirTopic("no.existe"));

        assertEquals("KFK-404", ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
