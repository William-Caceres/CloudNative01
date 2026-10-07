package Hotel.usuario.messaging.event;

/** Evento que publica el micro reserva cuando se crea una reserva. Cada micro tiene su copia (contrato JSON). */
public record ReservaEvento(
        String tipoEvento,
        Integer idReserva,
        Integer idUsuario,
        String tipoReserva,
        String fechaReserva,
        String fechaTermino,
        Integer cantidadPersonas,
        Integer valorFinal) {
}
