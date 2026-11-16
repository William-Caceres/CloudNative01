package Hotel.servicio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import Hotel.servicio.entity.Servicio;
import Hotel.servicio.repository.ServicioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("mysql-test")
class ServicioMySqlIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    private ServicioRepository repo;

    @Test
    void persisteYConsultaUnServicioEnMySql() {
        Servicio servicio = new Servicio();
        servicio.setNombre("Suite MySQL");
        servicio.setDescripcion("Servicio de prueba MySQL");
        servicio.setTipoServicio("Habitacion");
        servicio.setPrecio(90000);
        servicio.setNumHabitacion(201);
        servicio.setCapacidad(2);
        servicio.setDisponible(true);
        servicio.setNivelServicio("Premium");

        Servicio guardado = repo.saveAndFlush(servicio);

        assertNotNull(guardado.getId());
        assertEquals("Suite MySQL", repo.findById(guardado.getId()).orElseThrow().getNombre());
    }
}
