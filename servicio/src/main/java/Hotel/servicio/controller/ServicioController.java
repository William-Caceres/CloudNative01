package Hotel.servicio.controller;

import Hotel.servicio.dto.ServicioRequestDto;
import Hotel.servicio.dto.ServicioResponseDto;
import Hotel.servicio.service.ServicioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/servicio")
@CrossOrigin(origins = "http://localhost:4200")
public class ServicioController {
    
    @Autowired
    private ServicioService ser;
    
    @PostMapping("/post")
    @PreAuthorize("hasRole('ADMIN')")
    public ServicioResponseDto sc_guardar(@Valid @RequestBody ServicioRequestDto req){
        return ser.s_guardar(req);
    }
    
    @GetMapping("/get/{id}")
    public ServicioResponseDto sc_recuperar(@PathVariable Integer id){
        return ser.s_recuperar(id);
    }
    
    @GetMapping("/list")
    public List<ServicioResponseDto> sc_listar(){
        return ser.s_listar();
    }
    
    @PutMapping("/put/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServicioResponseDto sc_modificar(@PathVariable Integer id, @Valid @RequestBody ServicioRequestDto req){
        return ser.s_modificar(id, req);
    }
    
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean sc_eliminar(@PathVariable Integer id){
        return ser.s_eliminar(id);
    }
}
