package Hotel.brokeradmin.dto;

/** Info resumida de una cola, sin exponer tipos de la libreria de RabbitMQ. */
public record ColaInfo(String nombre, long mensajes, long consumidores) {
}
