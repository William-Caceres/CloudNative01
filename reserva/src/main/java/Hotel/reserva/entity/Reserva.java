package Hotel.reserva.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="reserva")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reserva {

    public static final String ESTADO_PAGO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_PAGO_CONFIRMADO = "CONFIRMADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El idUsuario es obligatorio")
    @Positive(message = "El idUsuario debe ser mayor a 0")
    @Column(nullable = false)
    private Integer idUsuario;
    @NotBlank(message = "La fecha de reserva es obligatoria")
    @Size(max = 40, message = "La fecha de reserva no puede superar los 40 caracteres")
    @Column(nullable = false)
    private String fechaReserva;
    @NotBlank(message = "La fecha de término es obligatoria")
    @Size(max = 40, message = "La fecha de término no puede superar los 40 caracteres")
    @Column(nullable = false)
    private String fechaTermino;
    @NotBlank(message = "El tipo de reserva es obligatorio")
    @Size(max = 50, message = "El tipo de reserva no puede superar los 50 caracteres")
    @Column(nullable = false, length = 50)
    private String tipoReserva;
    @NotNull(message = "La cantidad de personas es obligatoria")
    @Positive(message = "La cantidad de personas debe ser mayor a 0")
    @Column(nullable = false)
    private Integer cantidadPersonas;
    @NotNull(message = "El valor final es obligatorio")
    @PositiveOrZero(message = "El valor final no puede ser negativo")
    @Column(nullable = false)
    private Integer valorFinal;

    @Column(name = "estado_pago", length = 20)
    private String estadoPago = ESTADO_PAGO_PENDIENTE;
    @Size(max = 30, message = "El metodo de pago no puede superar los 30 caracteres")
    @Column(name = "metodo_pago", length = 30)
    private String metodoPago;
    @Size(max = 36, message = "La referencia de pago no puede superar los 36 caracteres")
    @Column(name = "referencia_pago", length = 36)
    private String referenciaPago;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.estadoPago == null || this.estadoPago.isBlank()) {
            this.estadoPago = ESTADO_PAGO_PENDIENTE;
        }
        this.createdAt = LocalDateTime.now(ZoneId.systemDefault());
        this.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }
}
