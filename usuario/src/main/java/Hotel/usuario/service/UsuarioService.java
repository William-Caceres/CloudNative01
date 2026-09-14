package Hotel.usuario.service;

import Hotel.usuario.entity.Usuario;
import Hotel.usuario.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository rep;
    
    public Usuario u_registrar(Usuario u){
        /*-LOGICA NEGOCIO-*/
        Optional<Usuario> u_find = rep.findByCorreo(u.getCorreo());
        if(u_find.isPresent()){
            return null;
        }else{
            return rep.save(u);
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
        Optional<Usuario> u_find = rep.findById(u.getId());
        
        if(u_find.isPresent()){
            Usuario u_mod = u_find.get();
            u_mod.setNombre(u.getNombre());
            u_mod.setA_paterno(u.getA_paterno());
            u_mod.setRut(u.getRut());
            u_mod.setDv_rut(u.getDv_rut());
            u_mod.setEdad(u.getEdad());
            u_mod.setCorreo(u.getCorreo());
            u_mod.setTelefono(u.getTelefono());        
            
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
