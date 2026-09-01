package Hotel.servicio.service;

import Hotel.servicio.entity.Servicio;
import Hotel.servicio.repository.ServicioRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioService {
    
    @Autowired
    private ServicioRepository rep;
    
    public Servicio s_guardar(Servicio s){
        /*-LOGICA NEGOCIO-*/
        return rep.save(s);
    };
    
    public Servicio s_recuperar(Integer id){
        /*-LOGICA NEGOCIO-*/
        return rep.findById(id).orElse(null);
    };
    
    public List<Servicio> s_listar(){
        /*-LOGICA NEGOCIO-*/
        return rep.findAll();
    };
    
    public Servicio s_modificar(Servicio s){
        /*-LOGICA NEGOCIO-*/
        Servicio s_mod = rep.findById(s.getId()).orElse(null);
        
        if(s_mod!=null){
            s_mod.setTipo_servicio(s.getTipo_servicio());
            s_mod.setPrecio(s.getPrecio());
            s_mod.setN_habitacion(s.getN_habitacion());
            s_mod.setCapacidad(s.getCapacidad());
            s_mod.setDisponible(s.getDisponible());
            s_mod.setNivel_servicio(s.getNivel_servicio());
                    
            return rep.save(s_mod);
        }else{
            return null;
        }
    };
    
    public Boolean s_eliminar(Integer id){
        rep.deleteById(id);
        return true;
    };
}
