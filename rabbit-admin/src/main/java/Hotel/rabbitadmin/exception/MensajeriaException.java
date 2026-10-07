package Hotel.rabbitadmin.exception;

import org.springframework.http.HttpStatus;

/** Error de operacion de mensajeria con codigo y estado HTTP propios. */
public class MensajeriaException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public MensajeriaException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
