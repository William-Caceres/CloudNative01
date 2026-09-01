package Hotel.usuario.service;

import Hotel.usuario.entity.Usuario;
import Hotel.usuario.repository.UsuarioRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository rep;
    
    public Usuario u_registrar(Usuario u){
        /*-LOGICA NEGOCIO-*/
        return rep.save(u);
    };
    
    public Usuario u_login(String correo, String contrasenia){
        /*-LOGICA NEGOCIO-*/
        Usuario u = rep.findByCorreo(correo).orElse(null);
        
        if(u.getContrasenia().equals(contrasenia)){
            return u;
        }else{
            return null;
        }
    };
    
    public Usuario u_recuperar(Integer id){
        /*-LOGICA NEGOCIO-*/
        return rep.findById(id).orElse(null);
    };
    
    public List<Usuario> u_listar(){
        /*-LOGICA NEGOCIO-*/
        return rep.findAll();
    }
    
    public Usuario u_modificar(Usuario u){
        /*-LOGICA NEGOCIO-*/
        Usuario u_mod = rep.findById(u.getId()).orElse(null);
        
        if(u_mod!=null){
            u_mod.setNombre(u.getNombre());
            u_mod.setS_nombre(u.getS_nombre());
            u_mod.setA_paterno(u.getA_paterno());
            u_mod.setA_materno(u.getA_materno());
            u_mod.setRut(u.getRut());
            u_mod.setDv_rut(u.getDv_rut());
            u_mod.setEdad(u.getEdad());
            u_mod.setF_registro(u.getF_registro());
            u_mod.setTipo_usuario(u.getTipo_usuario());
            u_mod.setCorreo(u.getCorreo());
            u_mod.setTelefono(u.getTelefono());        
            u_mod.setContrasenia(u.getContrasenia());
            
            return rep.save(u_mod);
        }else{
            return null;
        }
    };
    
    public Boolean u_retirar(Integer id){
        rep.deleteById(id);
        return true;
    }

}
