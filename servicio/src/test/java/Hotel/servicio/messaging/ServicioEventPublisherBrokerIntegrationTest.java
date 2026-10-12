package Hotel.servicio.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import Hotel.servicio.entity.Servicio;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Integracion real del productor contra el broker local (se omite si no esta disponible). */
@SpringBootTest
@ActiveProfiles({"test", "integration"})
class ServicioEventPublisherBrokerIntegrationTest {

    private static final int PUERTO_BROKER = 5680;
    private static final long ESPERA_MAXIMA_MS = 10000;

    @Autowired
    private ServicioEventPublisher publisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private PublicacionObservable observador;

    @Value("${hotel.mensajeria.exchanges.topic}")
    private String exchangeTopic;

    @Value("${hotel.mensajeria.routing.servicioActualizado}")
    private String routingServicioActualizado;

    @BeforeAll
    static void brokerDisponible() {
        assumeTrue(puertoAbierto("localhost", PUERTO_BROKER),
                "broker local no disponible en " + PUERTO_BROKER + " (se omite la suite de integracion)");
    }

    private static boolean puertoAbierto(String host, int puerto) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, puerto), 1000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    void publicaEnElBrokerRealYElEventoLlegaALaColaDeAuditoria() throws InterruptedException {
        String colaAuditoria = "audit.servicio.test.queue";
        TopicExchange exchange = new TopicExchange(exchangeTopic, true, false);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(QueueBuilder.durable(colaAuditoria).build());
        rabbitAdmin.declareBinding(BindingBuilder.bind(new Queue(colaAuditoria)).to(exchange)
                .with(routingServicioActualizado));
        try {
            Servicio servicio = new Servicio();
            servicio.setId(42);

            publisher.publicarServicioActualizado(servicio);

            Object recibido = null;
            long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
            while (recibido == null && System.currentTimeMillis() < limite) {
                recibido = rabbitTemplate.receiveAndConvert(colaAuditoria, 500);
            }

            assertNotNull(recibido);
            @SuppressWarnings("unchecked")
            Map<String, Object> evento = (Map<String, Object>) recibido;
            assertEquals(42, evento.get("servicioId"));
        } finally {
            rabbitAdmin.deleteQueue(colaAuditoria);
        }
    }

    @Test
    void publicacionSinColaDestinoSeConfirmaYSeDevuelve() throws InterruptedException {
        TopicExchange exchange = new TopicExchange(exchangeTopic, true, false);
        rabbitAdmin.declareExchange(exchange);
        long confirmadasAntes = observador.confirmadas();
        long devueltasAntes = observador.devueltas();

        rabbitTemplate.convertAndSend(exchangeTopic, "audit.sin.destino.d3", Map.of("x", 1));

        long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
        while ((observador.confirmadas() <= confirmadasAntes || observador.devueltas() <= devueltasAntes)
                && System.currentTimeMillis() < limite) {
            Thread.sleep(100);
        }
        assertTrue(observador.confirmadas() > confirmadasAntes,
                "el broker debe confirmar la publicacion");
        assertTrue(observador.devueltas() > devueltasAntes,
                "un mensaje sin cola destino debe devolverse");
    }
}
