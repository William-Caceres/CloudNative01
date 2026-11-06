package Hotel.reserva.dto;

public record PagoEvent(
        String eventId,
        String eventType,
        String occurredAt,
        int version,
        String pagoId,
        Integer reservaId,
        Integer idUsuario,
        Integer monto,
        String metodoPago,
        String estadoPago) {
}
