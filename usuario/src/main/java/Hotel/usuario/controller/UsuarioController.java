package Hotel.usuario.controller;

import Hotel.usuario.entity.Usuario;
import Hotel.usuario.service.UsuarioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {
    
    @Autowired
    private UsuarioService ser;
    
    @PostMapping("/register")
    public Usuario uc_registrar(@RequestBody Usuario u){
        return ser.u_registrar(u);
    };
    
    @GetMapping("/login/{correo}/{contra}")
    public Usuario uc_login(@PathVariable String correo, @PathVariable String contra){
        return ser.u_login(correo, contra);
    };
    
    @GetMapping("/get/{id}")
    public Usuario uc_recuperar(@PathVariable Integer id){
        return ser.u_recuperar(id);
    };
    
    @GetMapping("/list")
    public List<Usuario> uc_listar(){
        return ser.u_listar();
    };
    
    @PutMapping("/put")
    public Usuario uc_modificar(@RequestBody Usuario u){
        return ser.u_modificar(u);
    };
    
    @DeleteMapping("/delete/{id}")
    public Boolean uc_retirar(@PathVariable Integer id){
        return ser.u_retirar(id);
    };
}
