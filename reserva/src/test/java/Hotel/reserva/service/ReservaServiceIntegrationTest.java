package Hotel.reserva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import Hotel.reserva.entity.Reserva;
import Hotel.reserva.exception.EntidadNoEncontradaException;
import Hotel.reserva.repository.ReservaRepository;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
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
import org.springframework.transaction.annotation.Transactional;

/** Cobertura real del servicio: persistencia en H2 y evento verificado contra el broker local. */
@SpringBootTest
@ActiveProfiles({"test", "integration"})
@Transactional
class ReservaServiceIntegrationTest {

    private static final int PUERTO_BROKER = 5680;
    private static final long ESPERA_MAXIMA_MS = 10000;
    private static final String CODIGO_NO_ENCONTRADA = "RES-001";
    private static final String FECHA_TERMINO_NUEVA = "2026-11-10";

    @Autowired
    private ReservaService ser;

    @Autowired
    private ReservaRepository rep;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Value("${hotel.mensajeria.exchanges.topic}")
    private String exchangeTopic;

    @Value("${hotel.mensajeria.routing.reservaCreada}")
    private String routingReservaCreada;

    private static Reserva reservaValida() {
        Reserva r = new Reserva();
        r.setIdUsuario(9);
        r.setFechaReserva("2026-11-01");
        r.setFechaTermino("2026-11-05");
        r.setTipoReserva("Doble");
        r.setCantidadPersonas(2);
        r.setValorFinal(100000);
        return r;
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
    void guardarPersisteYElEventoLlegaAlBrokerReal() throws InterruptedException {
        assumeTrue(puertoAbierto("localhost", PUERTO_BROKER),
                "broker local no disponible en " + PUERTO_BROKER + " (se omite esta prueba)");
        String colaAuditoria = "audit.reserva.servicio.test.queue";
        TopicExchange exchange = new TopicExchange(exchangeTopic, true, false);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(QueueBuilder.durable(colaAuditoria).build());
        rabbitAdmin.declareBinding(BindingBuilder.bind(new Queue(colaAuditoria)).to(exchange)
                .with(routingReservaCreada));
        try {
            Reserva guardada = ser.r_guardar(reservaValida());

            assertNotNull(guardada.getId());
            assertTrue(rep.findById(guardada.getId()).isPresent(),
                    "la reserva debe quedar persistida en la base");

            Object recibido = null;
            long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
            while (recibido == null && System.currentTimeMillis() < limite) {
                recibido = rabbitTemplate.receiveAndConvert(colaAuditoria, 500);
            }

            assertNotNull(recibido, "el evento reserva.creada debe llegar al broker");
            @SuppressWarnings("unchecked")
            Map<String, Object> evento = (Map<String, Object>) recibido;
            assertEquals(guardada.getId(), evento.get("reservaId"));
            assertEquals(9, evento.get("idUsuario"));
        } finally {
            rabbitAdmin.deleteQueue(colaAuditoria);
        }
    }

    @Test
    void recuperarExistenteDevuelveLaReserva() {
        Reserva persistida = rep.save(reservaValida());

        Reserva encontrada = ser.r_recuperar(persistida.getId());

        assertEquals(persistida.getId(), encontrada.getId());
        assertEquals(9, encontrada.getIdUsuario());
    }

    @Test
    void recuperarInexistenteLanzaRes001() {
        EntidadNoEncontradaException ex = assertThrows(EntidadNoEncontradaException.class,
                () -> ser.r_recuperar(999999));

        assertEquals(CODIGO_NO_ENCONTRADA, ex.getCode());
    }

    @Test
    void listarDevuelveLoPersistido() {
        Reserva persistida = rep.save(reservaValida());

        List<Reserva> lista = ser.r_listar();

        assertTrue(lista.stream().anyMatch(r -> persistida.getId().equals(r.getId())));
    }

    @Test
    void listarPorUsuarioFiltraPorIdUsuario() {
        Reserva delUsuario9 = rep.save(reservaValida());
        Reserva delUsuario10 = reservaValida();
        delUsuario10.setIdUsuario(10);
        Reserva otra = rep.save(delUsuario10);

        List<Reserva> lista = ser.r_listar_por_usuario(9);

        assertTrue(lista.stream().anyMatch(r -> delUsuario9.getId().equals(r.getId())));
        assertFalse(lista.stream().anyMatch(r -> otra.getId().equals(r.getId())));
    }

    @Test
    void modificarExistenteActualizaLosCampos() {
        Reserva persistida = rep.save(reservaValida());
        Reserva cambios = reservaValida();
        cambios.setFechaTermino(FECHA_TERMINO_NUEVA);
        cambios.setCantidadPersonas(4);
        cambios.setValorFinal(180000);

        Reserva modificada = ser.r_modificar(persistida.getId(), cambios);

        assertEquals(FECHA_TERMINO_NUEVA, modificada.getFechaTermino());
        assertEquals(4, modificada.getCantidadPersonas());
        assertEquals(180000, modificada.getValorFinal());
        assertEquals(FECHA_TERMINO_NUEVA, rep.findById(persistida.getId()).orElseThrow().getFechaTermino());
    }

    @Test
    void modificarInexistenteLanzaRes001() {
        EntidadNoEncontradaException ex = assertThrows(EntidadNoEncontradaException.class,
                () -> ser.r_modificar(999999, reservaValida()));

        assertEquals(CODIGO_NO_ENCONTRADA, ex.getCode());
    }

    @Test
    void eliminarExistenteDevuelveTrueYDesaparece() {
        Reserva persistida = rep.save(reservaValida());

        assertTrue(ser.r_eliminar(persistida.getId()));
        assertFalse(rep.existsById(persistida.getId()));
    }

    @Test
    void eliminarInexistenteLanzaRes001() {
        EntidadNoEncontradaException ex = assertThrows(EntidadNoEncontradaException.class,
                () -> ser.r_eliminar(999999));

        assertEquals(CODIGO_NO_ENCONTRADA, ex.getCode());
    }
}
