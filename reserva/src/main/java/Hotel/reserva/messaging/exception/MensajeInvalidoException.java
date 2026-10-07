package Hotel.reserva.messaging.exception;

/** Error NO recuperable: reintentar no sirve (mensaje mal formado). Va directo a la DLQ. */
public class MensajeInvalidoException extends RuntimeException {
    public MensajeInvalidoException(String mensaje) {
        super(mensaje);
    }
}
