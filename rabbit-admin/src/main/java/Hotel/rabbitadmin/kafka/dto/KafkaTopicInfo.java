package Hotel.rabbitadmin.kafka.dto;

import java.util.List;
import java.util.Map;

public record KafkaTopicInfo(
        String nombre,
        int particiones,
        int replicas,
        List<KafkaParticionInfo> detalle,
        Map<String, String> configuraciones) {
}
