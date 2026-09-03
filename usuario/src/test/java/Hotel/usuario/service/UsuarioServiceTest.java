package Hotel.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Hotel.usuario.dto.UsuarioRequestDto;
import Hotel.usuario.dto.UsuarioResponseDto;
import Hotel.usuario.entity.Usuario;
import Hotel.usuario.exception.BusinessRuleException;
import Hotel.usuario.exception.EntidadNoEncontradaException;
import Hotel.usuario.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository rep;

    @InjectMocks
    private UsuarioService service;

    private UsuarioRequestDto request;

    @BeforeEach
    void setUp() {
        request = UsuarioRequestDto.builder()
                .nombre("Emilio")
                .s_nombre("Andres")
                .a_paterno("Caceres")
                .a_materno("Muñoz")
                .rut(12345678)
                .dv_rut("9")
                .edad(25)
                .tipo_usuario("CLIENTE")
                .correo("emilio@hotel.cl")
                .contrasenia("clave123")
                .telefono(987654321)
                .build();
    }

    @Test
    void registrar_usuarioNuevo_guardaYRetornaResponse() {
        Usuario guardado = toEntity(request);
        guardado.setId(1);
        when(rep.findByCorreo("emilio@hotel.cl")).thenReturn(Optional.empty());
        when(rep.save(any(Usuario.class))).thenReturn(guardado);

        UsuarioResponseDto resultado = service.u_registrar(request);

        assertEquals(1, resultado.getId());
        assertEquals("emilio@hotel.cl", resultado.getCorreo());
        verify(rep).save(any(Usuario.class));
    }

    @Test
    void registrar_correoDuplicado_lanzaConflict() {
        Usuario existente = toEntity(request);
        when(rep.findByCorreo("emilio@hotel.cl")).thenReturn(Optional.of(existente));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.u_registrar(request));

        assertEquals("USER-002", ex.getCode());
        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
        verify(rep, never()).save(any(Usuario.class));
    }

    @Test
    void login_correoInexistente_lanzaNotFound_sinNPE() {
        when(rep.findByCorreo("nadie@hotel.cl")).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class,
                () -> service.u_login("nadie@hotel.cl", "cualquier"));
    }

    @Test
    void login_credencialesCorrectas_retornaUsuario() {
        Usuario u = toEntity(request);
        when(rep.findByCorreo("emilio@hotel.cl")).thenReturn(Optional.of(u));

        UsuarioResponseDto resultado = service.u_login("emilio@hotel.cl", "clave123");

        assertEquals("emilio@hotel.cl", resultado.getCorreo());
    }

    @Test
    void login_contraseniaIncorrecta_lanzaUnauthorized() {
        when(rep.findByCorreo("emilio@hotel.cl")).thenReturn(Optional.of(toEntity(request)));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.u_login("emilio@hotel.cl", "incorrecta"));

        assertEquals("USER-004", ex.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getHttpStatus());
    }

    @Test
    void recuperar_idInexistente_lanzaNotFound() {
        when(rep.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> service.u_recuperar(999));
    }

    @Test
    void retirar_idInexistente_lanzaNotFound() {
        when(rep.existsById(999)).thenReturn(false);

        assertThrows(EntidadNoEncontradaException.class, () -> service.u_retirar(999));
    }

    @Test
    void retirar_idExistente_devuelveTrue() {
        when(rep.existsById(1)).thenReturn(true);

        assertTrue(service.u_retirar(1));
        verify(rep).deleteById(1);
    }

    private Usuario toEntity(UsuarioRequestDto req) {
        Usuario u = new Usuario();
        u.setNombre(req.getNombre());
        u.setS_nombre(req.getS_nombre());
        u.setA_paterno(req.getA_paterno());
        u.setA_materno(req.getA_materno());
        u.setRut(req.getRut());
        u.setDv_rut(req.getDv_rut());
        u.setEdad(req.getEdad());
        u.setTipo_usuario(req.getTipo_usuario());
        u.setCorreo(req.getCorreo());
        u.setContrasenia(req.getContrasenia());
        u.setTelefono(req.getTelefono());
        return u;
    }
}
