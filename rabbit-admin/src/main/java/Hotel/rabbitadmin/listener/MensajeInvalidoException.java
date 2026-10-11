package Hotel.rabbitadmin.listener;

/**
 * Evento NO recuperable (datos mal formados): el consumidor lo rechaza sin
 * reencolar y cae a la DLQ.
 */
public class MensajeInvalidoException extends RuntimeException {

    public MensajeInvalidoException(String mensaje) {
        super(mensaje);
    }
}
