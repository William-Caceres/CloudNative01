package Hotel.reserva.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_event", uniqueConstraints = @UniqueConstraint(columnNames = {"reserva_id", "tipo"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    public static final String RESERVA_CREADA = "RESERVA_CREADA";
    public static final String PAGO_CONFIRMADO = "PAGO_CONFIRMADO";
    public static final String PENDIENTE = "PENDIENTE";
    public static final String ENVIADO = "ENVIADO";
    public static final String FALLIDO = "FALLIDO";
    public static final String AGOTADO = "AGOTADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reserva_id", nullable = false)
    private Integer reservaId;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private int intentos;

    @Column(name = "proximo_intento", nullable = false)
    private LocalDateTime proximoIntento;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "enviado_en")
    private LocalDateTime enviadoEn;
}
