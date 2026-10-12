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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneId.systemDefault());
        this.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }
}
