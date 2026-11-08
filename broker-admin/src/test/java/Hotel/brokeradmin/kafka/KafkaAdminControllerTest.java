package Hotel.brokeradmin.kafka;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Hotel.brokeradmin.kafka.dto.KafkaGrupoInfo;
import Hotel.brokeradmin.kafka.dto.KafkaGrupoResumen;
import Hotel.brokeradmin.kafka.dto.KafkaResumen;
import Hotel.brokeradmin.kafka.dto.KafkaTopicInfo;
import Hotel.brokeradmin.kafka.service.KafkaAdminService;
import java.util.List;
import java.util.Map;
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
class KafkaAdminControllerTest {

    private static final String BASE = "/api/v1/broker-admin/kafka";
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority CLIENTE = new SimpleGrantedAuthority("ROLE_CLIENTE");
    private static final String JSON_CODE = "$.code";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private KafkaAdminService ser;

    @Test
    void listarTopicsSinTokenDevuelve401() throws Exception {
        mvc.perform(get(BASE + "/topics"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-001"));
    }

    @Test
    void clienteNoPuedeListarTopics403() throws Exception {
        mvc.perform(get(BASE + "/topics").with(jwt().authorities(CLIENTE)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
    }

    @Test
    void clienteNoPuedeCrearTopic403() throws Exception {
        mvc.perform(post(BASE + "/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"prueba\",\"particiones\":3,\"replicas\":3}")
                .with(jwt().authorities(CLIENTE)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
    }

    @Test
    void adminCreaTopic201() throws Exception {
        mvc.perform(post(BASE + "/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"prueba\",\"particiones\":3,\"replicas\":3}")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.mensaje").value("Topic creado: prueba"));
    }

    @Test
    void adminCrearTopicInvalidoDevuelve400Valid001() throws Exception {
        mvc.perform(post(BASE + "/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"prueba\",\"particiones\":0,\"replicas\":3}")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_CODE).value("VALID-001"));
    }

    @Test
    void adminEliminaTopic200() throws Exception {
        mvc.perform(delete(BASE + "/topics/prueba").with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value("Topic eliminado: prueba"));
    }

    @Test
    void adminAlteraConfiguraciones200() throws Exception {
        mvc.perform(put(BASE + "/topics/prueba/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"configuraciones\":{\"retention.ms\":\"120000\"}}")
                .with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value("Configuraciones actualizadas: prueba"));
    }

    @Test
    void clienteNoPuedeDescribirTopic403() throws Exception {
        mvc.perform(get(BASE + "/topics/reservas").with(jwt().authorities(CLIENTE)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
    }

    @Test
    void clienteNoPuedeListarGrupos403() throws Exception {
        mvc.perform(get(BASE + "/groups").with(jwt().authorities(CLIENTE)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
    }

    @Test
    void clienteNoPuedeDescribirGrupo403() throws Exception {
        mvc.perform(get(BASE + "/groups/analytics-group").with(jwt().authorities(CLIENTE)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
    }

    @Test
    void resumenDevuelveMetricasYCluster() throws Exception {
        mvc.perform(get(BASE + "/summary").with(jwt().authorities(CLIENTE)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(JSON_CODE).value("AUTH-003"));
    }

    @Test
    void adminPuedeListarTopics200() throws Exception {
        when(ser.listarTopics()).thenReturn(List.of("reservas", "pagos"));

        mvc.perform(get(BASE + "/topics").with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value("reservas"));
    }

    @Test
    void adminPuedeVerResumen200() throws Exception {
        KafkaResumen resumen = new KafkaResumen(3, 4, 2, 0, 10, 20,
                "pago:evt-1", "2026-11-06T05:30:00Z",
                List.of(new KafkaGrupoResumen("analytics-group", "STABLE", 3, 0)), List.of());
        when(ser.resumen()).thenReturn(resumen);

        mvc.perform(get(BASE + "/summary").with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.brokers").value(3))
            .andExpect(jsonPath("$.reservasProcesadas").value(10));
    }
}
