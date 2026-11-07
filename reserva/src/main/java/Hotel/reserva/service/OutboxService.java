package Hotel.reserva.service;

import Hotel.reserva.entity.OutboxEvent;
import Hotel.reserva.repository.OutboxEventRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository repo;

    public void registrarReserva(Integer reservaId) {
        repo.save(nuevo(reservaId, OutboxEvent.RESERVA_CREADA));
    }

    public void registrarPago(Integer reservaId) {
        repo.save(nuevo(reservaId, OutboxEvent.PAGO_CONFIRMADO));
    }

    public Map<String, Object> resumen() {
        Map<String, Object> salida = new LinkedHashMap<>();
        salida.put("pendientes", repo.countByEstado(OutboxEvent.PENDIENTE));
        salida.put("fallidos", repo.countByEstado(OutboxEvent.FALLIDO));
        salida.put("enviados", repo.countByEstado(OutboxEvent.ENVIADO));
        salida.put("agotados", repo.countByEstado(OutboxEvent.AGOTADO));
        salida.put("ultimos", List.copyOf(repo.findTop10ByOrderByIdDesc()));
        return salida;
    }

    private OutboxEvent nuevo(Integer reservaId, String tipo) {
        LocalDateTime ahora = LocalDateTime.now();
        return new OutboxEvent(null, reservaId, tipo, OutboxEvent.PENDIENTE, 0, ahora, ahora, null);
    }
}
