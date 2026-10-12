package Hotel.rabbitadmin.listener.documento;

import Hotel.rabbitadmin.listener.MensajeInvalidoException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Logica de negocio del dominio documentos: genera el documento del servicio.
 * No conoce RabbitMQ.
 */
@Slf4j
@Service
public class DocumentoProcessor {

    public void procesar(Map<String, Object> evento) {
        Object servicioId = evento.get("servicioId");
        if (servicioId == null) {
            throw new MensajeInvalidoException("Evento sin servicioId");
        }
        log.info("Documento generado para el servicio {}", servicioId);
    }
}
