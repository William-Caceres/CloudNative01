package Hotel.rabbitadmin.kafka.dto;

public record KafkaGrupoOffset(
        String topic,
        int particion,
        long offsetConsumido,
        long offsetFinal,
        long lag) {
}
