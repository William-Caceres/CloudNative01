package Hotel.brokeradmin.idempotencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "evento_procesado", uniqueConstraints = @UniqueConstraint(
        name = "uk_evento_procesado_consumidor_evento", columnNames = {"consumer_key", "event_key"}))
public class EventoProcesado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_key", nullable = false, length = 120)
    private String consumerKey;

    @Column(name = "event_key", nullable = false, length = 160)
    private String eventKey;

    @Column(name = "procesado_en", nullable = false)
    private Instant procesadoEn;

    protected EventoProcesado() {
    }

    public EventoProcesado(String consumerKey, String eventKey, Instant procesadoEn) {
        this.consumerKey = consumerKey;
        this.eventKey = eventKey;
        this.procesadoEn = procesadoEn;
    }

    public Long getId() {
        return id;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getEventKey() {
        return eventKey;
    }

    public Instant getProcesadoEn() {
        return procesadoEn;
    }
}
