package Hotel.usuario.controller;

import Hotel.usuario.dto.UsuarioRequestDto;
import Hotel.usuario.dto.UsuarioResponseDto;
import Hotel.usuario.service.UsuarioService;
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
@RequestMapping("/api/v1/usuario")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {
    
    @Autowired
    private UsuarioService ser;
    
    @PostMapping("/register")
    public UsuarioResponseDto uc_registrar(@Valid @RequestBody UsuarioRequestDto req){
        return ser.u_registrar(req);
    }
    
    @GetMapping("/login/{correo}/{contra}")
    public UsuarioResponseDto uc_login(@PathVariable String correo, @PathVariable String contra){
        return ser.u_login(correo, contra);
    }
    
    @GetMapping("/get/{id}")
    public UsuarioResponseDto uc_recuperar(@PathVariable Integer id){
        return ser.u_recuperar(id);
    }
    
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioResponseDto> uc_listar(){
        return ser.u_listar();
    }
    
    @PutMapping("/put/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponseDto uc_modificar(@PathVariable Integer id, @Valid @RequestBody UsuarioRequestDto req){
        return ser.u_modificar(id, req);
    }
    
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean uc_retirar(@PathVariable Integer id){
        return ser.u_retirar(id);
    }
}
