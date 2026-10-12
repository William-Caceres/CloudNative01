package Hotel.reserva.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDACION_FALLIDA = "Validacion Fallida";
    private static final String VALID_002 = "VALID-002";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Fallo de validacion en reserva: {} [URI: {}]", errors, request.getDescription(false));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now(ZoneId.systemDefault()))
                .status(HttpStatus.BAD_REQUEST.value())
                .error(VALIDACION_FALLIDA)
                .code("VALID-001")
                .message("Uno o más campos de la reserva no cumplen con los requisitos")
                .validations(errors)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonIlegible(HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("JSON ilegible: {} [URI: {}]", ex.getMessage(), request.getDescription(false));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now(ZoneId.systemDefault()))
                .status(HttpStatus.BAD_REQUEST.value())
                .error(VALIDACION_FALLIDA)
                .code(VALID_002)
                .message("El cuerpo de la petición no es un JSON válido")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Parametro invalido: {} [URI: {}]", ex.getName(), request.getDescription(false));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now(ZoneId.systemDefault()))
                .status(HttpStatus.BAD_REQUEST.value())
                .error(VALIDACION_FALLIDA)
                .code(VALID_002)
                .message("Valor '" + ex.getValue() + "' con formato inválido")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNoSoportado(HttpMediaTypeNotSupportedException ex, WebRequest request) {
        log.warn("Tipo de contenido no soportado: {} [URI: {}]", ex.getContentType(), request.getDescription(false));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now(ZoneId.systemDefault()))
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .error(VALIDACION_FALLIDA)
                .code(VALID_002)
                .message("Tipo de contenido no soportado (usa application/json)")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(EntidadNoEncontradaException.class)
    public ResponseEntity<ApiError> handleNotFound(EntidadNoEncontradaException ex, WebRequest request) {
        log.error("Recurso no encontrado [{}]: {} [URI: {}]", ex.getCode(), ex.getMessage(), request.getDescription(false));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now(ZoneId.systemDefault()))
                .status(HttpStatus.NOT_FOUND.value())
                .error("No Encontrado")
                .code(ex.getCode())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex, WebRequest request) {
        log.warn("Regla de negocio [{}]: {} [URI: {}]", ex.getCode(), ex.getMessage(), request.getDescription(false));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now(ZoneId.systemDefault()))
                .status(ex.getHttpStatus().value())
                .error("Regla de Negocio")
                .code(ex.getCode())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, ex.getHttpStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Acceso denegado [URI: {}]", request.getDescription(false));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now(ZoneId.systemDefault()))
                .status(HttpStatus.FORBIDDEN.value())
                .error("Acceso Denegado")
                .code("AUTH-003")
                .message("No tiene permisos para realizar esta operación")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleRutaNoEncontrada(NoResourceFoundException ex, WebRequest request) {
        log.warn("Ruta no encontrada [URI: {}]", request.getDescription(false));

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now(ZoneId.systemDefault()))
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
                .timestamp(LocalDateTime.now(ZoneId.systemDefault()))
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Metodo No Permitido")
                .code("SYS-003")
                .message("El método " + ex.getMethod() + " no está permitido en esta ruta")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, WebRequest request) {
        log.error("Error no controlado en reserva: {} [URI: {}]", ex.getMessage(), request.getDescription(false), ex);

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now(ZoneId.systemDefault()))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error Interno")
                .code("SYS-001")
                .message("Ocurrió un error inesperado en el sistema")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
