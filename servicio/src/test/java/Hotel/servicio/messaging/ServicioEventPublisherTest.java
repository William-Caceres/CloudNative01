package Hotel.servicio.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import Hotel.servicio.entity.Servicio;
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
class ServicioEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ServicioEventPublisher publisher;

    @BeforeEach
    void armar() {
        publisher = new ServicioEventPublisher(rabbitTemplate, "hotel.topic", "servicio.actualizado");
    }

    @Test
    void publicaElEventoConExchangeRoutingKeyYContenidoCorrectos() {
        publisher.publicarServicioActualizado(servicio(42));

        ArgumentCaptor<Map<String, Object>> captor = captorDeMapa();
        verify(rabbitTemplate).convertAndSend(eq("hotel.topic"), eq("servicio.actualizado"), captor.capture());
        assertEquals(42, captor.getValue().get("servicioId"));
    }

    @Test
    void siRabbitFallaElServicioNoSePierde() {
        doThrow(new AmqpException("broker caido"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

        assertDoesNotThrow(() -> publisher.publicarServicioActualizado(servicio(42)));
    }

    private Servicio servicio(Integer id) {
        Servicio s = new Servicio();
        s.setId(id);
        return s;
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, Object>> captorDeMapa() {
        return ArgumentCaptor.forClass(Map.class);
    }
}
