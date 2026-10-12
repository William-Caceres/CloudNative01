package Hotel.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Hotel.usuario.entity.Usuario;
import Hotel.usuario.exception.EntidadNoEncontradaException;
import Hotel.usuario.repository.UsuarioRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Cobertura real del servicio: persistencia en H2 y flujo de alta por token. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioServiceIntegrationTest {

    private static final String CODIGO_NO_ENCONTRADO = "USER-005";
    private static final String CORREO_NUEVO = "nuevo@test.cl";
    private static final String NOMBRE_MODIFICADO = "Modificado";

    @Autowired
    private UsuarioService ser;

    @Autowired
    private UsuarioRepository rep;

    private static Usuario usuarioValido(String correo) {
        Usuario u = new Usuario();
        u.setNombre("Prueba");
        u.setCorreo(correo);
        u.setA_paterno("Apellido");
        u.setRut(12345678);
        u.setDv_rut("9");
        u.setContrasenia("secreta");
        u.setTipo_usuario("CLIENTE");
        return u;
    }

    @Test
    void porCorreoExistenteDevuelveSinCrear() {
        Usuario persistido = rep.save(usuarioValido("existe@test.cl"));

        Usuario encontrado = ser.u_por_correo("existe@test.cl", "Otro Nombre");

        assertEquals(persistido.getId(), encontrado.getId());
        assertEquals("Prueba", encontrado.getNombre());
    }

    @Test
    void porCorreoNuevoLoCreaConDatosDelToken() {
        Usuario creado = ser.u_por_correo(CORREO_NUEVO, "Nombre Token");

        assertNotNull(creado.getId());
        assertEquals(CORREO_NUEVO, creado.getCorreo());
        assertEquals("Nombre Token", creado.getNombre());
        assertEquals("CLIENTE", creado.getTipo_usuario());
        assertEquals("", creado.getA_paterno());
        assertEquals(0, creado.getRut());
        assertEquals("0", creado.getDv_rut());
        assertTrue(rep.findByCorreo(CORREO_NUEVO).isPresent());
    }

    @Test
    void porCorreoNuevoSinNombreUsaElCorreo() {
        Usuario creado = ser.u_por_correo("sin.nombre@test.cl", null);

        assertEquals("sin.nombre@test.cl", creado.getNombre());
    }

    @Test
    void recuperarExistenteDevuelveElUsuario() {
        Usuario persistido = rep.save(usuarioValido("recuperar@test.cl"));

        Usuario encontrado = ser.u_recuperar(persistido.getId());

        assertEquals(persistido.getId(), encontrado.getId());
    }

    @Test
    void recuperarInexistenteLanzaUser005() {
        EntidadNoEncontradaException ex = assertThrows(EntidadNoEncontradaException.class,
                () -> ser.u_recuperar(999999));

        assertEquals(CODIGO_NO_ENCONTRADO, ex.getCode());
    }

    @Test
    void listarDevuelveLoPersistido() {
        Usuario persistido = rep.save(usuarioValido("listar@test.cl"));

        List<Usuario> lista = ser.u_listar();

        assertTrue(lista.stream().anyMatch(u -> persistido.getId().equals(u.getId())));
    }

    @Test
    void modificarActualizaLosCampos() {
        Usuario persistido = rep.save(usuarioValido("original@test.cl"));
        Usuario cambios = usuarioValido("modificado@test.cl");
        cambios.setNombre(NOMBRE_MODIFICADO);
        cambios.setTelefono(987654321);

        Usuario modificado = ser.u_modificar(persistido.getId(), cambios);

        assertEquals(NOMBRE_MODIFICADO, modificado.getNombre());
        assertEquals("modificado@test.cl", modificado.getCorreo());
        assertEquals(987654321, modificado.getTelefono());
        assertEquals(NOMBRE_MODIFICADO, rep.findById(persistido.getId()).orElseThrow().getNombre());
    }

    @Test
    void modificarInexistenteLanzaUser005() {
        EntidadNoEncontradaException ex = assertThrows(EntidadNoEncontradaException.class,
                () -> ser.u_modificar(999999, usuarioValido("x@test.cl")));

        assertEquals(CODIGO_NO_ENCONTRADO, ex.getCode());
    }

    @Test
    void retirarExistenteDevuelveTrueYDesaparece() {
        Usuario persistido = rep.save(usuarioValido("retirar@test.cl"));

        assertTrue(ser.u_retirar(persistido.getId()));
        assertFalse(rep.existsById(persistido.getId()));
    }

    @Test
    void retirarInexistenteLanzaUser005() {
        EntidadNoEncontradaException ex = assertThrows(EntidadNoEncontradaException.class,
                () -> ser.u_retirar(999999));

        assertEquals(CODIGO_NO_ENCONTRADO, ex.getCode());
    }
}
