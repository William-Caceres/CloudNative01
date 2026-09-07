package Hotel.reserva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Hotel.reserva.dto.ReservaRequestDto;
import Hotel.reserva.dto.ReservaResponseDto;
import Hotel.reserva.entity.Reserva;
import Hotel.reserva.exception.EntidadNoEncontradaException;
import Hotel.reserva.repository.ReservaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository rep;

    @InjectMocks
    private ReservaService service;

    private ReservaRequestDto request;

    @BeforeEach
    void setUp() {
        request = ReservaRequestDto.builder()
                .idUsuario(3)
                .fechaReserva("2026-09-01")
                .fechaTermino("2026-09-05")
                .tipoReserva("STANDARD")
                .cantidadPersonas(2)
                .valorFinal(200000)
                .build();
    }

    @Test
    void guardar_guardaYRetornaResponse() {
        Reserva guardada = toEntity(request);
        guardada.setId(1);
        when(rep.save(any(Reserva.class))).thenReturn(guardada);

        ReservaResponseDto resultado = service.r_guardar(request);

        assertEquals(1, resultado.getId());
        assertEquals(3, resultado.getIdUsuario());
        assertEquals("STANDARD", resultado.getTipoReserva());
        verify(rep).save(any(Reserva.class));
    }

    @Test
    void recuperar_idInexistente_lanzaNotFound() {
        when(rep.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> service.r_recuperar(999));
    }

    @Test
    void modificar_idInexistente_lanzaNotFound() {
        when(rep.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> service.r_modificar(999, request));
    }

    @Test
    void eliminar_idInexistente_lanzaNotFound() {
        when(rep.existsById(999)).thenReturn(false);

        assertThrows(EntidadNoEncontradaException.class, () -> service.r_eliminar(999));
    }

    @Test
    void eliminar_idExistente_devuelveTrue() {
        when(rep.existsById(1)).thenReturn(true);

        assertTrue(service.r_eliminar(1));
        verify(rep).deleteById(1);
    }

    private Reserva toEntity(ReservaRequestDto req) {
        Reserva r = new Reserva();
        r.setIdUsuario(req.getIdUsuario());
        r.setFechaReserva(req.getFechaReserva());
        r.setFechaTermino(req.getFechaTermino());
        r.setTipoReserva(req.getTipoReserva());
        r.setCantidadPersonas(req.getCantidadPersonas());
        r.setValorFinal(req.getValorFinal());
        return r;
    }
}
