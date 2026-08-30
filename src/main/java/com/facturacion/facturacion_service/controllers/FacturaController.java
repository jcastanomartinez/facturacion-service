package com.facturacion.facturacion_service.controllers;

import com.facturacion.facturacion_service.dtos.ActualizarFacturaRequest;
import com.facturacion.facturacion_service.dtos.CrearFacturaRequest;
import com.facturacion.facturacion_service.modelo.Factura;
import com.facturacion.facturacion_service.services.FacturaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }


    @GetMapping
    public List<Factura> getAllFacturas() {
        return this.facturaService.getAllFacturas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> getFacturaById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(facturaService.getFacturaById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body((Factura) Map.of("error", e.getMessage()));
        }
    }
    @PostMapping
    public Factura createFactura(CrearFacturaRequest nuevaFactura){
        return facturaService.crearFactura(nuevaFactura);

    }
    @DeleteMapping
    public void deleteFactura(Long id) {
        facturaService.eliminarFactura(id);
    }
    @PatchMapping
    public Factura updateFactura(Long id,ActualizarFacturaRequest facturaModificada) {
        return facturaService.actualizarFactura(id,facturaModificada);
    }
}
