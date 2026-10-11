package Hotel.servicio.entity;

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
@Table(name="servicio")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    @Column(nullable = false, length = 50)
    private String nombre;
    @Size(max = 150, message = "La descripción no puede superar los 150 caracteres")
    @Column(length = 150)
    private String descripcion;
    @NotBlank(message = "El tipo de servicio es obligatorio")
    @Size(max = 50, message = "El tipo de servicio no puede superar los 50 caracteres")
    @Column(nullable = false, length = 50)
    private String tipoServicio;
    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio no puede ser negativo")
    @Column(nullable = false)
    private Integer precio;
    @NotNull(message = "El número de habitación es obligatorio")
    @Column(nullable = false)
    private Integer numHabitacion;
    @Positive(message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;
    @NotNull(message = "El campo disponible es obligatorio")
    @Column(nullable = false)
    private Boolean disponible;
    @Size(max = 20, message = "El nivel de servicio no puede superar los 20 caracteres")
    @Column(length = 20)
    private String nivelServicio;

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
