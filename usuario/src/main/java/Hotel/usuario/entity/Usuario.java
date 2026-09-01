package Hotel.usuario.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="usuario")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String contrasenia;
    private Integer telefono;
}
