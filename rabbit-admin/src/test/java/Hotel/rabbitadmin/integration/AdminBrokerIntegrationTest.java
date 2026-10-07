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
        assertEquals("notificaciones.queue", ser.ra_infoCola("notificaciones.queue").nombre());
        assertEquals("tickets.queue", ser.ra_infoCola("tickets.queue").nombre());
        assertEquals("documentos.queue", ser.ra_infoCola("documentos.queue").nombre());
        assertNotNull(ser.ra_infoCola("notificaciones.dlq"));
        assertNotNull(ser.ra_infoCola("tickets.dlq"));
        assertNotNull(ser.ra_infoCola("documentos.dlq"));
    }

    @Test
    void crudDeColaDePruebaContraElBroker() {
        ser.ra_crearCola("audit.prueba.queue");
        try {
            assertEquals("audit.prueba.queue", ser.ra_infoCola("audit.prueba.queue").nombre());
            assertTrue(ser.ra_purgarCola("audit.prueba.queue") >= 0);

            ser.ra_eliminarCola("audit.prueba.queue");
            MensajeriaException ex = assertThrows(MensajeriaException.class,
                    () -> ser.ra_infoCola("audit.prueba.queue"));
            assertEquals("MSJ-002", ex.getCode());
        } finally {
            try {
                ser.ra_eliminarCola("audit.prueba.queue");
            } catch (Exception ignorada) {
                // ya estaba eliminada
            }
        }
    }

    @Test
    void exchangeYBindingDePruebaContraElBroker() {
        ser.ra_crearExchange("audit.prueba.exchange", "topic");
        try {
            ser.ra_crearBinding("notificaciones.queue", "audit.prueba.exchange", "audit.*");

            ser.ra_eliminarExchange("audit.prueba.exchange");

            // el exchange ya no existe: bindear contra el debe fallar con MSJ-005
            MensajeriaException ex = assertThrows(MensajeriaException.class,
                    () -> ser.ra_crearBinding("notificaciones.queue", "audit.prueba.exchange", "audit.*"));
            assertEquals("MSJ-005", ex.getCode());

            // borrar de nuevo no falla: el broker es idempotente en el delete
            ser.ra_eliminarExchange("audit.prueba.exchange");
        } finally {
            try {
                ser.ra_eliminarExchange("audit.prueba.exchange");
            } catch (Exception ignorada) {
                // ya estaba eliminado
            }
        }
    }

    @Test
    void bindingConExchangeInexistente_devuelveMsj005() {
        MensajeriaException ex = assertThrows(MensajeriaException.class,
                () -> ser.ra_crearBinding("notificaciones.queue", "exchange.inexistente", "k"));
        assertEquals("MSJ-005", ex.getCode());
    }
}
