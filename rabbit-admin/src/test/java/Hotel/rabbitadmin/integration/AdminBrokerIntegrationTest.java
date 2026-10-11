package Hotel.rabbitadmin.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import Hotel.rabbitadmin.exception.MensajeriaException;
import Hotel.rabbitadmin.service.RabbitAdminService;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Integracion real contra un broker local (se omite en silencio si no esta disponible). */
@SpringBootTest
@ActiveProfiles("integration")
class AdminBrokerIntegrationTest {

    private static final int PUERTO_BROKER = 5680;
    private static final String COLA_NOTIFICACIONES = "notificaciones.queue";
    private static final String COLA_AUDITORIA = "audit.prueba.queue";
    private static final String EXCHANGE_AUDITORIA = "audit.prueba.exchange";
    private static final String COLA_PUBLISH = "audit.publish.queue";

    @Autowired
    private RabbitAdminService ser;

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
    void topologiaDelHotelDeclaradaConSusDlq() {
        assertEquals(COLA_NOTIFICACIONES, ser.ra_infoCola(COLA_NOTIFICACIONES).nombre());
        assertEquals("tickets.queue", ser.ra_infoCola("tickets.queue").nombre());
        assertEquals("documentos.queue", ser.ra_infoCola("documentos.queue").nombre());
        assertNotNull(ser.ra_infoCola("notificaciones.dlq"));
        assertNotNull(ser.ra_infoCola("tickets.dlq"));
        assertNotNull(ser.ra_infoCola("documentos.dlq"));
    }

    @Test
    void crudDeColaDePruebaContraElBroker() {
        ser.ra_crearCola(COLA_AUDITORIA);
        try {
            assertEquals(COLA_AUDITORIA, ser.ra_infoCola(COLA_AUDITORIA).nombre());
            assertTrue(ser.ra_purgarCola(COLA_AUDITORIA) >= 0);

            ser.ra_eliminarCola(COLA_AUDITORIA);
            MensajeriaException ex = assertThrows(MensajeriaException.class,
                    () -> ser.ra_infoCola(COLA_AUDITORIA));
            assertEquals("MSJ-002", ex.getCode());
        } finally {
            try {
                ser.ra_eliminarCola(COLA_AUDITORIA);
            } catch (Exception ignorada) {
                // ya estaba eliminada
            }
        }
    }

    @Test
    void exchangeYBindingDePruebaContraElBroker() {
        ser.ra_crearExchange(EXCHANGE_AUDITORIA, "topic");
        try {
            ser.ra_crearBinding(COLA_NOTIFICACIONES, EXCHANGE_AUDITORIA, "audit.*");

            ser.ra_eliminarExchange(EXCHANGE_AUDITORIA);

            // el exchange ya no existe: bindear contra el debe fallar con MSJ-005
            MensajeriaException ex = assertThrows(MensajeriaException.class,
                    () -> ser.ra_crearBinding(COLA_NOTIFICACIONES, EXCHANGE_AUDITORIA, "audit.*"));
            assertEquals("MSJ-005", ex.getCode());

            // borrar de nuevo no falla: el broker es idempotente en el delete
            ser.ra_eliminarExchange(EXCHANGE_AUDITORIA);
        } finally {
            try {
                ser.ra_eliminarExchange(EXCHANGE_AUDITORIA);
            } catch (Exception ignorada) {
                // ya estaba eliminado
            }
        }
    }

    @Test
    void bindingConExchangeInexistenteDevuelveMsj005() {
        MensajeriaException ex = assertThrows(MensajeriaException.class,
                () -> ser.ra_crearBinding(COLA_NOTIFICACIONES, "exchange.inexistente", "k"));
        assertEquals("MSJ-005", ex.getCode());
    }

    @Test
    void publicarLlegaALaColaDePrueba() throws InterruptedException {
        String exchange = "audit.publish.exchange";
        ser.ra_crearExchange(exchange, "direct");
        ser.ra_crearCola(COLA_PUBLISH);
        try {
            ser.ra_crearBinding(COLA_PUBLISH, exchange, "audit.publish.rk");
            ser.ra_publicar(exchange, "audit.publish.rk", "{\"foo\":\"bar\"}");

            long mensajes = 0;
            long limite = System.currentTimeMillis() + 10000;
            while (mensajes < 1 && System.currentTimeMillis() < limite) {
                mensajes = ser.ra_infoCola(COLA_PUBLISH).mensajes();
                if (mensajes < 1) {
                    Thread.sleep(200);
                }
            }
            assertEquals(1, mensajes);
        } finally {
            try {
                ser.ra_eliminarCola(COLA_PUBLISH);
            } catch (Exception ignorada) {
                // ya estaba eliminada
            }
            try {
                ser.ra_eliminarExchange(exchange);
            } catch (Exception ignorada) {
                // ya estaba eliminado
            }
        }
    }
}
