package Hotel.reserva.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "idempotency_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyRecord {

    @Id
    @Column(name = "idempotency_key", length = 100, nullable = false)
    private String key;

    @Column(name = "request_hash", length = 64, nullable = false)
    private String requestHash;

    @Column(name = "reserva_id", nullable = false)
    private Integer reservaId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
