package Hotel.reserva.service;

import Hotel.reserva.dto.ReservaRequestDto;
import Hotel.reserva.dto.ReservaResponseDto;
import Hotel.reserva.entity.Reserva;
import Hotel.reserva.exception.EntidadNoEncontradaException;
import Hotel.reserva.repository.ReservaRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {
    
    @Autowired
    private ReservaRepository rep;

    public ReservaResponseDto r_guardar(ReservaRequestDto req){
        Reserva r = new Reserva();
        r.setF_reserva(req.getF_reserva());
        r.setF_termino(req.getF_termino());
        r.setTipo_reserva(req.getTipo_reserva());
        r.setC_personas(req.getC_personas());
        r.setValor_final(req.getValor_final());
        return toResponse(rep.save(r));
    }
    
    public ReservaResponseDto r_recuperar(Integer id){
        Reserva r = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("RES-001",
                        "No existe una reserva con id: " + id));
        return toResponse(r);
    }
    
    public List<ReservaResponseDto> r_listar(){
        return rep.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public ReservaResponseDto r_modificar(Integer id, ReservaRequestDto req){
        Reserva r_mod = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("RES-001",
                        "No existe una reserva con id: " + id));
        r_mod.setF_reserva(req.getF_reserva());
        r_mod.setF_termino(req.getF_termino());
        r_mod.setTipo_reserva(req.getTipo_reserva());
        r_mod.setC_personas(req.getC_personas());
        r_mod.setValor_final(req.getValor_final());
        return toResponse(rep.save(r_mod));
    }
    
    public Boolean r_eliminar(Integer id){
        if (!rep.existsById(id)) {
            throw new EntidadNoEncontradaException("RES-001",
                    "No existe una reserva con id: " + id);
        }
        rep.deleteById(id);
        return true;
    }

    private ReservaResponseDto toResponse(Reserva r) {
        return ReservaResponseDto.builder()
                .id(r.getId())
                .f_reserva(r.getF_reserva())
                .f_termino(r.getF_termino())
                .tipo_reserva(r.getTipo_reserva())
                .c_personas(r.getC_personas())
                .valor_final(r.getValor_final())
                .build();
    }
}
