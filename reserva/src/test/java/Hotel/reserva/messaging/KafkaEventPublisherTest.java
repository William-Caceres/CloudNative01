package Hotel.reserva.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Hotel.reserva.dto.PagoEvent;
import Hotel.reserva.dto.ReservaEvent;
import Hotel.reserva.entity.Reserva;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    private KafkaEventPublisher publisher;

    @BeforeEach
    void armar() {
        publisher = new KafkaEventPublisher(kafkaTemplate, "reservas", "pagos");
    }

    @Test
    void publicaReservaConTopicKeyYDtoCorrectos() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(futuroCorrecto());
        Reserva reserva = reservaPendiente();

        publisher.publicarReservaCreada(reserva);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("reservas"), eq("7"), captor.capture());
        ReservaEvent evento = assertInstanceOf(ReservaEvent.class, captor.getValue());
        assertNotNull(evento.eventId());
        assertEquals("RESERVA_CREADA", evento.eventType());
        assertEquals(7, evento.reservaId());
        assertEquals(9, evento.idUsuario());
        assertEquals(Reserva.ESTADO_PAGO_PENDIENTE, evento.estadoPago());
    }

    @Test
    void publicaPagoConTopicKeyYDtoCorrectos() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(futuroCorrecto());
        Reserva reserva = reservaPendiente();
        reserva.setEstadoPago(Reserva.ESTADO_PAGO_CONFIRMADO);
        reserva.setMetodoPago("TARJETA");
        reserva.setReferenciaPago("pago-123");

        publisher.publicarPago(reserva);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("pagos"), eq("7"), captor.capture());
        PagoEvent evento = assertInstanceOf(PagoEvent.class, captor.getValue());
        assertEquals("PAGO_CONFIRMADO", evento.eventType());
        assertEquals("pago-123", evento.pagoId());
        assertEquals(7, evento.reservaId());
        assertEquals(9, evento.idUsuario());
        assertEquals(100000, evento.monto());
        assertEquals("TARJETA", evento.metodoPago());
        assertEquals(Reserva.ESTADO_PAGO_CONFIRMADO, evento.estadoPago());
    }

    @Test
    void siKafkaFallaLaOperacionNoSePierde() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("kafka caido"));

        assertDoesNotThrow(() -> publisher.publicarReservaCreada(reservaPendiente()));
    }

    @Test
    void registraLaMetadataCuandoElEnvioEsExitoso() {
        SendResult<Object, Object> resultado = mock(SendResult.class);
        when(resultado.getRecordMetadata()).thenReturn(mock(RecordMetadata.class));
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(resultado));

        assertDoesNotThrow(() -> publisher.publicarReservaCreada(reservaPendiente()));
    }

    @Test
    void registraElFalloAsincronoCuandoElEnvioFalla() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka async caido")));

        assertDoesNotThrow(() -> publisher.publicarReservaCreada(reservaPendiente()));
    }

    @Test
    void usaPendienteCuandoElEstadoEsNulo() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(futuroCorrecto());
        Reserva reserva = reservaPendiente();
        reserva.setEstadoPago(null);

        publisher.publicarReservaCreada(reserva);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("reservas"), eq("7"), captor.capture());
        ReservaEvent evento = assertInstanceOf(ReservaEvent.class, captor.getValue());
        assertEquals(Reserva.ESTADO_PAGO_PENDIENTE, evento.estadoPago());
    }

    @Test
    void usaPendienteCuandoElEstadoEstaEnBlanco() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(futuroCorrecto());
        Reserva reserva = reservaPendiente();
        reserva.setEstadoPago(" ");

        publisher.publicarReservaCreada(reserva);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("reservas"), eq("7"), captor.capture());
        ReservaEvent evento = assertInstanceOf(ReservaEvent.class, captor.getValue());
        assertEquals(Reserva.ESTADO_PAGO_PENDIENTE, evento.estadoPago());
    }

    private Reserva reservaPendiente() {
        Reserva reserva = new Reserva();
        reserva.setId(7);
        reserva.setIdUsuario(9);
        reserva.setFechaReserva("2026-11-01");
        reserva.setFechaTermino("2026-11-05");
        reserva.setTipoReserva("Doble");
        reserva.setCantidadPersonas(2);
        reserva.setValorFinal(100000);
        return reserva;
    }

    private CompletableFuture<SendResult<Object, Object>> futuroCorrecto() {
        return CompletableFuture.completedFuture(null);
    }
}
