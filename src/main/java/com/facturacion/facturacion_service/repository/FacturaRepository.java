package com.facturacion.facturacion_service.repository;

import com.facturacion.facturacion_service.modelo.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
}
