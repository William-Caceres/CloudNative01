package Hotel.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import Hotel.usuario.entity.Usuario;
import Hotel.usuario.repository.UsuarioRepository;
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
class UsuarioMySqlIntegrationTest {

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
    private UsuarioRepository repo;

    @Test
    void persisteYConsultaUnUsuarioEnMySql() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Prueba MySQL");
        usuario.setCorreo("mysql@test.cl");
        usuario.setA_paterno("Apellido");
        usuario.setRut(12345678);
        usuario.setDv_rut("9");
        usuario.setContrasenia("secreta");
        usuario.setTipo_usuario("CLIENTE");

        Usuario guardado = repo.saveAndFlush(usuario);

        assertNotNull(guardado.getId());
        assertEquals("mysql@test.cl", repo.findByCorreo("mysql@test.cl").orElseThrow().getCorreo());
    }
}
