package Hotel.servicio.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Hotel.servicio.service.ServicioService;
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
class ServicioControllerTest {

    private static final String JSON_CODE = "$.code";
    private static final String VALID_001 = "VALID-001";
    private static final String VALID_002 = "VALID-002";
    @SuppressWarnings("java:S1075")
    private static final String PATH_LIST = "/api/v1/servicio/list";
    @SuppressWarnings("java:S1075")
    private static final String PATH_POST = "/api/v1/servicio/post";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_CLIENTE = "ROLE_CLIENTE";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ServicioService ser;

    @Test
    void listSinTokenDevuelve401() throws Exception {
        mvc.perform(get(PATH_LIST))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-001"));
    }

    @Test
    void listConClienteDevuelve200() throws Exception {
        when(ser.s_listar()).thenReturn(List.of());
        mvc.perform(get(PATH_LIST)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isOk());
    }

    @Test
    void deleteSinRolAdminDevuelve403() throws Exception {
        mvc.perform(delete("/api/v1/servicio/delete/1")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
    }

    @Test
    void deleteConRolAdminDevuelve200() throws Exception {
        when(ser.s_eliminar(1)).thenReturn(true);
        mvc.perform(delete("/api/v1/servicio/delete/1")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isOk());
    }

    @Test
    void rutaInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/v1/servicio/no-existe")
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
                .content("{\"nombre\":\"Test\",\"tipoServicio\":\"habitacion\",\"precio\":1000,\"numHabitacion\":0,\"capacidad\":1,\"disponible\":true}")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isOk());
    }

    @Test
    void postInvalidoDevuelve400Valid001() throws Exception {
        mvc.perform(post(PATH_POST)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"\",\"tipoServicio\":\"\",\"precio\":-5,\"numHabitacion\":0,\"capacidad\":-1,\"disponible\":true}")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value(VALID_001));
    }

    @Test
    void postJsonMalformadoDevuelve400Valid002() throws Exception {
        mvc.perform(post(PATH_POST)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{roto")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value(VALID_002));
    }

    @Test
    void getIdInvalidoDevuelve400Valid002() throws Exception {
        mvc.perform(get("/api/v1/servicio/get/abc")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value(VALID_002));
    }

    @Test
    void postNombreLargoDevuelve400Valid001() throws Exception {
        String largo = "a".repeat(60);
        mvc.perform(post(PATH_POST)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + largo + "\",\"tipoServicio\":\"x\",\"precio\":1,\"numHabitacion\":0,\"capacidad\":1,\"disponible\":true}")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value(VALID_001));
    }

    @Test
    void postContenidoNoSoportadoDevuelve415() throws Exception {
        mvc.perform(post(PATH_POST)
                .contentType(MediaType.TEXT_PLAIN)
                .content("hola")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(jsonPath(JSON_CODE).value(VALID_002));
    }

    @Test
    void postSinPrecioDevuelve400Valid001() throws Exception {
        mvc.perform(post(PATH_POST)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"sin precio\",\"tipoServicio\":\"x\",\"numHabitacion\":0,\"capacidad\":1,\"disponible\":true}")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value(VALID_001));
    }
}
