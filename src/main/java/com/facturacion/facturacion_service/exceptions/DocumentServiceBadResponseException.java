package com.facturacion.facturacion_service.exceptions;

public class DocumentServiceBadResponseException extends RuntimeException {

    public DocumentServiceBadResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
