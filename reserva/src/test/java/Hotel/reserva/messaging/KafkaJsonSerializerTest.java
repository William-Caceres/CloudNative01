package Hotel.reserva.messaging;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Hotel.reserva.dto.PagoEvent;
import Hotel.reserva.dto.ReservaEvent;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerializer;

class KafkaJsonSerializerTest {

    @Test
    void serializaReservaEventConElSerializerDeKafka() {
        ReservaEvent evento = new ReservaEvent("evt-r", "RESERVA_CREADA",
                Instant.now().toString(), 1, 7, 9, "2026-11-01", "2026-11-05",
                "Doble", 2, 100000, "PENDIENTE", null);

        byte[] bytes = new JsonSerializer<ReservaEvent>().serialize("reservas", evento);

        assertNotNull(bytes);
        String json = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(json.contains("\"reservaId\":7"));
        assertTrue(json.contains("\"eventType\":\"RESERVA_CREADA\""));
    }

    @Test
    void serializaPagoEventConElSerializerDeKafka() {
        PagoEvent evento = new PagoEvent("evt-p", "PAGO_CONFIRMADO",
                Instant.now().toString(), 1, "pago-1", 7, 9, 100000,
                "TARJETA", "CONFIRMADO");

        byte[] bytes = new JsonSerializer<PagoEvent>().serialize("pagos", evento);

        assertNotNull(bytes);
        String json = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(json.contains("\"pagoId\":\"pago-1\""));
        assertTrue(json.contains("\"metodoPago\":\"TARJETA\""));
    }
}
