package com.facturacion.facturacion_service.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FacturaItemRequest {

    private String description;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal tax;

    private BigDecimal total;
}