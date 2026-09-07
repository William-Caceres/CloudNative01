package Hotel.servicio.dto;

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
public class ServicioRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "El tipo de servicio es obligatorio")
    private String tipoServicio;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private Integer precio;

    @NotNull(message = "El numero de habitacion es obligatorio")
    @Positive(message = "El numero de habitacion debe ser positivo")
    private Integer numHabitacion;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser positiva")
    private Integer capacidad;

    @NotNull(message = "La disponibilidad es obligatoria")
    private Boolean disponible;

    @NotBlank(message = "El nivel de servicio es obligatorio")
    private String nivelServicio;
}