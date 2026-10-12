package Hotel.rabbitadmin.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import Hotel.rabbitadmin.service.RabbitAdminService;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Flujo real de mensajeria contra un broker local (se omite si no esta disponible). */
@SpringBootTest
@ActiveProfiles("integration")
class ListenerBrokerIntegrationTest {

    private static final int PUERTO_BROKER = 5680;
    private static final long ESPERA_MAXIMA_MS = 15000;

    @Autowired
    private RabbitAdminService ser;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${hotel.mensajeria.exchanges.topic}")
    private String exchangeTopic;

    @Value("${hotel.mensajeria.colas.notificaciones}")
    private String colaNotificaciones;

    @Value("${hotel.mensajeria.colas.tickets}")
    private String colaTickets;

    @Value("${hotel.mensajeria.colas.documentos}")
    private String colaDocumentos;

    @Value("${hotel.mensajeria.dlq.notificaciones}")
    private String dlqNotificaciones;

    @Value("${hotel.mensajeria.dlq.tickets}")
    private String dlqTickets;

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
    void losTresConsumidoresQuedanConectados() throws InterruptedException {
        long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
        while (System.currentTimeMillis() < limite
                && (ser.ra_infoCola(colaNotificaciones).consumidores() < 1
                        || ser.ra_infoCola(colaTickets).consumidores() < 1
                        || ser.ra_infoCola(colaDocumentos).consumidores() < 1)) {
            Thread.sleep(250);
        }

        assertTrue(ser.ra_infoCola(colaNotificaciones).consumidores() >= 1);
        assertTrue(ser.ra_infoCola(colaTickets).consumidores() >= 1);
        assertTrue(ser.ra_infoCola(colaDocumentos).consumidores() >= 1);
    }

    @Test
    void eventoValidoSeConsumeYConfirma() throws InterruptedException {
        rabbitTemplate.convertAndSend(exchangeTopic, "reserva.creada", Map.of("reservaId", 123));

        esperarColaEn(colaNotificaciones, 0);
        esperarColaEn(colaTickets, 0);

        assertEquals(0, ser.ra_infoCola(colaNotificaciones).mensajes());
        assertEquals(0, ser.ra_infoCola(colaTickets).mensajes());
    }

    @Test
    void eventoInvalidoTerminaEnSuDlq() throws InterruptedException {
        long antes = ser.ra_infoCola(dlqNotificaciones).mensajes();

        rabbitTemplate.convertAndSend(exchangeTopic, "reserva.otro", Map.of("foo", "bar"));

        esperarColaEn(dlqNotificaciones, antes + 1);
        assertEquals(antes + 1, ser.ra_infoCola(dlqNotificaciones).mensajes());
    }

    @Test
    void loteDeEventosSeConsumeSinPerdidas() throws InterruptedException {
        long dlqNotificacionesAntes = ser.ra_infoCola(dlqNotificaciones).mensajes();
        long dlqTicketsAntes = ser.ra_infoCola(dlqTickets).mensajes();

        for (int i = 1; i <= 200; i++) {
            rabbitTemplate.convertAndSend(exchangeTopic, "reserva.creada", Map.of("reservaId", i));
        }
        rabbitTemplate.convertAndSend(exchangeTopic, "servicio.actualizado", Map.of("servicioId", 1));

        esperarColaEn(colaNotificaciones, 0);
        esperarColaEn(colaTickets, 0);
        esperarColaEn(colaDocumentos, 0);
        // Deja asentar el procesamiento en vuelo
        Thread.sleep(1000);

        assertEquals(0, ser.ra_infoCola(colaNotificaciones).mensajes());
        assertEquals(0, ser.ra_infoCola(colaTickets).mensajes());
        assertEquals(0, ser.ra_infoCola(colaDocumentos).mensajes());
        assertEquals(dlqNotificacionesAntes, ser.ra_infoCola(dlqNotificaciones).mensajes());
        assertEquals(dlqTicketsAntes, ser.ra_infoCola(dlqTickets).mensajes());
    }

    private void esperarColaEn(String cola, long esperado) throws InterruptedException {
        long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
        while (System.currentTimeMillis() < limite && ser.ra_infoCola(cola).mensajes() != esperado) {
            Thread.sleep(250);
        }
    }
}
