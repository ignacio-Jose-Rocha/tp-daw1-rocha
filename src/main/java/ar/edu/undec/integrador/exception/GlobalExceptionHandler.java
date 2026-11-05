package ar.edu.undec.integrador.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static ResponseEntity<Object> problema(HttpStatus status, String titulo, String detalle,
                                                   Map<String, String> errores) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalle);
        pd.setTitle(titulo);
        if (errores != null && !errores.isEmpty()) {
            pd.setProperty("errores", errores);
        }
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(pd);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApi(ApiException ex) {
        HttpStatus s = ex.getStatus();
        String titulo = switch (s) {
            case NOT_FOUND -> "No encontrado";
            case CONFLICT -> "Conflicto";
            default -> "Datos inválidos";
        };
        return problema(s, titulo, ex.getMessage(), null);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return problema(HttpStatus.BAD_REQUEST, "Datos inválidos", "La petición tiene errores", errores);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "El cuerpo de la petición está vacío o tiene un formato inválido", null);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "El valor '" + ex.getValue() + "' no es válido para '" + ex.getPropertyName() + "'", null);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Falta el parámetro '" + ex.getParameterName() + "'", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleOtros(Exception ex) {
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado", null);
    }
}
