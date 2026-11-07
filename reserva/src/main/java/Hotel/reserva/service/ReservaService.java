package Hotel.reserva.service;

import Hotel.reserva.dto.PagoRequest;
import Hotel.reserva.entity.IdempotencyRecord;
import Hotel.reserva.entity.Reserva;
import Hotel.reserva.exception.BusinessRuleException;
import Hotel.reserva.exception.EntidadNoEncontradaException;
import Hotel.reserva.messaging.KafkaEventPublisher;
import Hotel.reserva.messaging.ReservaEventPublisher;
import Hotel.reserva.repository.IdempotencyRecordRepository;
import Hotel.reserva.repository.ReservaRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository rep;

    private final IdempotencyRecordRepository idempotency;

    private final ReservaEventPublisher publisher;

    private final KafkaEventPublisher kafkaPublisher;

    public Reserva r_guardar(Reserva req){
        return r_guardar(req, null);
    }

    @Transactional
    public Reserva r_guardar(Reserva req, String idempotencyKey){
        if (idempotencyKey != null && idempotencyKey.length() > 100) {
            throw new BusinessRuleException("RES-003", HttpStatus.BAD_REQUEST,
                    "La cabecera Idempotency-Key no puede superar los 100 caracteres");
        }
        String hash = hash(req);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var previa = idempotency.findById(idempotencyKey);
            if (previa.isPresent()) {
                if (!Objects.equals(previa.get().getRequestHash(), hash)) {
                    throw new BusinessRuleException("RES-002", HttpStatus.CONFLICT,
                            "La Idempotency-Key ya fue usada con otra carga");
                }
                return r_recuperar(previa.get().getReservaId());
            }
        }
        req.setEstadoPago(Reserva.ESTADO_PAGO_PENDIENTE);
        req.setMetodoPago(null);
        req.setReferenciaPago(null);
        Reserva guardada = rep.save(req);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotency.save(new IdempotencyRecord(idempotencyKey, hash, guardada.getId(), LocalDateTime.now()));
        }
        publisher.publicarReservaCreada(guardada);
        kafkaPublisher.publicarReservaCreada(guardada);
        return guardada;
    }

    private String hash(Reserva req) {
        String canonical = String.join("|",
                Objects.toString(req.getIdUsuario(), ""),
                Objects.toString(req.getFechaReserva(), ""),
                Objects.toString(req.getFechaTermino(), ""),
                Objects.toString(req.getTipoReserva(), ""),
                Objects.toString(req.getCantidadPersonas(), ""),
                Objects.toString(req.getValorFinal(), ""));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public Reserva r_recuperar(Integer id){
        return rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("RES-001",
                        "No existe una reserva con id: " + id));
    }

    public List<Reserva> r_listar(){
        return rep.findAll();
    }

    public List<Reserva> r_listar_por_usuario(Integer idUsuario){
        return rep.findByIdUsuario(idUsuario);
    }

    public Reserva r_modificar(Integer id, Reserva req){
        Reserva r_mod = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("RES-001",
                        "No existe una reserva con id: " + id));
        r_mod.setIdUsuario(req.getIdUsuario());
        r_mod.setFechaReserva(req.getFechaReserva());
        r_mod.setFechaTermino(req.getFechaTermino());
        r_mod.setTipoReserva(req.getTipoReserva());
        r_mod.setCantidadPersonas(req.getCantidadPersonas());
        r_mod.setValorFinal(req.getValorFinal());
        return rep.save(r_mod);
    }

    public Reserva r_confirmarPago(Integer id, PagoRequest req){
        Reserva reserva = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("RES-001",
                        "No existe una reserva con id: " + id));
        if (Reserva.ESTADO_PAGO_CONFIRMADO.equals(reserva.getEstadoPago())) {
            return reserva;
        }
        reserva.setEstadoPago(Reserva.ESTADO_PAGO_CONFIRMADO);
        reserva.setMetodoPago(req.metodoPago());
        reserva.setReferenciaPago(UUID.randomUUID().toString());
        Reserva guardada = rep.save(reserva);
        kafkaPublisher.publicarPago(guardada);
        return guardada;
    }

    public Boolean r_eliminar(Integer id){
        if (!rep.existsById(id)) {
            throw new EntidadNoEncontradaException("RES-001",
                    "No existe una reserva con id: " + id);
        }
        rep.deleteById(id);
        return true;
    }
}
