package Hotel.rabbitadmin.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Swagger queda expuesto sin token; la API de administracion la cubre
 * AdminControllerTest.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerExposureTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void apiDocsSinTokenDevuelve200ConRutasDeAdministracion() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/api/v1/rabbit-admin/queues")));
    }

    @Test
    void swaggerUiSinTokenDevuelve200() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk());
    }
}
