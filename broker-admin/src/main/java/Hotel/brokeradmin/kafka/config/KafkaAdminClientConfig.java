package Hotel.brokeradmin.kafka.config;

import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaAdminClientConfig {

    @Bean(destroyMethod = "close")
    public AdminClient kafkaAdminClient(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));
    }
}
