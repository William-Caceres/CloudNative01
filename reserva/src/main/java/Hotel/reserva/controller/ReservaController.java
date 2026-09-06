package Hotel.reserva.controller;

import Hotel.reserva.dto.ReservaRequestDto;
import Hotel.reserva.dto.ReservaResponseDto;
import Hotel.reserva.service.ReservaService;
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
@RequestMapping("/api/v1/reserva")
@CrossOrigin(origins = "http://localhost:5173")
public class ReservaController {
    
    @Autowired
    private ReservaService ser;
    
    @PostMapping("/post")
    @PreAuthorize("hasRole('ADMIN')")
    public ReservaResponseDto rc_guardar(@Valid @RequestBody ReservaRequestDto req){
        return ser.r_guardar(req);
    }
    
    @GetMapping("/get/{id}")
    public ReservaResponseDto rc_recuperar(@PathVariable Integer id){
        return ser.r_recuperar(id);
    }
    
    @GetMapping("/list")
    public List<ReservaResponseDto> rc_listar(){
        return ser.r_listar();
    }
    
    @PutMapping("/put/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ReservaResponseDto rc_modificar(@PathVariable Integer id, @Valid @RequestBody ReservaRequestDto req){
        return ser.r_modificar(id, req);
    }
    
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean rc_eliminar(@PathVariable Integer id){
        return ser.r_eliminar(id);
    }
}
