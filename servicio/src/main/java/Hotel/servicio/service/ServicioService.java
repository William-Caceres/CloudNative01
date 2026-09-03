package Hotel.servicio.service;

import Hotel.servicio.dto.ServicioRequestDto;
import Hotel.servicio.dto.ServicioResponseDto;
import Hotel.servicio.entity.Servicio;
import Hotel.servicio.exception.EntidadNoEncontradaException;
import Hotel.servicio.repository.ServicioRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioService {
    
    @Autowired
    private ServicioRepository rep;

    public ServicioResponseDto s_guardar(ServicioRequestDto req){
        Servicio s = new Servicio();
        s.setTipo_servicio(req.getTipo_servicio());
        s.setPrecio(req.getPrecio());
        s.setN_habitacion(req.getN_habitacion());
        s.setCapacidad(req.getCapacidad());
        s.setDisponible(req.getDisponible());
        s.setNivel_servicio(req.getNivel_servicio());
        return toResponse(rep.save(s));
    }
    
    public ServicioResponseDto s_recuperar(Integer id){
        Servicio s = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("SER-001",
                        "No existe un servicio con id: " + id));
        return toResponse(s);
    }
    
    public List<ServicioResponseDto> s_listar(){
        return rep.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public ServicioResponseDto s_modificar(Integer id, ServicioRequestDto req){
        Servicio s_mod = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("SER-001",
                        "No existe un servicio con id: " + id));
        s_mod.setTipo_servicio(req.getTipo_servicio());
        s_mod.setPrecio(req.getPrecio());
        s_mod.setN_habitacion(req.getN_habitacion());
        s_mod.setCapacidad(req.getCapacidad());
        s_mod.setDisponible(req.getDisponible());
        s_mod.setNivel_servicio(req.getNivel_servicio());
        return toResponse(rep.save(s_mod));
    }
    
    public Boolean s_eliminar(Integer id){
        if (!rep.existsById(id)) {
            throw new EntidadNoEncontradaException("SER-001",
                    "No existe un servicio con id: " + id);
        }
        rep.deleteById(id);
        return true;
    }

    private ServicioResponseDto toResponse(Servicio s) {
        return ServicioResponseDto.builder()
                .id(s.getId())
                .tipo_servicio(s.getTipo_servicio())
                .precio(s.getPrecio())
                .n_habitacion(s.getN_habitacion())
                .capacidad(s.getCapacidad())
                .disponible(s.getDisponible())
                .nivel_servicio(s.getNivel_servicio())
                .build();
    }
}
