package Hotel.reserva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import Hotel.reserva.dto.PagoRequest;
import Hotel.reserva.entity.Reserva;
import Hotel.reserva.exception.EntidadNoEncontradaException;
import Hotel.reserva.messaging.KafkaEventPublisher;
import Hotel.reserva.messaging.ReservaEventPublisher;
import Hotel.reserva.repository.ReservaRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservaServicePagoTest {

    @Mock
    private ReservaRepository rep;

    @Mock
    private ReservaEventPublisher rabbitPublisher;

    @Mock
    private KafkaEventPublisher kafkaPublisher;

    @InjectMocks
    private ReservaService ser;

    @Test
    void guardarFuerzaPagoPendienteYPublicaEnRabbitYKafka() {
        when(rep.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
        Reserva req = reserva(7);
        req.setEstadoPago(Reserva.ESTADO_PAGO_CONFIRMADO);
        req.setMetodoPago("TARJETA");
        req.setReferenciaPago("no-permitida");

        Reserva guardada = ser.r_guardar(req);

        assertEquals(Reserva.ESTADO_PAGO_PENDIENTE, guardada.getEstadoPago());
        assertNull(guardada.getMetodoPago());
        assertNull(guardada.getReferenciaPago());
        verify(rabbitPublisher).publicarReservaCreada(guardada);
        verify(kafkaPublisher).publicarReservaCreada(guardada);
    }

    @Test
    void confirmarPagoActualizaEstadoGeneraReferenciaYPublicaKafka() {
        Reserva reserva = reserva(7);
        when(rep.findById(7)).thenReturn(Optional.of(reserva));
        when(rep.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva pagada = ser.r_confirmarPago(7, new PagoRequest("TARJETA"));

        assertEquals(Reserva.ESTADO_PAGO_CONFIRMADO, pagada.getEstadoPago());
        assertEquals("TARJETA", pagada.getMetodoPago());
        assertNotNull(pagada.getReferenciaPago());
        verify(kafkaPublisher).publicarPago(pagada);
    }

    @Test
    void confirmarPagoYaConfirmadoEsIdempotenteYNoDuplicaEvento() {
        Reserva reserva = reserva(7);
        reserva.setEstadoPago(Reserva.ESTADO_PAGO_CONFIRMADO);
        reserva.setMetodoPago("TARJETA");
        reserva.setReferenciaPago("pago-123");
        when(rep.findById(7)).thenReturn(Optional.of(reserva));

        Reserva pagada = ser.r_confirmarPago(7, new PagoRequest("EFECTIVO"));

        assertSame(reserva, pagada);
        assertEquals("pago-123", pagada.getReferenciaPago());
        assertEquals("TARJETA", pagada.getMetodoPago());
        verify(rep, never()).save(any(Reserva.class));
        verifyNoInteractions(kafkaPublisher);
    }

    @Test
    void confirmarPagoInexistenteLanzaRes001() {
        when(rep.findById(99)).thenReturn(Optional.empty());

        EntidadNoEncontradaException ex = assertThrows(EntidadNoEncontradaException.class,
                () -> ser.r_confirmarPago(99, new PagoRequest("TARJETA")));

        assertEquals("RES-001", ex.getCode());
        verifyNoInteractions(kafkaPublisher);
    }

    private Reserva reserva(Integer id) {
        Reserva reserva = new Reserva();
        reserva.setId(id);
        reserva.setIdUsuario(9);
        reserva.setFechaReserva("2026-11-01");
        reserva.setFechaTermino("2026-11-05");
        reserva.setTipoReserva("Doble");
        reserva.setCantidadPersonas(2);
        reserva.setValorFinal(100000);
        return reserva;
    }
}
