package Hotel.reserva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaRequestDto {

    @NotNull(message = "El id de usuario es obligatorio")
    @Positive(message = "El id de usuario debe ser positivo")
    private Integer idUsuario;

    @NotBlank(message = "La fecha de reserva es obligatoria")
    private String fechaReserva;

    @NotBlank(message = "La fecha de termino es obligatoria")
    private String fechaTermino;

    @NotBlank(message = "El tipo de reserva es obligatorio")
    private String tipoReserva;

    @NotNull(message = "La cantidad de personas es obligatoria")
    @Positive(message = "La cantidad de personas debe ser positiva")
    private Integer cantidadPersonas;

    @NotNull(message = "El valor final es obligatorio")
    @Positive(message = "El valor final debe ser positivo")
    private Integer valorFinal;
}