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
    
    private String tipo_servicio;
    private Integer precio;
    private Integer n_habitacion;    
    private Integer capacidad;
    private Boolean disponible;
    private String nivel_servicio;
    
}
