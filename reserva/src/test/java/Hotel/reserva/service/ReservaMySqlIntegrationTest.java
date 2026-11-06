package Hotel.reserva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import Hotel.reserva.entity.Reserva;
import Hotel.reserva.repository.ReservaRepository;
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
class ReservaMySqlIntegrationTest {

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
    private ReservaRepository repo;

    @Test
    void persisteYConsultaUnaReservaEnMySql() {
        Reserva reserva = new Reserva();
        reserva.setIdUsuario(2);
        reserva.setFechaReserva("2026-11-10");
        reserva.setFechaTermino("2026-11-12");
        reserva.setTipoReserva("Doble");
        reserva.setCantidadPersonas(2);
        reserva.setValorFinal(120000);

        Reserva guardada = repo.saveAndFlush(reserva);

        assertNotNull(guardada.getId());
        assertEquals(Reserva.ESTADO_PAGO_PENDIENTE, guardada.getEstadoPago());
        assertEquals("2026-11-10", repo.findById(guardada.getId()).orElseThrow().getFechaReserva());
    }
}
