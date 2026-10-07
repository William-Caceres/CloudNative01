package Hotel.rabbitadmin.controller;

import Hotel.rabbitadmin.dto.BindingRequest;
import Hotel.rabbitadmin.dto.ColaInfo;
import Hotel.rabbitadmin.dto.ColaRequest;
import Hotel.rabbitadmin.dto.ExchangeRequest;
import Hotel.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API de administracion de RabbitMQ: colas, exchanges y bindings. */
@RestController
@RequestMapping("/api/v1/rabbit-admin")
public class AdminController {

    @Autowired
    private RabbitAdminService ser;

    /** Crea una cola durable. Solo ADMIN. */
    @PostMapping("/queues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rac_crearCola(@Valid @RequestBody ColaRequest req) {
        ser.ra_crearCola(req.nombre());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Cola creada: " + req.nombre()));
    }

    /** Info de una cola: mensajes en espera y consumidores. */
    @GetMapping("/queues/{nombre}")
    public ResponseEntity<Map<String, Object>> rac_infoCola(@PathVariable String nombre) {
        ColaInfo info = ser.ra_infoCola(nombre);
        Map<String, Object> salida = new LinkedHashMap<>();
        salida.put("nombre", info.nombre());
        salida.put("mensajes", info.mensajes());
        salida.put("consumidores", info.consumidores());
        return ResponseEntity.ok(salida);
    }

    /** Elimina una cola. Solo ADMIN. */
    @DeleteMapping("/queues/{nombre}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rac_eliminarCola(@PathVariable String nombre) {
        ser.ra_eliminarCola(nombre);
        return ResponseEntity.ok(Map.of("mensaje", "Cola eliminada: " + nombre));
    }

    /** Purga (vacia) una cola. Solo ADMIN. */
    @PostMapping("/queues/{nombre}/purge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rac_purgarCola(@PathVariable String nombre) {
        int purgados = ser.ra_purgarCola(nombre);
        return ResponseEntity.ok(Map.of("mensaje", "Cola purgada: " + nombre, "purgados", purgados));
    }

    /** Crea un exchange direct o topic. Solo ADMIN. */
    @PostMapping("/exchanges")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rac_crearExchange(@Valid @RequestBody ExchangeRequest req) {
        ser.ra_crearExchange(req.nombre(), req.tipo());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Exchange creado: " + req.nombre() + " (" + req.tipo() + ")"));
    }

    /** Elimina un exchange. Solo ADMIN. */
    @DeleteMapping("/exchanges/{nombre}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rac_eliminarExchange(@PathVariable String nombre) {
        ser.ra_eliminarExchange(nombre);
        return ResponseEntity.ok(Map.of("mensaje", "Exchange eliminado: " + nombre));
    }

    /** Crea un binding entre cola y exchange. Solo ADMIN. */
    @PostMapping("/bindings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rac_crearBinding(@Valid @RequestBody BindingRequest req) {
        ser.ra_crearBinding(req.cola(), req.exchange(), req.routingKey());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Binding creado: " + req.cola() + " <- " + req.exchange() + " (" + req.routingKey() + ")"));
    }
}
