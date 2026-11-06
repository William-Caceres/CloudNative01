package Hotel.rabbitadmin.kafka.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

public record KafkaTopicRequest(
        @NotBlank(message = "El nombre del topic es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9._-]{1,249}$",
                message = "El nombre solo admite letras, números, punto, guion y guion bajo (1 a 249 caracteres)")
        String nombre,

        @NotNull(message = "La cantidad de particiones es obligatoria")
        @Min(value = 1, message = "El topic debe tener al menos 1 partición")
        @Max(value = 100, message = "El topic no puede superar 100 particiones")
        Integer particiones,

        @NotNull(message = "El factor de replicación es obligatorio")
        @Min(value = 1, message = "El factor de replicación debe ser al menos 1")
        @Max(value = 10, message = "El factor de replicación no puede superar 10")
        Integer replicas,

        Map<String, String> configuraciones) {
}
