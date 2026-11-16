package Hotel.brokeradmin.idempotencia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoProcesadoRepository extends JpaRepository<EventoProcesado, Long> {

    boolean existsByConsumerKeyAndEventKey(String consumerKey, String eventKey);

    void deleteByConsumerKeyAndEventKey(String consumerKey, String eventKey);
}
