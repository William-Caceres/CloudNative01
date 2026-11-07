package Hotel.reserva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Hotel.reserva.entity.OutboxEvent;
import Hotel.reserva.entity.Reserva;
import Hotel.reserva.messaging.KafkaEventPublisher;
import Hotel.reserva.messaging.ReservaEventPublisher;
import Hotel.reserva.repository.OutboxEventRepository;
import Hotel.reserva.repository.ReservaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    private OutboxEventRepository outbox;

    @Mock
    private ReservaRepository reservas;

    @Mock
    private ReservaEventPublisher rabbitPublisher;

    @Mock
    private KafkaEventPublisher kafkaPublisher;

    @InjectMocks
    private OutboxRelay relay;

    @Test
    void publicaReservaYLaMarcaEnviada() {
        OutboxEvent evento = evento(OutboxEvent.RESERVA_CREADA);
        when(outbox.findTop50ByEstadoInAndProximoIntentoLessThanEqualOrderByIdAsc(anyList(), any()))
                .thenReturn(List.of(evento));
        when(reservas.findById(7)).thenReturn(Optional.of(reserva()));

        relay.procesar();

        assertEquals(OutboxEvent.ENVIADO, evento.getEstado());
        verify(rabbitPublisher).enviarReservaCreada(any(Reserva.class));
        verify(kafkaPublisher).enviarReservaCreada(any(Reserva.class));
        verify(outbox).save(evento);
    }

    @Test
    void pagoSoloPublicaKafka() {
        OutboxEvent evento = evento(OutboxEvent.PAGO_CONFIRMADO);
        when(outbox.findTop50ByEstadoInAndProximoIntentoLessThanEqualOrderByIdAsc(anyList(), any()))
                .thenReturn(List.of(evento));
        when(reservas.findById(7)).thenReturn(Optional.of(reserva()));

        relay.procesar();

        assertEquals(OutboxEvent.ENVIADO, evento.getEstado());
        verify(kafkaPublisher).enviarPago(any(Reserva.class));
        verify(rabbitPublisher, never()).enviarReservaCreada(any(Reserva.class));
    }

    @Test
    void falloProgramaReintento() {
        OutboxEvent evento = evento(OutboxEvent.RESERVA_CREADA);
        when(outbox.findTop50ByEstadoInAndProximoIntentoLessThanEqualOrderByIdAsc(anyList(), any()))
                .thenReturn(List.of(evento));
        when(reservas.findById(7)).thenReturn(Optional.of(reserva()));
        doThrow(new IllegalStateException("broker caido")).when(rabbitPublisher).enviarReservaCreada(any());

        relay.procesar();

        assertEquals(OutboxEvent.FALLIDO, evento.getEstado());
        assertEquals(1, evento.getIntentos());
        verify(kafkaPublisher, never()).enviarReservaCreada(any(Reserva.class));
        verify(outbox).save(evento);
    }

    @Test
    void reservaInexistenteAgotaEvento() {
        OutboxEvent evento = evento(OutboxEvent.RESERVA_CREADA);
        when(outbox.findTop50ByEstadoInAndProximoIntentoLessThanEqualOrderByIdAsc(anyList(), any()))
                .thenReturn(List.of(evento));
        when(reservas.findById(7)).thenReturn(Optional.empty());

        relay.procesar();

        assertEquals(OutboxEvent.AGOTADO, evento.getEstado());
        verify(rabbitPublisher, never()).enviarReservaCreada(any(Reserva.class));
        verify(kafkaPublisher, never()).enviarReservaCreada(any(Reserva.class));
    }

    private OutboxEvent evento(String tipo) {
        LocalDateTime ahora = LocalDateTime.now();
        return new OutboxEvent(1L, 7, tipo, OutboxEvent.PENDIENTE, 0, ahora, ahora, null);
    }

    private Reserva reserva() {
        Reserva reserva = new Reserva();
        reserva.setId(7);
        reserva.setIdUsuario(2);
        return reserva;
    }
}
