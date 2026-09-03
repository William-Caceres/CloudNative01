package Hotel.usuario.dto;

import jakarta.validation.constraints.Email;
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
public class UsuarioRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String s_nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String a_paterno;

    private String a_materno;

    @NotNull(message = "El RUT es obligatorio")
    private Integer rut;

    @NotBlank(message = "El dígito verificador es obligatorio")
    private String dv_rut;

    @Positive(message = "La edad debe ser un número positivo")
    private Integer edad;

    private String tipo_usuario;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenia;

    private Integer telefono;
}
