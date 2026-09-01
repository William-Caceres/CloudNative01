package Hotel.reserva.service;

import Hotel.reserva.entity.Reserva;
import Hotel.reserva.repository.ReservaRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {
    
    @Autowired
    private ReservaRepository rep;
    
    public Reserva r_guardar(Reserva r){
        /*-LOGICA NEGOCIO-*/
        return rep.save(r);
    };
    
    public Reserva r_recuperar(Integer id){
        /*-LOGICA NEGOCIO-*/
        return rep.findById(id).orElse(null);
    };
    
    public List<Reserva> r_listar(){
        /*-LOGICA NEGOCIO-*/
        return rep.findAll();
    };
    
    public Reserva r_modificar(Reserva r){
        /*-LOGICA NEGOCIO-*/
        Reserva r_mod = rep.findById(r.getId()).orElse(null);
        
        if(r_mod!=null){
            r_mod.setF_reserva(r.getF_reserva());
            r_mod.setF_termino(r.getF_termino());
            r_mod.setTipo_reserva(r.getTipo_reserva());
            r_mod.setC_personas(r.getC_personas());
            r_mod.setValor_final(r.getValor_final());
                    
            return rep.save(r_mod);
        }else{
            return null;
        }
    };
    
    public Boolean r_eliminar(Integer id){
        rep.deleteById(id);
        return true;
    };
}
