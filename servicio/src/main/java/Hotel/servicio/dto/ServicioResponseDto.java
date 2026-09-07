package Hotel.servicio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioResponseDto {

    private Integer id;
    private String nombre;
    private String descripcion;
    private String tipoServicio;
    private Integer precio;
    private Integer numHabitacion;
    private Integer capacidad;
    private Boolean disponible;
    private String nivelServicio;
}