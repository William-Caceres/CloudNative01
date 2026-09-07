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
        r.setIdUsuario(req.getIdUsuario());
        r.setFechaReserva(req.getFechaReserva());
        r.setFechaTermino(req.getFechaTermino());
        r.setTipoReserva(req.getTipoReserva());
        r.setCantidadPersonas(req.getCantidadPersonas());
        r.setValorFinal(req.getValorFinal());
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

    public List<ReservaResponseDto> r_listar_por_usuario(Integer idUsuario){
        return rep.findByIdUsuario(idUsuario).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public ReservaResponseDto r_modificar(Integer id, ReservaRequestDto req){
        Reserva r_mod = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("RES-001",
                        "No existe una reserva con id: " + id));
        r_mod.setIdUsuario(req.getIdUsuario());
        r_mod.setFechaReserva(req.getFechaReserva());
        r_mod.setFechaTermino(req.getFechaTermino());
        r_mod.setTipoReserva(req.getTipoReserva());
        r_mod.setCantidadPersonas(req.getCantidadPersonas());
        r_mod.setValorFinal(req.getValorFinal());
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
                .idUsuario(r.getIdUsuario())
                .fechaReserva(r.getFechaReserva())
                .fechaTermino(r.getFechaTermino())
                .tipoReserva(r.getTipoReserva())
                .cantidadPersonas(r.getCantidadPersonas())
                .valorFinal(r.getValorFinal())
                .build();
    }
}
