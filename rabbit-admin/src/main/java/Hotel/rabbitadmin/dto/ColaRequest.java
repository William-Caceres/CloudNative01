package Hotel.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Peticion para crear una cola. */
public record ColaRequest(
        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9._-]{1,64}$",
                message = "El nombre solo admite letras, números, punto, guion y guion bajo (1 a 64 caracteres)")
        String nombre) {
}
