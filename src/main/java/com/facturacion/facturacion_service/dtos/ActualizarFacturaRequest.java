package com.facturacion.facturacion_service.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ActualizarFacturaRequest {

    private LocalDate invoiceDate;

    // =========================
    // EMPRESA
    // =========================

    private String companyName;
    private String companyTaxId;
    private String companyAddress;
    private String companyEmail;

    // =========================
    // CLIENTE
    // =========================

    private String customerName;
    private String customerTaxId;
    private String customerAddress;

    // =========================
    // TOTALES
    // =========================

    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal total;

    private String estado;

    // =========================
    // LÍNEAS
    // =========================

    private List<FacturaItemRequest> items;
}