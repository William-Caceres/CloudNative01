package Hotel.rabbitadmin.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacion(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validacion fallida en rabbit-admin [URI: {}]", request.getDescription(false));

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                e -> errors.put(e.getField(), e.getDefaultMessage()));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validacion Fallida")
                .code("VALID-001")
                .message("Uno o mas campos de la peticion no son validos")
                .validations(errors)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Parametro invalido en rabbit-admin: {} [URI: {}]", ex.getName(), request.getDescription(false));

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getName(), "Valor '" + ex.getValue() + "' con formato invalido");

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validacion Fallida")
                .code("VALID-002")
                .message("Uno o mas parametros de la peticion no cumplen con el formato esperado")
                .validations(errors)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MensajeriaException.class)
    public ResponseEntity<ApiError> handleMensajeria(MensajeriaException ex, WebRequest request) {
        log.warn("Error de mensajeria {} [URI: {}]: {}", ex.getCode(), request.getDescription(false), ex.getMessage());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .code(ex.getCode())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, ex.getStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Acceso denegado [URI: {}]", request.getDescription(false));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Acceso Denegado")
                .code("AUTH-003")
                .message("No tiene permisos para realizar esta operacion")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleRutaNoEncontrada(NoResourceFoundException ex, WebRequest request) {
        log.warn("Ruta no encontrada [URI: {}]", request.getDescription(false));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("No Encontrado")
                .code("SYS-002")
                .message("La ruta solicitada no existe")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMetodoNoPermitido(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.warn("Metodo no permitido [URI: {}]", request.getDescription(false));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Metodo No Permitido")
                .code("SYS-003")
                .message("El metodo " + ex.getMethod() + " no esta permitido en esta ruta")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, WebRequest request) {
        log.error("Error no controlado en rabbit-admin: {} [URI: {}]", ex.getMessage(), request.getDescription(false), ex);

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error Interno")
                .code("SYS-001")
                .message("Ocurrio un error inesperado en el sistema")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
