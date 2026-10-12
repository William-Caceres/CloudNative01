package Hotel.reserva.service;

import Hotel.reserva.entity.Reserva;
import Hotel.reserva.exception.EntidadNoEncontradaException;
import Hotel.reserva.messaging.ReservaEventPublisher;
import Hotel.reserva.repository.ReservaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository rep;

    private final ReservaEventPublisher publisher;

    public Reserva r_guardar(Reserva req){
        Reserva guardada = rep.save(req);
        publisher.publicarReservaCreada(guardada);
        return guardada;
    }

    public Reserva r_recuperar(Integer id){
        return rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("RES-001",
                        "No existe una reserva con id: " + id));
    }

    public List<Reserva> r_listar(){
        return rep.findAll();
    }

    public List<Reserva> r_listar_por_usuario(Integer idUsuario){
        return rep.findByIdUsuario(idUsuario);
    }

    public Reserva r_modificar(Integer id, Reserva req){
        Reserva r_mod = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("RES-001",
                        "No existe una reserva con id: " + id));
        r_mod.setIdUsuario(req.getIdUsuario());
        r_mod.setFechaReserva(req.getFechaReserva());
        r_mod.setFechaTermino(req.getFechaTermino());
        r_mod.setTipoReserva(req.getTipoReserva());
        r_mod.setCantidadPersonas(req.getCantidadPersonas());
        r_mod.setValorFinal(req.getValorFinal());
        return rep.save(r_mod);
    }

    public Boolean r_eliminar(Integer id){
        if (!rep.existsById(id)) {
            throw new EntidadNoEncontradaException("RES-001",
                    "No existe una reserva con id: " + id);
        }
        rep.deleteById(id);
        return true;
    }
}
