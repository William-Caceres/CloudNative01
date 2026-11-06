package Hotel.rabbitadmin.kafka.dto;

public record KafkaGrupoResumen(
        String grupo,
        String estado,
        int miembros,
        long lagTotal) {
}
