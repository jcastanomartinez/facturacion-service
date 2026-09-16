package com.facturacion.facturacion_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceDocumentRequest(

        String invoiceNumber,

        LocalDate invoiceDate,

        Company company,

        Customer customer,

        List<InvoiceItem> items,

        BigDecimal subtotal,

        BigDecimal taxTotal,

        BigDecimal total
) {

    public record Company(
            String name,
            String taxId,
            String address,
            String email
    ) {
    }

    public record Customer(
            String name,
            String taxId,
            String address
    ) {
    }

    public record InvoiceItem(
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal tax,
            BigDecimal total
    ) {
    }
}