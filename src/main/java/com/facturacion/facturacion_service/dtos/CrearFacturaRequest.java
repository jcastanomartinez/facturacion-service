package com.facturacion.facturacion_service.dtos;

import lombok.Data;

@Data
public class CrearFacturaRequest {
    private String num_factura;
    private String cliente;
    private java.sql.Date fecha_factura;
    private Long importe;
    private String estado;

    // getters y setters (o @Data de Lombok, como ya usas en Factura)
}