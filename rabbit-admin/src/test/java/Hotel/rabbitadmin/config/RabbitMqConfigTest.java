package Hotel.rabbitadmin.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Contrato de la topologia: nombres, durabilidad, DLX y bindings exactos. */
@SpringBootTest
@ActiveProfiles("test")
class RabbitMqConfigTest {

    @Autowired
    private RabbitMqConfig config;

    @Test
    void colasDeDominioSonDurablesConSuDlxYSuDlq() {
        assertColaConDlq(config.colaNotificaciones(), "notificaciones.queue", "notificaciones.dlq");
        assertColaConDlq(config.colaTickets(), "tickets.queue", "tickets.dlq");
        assertColaConDlq(config.colaDocumentos(), "documentos.queue", "documentos.dlq");
    }

    private void assertColaConDlq(Queue cola, String nombreEsperado, String dlqEsperada) {
        assertEquals(nombreEsperado, cola.getName());
        assertTrue(cola.isDurable(), nombreEsperado + " debe ser durable");
        assertEquals("hotel.dlx", cola.getArguments().get("x-dead-letter-exchange"));
        assertEquals(dlqEsperada, cola.getArguments().get("x-dead-letter-routing-key"));
    }

    @Test
    void dlqsSonDurables() {
        List<Queue> dlqs = List.of(config.dlqNotificaciones(), config.dlqTickets(), config.dlqDocumentos());
        assertEquals(Set.of("notificaciones.dlq", "tickets.dlq", "documentos.dlq"),
                dlqs.stream().map(Queue::getName).collect(Collectors.toSet()));
        assertTrue(dlqs.stream().allMatch(Queue::isDurable));
    }

    @Test
    void exchangesDeclaradosConSuNombreYTipo() {
        assertTrue(config.exchangeTopic() instanceof TopicExchange);
        assertEquals("hotel.topic", config.exchangeTopic().getName());
        assertTrue(config.exchangeDirect() instanceof DirectExchange);
        assertEquals("hotel.direct", config.exchangeDirect().getName());
        assertTrue(config.exchangeDlx() instanceof DirectExchange);
        assertEquals("hotel.dlx", config.exchangeDlx().getName());
    }

    @Test
    void bindingsDelDominioExactos() {
        List<Binding> bindings = List.of(
                config.notificacionesEscuchaReservas(config.exchangeTopic(), config.colaNotificaciones()),
                config.ticketsEscuchaReservaCreada(config.exchangeTopic(), config.colaTickets()),
                config.documentosEscuchaServicios(config.exchangeTopic(), config.colaDocumentos()),
                config.ticketsPorTareaDirecta(config.exchangeDirect(), config.colaTickets()),
                config.dlxHaciaDlqNotificaciones(config.exchangeDlx(), config.dlqNotificaciones()),
                config.dlxHaciaDlqTickets(config.exchangeDlx(), config.dlqTickets()),
                config.dlxHaciaDlqDocumentos(config.exchangeDlx(), config.dlqDocumentos()));

        Set<String> tuplas = bindings.stream()
                .map(b -> b.getExchange() + " -> " + b.getDestination() + " [" + b.getRoutingKey() + "]")
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "hotel.topic -> notificaciones.queue [reserva.*]",
                "hotel.topic -> tickets.queue [reserva.creada]",
                "hotel.topic -> documentos.queue [servicio.*]",
                "hotel.direct -> tickets.queue [ticket.generar]",
                "hotel.dlx -> notificaciones.dlq [notificaciones.dlq]",
                "hotel.dlx -> tickets.dlq [tickets.dlq]",
                "hotel.dlx -> documentos.dlq [documentos.dlq]"), tuplas);
    }
}
