package com.facturacion.facturacion_service.dtos;

import lombok.Data;

@Data
    public class ActualizarFacturaRequest {
        private String cliente;      // todos opcionales — el cliente manda solo lo que quiere cambiar
        private java.sql.Date fecha_factura;
        private Long importe;
        private String estado;
        // ¿incluirías num_factura aquí? piensa si tiene sentido "editar" el número de factura una vez creada
    }

