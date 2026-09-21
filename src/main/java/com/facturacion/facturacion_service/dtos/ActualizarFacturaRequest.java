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

    /** Calculados por servidor; el valor recibido del cliente se ignora. */
    private BigDecimal subtotal;
    /** Calculado por servidor; el valor recibido del cliente se ignora. */
    private BigDecimal taxTotal;
    /** Calculado por servidor; el valor recibido del cliente se ignora. */
    private BigDecimal total;

    private String estado;

    // =========================
    // LÍNEAS
    // =========================

    private List<FacturaItemRequest> items;
}