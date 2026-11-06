package Hotel.rabbitadmin.kafka.dto;

import java.util.List;

public record KafkaParticionInfo(
        int particion,
        Integer lider,
        List<Integer> replicas,
        List<Integer> isr) {
}
