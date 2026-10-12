package Hotel.usuario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Hotel.usuario.entity.Usuario;
import Hotel.usuario.service.UsuarioService;
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
class UsuarioControllerTest {

    private static final String JSON_CODE = "$.code";
    @SuppressWarnings("java:S1075")
    private static final String PATH_LIST = "/api/v1/usuario/list";
    @SuppressWarnings("java:S1075")
    private static final String PATH_PUT = "/api/v1/usuario/put/1";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_CLIENTE = "ROLE_CLIENTE";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UsuarioService ser;

    @Test
    void listSinTokenDevuelve401() throws Exception {
        mvc.perform(get(PATH_LIST))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-001"));
    }

    @Test
    void listSinRolAdminDevuelve403() throws Exception {
        mvc.perform(get(PATH_LIST)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
    }

    @Test
    void listConRolAdminDevuelve200() throws Exception {
        when(ser.u_listar()).thenReturn(List.of(new Usuario()));
        mvc.perform(get(PATH_LIST)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isOk());
    }

    @Test
    void getConRolAdminDevuelve200() throws Exception {
        when(ser.u_recuperar(anyInt())).thenReturn(new Usuario());
        mvc.perform(get("/api/v1/usuario/get/1")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
            .andExpect(status().isOk());
    }

    @Test
    void getSinRolAdminDevuelve403ConAuth003() throws Exception {
        mvc.perform(get("/api/v1/usuario/get/1")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
    }

    @Test
    void meSinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/v1/usuario/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void meAutenticadoResuelvePorPreferredUsername() throws Exception {
        when(ser.u_por_correo(eq("cliente.prueba@test.cl"), isNull())).thenReturn(new Usuario());
        mvc.perform(get("/api/v1/usuario/me")
                .with(jwt().jwt(j -> j.claim("preferred_username", "cliente.prueba@test.cl"))))
            .andExpect(status().isOk());
    }

    @Test
    void rutaInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/v1/usuario/no-existe")
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
    void putSinNombreDevuelve400Valid001() throws Exception {
        mvc.perform(put(PATH_PUT)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"a@b.cl\",\"a_paterno\":\"x\",\"rut\":1,\"dv_rut\":\"1\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value("VALID-001"));
    }

    @Test
    void putSinRutDevuelve400Valid001() throws Exception {
        mvc.perform(put(PATH_PUT)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Nombre\",\"correo\":\"a@b.cl\",\"a_paterno\":\"x\",\"dv_rut\":\"1\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value("VALID-001"));
    }

    @Test
    void putValidoDevuelve200() throws Exception {
        when(ser.u_modificar(eq(1), any(Usuario.class))).thenReturn(new Usuario());
        mvc.perform(put(PATH_PUT)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Nombre\",\"correo\":\"a@b.cl\",\"a_paterno\":\"x\",\"rut\":1,\"dv_rut\":\"1\"}"))
            .andExpect(status().isOk());
    }
}
