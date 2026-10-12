package Hotel.rabbitadmin;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RabbitAdminApplicationTests {

    @Autowired
    private ApplicationContext contexto;

    @Test
    void contextLoads() {
        assertNotNull(contexto.getBean("colaNotificaciones"));
        assertNotNull(contexto.getBean("dlqNotificaciones"));
    }

}
