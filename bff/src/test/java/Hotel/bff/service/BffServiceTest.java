package Hotel.bff.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class BffServiceTest {

    private static final String URL_USUARIO = "http://usuario.test";
    private static final String URL_USUARIO_ME = "http://usuario.test/api/v1/usuario/me";
    private static final String URL_RESERVA = "http://reserva.test";
    private static final String URL_SERVICIO = "http://servicio.test";
    private static final String URL_SERVICIO_LIST = "http://servicio.test/api/v1/servicio/list";
    private static final String BEARER_ABC = "Bearer abc";
    private static final String CLAVE_USUARIO = "usuario";
    private static final String CLAVE_RESERVAS = "reservas";
    private static final String CLAVE_SERVICIOS = "servicios";
    private static final String CLAVE_DATOS = "datos";
    private static final String CLAVE_ERROR = "error";

    private RestClient.Builder builder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
    }

    private BffService service(String usuario, String reserva, String servicio) {
        return new BffService(builder, usuario, reserva, servicio);
    }

    @Test
    void dashboardAgregaLasTresFuentesYReenviaElToken() {
        server.expect(requestTo(URL_USUARIO_ME))
              .andExpect(header(HttpHeaders.AUTHORIZATION, BEARER_ABC))
              .andRespond(withSuccess("{\"id\":7,\"correo\":\"a@b.c\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://reserva.test/api/v1/reserva/usuario/7"))
              .andRespond(withSuccess("[{\"id\":1},{\"id\":2}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(URL_SERVICIO_LIST))
              .andRespond(withSuccess("[{\"id\":9}]", MediaType.APPLICATION_JSON));

        Map<String, Object> salida = service(URL_USUARIO, URL_RESERVA, URL_SERVICIO)
                .b_dashboard(BEARER_ABC);

        Map<?, ?> usuario = (Map<?, ?>) ((Map<?, ?>) salida.get(CLAVE_USUARIO)).get(CLAVE_DATOS);
        assertEquals(7, usuario.get("id"));
        assertEquals(2, ((List<?>) ((Map<?, ?>) salida.get(CLAVE_RESERVAS)).get(CLAVE_DATOS)).size());
        assertEquals(1, ((List<?>) ((Map<?, ?>) salida.get(CLAVE_SERVICIOS)).get(CLAVE_DATOS)).size());
        server.verify();
    }

    @Test
    void microservicioCaidoReportaErrorSinTumbarElResto() {
        server.expect(requestTo(URL_USUARIO_ME))
              .andRespond(withServerError());
        server.expect(requestTo(URL_SERVICIO_LIST))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Map<String, Object> salida = service(URL_USUARIO, URL_RESERVA, URL_SERVICIO)
                .b_dashboard(BEARER_ABC);

        assertEquals("No se pudo consultar el microservicio", ((Map<?, ?>) salida.get(CLAVE_USUARIO)).get(CLAVE_ERROR));
        assertNull(((Map<?, ?>) salida.get(CLAVE_USUARIO)).get(CLAVE_DATOS));
        assertEquals("No se pudo resolver el usuario del token", ((Map<?, ?>) salida.get(CLAVE_RESERVAS)).get(CLAVE_ERROR));
        assertNotNull(((Map<?, ?>) salida.get(CLAVE_SERVICIOS)).get(CLAVE_DATOS));
        server.verify();
    }

    @Test
    void destinoNoConfiguradoReportaErrorSinIntentarLlamar() {
        server.expect(requestTo(URL_USUARIO_ME))
              .andRespond(withSuccess("{\"id\":7}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(URL_SERVICIO_LIST))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Map<String, Object> salida = service(URL_USUARIO, "", URL_SERVICIO)
                .b_dashboard(BEARER_ABC);

        assertEquals("Destino no configurado", ((Map<?, ?>) salida.get(CLAVE_RESERVAS)).get(CLAVE_ERROR));
        assertTrue(((Map<?, ?>) salida.get(CLAVE_SERVICIOS)).containsKey(CLAVE_DATOS));
        server.verify();
    }

    @Test
    void adminResumenAgregaLasTresListas() {
        server.expect(requestTo("http://usuario.test/api/v1/usuario/list"))
              .andExpect(header(HttpHeaders.AUTHORIZATION, BEARER_ABC))
              .andRespond(withSuccess("[{\"id\":1}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://reserva.test/api/v1/reserva/list"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(URL_SERVICIO_LIST))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Map<String, Object> salida = service(URL_USUARIO, URL_RESERVA, URL_SERVICIO)
                .b_admin(BEARER_ABC);

        assertEquals(1, ((List<?>) ((Map<?, ?>) salida.get("usuarios")).get(CLAVE_DATOS)).size());
        assertNotNull(((Map<?, ?>) salida.get(CLAVE_RESERVAS)).get(CLAVE_DATOS));
        assertNotNull(((Map<?, ?>) salida.get(CLAVE_SERVICIOS)).get(CLAVE_DATOS));
        server.verify();
    }
}
