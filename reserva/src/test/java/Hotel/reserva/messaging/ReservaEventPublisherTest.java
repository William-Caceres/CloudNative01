package Hotel.reserva.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import Hotel.reserva.entity.Reserva;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/** Contrato del productor: evento correcto hacia hotel.topic y fail-safe si RabbitMQ falla. */
@ExtendWith(MockitoExtension.class)
class ReservaEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ReservaEventPublisher publisher;

    @BeforeEach
    void armar() {
        publisher = new ReservaEventPublisher(rabbitTemplate, "hotel.topic", "reserva.creada");
    }

    @Test
    void publicaElEventoConExchangeRoutingKeyYContenidoCorrectos() {
        publisher.publicarReservaCreada(reserva(42, 9));

        ArgumentCaptor<Map<String, Object>> captor = captorDeMapa();
        verify(rabbitTemplate).convertAndSend(eq("hotel.topic"), eq("reserva.creada"), captor.capture());
        assertEquals(42, captor.getValue().get("reservaId"));
        assertEquals(9, captor.getValue().get("idUsuario"));
    }

    @Test
    void siRabbitFallaLaReservaNoSePierde() {
        doThrow(new AmqpException("broker caido"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

        assertDoesNotThrow(() -> publisher.publicarReservaCreada(reserva(42, 9)));
    }

    private Reserva reserva(Integer id, Integer idUsuario) {
        Reserva r = new Reserva();
        r.setId(id);
        r.setIdUsuario(idUsuario);
        return r;
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, Object>> captorDeMapa() {
        return ArgumentCaptor.forClass(Map.class);
    }
}
