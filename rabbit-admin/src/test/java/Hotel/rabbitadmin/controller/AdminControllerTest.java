package Hotel.rabbitadmin.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Hotel.rabbitadmin.dto.ColaInfo;
import Hotel.rabbitadmin.exception.MensajeriaException;
import Hotel.rabbitadmin.service.RabbitAdminService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    private static final String BASE = "/api/v1/rabbit-admin";
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority CLIENTE = new SimpleGrantedAuthority("ROLE_CLIENTE");
    private static final String VALID_001 = "VALID-001";
    private static final String JSON_CODE = "$.code";
    private static final String JSON_MENSAJE = "$.mensaje";
    private static final String COLA_NOTIFICACIONES = "notificaciones.queue";
    private static final String PATH_QUEUES = "/queues";
    private static final String PATH_EXCHANGES = "/exchanges";
    private static final String PATH_BINDINGS = "/bindings";
    private static final String PATH_PUBLISH = "/publish";
    private static final String NOMBRE_INEXISTENTE = "no.existe";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RabbitAdminService ser;

    // ---- Seguridad ----

    @Test
    void sinTokenDevuelve401ConAuth001() throws Exception {
        mvc.perform(get(BASE + "/queues/notificaciones.queue"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-001"));
    }

    @Test
    void clienteEnCualquierMutacionDevuelve403ConAuth003() throws Exception {
        record Caso(String nombre, org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder req) {
        }
        List<Caso> mutaciones = List.of(
                new Caso("crear cola", post(BASE + PATH_QUEUES).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"x\"}")),
                new Caso("eliminar cola", delete(BASE + "/queues/x")),
                new Caso("purgar cola", post(BASE + "/queues/x/purge")),
                new Caso("crear exchange", post(BASE + PATH_EXCHANGES).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"x\",\"tipo\":\"direct\"}")),
                new Caso("eliminar exchange", delete(BASE + "/exchanges/x")),
                new Caso("crear binding", post(BASE + PATH_BINDINGS).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cola\":\"c\",\"exchange\":\"e\",\"routingKey\":\"k\"}")),
                new Caso("publicar mensaje", post(BASE + PATH_PUBLISH).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exchange\":\"e\",\"routingKey\":\"k\",\"payload\":\"{}\"}")));

        for (Caso c : mutaciones) {
            mvc.perform(c.req().with(jwt().authorities(CLIENTE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
        }
    }

    @Test
    void clientePuedeVerInfoDeCola200() throws Exception {
        when(ser.ra_infoCola(COLA_NOTIFICACIONES))
                .thenReturn(new ColaInfo(COLA_NOTIFICACIONES, 0, 1));

        mvc.perform(get(BASE + "/queues/notificaciones.queue")
                .with(jwt().authorities(CLIENTE)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value(COLA_NOTIFICACIONES))
            .andExpect(jsonPath("$.mensajes").value(0))
            .andExpect(jsonPath("$.consumidores").value(1));
    }

    // ---- Creaciones y operaciones de admin ----

    @Test
    void adminCreaCola201() throws Exception {
        mvc.perform(post(BASE + PATH_QUEUES)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"mi.cola\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath(JSON_MENSAJE).value("Cola creada: mi.cola"));
    }

    @Test
    void adminEliminaCola200() throws Exception {
        mvc.perform(delete(BASE + "/queues/mi.cola")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JSON_MENSAJE).value("Cola eliminada: mi.cola"));
    }

    @Test
    void adminPurgaCola200ConPurgados() throws Exception {
        when(ser.ra_purgarCola("mi.cola")).thenReturn(3);

        mvc.perform(post(BASE + "/queues/mi.cola/purge")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.purgados").value(3));
    }

    @Test
    void adminCreaExchange201() throws Exception {
        mvc.perform(post(BASE + PATH_EXCHANGES)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"mi.exchange\",\"tipo\":\"topic\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath(JSON_MENSAJE).value("Exchange creado: mi.exchange (topic)"));
    }

    @Test
    void adminEliminaExchange200() throws Exception {
        mvc.perform(delete(BASE + "/exchanges/mi.exchange")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk());
    }

    @Test
    void adminCreaBinding201() throws Exception {
        mvc.perform(post(BASE + PATH_BINDINGS)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cola\":\"mi.cola\",\"exchange\":\"mi.exchange\",\"routingKey\":\"mi.clave\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath(JSON_MENSAJE).value("Binding creado: mi.cola <- mi.exchange (mi.clave)"));
    }

    @Test
    void adminPublicaMensaje202() throws Exception {
        mvc.perform(post(BASE + PATH_PUBLISH)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exchange\":\"hotel.topic\",\"routingKey\":\"reserva.creada\",\"payload\":\"{\\\"foo\\\":\\\"bar\\\"}\"}"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath(JSON_MENSAJE).value("Mensaje publicado en hotel.topic (reserva.creada)"));
    }

    @Test
    void publishSinExchange400Valid001() throws Exception {
        mvc.perform(post(BASE + PATH_PUBLISH)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"routingKey\":\"k\",\"payload\":\"{}\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value(VALID_001))
            .andExpect(jsonPath("$.validations.exchange").exists());
    }

    @Test
    void publishExchangeInexistente400Msj004() throws Exception {
        org.mockito.Mockito.doThrow(new MensajeriaException("MSJ-004", HttpStatus.BAD_REQUEST,
                "No se pudo publicar, verifica que el exchange no.existe exista"))
                .when(ser).ra_publicar(NOMBRE_INEXISTENTE, "k", "{}");

        mvc.perform(post(BASE + PATH_PUBLISH)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exchange\":\"no.existe\",\"routingKey\":\"k\",\"payload\":\"{}\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value("MSJ-004"));
    }

    // ---- Validacion de entrada (VALID-001) ----

    @Test
    void nombreColaVacio400Valid001() throws Exception {
        mvc.perform(post(BASE + PATH_QUEUES)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value(VALID_001))
            .andExpect(jsonPath("$.validations.nombre").exists());
    }

    @Test
    void nombreColaConCaracteresInvalidos400Valid001() throws Exception {
        mvc.perform(post(BASE + PATH_QUEUES)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"nombre con espacios!\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value(VALID_001));
    }

    @Test
    void tipoExchangeInvalido400Valid001() throws Exception {
        mvc.perform(post(BASE + PATH_EXCHANGES)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"x\",\"tipo\":\"fanout\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value(VALID_001))
            .andExpect(jsonPath("$.validations.tipo").exists());
    }

    @Test
    void routingKeyInvalida400Valid001() throws Exception {
        mvc.perform(post(BASE + PATH_BINDINGS)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cola\":\"c\",\"exchange\":\"e\",\"routingKey\":\"con espacios\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value(VALID_001));
    }

    @Test
    void bindingSinCola400Valid001() throws Exception {
        mvc.perform(post(BASE + PATH_BINDINGS)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exchange\":\"e\",\"routingKey\":\"k\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value(VALID_001))
            .andExpect(jsonPath("$.validations.cola").exists());
    }

    // ---- Errores de mensajeria propagados ----

    @Test
    void infoColaInexistente404Msj002() throws Exception {
        when(ser.ra_infoCola(NOMBRE_INEXISTENTE))
                .thenThrow(new MensajeriaException("MSJ-002", HttpStatus.NOT_FOUND, "No existe la cola: " + NOMBRE_INEXISTENTE));

        mvc.perform(get(BASE + "/queues/no.existe")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(JSON_CODE).value("MSJ-002"));
    }

    @Test
    void bindingConExchangeInexistente404Msj005() throws Exception {
        org.mockito.Mockito.doThrow(new MensajeriaException("MSJ-005", HttpStatus.NOT_FOUND,
                "No se pudo crear el binding, verifica que el exchange no.existe exista"))
                .when(ser).ra_crearBinding("mi.cola", NOMBRE_INEXISTENTE, "k");

        mvc.perform(post(BASE + PATH_BINDINGS)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cola\":\"mi.cola\",\"exchange\":\"no.existe\",\"routingKey\":\"k\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(JSON_CODE).value("MSJ-005"));
    }

    // ---- Errores de sistema (SYS) ----

    @Test
    void rutaInexistente404Sys002() throws Exception {
        mvc.perform(get(BASE + "/no-existe")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(JSON_CODE).value("SYS-002"));
    }

    @Test
    void metodoNoPermitido405Sys003() throws Exception {
        mvc.perform(put(BASE + PATH_QUEUES)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"x\"}"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath(JSON_CODE).value("SYS-003"));
    }

    @Test
    void postJsonMalformado400Valid002() throws Exception {
        mvc.perform(post(BASE + PATH_QUEUES)
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{roto"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value("VALID-002"));
    }
}
