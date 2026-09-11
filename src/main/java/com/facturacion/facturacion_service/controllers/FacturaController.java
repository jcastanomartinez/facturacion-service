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
    public ResponseEntity<?> getFacturaById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(facturaService.getFacturaById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createFactura(@RequestBody CrearFacturaRequest nuevaFactura) {
        try {
            Factura creada = facturaService.crearFactura(nuevaFactura);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            // ej. datos inválidos en el request -> error del cliente, no del servidor
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFactura(@PathVariable Long id) {
        try {
            facturaService.getFacturaById(id); // lanza si no existe -> 404
            facturaService.eliminarFactura(id); // la llamada que realmente borra
            return ResponseEntity.noContent().build(); // 204: borrado correcto, sin cuerpo
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFactura(@PathVariable Long id, @RequestBody ActualizarFacturaRequest facturaModificada) {
        try {
            Factura actualizada = facturaService.actualizarFactura(id, facturaModificada);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}