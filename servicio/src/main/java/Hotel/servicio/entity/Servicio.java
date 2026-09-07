package Hotel.servicio.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="servicio")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Servicio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String tipoServicio;
    private Integer precio;
    private Integer numHabitacion;    
    private Integer capacidad;
    private Boolean disponible;
    private String nivelServicio;
    private String nombre;
    private String descripcion;
    
}
