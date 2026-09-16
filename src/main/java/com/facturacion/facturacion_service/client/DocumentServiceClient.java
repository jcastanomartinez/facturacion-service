package com.facturacion.facturacion_service.client;

import com.facturacion.facturacion_service.dtos.InvoiceDocumentRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DocumentServiceClient {

    private final RestClient restClient;

    public DocumentServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public byte[] generateInvoice(InvoiceDocumentRequest request) {

        return restClient
                .post()
                .uri("/documents/invoice")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PDF)
                .body(request)
                .retrieve()
                .body(byte[].class);
    }
}