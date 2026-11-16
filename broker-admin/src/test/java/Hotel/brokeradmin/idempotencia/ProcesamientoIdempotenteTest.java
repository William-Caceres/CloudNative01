package Hotel.brokeradmin.idempotencia;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProcesamientoIdempotenteTest {

    private static final String CONSUMIDOR = "kafka:analytics-group:reservas";
    private static final String EVENTO = "evt-123";

    @Autowired
    private ProcesamientoIdempotente servicio;

    @Autowired
    private EventoProcesadoRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    @Test
    void primerEventoSeReclamaYSegundoSeIgnora() {
        assertTrue(servicio.reclamar(CONSUMIDOR, EVENTO));
        assertFalse(servicio.reclamar(CONSUMIDOR, EVENTO));
    }

    @Test
    void consumidoresDistintosProcesanElMismoEvento() {
        assertTrue(servicio.reclamar(CONSUMIDOR, EVENTO));
        assertTrue(servicio.reclamar("kafka:audit-group:reservas", EVENTO));
    }

    @Test
    void liberarPermiteReprocesarDespuesDeUnFallo() {
        assertTrue(servicio.reclamar(CONSUMIDOR, EVENTO));
        servicio.liberar(CONSUMIDOR, EVENTO);
        assertTrue(servicio.reclamar(CONSUMIDOR, EVENTO));
    }
}
