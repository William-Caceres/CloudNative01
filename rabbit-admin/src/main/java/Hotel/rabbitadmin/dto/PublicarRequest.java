package Hotel.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Peticion para publicar un mensaje de prueba en un exchange. */
public record PublicarRequest(
        @NotBlank(message = "El exchange es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9._-]{1,64}$",
                message = "El nombre solo admite letras, números, punto, guion y guion bajo (1 a 64 caracteres)")
        String exchange,

        @NotBlank(message = "La routing key es obligatoria")
        @Pattern(regexp = "^[A-Za-z0-9._*#-]{1,64}$",
                message = "La routing key solo admite letras, números, punto, guion, comodín * y # (1 a 64 caracteres)")
        String routingKey,

        @NotBlank(message = "El payload es obligatorio")
        @Size(max = 10000, message = "El payload no puede superar los 10000 caracteres")
        String payload) {
}
