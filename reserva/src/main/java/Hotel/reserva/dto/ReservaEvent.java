package Hotel.reserva.dto;

public record ReservaEvent(
        String eventId,
        String eventType,
        String occurredAt,
        int version,
        Integer reservaId,
        Integer idUsuario,
        String fechaReserva,
        String fechaTermino,
        String tipoReserva,
        Integer cantidadPersonas,
        Integer valorFinal,
        String estadoPago,
        String referenciaPago) {
}
