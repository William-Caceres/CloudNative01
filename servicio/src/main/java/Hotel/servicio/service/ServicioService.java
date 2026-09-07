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
        s.setNombre(req.getNombre());
        s.setDescripcion(req.getDescripcion());
        s.setTipoServicio(req.getTipoServicio());
        s.setPrecio(req.getPrecio());
        s.setNumHabitacion(req.getNumHabitacion());
        s.setCapacidad(req.getCapacidad());
        s.setDisponible(req.getDisponible());
        s.setNivelServicio(req.getNivelServicio());
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
        s_mod.setNombre(req.getNombre());
        s_mod.setDescripcion(req.getDescripcion());
        s_mod.setTipoServicio(req.getTipoServicio());
        s_mod.setPrecio(req.getPrecio());
        s_mod.setNumHabitacion(req.getNumHabitacion());
        s_mod.setCapacidad(req.getCapacidad());
        s_mod.setDisponible(req.getDisponible());
        s_mod.setNivelServicio(req.getNivelServicio());
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
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .tipoServicio(s.getTipoServicio())
                .precio(s.getPrecio())
                .numHabitacion(s.getNumHabitacion())
                .capacidad(s.getCapacidad())
                .disponible(s.getDisponible())
                .nivelServicio(s.getNivelServicio())
                .build();
    }
}
