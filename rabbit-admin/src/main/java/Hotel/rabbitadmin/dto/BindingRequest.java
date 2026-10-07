package Hotel.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Peticion para crear un binding entre una cola y un exchange. */
public record BindingRequest(
        @NotBlank(message = "El nombre de la cola es obligatorio")
        String cola,

        @NotBlank(message = "El nombre del exchange es obligatorio")
        String exchange,

        @NotBlank(message = "La routing key es obligatoria")
        @Pattern(regexp = "^[A-Za-z0-9._*#-]{1,64}$",
                message = "La routing key solo admite letras, numeros, punto, guion, comodin * y # (1 a 64 caracteres)")
        String routingKey) {
}
