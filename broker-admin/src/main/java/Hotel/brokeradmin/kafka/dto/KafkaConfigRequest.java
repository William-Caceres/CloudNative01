package Hotel.brokeradmin.kafka.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

public record KafkaConfigRequest(
        @NotEmpty(message = "Debe indicar al menos una configuración")
        Map<String, String> configuraciones) {
}
