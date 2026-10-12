package Hotel.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import Hotel.usuario.entity.Usuario;
import Hotel.usuario.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String CORREO_EXISTENTE = "admin.prueba@test.cl";
    private static final String CORREO_NUEVO = "nuevo@test.cl";
    private static final String CORREO_CARRERA = "carrera@test.cl";

    @Mock
    private UsuarioRepository rep;

    @InjectMocks
    private UsuarioService ser;

    @Test
    void usuarioExistenteSeDevuelveSinCrear() {
        Usuario existente = new Usuario();
        existente.setCorreo(CORREO_EXISTENTE);
        when(rep.findByCorreo(CORREO_EXISTENTE)).thenReturn(Optional.of(existente));

        Usuario res = ser.u_por_correo(CORREO_EXISTENTE, "Admin Prueba");

        assertEquals(existente, res);
    }

    @Test
    void usuarioNuevoSeCreaDesdeElToken() {
        when(rep.findByCorreo(CORREO_NUEVO)).thenReturn(Optional.empty());
        when(rep.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario res = ser.u_por_correo(CORREO_NUEVO, "Nuevo Usuario");

        assertNotNull(res);
        assertEquals(CORREO_NUEVO, res.getCorreo());
        assertEquals("Nuevo Usuario", res.getNombre());
        assertEquals("CLIENTE", res.getTipo_usuario());
    }

    @Test
    void usuarioNuevoConCarreraSeRecuperaDelConflicto() {
        Usuario existente = new Usuario();
        existente.setCorreo(CORREO_CARRERA);
        when(rep.findByCorreo(CORREO_CARRERA))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existente));
        when(rep.save(any(Usuario.class)))
                .thenThrow(new DataIntegrityViolationException("correo duplicado"));

        Usuario res = ser.u_por_correo(CORREO_CARRERA, "Carrera Test");

        assertEquals(existente, res);
    }
}
