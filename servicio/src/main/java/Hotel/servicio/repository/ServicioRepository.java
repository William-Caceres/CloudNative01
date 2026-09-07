package Hotel.servicio.repository;

import Hotel.servicio.entity.Servicio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Integer>{
    
    List<Servicio> findByNumHabitacion(Integer numHabitacion);
}
