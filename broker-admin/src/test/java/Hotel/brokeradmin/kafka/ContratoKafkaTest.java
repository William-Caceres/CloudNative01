package Hotel.brokeradmin.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;

import Hotel.brokeradmin.kafka.dto.PagoEvent;
import Hotel.brokeradmin.kafka.dto.ReservaEvent;
import java.io.InputStream;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ContratoKafkaTest {

    @Test
    void reservaEventCoincideConElContratoCompartido() throws Exception {
        assertRecord(ReservaEvent.class, "ReservaEvent");
    }

    @Test
    void pagoEventCoincideConElContratoCompartido() throws Exception {
        assertRecord(PagoEvent.class, "PagoEvent");
    }

    private void assertRecord(Class<?> tipo, String nombre) throws Exception {
        Map<String, String> esperado = contrato(nombre);
        Map<String, String> actual = new LinkedHashMap<>();
        Arrays.stream(tipo.getRecordComponents())
                .forEach(componente -> actual.put(componente.getName(), tipoSimple(componente)));
        assertEquals(esperado, actual, "El DTO " + nombre
                + " no coincide con contracts/kafka-eventos.properties");
    }

    private Map<String, String> contrato(String nombre) throws Exception {
        Path desdeModulo = Path.of("..", "contracts", "kafka-eventos.properties");
        Path ruta = Files.exists(desdeModulo)
                ? desdeModulo
                : Path.of("contracts", "kafka-eventos.properties");
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(ruta)) {
            props.load(in);
        }
        String prefijo = nombre + ".";
        Map<String, String> campos = new LinkedHashMap<>();
        props.stringPropertyNames().stream()
                .filter(clave -> clave.startsWith(prefijo))
                .sorted()
                .forEach(clave -> campos.put(clave.substring(prefijo.length()), props.getProperty(clave)));
        return campos;
    }

    private String tipoSimple(RecordComponent componente) {
        return componente.getType().getSimpleName();
    }
}
