package Hotel.rabbitadmin.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import Hotel.rabbitadmin.listener.notificacion.NotificacionProcessor;
import Hotel.rabbitadmin.service.RabbitAdminService;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Flujo real de reintentos contra el broker local: con el procesador mockeado
 * se verifica el reintento acotado y la derivacion a DLQ.
 */
@SpringBootTest
@ActiveProfiles("integration")
class ReintentoIntegracionTest {

    private static final int PUERTO_BROKER = 5680;
    private static final long ESPERA_MAXIMA_MS = 15000;
    private static final String CAMPO_RESERVA_ID = "reservaId";

    @Autowired
    private RabbitAdminService ser;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private NotificacionProcessor procesadorNotificacion;

    @Value("${hotel.mensajeria.exchanges.topic}")
    private String exchangeTopic;

    @Value("${hotel.mensajeria.colas.notificaciones}")
    private String colaNotificaciones;

    @Value("${hotel.mensajeria.dlq.notificaciones}")
    private String dlqNotificaciones;

    @Autowired
    private RabbitAdmin rabbitAdmin;

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

    // El broker local es compartido entre suites: se purgan las colas antes de
    // cada prueba para que mensajes de otras corridas no alteren los conteos.
    @BeforeEach
    void limpiarColas() {
        rabbitAdmin.purgeQueue(colaNotificaciones);
        rabbitAdmin.purgeQueue(dlqNotificaciones);
    }

    @Test
    void falloTransitorioPrimerIntentoReencolaYElSegundoConfirma() {
        doThrow(new RuntimeException("fallo transitorio simulado"))
                .doNothing()
                .when(procesadorNotificacion).procesar(anyMap());
        long dlqAntes = ser.ra_infoCola(dlqNotificaciones).mensajes();

        rabbitTemplate.convertAndSend(exchangeTopic, "reserva.otro", Map.of(CAMPO_RESERVA_ID, 55));

        verify(procesadorNotificacion, timeout(ESPERA_MAXIMA_MS).times(2))
                .procesar(argThat(m -> "55".equals(String.valueOf(m.get(CAMPO_RESERVA_ID)))));
        assertEquals(dlqAntes, ser.ra_infoCola(dlqNotificaciones).mensajes());
    }

    @Test
    void falloPersistenteReintentaUnaVezYTerminaEnDlq() throws InterruptedException {
        doThrow(new RuntimeException("fallo persistente simulado"))
                .when(procesadorNotificacion).procesar(anyMap());
        long dlqAntes = ser.ra_infoCola(dlqNotificaciones).mensajes();

        rabbitTemplate.convertAndSend(exchangeTopic, "reserva.otro", Map.of(CAMPO_RESERVA_ID, 56));

        esperarColaEn(dlqNotificaciones, dlqAntes + 1);
        verify(procesadorNotificacion, times(2))
                .procesar(argThat(m -> "56".equals(String.valueOf(m.get(CAMPO_RESERVA_ID)))));
        assertEquals(0, ser.ra_infoCola(colaNotificaciones).mensajes());
    }

    private void esperarColaEn(String cola, long esperado) throws InterruptedException {
        long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
        while (System.currentTimeMillis() < limite && ser.ra_infoCola(cola).mensajes() != esperado) {
            Thread.sleep(250);
        }
    }
}
