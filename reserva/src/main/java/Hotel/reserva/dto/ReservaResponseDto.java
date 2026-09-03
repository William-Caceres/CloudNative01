package Hotel.reserva.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaResponseDto {

    private Integer id;
    private String f_reserva;
    private String f_termino;
    private String tipo_reserva;
    private Integer c_personas;
    private Integer valor_final;
}
