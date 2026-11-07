package Hotel.brokeradmin.kafka.dto;

import java.util.List;

public record KafkaGrupoInfo(
        String grupo,
        String estado,
        int miembros,
        long lagTotal,
        List<KafkaGrupoOffset> offsets) {
}
