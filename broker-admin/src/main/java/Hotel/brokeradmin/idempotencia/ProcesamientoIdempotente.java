package Hotel.brokeradmin.idempotencia;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro persistente por consumidor y evento. La marca se crea antes de
 * procesar para que dos entregas simultaneas no ejecuten el mismo efecto; si
 * el procesamiento falla, el listener llama a liberar() antes de reintentar.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcesamientoIdempotente {

    private final EventoProcesadoRepository repository;

    @Transactional
    public boolean reclamar(String consumerKey, String eventKey) {
        if (repository.existsByConsumerKeyAndEventKey(consumerKey, eventKey)) {
            return false;
        }
        try {
            repository.saveAndFlush(new EventoProcesado(consumerKey, eventKey, Instant.now()));
            return true;
        } catch (DataIntegrityViolationException e) {
            log.info("Evento duplicado ignorado consumer={} event={}", consumerKey, eventKey);
            return false;
        }
    }

    @Transactional
    public void liberar(String consumerKey, String eventKey) {
        repository.deleteByConsumerKeyAndEventKey(consumerKey, eventKey);
    }
}
