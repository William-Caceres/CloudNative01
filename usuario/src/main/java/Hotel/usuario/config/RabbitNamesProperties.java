package Hotel.usuario.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Nombres de exchanges, colas y routing keys leídos desde application.yml (prefijo app.rabbit). */
@ConfigurationProperties(prefix = "app.rabbit")
public record RabbitNamesProperties(Exchanges exchanges, RoutingKeys routingKeys, Queues queues, int deliveryLimit) {

    public record Exchanges(String direct, String topic, String dlx) {}

    public record RoutingKeys(String ticketGenerar, String documentoGenerar) {}

    public record Queues(QueueDef notificaciones, QueueDef tickets, QueueDef documentos) {}

    public record QueueDef(String name, String dlq, String routingKey) {}
}
