package Hotel.reserva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Hotel.reserva.entity.IdempotencyRecord;
import Hotel.reserva.entity.Reserva;
import Hotel.reserva.exception.BusinessRuleException;
import Hotel.reserva.repository.IdempotencyRecordRepository;
import Hotel.reserva.repository.ReservaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository rep;

    @Mock
    private IdempotencyRecordRepository idempotency;

    @Mock
    private OutboxService outbox;

    @InjectMocks
    private ReservaService service;

    @Test
    void guardaReservaYRegistraIdempotencia() {
        when(rep.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(10);
            return r;
        });

        Reserva guardada = service.r_guardar(req(), "key-1");
        ArgumentCaptor<IdempotencyRecord> captor = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(idempotency).save(captor.capture());

        assertEquals(10, guardada.getId());
        assertEquals("key-1", captor.getValue().getKey());
        assertEquals(10, captor.getValue().getReservaId());
        verify(outbox).registrarReserva(10);
    }

    @Test
    void mismaKeyDevuelveLaReservaOriginal() {
        Reserva guardada = req();
        guardada.setId(10);
        when(rep.save(any(Reserva.class))).thenReturn(guardada);

        service.r_guardar(req(), "key-2");
        ArgumentCaptor<IdempotencyRecord> captor = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(idempotency).save(captor.capture());
        when(idempotency.findById("key-2")).thenReturn(Optional.of(captor.getValue()));
        when(rep.findById(10)).thenReturn(Optional.of(guardada));

        Reserva repetida = service.r_guardar(req(), "key-2");

        assertSame(guardada, repetida);
        verify(rep, times(1)).save(any(Reserva.class));
    }

    @Test
    void mismaKeyConOtraCargaDevuelve409() {
        when(idempotency.findById("key-3"))
                .thenReturn(Optional.of(new IdempotencyRecord("key-3", "otro-hash", 10, LocalDateTime.now())));

        BusinessRuleException error = assertThrows(BusinessRuleException.class,
                () -> service.r_guardar(req(), "key-3"));

        assertEquals("RES-002", error.getCode());
        assertEquals(HttpStatus.CONFLICT, error.getHttpStatus());
        verify(rep, never()).save(any(Reserva.class));
    }

    @Test
    void keyDemasiadoLargaDevuelve400() {
        BusinessRuleException error = assertThrows(BusinessRuleException.class,
                () -> service.r_guardar(req(), "x".repeat(101)));

        assertEquals("RES-003", error.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
    }

    private Reserva req() {
        Reserva req = new Reserva();
        req.setIdUsuario(2);
        req.setFechaReserva("2026-12-01");
        req.setFechaTermino("2026-12-02");
        req.setTipoReserva("habitacion");
        req.setCantidadPersonas(1);
        req.setValorFinal(1000);
        return req;
    }
}
