package Hotel.reserva.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Hotel.reserva.service.ReservaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservaControllerTest {

    private static final String JSON_CODE = "$.code";
    @SuppressWarnings("java:S1075")
    private static final String PATH_LIST = "/api/v1/reserva/list";
    @SuppressWarnings("java:S1075")
    private static final String PATH_POST = "/api/v1/reserva/post";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_CLIENTE = "ROLE_CLIENTE";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ReservaService ser;

    @Test
    void listSinTokenDevuelve401() throws Exception {
        mvc.perform(get(PATH_LIST))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-001"));
    }

    @Test
    void listConClienteDevuelve200() throws Exception {
        when(ser.r_listar()).thenReturn(List.of());
        mvc.perform(get(PATH_LIST)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isOk());
    }

    @Test
    void deleteSinRolAdminDevuelve403() throws Exception {
        mvc.perform(delete("/api/v1/reserva/delete/1")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
    }

    @Test
    void deleteConRolAdminDevuelve200() throws Exception {
        when(ser.r_eliminar(1)).thenReturn(true);
        mvc.perform(delete("/api/v1/reserva/delete/1")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isOk());
    }

    @Test
    void rutaInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/v1/reserva/no-existe")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(JSON_CODE).value("SYS-002"));
    }

    @Test
    void metodoNoPermitidoDevuelve405() throws Exception {
        mvc.perform(post(PATH_LIST)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath(JSON_CODE).value("SYS-003"));
    }

    @Test
    void postValidoDevuelve200() throws Exception {
        mvc.perform(post(PATH_POST)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idUsuario\":1,\"fechaReserva\":\"2026-12-01\",\"fechaTermino\":\"2026-12-02\",\"tipoReserva\":\"habitacion\",\"cantidadPersonas\":1,\"valorFinal\":1000}")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isOk());
    }

    @Test
    void postInvalidoDevuelve400Valid001() throws Exception {
        mvc.perform(post(PATH_POST)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value("VALID-001"));
    }

    @Test
    void postJsonMalformadoDevuelve400Valid002() throws Exception {
        mvc.perform(post(PATH_POST)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{roto")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value("VALID-002"));
    }

    @Test
    void getIdInvalidoDevuelve400Valid002() throws Exception {
        mvc.perform(get("/api/v1/reserva/get/abc")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value("VALID-002"));
    }

    @Test
    void postSinIdUsuarioDevuelve400Valid001() throws Exception {
        mvc.perform(post(PATH_POST)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fechaReserva\":\"2026-10-10\",\"fechaTermino\":\"2026-10-12\",\"tipoReserva\":\"simple\",\"cantidadPersonas\":2,\"valorFinal\":50000}")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value("VALID-001"));
    }
}
