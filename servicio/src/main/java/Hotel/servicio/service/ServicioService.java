package Hotel.servicio.service;

import Hotel.servicio.entity.Servicio;
import Hotel.servicio.repository.ServicioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioService {
    
    @Autowired
    private ServicioRepository rep;
    
    public Servicio s_guardar(Servicio s){
        /*-LOGICA NEGOCIO-*/
        List<Servicio> servicios = rep.findByNumHabitacion(s.getNumHabitacion());

        for (Servicio s_for : servicios) {

            if (s_for.getTipoServicio().equalsIgnoreCase(s.getTipoServicio())) {
                return null;
            }
        }
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
        Optional<Servicio> s_find = rep.findById(s.getId());
        
        if(s_find.isPresent()){
            Servicio s_mod = s_find.get();
            s_mod.setTipoServicio(s.getTipoServicio());
            s_mod.setPrecio(s.getPrecio());
            s_mod.setNumHabitacion(s.getNumHabitacion());
            s_mod.setCapacidad(s.getCapacidad());
            s_mod.setDisponible(s.getDisponible());
            s_mod.setNivelServicio(s.getNivelServicio());
            s_mod.setNombre(s.getNombre());
            s_mod.setDescripcion(s.getDescripcion());
                    
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
