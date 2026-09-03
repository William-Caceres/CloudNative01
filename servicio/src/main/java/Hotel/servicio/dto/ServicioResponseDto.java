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
    private String tipo_servicio;
    private Integer precio;
    private Integer n_habitacion;
    private Integer capacidad;
    private Boolean disponible;
    private String nivel_servicio;
}
