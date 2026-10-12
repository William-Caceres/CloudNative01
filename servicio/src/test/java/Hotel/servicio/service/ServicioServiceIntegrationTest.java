package Hotel.servicio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import Hotel.servicio.entity.Servicio;
import Hotel.servicio.exception.EntidadNoEncontradaException;
import Hotel.servicio.repository.ServicioRepository;
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
class ServicioServiceIntegrationTest {

    private static final int PUERTO_BROKER = 5680;
    private static final long ESPERA_MAXIMA_MS = 10000;
    private static final String CODIGO_NO_ENCONTRADO = "SER-001";
    private static final String NOMBRE_NUEVO = "Suite Premium";

    @Autowired
    private ServicioService ser;

    @Autowired
    private ServicioRepository rep;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Value("${hotel.mensajeria.exchanges.topic}")
    private String exchangeTopic;

    @Value("${hotel.mensajeria.routing.servicioActualizado}")
    private String routingServicioActualizado;

    private static Servicio servicioValido() {
        Servicio s = new Servicio();
        s.setNombre("Habitacion Estandar");
        s.setDescripcion("Servicio de prueba de integracion");
        s.setTipoServicio("Habitacion");
        s.setPrecio(50000);
        s.setNumHabitacion(101);
        s.setCapacidad(2);
        s.setDisponible(true);
        s.setNivelServicio("Premium");
        return s;
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
        String colaAuditoria = "audit.servicio.servicio.test.queue";
        TopicExchange exchange = new TopicExchange(exchangeTopic, true, false);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(QueueBuilder.durable(colaAuditoria).build());
        rabbitAdmin.declareBinding(BindingBuilder.bind(new Queue(colaAuditoria)).to(exchange)
                .with(routingServicioActualizado));
        try {
            Servicio guardado = ser.s_guardar(servicioValido());

            assertNotNull(guardado.getId());
            assertTrue(rep.findById(guardado.getId()).isPresent(),
                    "el servicio debe quedar persistido en la base");

            Object recibido = null;
            long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
            while (recibido == null && System.currentTimeMillis() < limite) {
                recibido = rabbitTemplate.receiveAndConvert(colaAuditoria, 500);
            }

            assertNotNull(recibido, "el evento servicio.actualizado debe llegar al broker");
            @SuppressWarnings("unchecked")
            Map<String, Object> evento = (Map<String, Object>) recibido;
            assertEquals(guardado.getId(), evento.get("servicioId"));
        } finally {
            rabbitAdmin.deleteQueue(colaAuditoria);
        }
    }

    @Test
    void recuperarExistenteDevuelveElServicio() {
        Servicio persistido = rep.save(servicioValido());

        Servicio encontrado = ser.s_recuperar(persistido.getId());

        assertEquals(persistido.getId(), encontrado.getId());
        assertEquals("Habitacion Estandar", encontrado.getNombre());
    }

    @Test
    void recuperarInexistenteLanzaSer001() {
        EntidadNoEncontradaException ex = assertThrows(EntidadNoEncontradaException.class,
                () -> ser.s_recuperar(999999));

        assertEquals(CODIGO_NO_ENCONTRADO, ex.getCode());
    }

    @Test
    void listarDevuelveLoPersistido() {
        Servicio persistido = rep.save(servicioValido());

        List<Servicio> lista = ser.s_listar();

        assertTrue(lista.stream().anyMatch(s -> persistido.getId().equals(s.getId())));
    }

    @Test
    void modificarExistenteActualizaLosCampos() {
        Servicio persistido = rep.save(servicioValido());
        Servicio cambios = servicioValido();
        cambios.setNombre(NOMBRE_NUEVO);
        cambios.setPrecio(120000);
        cambios.setDisponible(false);

        Servicio modificado = ser.s_modificar(persistido.getId(), cambios);

        assertEquals(NOMBRE_NUEVO, modificado.getNombre());
        assertEquals(120000, modificado.getPrecio());
        assertFalse(modificado.getDisponible());
        assertEquals(NOMBRE_NUEVO, rep.findById(persistido.getId()).orElseThrow().getNombre());
    }

    @Test
    void modificarInexistenteLanzaSer001() {
        EntidadNoEncontradaException ex = assertThrows(EntidadNoEncontradaException.class,
                () -> ser.s_modificar(999999, servicioValido()));

        assertEquals(CODIGO_NO_ENCONTRADO, ex.getCode());
    }

    @Test
    void eliminarExistenteDevuelveTrueYDesaparece() {
        Servicio persistido = rep.save(servicioValido());

        assertTrue(ser.s_eliminar(persistido.getId()));
        assertFalse(rep.existsById(persistido.getId()));
    }

    @Test
    void eliminarInexistenteLanzaSer001() {
        EntidadNoEncontradaException ex = assertThrows(EntidadNoEncontradaException.class,
                () -> ser.s_eliminar(999999));

        assertEquals(CODIGO_NO_ENCONTRADO, ex.getCode());
    }
}
