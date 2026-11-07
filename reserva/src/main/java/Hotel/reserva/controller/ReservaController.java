package Hotel.reserva.controller;

import Hotel.reserva.dto.PagoRequest;
import Hotel.reserva.entity.Reserva;
import Hotel.reserva.service.OutboxService;
import Hotel.reserva.service.ReservaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reserva")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService ser;

    private final OutboxService outbox;

    @PostMapping("/post")
    @PreAuthorize("hasRole('ADMIN')")
    public Reserva rc_guardar(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody Reserva req){
        return ser.r_guardar(req, idempotencyKey);
    }

    @GetMapping("/get/{id}")
    public Reserva rc_recuperar(@PathVariable Integer id){
        return ser.r_recuperar(id);
    }

    @GetMapping("/list")
    public List<Reserva> rc_listar(){
        return ser.r_listar();
    }

    @GetMapping("/outbox/resumen")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.Map<String, Object> rc_outbox() {
        return outbox.resumen();
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<Reserva> rc_listar_por_usuario(@PathVariable Integer idUsuario){
        return ser.r_listar_por_usuario(idUsuario);
    }

    @PutMapping("/put/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Reserva rc_modificar(@PathVariable Integer id, @Valid @RequestBody Reserva req){
        return ser.r_modificar(id, req);
    }

    @PutMapping("/{id}/pago")
    @PreAuthorize("hasRole('ADMIN')")
    public Reserva rc_confirmarPago(@PathVariable Integer id, @Valid @RequestBody PagoRequest req){
        return ser.r_confirmarPago(id, req);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean rc_eliminar(@PathVariable Integer id){
        return ser.r_eliminar(id);
    }
}
