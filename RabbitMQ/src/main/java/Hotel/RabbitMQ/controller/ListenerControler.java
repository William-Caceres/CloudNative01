package Hotel.RabbitMQ.controller;

import Hotel.RabbitMQ.service.ListenerManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/listeners")
@RequiredArgsConstructor
public class ListenerControler {

    private final ListenerManagementService listenerService;

    @GetMapping
    public ResponseEntity<Object> getAllListeners() {
        return ResponseEntity.ok(listenerService.getAllListenerIds());
    }

    @PostMapping("/{listenerId}/pause")
    public ResponseEntity<String> pauseListener(@PathVariable String listenerId) {
        listenerService.pauseListener(listenerId);
        return ResponseEntity.ok("Listener pausado: " + listenerId);
    }

    @PostMapping("/{listenerId}/resume")
    public ResponseEntity<String> resumeListener(@PathVariable String listenerId) {
        listenerService.resumeListener(listenerId);
        return ResponseEntity.ok("Listener reanudado: " + listenerId);
    }
}
