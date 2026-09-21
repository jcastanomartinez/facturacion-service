package com.facturacion.facturacion_service.client;

import com.facturacion.facturacion_service.dtos.InvoiceDocumentRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import com.facturacion.facturacion_service.exceptions.DocumentServiceBadResponseException;
import com.facturacion.facturacion_service.exceptions.DocumentServiceUnavailableException;

@Component
public class DocumentServiceClient {

    private final RestClient restClient;

    public DocumentServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public byte[] generateInvoice(InvoiceDocumentRequest request) {
        try {
            return restClient
                    .post()
                    .uri("/documents/invoice")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_PDF)
                    .body(request)
                    .retrieve()
                    .body(byte[].class);
        } catch (ResourceAccessException ex) {
            throw new DocumentServiceUnavailableException(
                    "No se ha podido conectar con document-service", ex);
        } catch (RestClientResponseException ex) {
            throw new DocumentServiceBadResponseException(
                    "document-service ha respondido con HTTP " + ex.getStatusCode().value(), ex);
        }
    }
}
