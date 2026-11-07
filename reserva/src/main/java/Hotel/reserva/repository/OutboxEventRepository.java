package Hotel.reserva.repository;

import Hotel.reserva.entity.OutboxEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop50ByEstadoInAndProximoIntentoLessThanEqualOrderByIdAsc(
            List<String> estados, LocalDateTime ahora);

    long countByEstado(String estado);

    List<OutboxEvent> findTop10ByOrderByIdDesc();
}
