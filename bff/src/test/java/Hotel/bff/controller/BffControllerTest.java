package Hotel.bff.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Hotel.bff.service.BffService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BffControllerTest {

    private static final String JSON_CODE = "$.code";
    @SuppressWarnings("java:S1075")
    private static final String PATH_DASHBOARD = "/api/v1/bff/dashboard";
    private static final String ROLE_CLIENTE = "ROLE_CLIENTE";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BffService ser;

    @Test
    void dashboardSinTokenDevuelve401ConAuth001() throws Exception {
        mvc.perform(get(PATH_DASHBOARD))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-001"));
    }

    @Test
    void dashboardAutenticadoDevuelve200() throws Exception {
        when(ser.b_dashboard("Bearer abc")).thenReturn(Map.of());
        mvc.perform(get(PATH_DASHBOARD)
                .with(jwt().jwt(j -> j.tokenValue("abc"))
                        .authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isOk());
    }

    @Test
    void adminSinRolAdminDevuelve403ConAuth003() throws Exception {
        mvc.perform(get("/api/v1/bff/admin/resumen")
                .with(jwt().jwt(j -> j.tokenValue("abc"))
                        .authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
    }

    @Test
    void adminConRolAdminDevuelve200() throws Exception {
        when(ser.b_admin("Bearer abc")).thenReturn(Map.of());
        mvc.perform(get("/api/v1/bff/admin/resumen")
                .with(jwt().jwt(j -> j.tokenValue("abc"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }

    @Test
    void rutaInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/v1/bff/no-existe")
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(JSON_CODE).value("SYS-002"));
    }

    @Test
    void metodoNoPermitidoDevuelve405() throws Exception {
        mvc.perform(post(PATH_DASHBOARD)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CLIENTE))))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath(JSON_CODE).value("SYS-003"));
    }
}
