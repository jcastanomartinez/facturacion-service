package com.facturacion.facturacion_service.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@NoArgsConstructor
@Data
@Entity
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (unique = true)
    private String num_factura;

    @Column
    private String cliente;

    @Column
    private Date fecha_factura;

    @Column
    private Long importe;

    @Column
    private String estado;

}
