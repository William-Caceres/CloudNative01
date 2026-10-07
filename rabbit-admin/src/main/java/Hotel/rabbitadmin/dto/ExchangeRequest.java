package Hotel.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Peticion para crear un exchange. */
public record ExchangeRequest(
        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9._-]{1,64}$",
                message = "El nombre solo admite letras, numeros, punto, guion y guion bajo (1 a 64 caracteres)")
        String nombre,

        @NotBlank(message = "El tipo de exchange es obligatorio")
        @Pattern(regexp = "direct|topic", message = "El tipo debe ser direct o topic")
        String tipo) {
}
