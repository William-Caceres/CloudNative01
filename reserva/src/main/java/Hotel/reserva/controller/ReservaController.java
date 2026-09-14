package Hotel.reserva.controller;

import Hotel.reserva.entity.Reserva;
import Hotel.reserva.service.ReservaService;
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
    @PreAuthorize("hasAnyRole('client', 'admin')")
    public Reserva rc_guardar(@RequestBody Reserva r){
        return ser.r_guardar(r);
    };
    
    @GetMapping("/get/{id}")
    @PreAuthorize("hasAnyRole('client', 'admin')")
    public Reserva rc_recuperar(@PathVariable Integer id){
        return ser.r_recuperar(id);
    };
    
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('client', 'admin')")
    public List<Reserva> rc_listar(){
        return ser.r_listar();
    };
    
    @PutMapping("/put")
    @PreAuthorize("hasAnyRole('admin')")
    public Reserva rc_modificar(@RequestBody Reserva r){
        return ser.r_modificar(r);
    };
    
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('client', 'admin')")
    public Boolean rc_eliminar(@PathVariable Integer id){
        return ser.r_eliminar(id);
    };
}
