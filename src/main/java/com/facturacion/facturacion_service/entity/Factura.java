package com.facturacion.facturacion_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "num_factura", unique = true, nullable = false)
    private String invoiceNumber;

    @Column(name = "fecha_factura", nullable = false)
    private LocalDate invoiceDate;

    // =========================
    // EMPRESA
    // =========================

    @Column(name = "empresa_nombre")
    private String companyName;

    @Column(name = "empresa_cif")
    private String companyTaxId;

    @Column(name = "empresa_direccion")
    private String companyAddress;

    @Column(name = "empresa_email")
    private String companyEmail;

    // =========================
    // CLIENTE
    // =========================

    @Column(name = "cliente_nombre")
    private String customerName;

    @Column(name = "cliente_cif")
    private String customerTaxId;

    @Column(name = "cliente_direccion")
    private String customerAddress;

    // =========================
    // TOTALES
    // =========================

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "iva_total", precision = 12, scale = 2)
    private BigDecimal taxTotal;

    @Column(name = "total", precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "estado")
    private String estado;

    // =========================
    // LÍNEAS
    // =========================

    @OneToMany(
            mappedBy = "factura",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<FacturaItem> items = new ArrayList<>();

    // =========================
    // MÉTODOS DE AYUDA
    // =========================

    public void addItem(FacturaItem item) {
        items.add(item);
        item.setFactura(this);
    }

    public void removeItem(FacturaItem item) {
        items.remove(item);
        item.setFactura(null);
    }
}