package Hotel.rabbitadmin.kafka.controller;

import Hotel.rabbitadmin.kafka.dto.KafkaConfigRequest;
import Hotel.rabbitadmin.kafka.dto.KafkaGrupoInfo;
import Hotel.rabbitadmin.kafka.dto.KafkaGrupoResumen;
import Hotel.rabbitadmin.kafka.dto.KafkaResumen;
import Hotel.rabbitadmin.kafka.dto.KafkaTopicInfo;
import Hotel.rabbitadmin.kafka.dto.KafkaTopicRequest;
import Hotel.rabbitadmin.kafka.service.KafkaAdminService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rabbit-admin/kafka")
@RequiredArgsConstructor
public class KafkaAdminController {

    private static final String MENSAJE = "mensaje";

    private final KafkaAdminService ser;

    @PostMapping("/topics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> crearTopic(@Valid @RequestBody KafkaTopicRequest req) {
        ser.crearTopic(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(MENSAJE, "Topic creado: " + req.nombre()));
    }

    @GetMapping("/topics")
    public List<String> listarTopics() {
        return ser.listarTopics();
    }

    @GetMapping("/topics/{nombre}")
    public KafkaTopicInfo describirTopic(@PathVariable String nombre) {
        return ser.describirTopic(nombre);
    }

    @PutMapping("/topics/{nombre}/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> alterarConfiguraciones(
            @PathVariable String nombre,
            @Valid @RequestBody KafkaConfigRequest req) {
        ser.alterarConfiguraciones(nombre, req);
        return ResponseEntity.ok(Map.of(MENSAJE, "Configuraciones actualizadas: " + nombre));
    }

    @DeleteMapping("/topics/{nombre}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> eliminarTopic(@PathVariable String nombre) {
        ser.eliminarTopic(nombre);
        return ResponseEntity.ok(Map.of(MENSAJE, "Topic eliminado: " + nombre));
    }

    @GetMapping("/groups")
    public List<KafkaGrupoResumen> listarGrupos() {
        return ser.listarGrupos();
    }

    @GetMapping("/groups/{grupo}")
    public KafkaGrupoInfo describirGrupo(@PathVariable String grupo) {
        return ser.describirGrupo(grupo);
    }

    @GetMapping("/summary")
    public KafkaResumen resumen() {
        return ser.resumen();
    }
}
