package com.facturacion.facturacion_service.services;

import com.facturacion.facturacion_service.dtos.ActualizarFacturaRequest;
import com.facturacion.facturacion_service.dtos.CrearFacturaRequest;
import com.facturacion.facturacion_service.modelo.Factura;
import com.facturacion.facturacion_service.repository.FacturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacturaService {
    private final FacturaRepository facturaRepository;

    public FacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    public List<Factura> getAllFacturas() {
        return facturaRepository.findAll();
    }

    public Factura getFacturaById(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
    }

    public Factura crearFactura(CrearFacturaRequest request) {
        Factura factura = new Factura();

        factura.setNum_factura(request.getNum_factura());
        factura.setCliente(request.getCliente());
        factura.setFecha_factura(request.getFecha_factura());
        factura.setImporte(request.getImporte());
        factura.setEstado(request.getEstado());
        return facturaRepository.save(factura);
    }

    public Factura actualizarFactura(Long id, ActualizarFacturaRequest request) {
        Factura factura = getFacturaById(id);

        if (request.getCliente() != null) {
            factura.setCliente(request.getCliente());
        }
        if (request.getFecha_factura() != null) {
            factura.setFecha_factura(request.getFecha_factura());
        }
        if (request.getImporte() != null) {
            factura.setImporte(request.getImporte());
        }
        if (request.getEstado() != null) {
            factura.setEstado(request.getEstado());
        }

        return facturaRepository.save(factura);
    }

    public void eliminarFactura(Long id) {
        if (!facturaRepository.existsById(id)) {
            throw new IllegalArgumentException("Factura no encontrada");
        }
        facturaRepository.deleteById(id);
    }
}
