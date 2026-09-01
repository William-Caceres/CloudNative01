package Hotel.reserva.repository;

import Hotel.reserva.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Integer>{
    
}
