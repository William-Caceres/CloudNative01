package Hotel.reserva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PagoRequest(
        @NotBlank(message = "El metodo de pago es obligatorio")
        @Size(max = 30, message = "El metodo de pago no puede superar los 30 caracteres")
        String metodoPago) {
}
