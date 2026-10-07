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

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RabbitAdminService ser;

    // ---- Seguridad ----

    @Test
    void sinToken_devuelve401ConAuth001() throws Exception {
        mvc.perform(get(BASE + "/queues/notificaciones.queue"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH-001"));
    }

    @Test
    void clienteEnCualquierMutacion_devuelve403ConAuth003() throws Exception {
        record Caso(String nombre, org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder req) {
        }
        List<Caso> mutaciones = List.of(
                new Caso("crear cola", post(BASE + "/queues").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"x\"}")),
                new Caso("eliminar cola", delete(BASE + "/queues/x")),
                new Caso("purgar cola", post(BASE + "/queues/x/purge")),
                new Caso("crear exchange", post(BASE + "/exchanges").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"x\",\"tipo\":\"direct\"}")),
                new Caso("eliminar exchange", delete(BASE + "/exchanges/x")),
                new Caso("crear binding", post(BASE + "/bindings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cola\":\"c\",\"exchange\":\"e\",\"routingKey\":\"k\"}")));

        for (Caso c : mutaciones) {
            mvc.perform(c.req().with(jwt().authorities(CLIENTE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH-003"));
        }
    }

    @Test
    void clientePuedeVerInfoDeCola_200() throws Exception {
        when(ser.ra_infoCola("notificaciones.queue"))
                .thenReturn(new ColaInfo("notificaciones.queue", 0, 1));

        mvc.perform(get(BASE + "/queues/notificaciones.queue")
                .with(jwt().authorities(CLIENTE)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("notificaciones.queue"))
            .andExpect(jsonPath("$.mensajes").value(0))
            .andExpect(jsonPath("$.consumidores").value(1));
    }

    // ---- Creaciones y operaciones de admin ----

    @Test
    void adminCreaCola_201() throws Exception {
        mvc.perform(post(BASE + "/queues")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"mi.cola\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.mensaje").value("Cola creada: mi.cola"));
    }

    @Test
    void adminEliminaCola_200() throws Exception {
        mvc.perform(delete(BASE + "/queues/mi.cola")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value("Cola eliminada: mi.cola"));
    }

    @Test
    void adminPurgaCola_200ConPurgados() throws Exception {
        when(ser.ra_purgarCola("mi.cola")).thenReturn(3);

        mvc.perform(post(BASE + "/queues/mi.cola/purge")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.purgados").value(3));
    }

    @Test
    void adminCreaExchange_201() throws Exception {
        mvc.perform(post(BASE + "/exchanges")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"mi.exchange\",\"tipo\":\"topic\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.mensaje").value("Exchange creado: mi.exchange (topic)"));
    }

    @Test
    void adminEliminaExchange_200() throws Exception {
        mvc.perform(delete(BASE + "/exchanges/mi.exchange")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk());
    }

    @Test
    void adminCreaBinding_201() throws Exception {
        mvc.perform(post(BASE + "/bindings")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cola\":\"mi.cola\",\"exchange\":\"mi.exchange\",\"routingKey\":\"mi.clave\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.mensaje").value("Binding creado: mi.cola <- mi.exchange (mi.clave)"));
    }

    // ---- Validacion de entrada (VALID-001) ----

    @Test
    void nombreColaVacio_400Valid001() throws Exception {
        mvc.perform(post(BASE + "/queues")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALID-001"))
            .andExpect(jsonPath("$.validations.nombre").exists());
    }

    @Test
    void nombreColaConCaracteresInvalidos_400Valid001() throws Exception {
        mvc.perform(post(BASE + "/queues")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"nombre con espacios!\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALID-001"));
    }

    @Test
    void tipoExchangeInvalido_400Valid001() throws Exception {
        mvc.perform(post(BASE + "/exchanges")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"x\",\"tipo\":\"fanout\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALID-001"))
            .andExpect(jsonPath("$.validations.tipo").exists());
    }

    @Test
    void routingKeyInvalida_400Valid001() throws Exception {
        mvc.perform(post(BASE + "/bindings")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cola\":\"c\",\"exchange\":\"e\",\"routingKey\":\"con espacios\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALID-001"));
    }

    @Test
    void bindingSinCola_400Valid001() throws Exception {
        mvc.perform(post(BASE + "/bindings")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exchange\":\"e\",\"routingKey\":\"k\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALID-001"))
            .andExpect(jsonPath("$.validations.cola").exists());
    }

    // ---- Errores de mensajeria propagados ----

    @Test
    void infoColaInexistente_404Msj002() throws Exception {
        when(ser.ra_infoCola("no.existe"))
                .thenThrow(new MensajeriaException("MSJ-002", HttpStatus.NOT_FOUND, "No existe la cola: no.existe"));

        mvc.perform(get(BASE + "/queues/no.existe")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MSJ-002"));
    }

    @Test
    void bindingConExchangeInexistente_404Msj005() throws Exception {
        org.mockito.Mockito.doThrow(new MensajeriaException("MSJ-005", HttpStatus.NOT_FOUND,
                "No se pudo crear el binding, verifica que el exchange no.existe exista"))
                .when(ser).ra_crearBinding("mi.cola", "no.existe", "k");

        mvc.perform(post(BASE + "/bindings")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cola\":\"mi.cola\",\"exchange\":\"no.existe\",\"routingKey\":\"k\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MSJ-005"));
    }

    // ---- Errores de sistema (SYS) ----

    @Test
    void rutaInexistente_404Sys002() throws Exception {
        mvc.perform(get(BASE + "/no-existe")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("SYS-002"));
    }

    @Test
    void metodoNoPermitido_405Sys003() throws Exception {
        mvc.perform(put(BASE + "/queues")
                .with(jwt().authorities(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"x\"}"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.code").value("SYS-003"));
    }
}
