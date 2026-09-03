package Hotel.servicio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Hotel.servicio.dto.ServicioRequestDto;
import Hotel.servicio.dto.ServicioResponseDto;
import Hotel.servicio.entity.Servicio;
import Hotel.servicio.exception.EntidadNoEncontradaException;
import Hotel.servicio.repository.ServicioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository rep;

    @InjectMocks
    private ServicioService service;

    private ServicioRequestDto request;

    @BeforeEach
    void setUp() {
        request = ServicioRequestDto.builder()
                .tipo_servicio("HABITACION")
                .precio(50000)
                .n_habitacion(101)
                .capacidad(2)
                .disponible(true)
                .nivel_servicio("STANDARD")
                .build();
    }

    @Test
    void guardar_guardaYRetornaResponse() {
        Servicio guardado = toEntity(request);
        guardado.setId(1);
        when(rep.save(any(Servicio.class))).thenReturn(guardado);

        ServicioResponseDto resultado = service.s_guardar(request);

        assertEquals(1, resultado.getId());
        assertEquals("HABITACION", resultado.getTipo_servicio());
        verify(rep).save(any(Servicio.class));
    }

    @Test
    void recuperar_idInexistente_lanzaNotFound() {
        when(rep.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> service.s_recuperar(999));
    }

    @Test
    void modificar_idInexistente_lanzaNotFound() {
        when(rep.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> service.s_modificar(999, request));
    }

    @Test
    void eliminar_idInexistente_lanzaNotFound() {
        when(rep.existsById(999)).thenReturn(false);

        assertThrows(EntidadNoEncontradaException.class, () -> service.s_eliminar(999));
    }

    @Test
    void eliminar_idExistente_devuelveTrue() {
        when(rep.existsById(1)).thenReturn(true);

        assertTrue(service.s_eliminar(1));
        verify(rep).deleteById(1);
    }

    private Servicio toEntity(ServicioRequestDto req) {
        Servicio s = new Servicio();
        s.setTipo_servicio(req.getTipo_servicio());
        s.setPrecio(req.getPrecio());
        s.setN_habitacion(req.getN_habitacion());
        s.setCapacidad(req.getCapacidad());
        s.setDisponible(req.getDisponible());
        s.setNivel_servicio(req.getNivel_servicio());
        return s;
    }
}
