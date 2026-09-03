package Hotel.usuario.service;

import Hotel.usuario.dto.UsuarioRequestDto;
import Hotel.usuario.dto.UsuarioResponseDto;
import Hotel.usuario.entity.Usuario;
import Hotel.usuario.exception.BusinessRuleException;
import Hotel.usuario.exception.EntidadNoEncontradaException;
import Hotel.usuario.repository.UsuarioRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository rep;

    public UsuarioResponseDto u_registrar(UsuarioRequestDto req){
        if (rep.findByCorreo(req.getCorreo()).isPresent()) {
            throw new BusinessRuleException("USER-002", HttpStatus.CONFLICT,
                    "Ya existe un usuario registrado con el correo: " + req.getCorreo());
        }
        Usuario u = new Usuario();
        u.setNombre(req.getNombre());
        u.setS_nombre(req.getS_nombre());
        u.setA_paterno(req.getA_paterno());
        u.setA_materno(req.getA_materno());
        u.setRut(req.getRut());
        u.setDv_rut(req.getDv_rut());
        u.setEdad(req.getEdad());
        u.setTipo_usuario(req.getTipo_usuario() != null ? req.getTipo_usuario() : "CLIENTE");
        u.setCorreo(req.getCorreo());
        u.setContrasenia(req.getContrasenia());
        u.setTelefono(req.getTelefono());
        return toResponse(rep.save(u));
    }
    
    public UsuarioResponseDto u_login(String correo, String contrasenia){
        Usuario u = rep.findByCorreo(correo)
                .orElseThrow(() -> new EntidadNoEncontradaException("USER-003",
                        "No existe un usuario con el correo: " + correo));
        
        if (!u.getContrasenia().equals(contrasenia)) {
            throw new BusinessRuleException("USER-004", HttpStatus.UNAUTHORIZED,
                    "Las credenciales ingresadas son incorrectas");
        }
        return toResponse(u);
    }
    
    public UsuarioResponseDto u_recuperar(Integer id){
        Usuario u = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("USER-005",
                        "No existe un usuario con id: " + id));
        return toResponse(u);
    }
    
    public List<UsuarioResponseDto> u_listar(){
        return rep.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public UsuarioResponseDto u_modificar(Integer id, UsuarioRequestDto req){
        Usuario u_mod = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("USER-005",
                        "No existe un usuario con id: " + id));
        u_mod.setNombre(req.getNombre());
        u_mod.setS_nombre(req.getS_nombre());
        u_mod.setA_paterno(req.getA_paterno());
        u_mod.setA_materno(req.getA_materno());
        u_mod.setRut(req.getRut());
        u_mod.setDv_rut(req.getDv_rut());
        u_mod.setEdad(req.getEdad());
        u_mod.setTipo_usuario(req.getTipo_usuario());
        u_mod.setCorreo(req.getCorreo());
        u_mod.setContrasenia(req.getContrasenia());
        u_mod.setTelefono(req.getTelefono());
        return toResponse(rep.save(u_mod));
    }
    
    public Boolean u_retirar(Integer id){
        if (!rep.existsById(id)) {
            throw new EntidadNoEncontradaException("USER-005",
                    "No existe un usuario con id: " + id);
        }
        rep.deleteById(id);
        return true;
    }

    private UsuarioResponseDto toResponse(Usuario u) {
        return UsuarioResponseDto.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .s_nombre(u.getS_nombre())
                .a_paterno(u.getA_paterno())
                .a_materno(u.getA_materno())
                .rut(u.getRut())
                .dv_rut(u.getDv_rut())
                .edad(u.getEdad())
                .f_registro(u.getF_registro())
                .tipo_usuario(u.getTipo_usuario())
                .correo(u.getCorreo())
                .telefono(u.getTelefono())
                .build();
    }
}
