package Hotel.servicio.controller;

import Hotel.servicio.entity.Servicio;
import Hotel.servicio.service.ServicioService;
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
@CrossOrigin(origins = "http://localhost:5173")
public class ServicioController {
    
    @Autowired
    private ServicioService ser;
    
    @PostMapping("/post")
    @PreAuthorize("hasAnyRole('admin')")
    public Servicio sc_guardar(@RequestBody Servicio s){
        return ser.s_guardar(s);
    };
    
    @GetMapping("/get/{id}")
    @PreAuthorize("hasAnyRole('client', 'admin')")
    public Servicio sc_recuperar(@PathVariable Integer id){
        return ser.s_recuperar(id);
    };
    
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('client', 'admin')")
    public List<Servicio> sc_listar(){
        return ser.s_listar();
    };
    
    @PutMapping("/put")
    @PreAuthorize("hasAnyRole('admin')")
    public Servicio sc_modificar(@RequestBody Servicio s){
        return ser.s_modificar(s);
    };
    
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('admin')")
    public Boolean sc_eliminar(@PathVariable Integer id){
        return ser.s_eliminar(id);
    };
}