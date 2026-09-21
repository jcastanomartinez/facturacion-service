package com.facturacion.facturacion_service.controllers;

import com.facturacion.facturacion_service.exceptions.DocumentServiceBadResponseException;
import com.facturacion.facturacion_service.exceptions.DocumentServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentServiceUnavailable(
            DocumentServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 503,
                "error", "Service Unavailable",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(DocumentServiceBadResponseException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentServiceBadResponse(
            DocumentServiceBadResponseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 502,
                "error", "Bad Gateway",
                "message", ex.getMessage()
        ));
    }
}
