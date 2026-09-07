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
    private Integer idUsuario;
    private String fechaReserva;
    private String fechaTermino;
    private String tipoReserva;
    private Integer cantidadPersonas;
    private Integer valorFinal;
}