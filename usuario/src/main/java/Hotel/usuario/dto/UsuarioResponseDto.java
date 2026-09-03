package Hotel.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDto {

    private Integer id;
    private String nombre;
    private String s_nombre;
    private String a_paterno;
    private String a_materno;
    private Integer rut;
    private String dv_rut;
    private Integer edad;
    private String f_registro;
    private String tipo_usuario;
    private String correo;
    private Integer telefono;
}
