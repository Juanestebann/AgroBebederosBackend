package com.example.agrobebederos_backend.exception;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.agrobebederos_backend.dto.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            RecursoNoEncontradoException exception,
            ServletWebRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            AccesoDenegadoException exception,
            ServletWebRequest request) {
        return build(HttpStatus.FORBIDDEN, exception.getMessage(), request, null);
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            SolicitudInvalidaException exception,
            ServletWebRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request, null);
    }

    @ExceptionHandler({
        ConflictoException.class,
        DataIntegrityViolationException.class,
        ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflict(Exception exception, ServletWebRequest request) {
        return build(HttpStatus.CONFLICT, "La operación entra en conflicto con el estado actual.", request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            ServletWebRequest request) {
        Map<String, String> errors = new LinkedHashMap<String, String>();
        for (int i = 0; i < exception.getBindingResult().getFieldErrors().size(); i++) {
            String field = exception.getBindingResult().getFieldErrors().get(i).getField();
            String message = exception.getBindingResult().getFieldErrors().get(i).getDefaultMessage();
            if (!errors.containsKey(field)) {
                errors.put(field, message);
            }
        }
        return build(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud.", request, errors);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleFileSize(
            MaxUploadSizeExceededException exception,
            ServletWebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "La fotografía supera el tamaño máximo permitido.", request, null);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class,
        MissingServletRequestPartException.class
    })
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            Exception exception,
            ServletWebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "La solicitud no tiene un formato válido.", request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownRoute(
            NoResourceFoundException exception,
            ServletWebRequest request) {
        return build(HttpStatus.NOT_FOUND, "La ruta solicitada no existe.", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, ServletWebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado.", request, null);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            ServletWebRequest request,
            Map<String, String> fieldErrors) {
        ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequest().getRequestURI(),
                fieldErrors);
        return ResponseEntity.status(status).body(response);
    }
}
