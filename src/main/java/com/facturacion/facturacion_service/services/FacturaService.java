package com.facturacion.facturacion_service.services;

import com.facturacion.facturacion_service.client.DocumentServiceClient;
import com.facturacion.facturacion_service.dtos.ActualizarFacturaRequest;
import com.facturacion.facturacion_service.dtos.CrearFacturaRequest;
import com.facturacion.facturacion_service.dtos.FacturaItemRequest;
import com.facturacion.facturacion_service.dtos.InvoiceDocumentRequest;
import com.facturacion.facturacion_service.entity.Factura;
import com.facturacion.facturacion_service.entity.FacturaItem;
import com.facturacion.facturacion_service.repository.FacturaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final DocumentServiceClient documentServiceClient;

    public FacturaService(FacturaRepository facturaRepository, DocumentServiceClient documentServiceClient) {
        this.facturaRepository = facturaRepository;
        this.documentServiceClient = documentServiceClient;
    }

    public List<Factura> getAllFacturas() {
        return facturaRepository.findAll();
    }

    public Factura getFacturaById(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Factura no encontrada"));
    }

    public byte[] generarPdf(Long id) {

        Factura factura = getFacturaById(id);

        InvoiceDocumentRequest.Company company =
                new InvoiceDocumentRequest.Company(
                        factura.getCompanyName(),
                        factura.getCompanyTaxId(),
                        factura.getCompanyAddress(),
                        factura.getCompanyEmail()
                );

        InvoiceDocumentRequest.Customer customer =
                new InvoiceDocumentRequest.Customer(
                        factura.getCustomerName(),
                        factura.getCustomerTaxId(),
                        factura.getCustomerAddress()
                );

        List<InvoiceDocumentRequest.InvoiceItem> items =
                factura.getItems()
                        .stream()
                        .map(item ->
                                new InvoiceDocumentRequest.InvoiceItem(
                                        item.getDescription(),
                                        item.getQuantity(),
                                        item.getUnitPrice(),
                                        item.getTax(),
                                        item.getTotal()
                                )
                        )
                        .toList();

        InvoiceDocumentRequest request =
                new InvoiceDocumentRequest(
                        factura.getInvoiceNumber(),
                        factura.getInvoiceDate(),
                        company,
                        customer,
                        items,
                        factura.getSubtotal(),
                        factura.getTaxTotal(),
                        factura.getTotal()
                );

        return documentServiceClient.generateInvoice(request);
    }

    public Factura crearFactura(CrearFacturaRequest request) {

        Factura factura = new Factura();

        // =========================
        // DATOS GENERALES
        // =========================

        factura.setInvoiceNumber(request.getInvoiceNumber());
        factura.setInvoiceDate(request.getInvoiceDate());

        // =========================
        // EMPRESA
        // =========================

        factura.setCompanyName(request.getCompanyName());
        factura.setCompanyTaxId(request.getCompanyTaxId());
        factura.setCompanyAddress(request.getCompanyAddress());
        factura.setCompanyEmail(request.getCompanyEmail());

        // =========================
        // CLIENTE
        // =========================

        factura.setCustomerName(request.getCustomerName());
        factura.setCustomerTaxId(request.getCustomerTaxId());
        factura.setCustomerAddress(request.getCustomerAddress());

        // =========================
        // TOTALES
        // =========================

        factura.setSubtotal(request.getSubtotal());
        factura.setTaxTotal(request.getTaxTotal());
        factura.setTotal(request.getTotal());
        factura.setEstado(request.getEstado());

        // =========================
        // LÍNEAS
        // =========================

        if (request.getItems() != null) {

            for (FacturaItemRequest itemRequest : request.getItems()) {

                FacturaItem item = new FacturaItem();

                item.setDescription(itemRequest.getDescription());
                item.setQuantity(itemRequest.getQuantity());
                item.setUnitPrice(itemRequest.getUnitPrice());
                item.setTax(itemRequest.getTax());
                item.setTotal(itemRequest.getTotal());

                factura.addItem(item);
            }
        }

        return facturaRepository.save(factura);
    }

    public Factura actualizarFactura(
            Long id,
            ActualizarFacturaRequest request) {

        Factura factura = getFacturaById(id);

        // =========================
        // DATOS GENERALES
        // =========================

        if (request.getInvoiceDate() != null) {
            factura.setInvoiceDate(request.getInvoiceDate());
        }

        // =========================
        // EMPRESA
        // =========================

        if (request.getCompanyName() != null) {
            factura.setCompanyName(request.getCompanyName());
        }

        if (request.getCompanyTaxId() != null) {
            factura.setCompanyTaxId(request.getCompanyTaxId());
        }

        if (request.getCompanyAddress() != null) {
            factura.setCompanyAddress(request.getCompanyAddress());
        }

        if (request.getCompanyEmail() != null) {
            factura.setCompanyEmail(request.getCompanyEmail());
        }

        // =========================
        // CLIENTE
        // =========================

        if (request.getCustomerName() != null) {
            factura.setCustomerName(request.getCustomerName());
        }

        if (request.getCustomerTaxId() != null) {
            factura.setCustomerTaxId(request.getCustomerTaxId());
        }

        if (request.getCustomerAddress() != null) {
            factura.setCustomerAddress(request.getCustomerAddress());
        }

        // =========================
        // TOTALES
        // =========================

        if (request.getSubtotal() != null) {
            factura.setSubtotal(request.getSubtotal());
        }

        if (request.getTaxTotal() != null) {
            factura.setTaxTotal(request.getTaxTotal());
        }

        if (request.getTotal() != null) {
            factura.setTotal(request.getTotal());
        }

        if (request.getEstado() != null) {
            factura.setEstado(request.getEstado());
        }

        // =========================
        // LÍNEAS
        // =========================

        if (request.getItems() != null) {

            factura.getItems().clear();

            for (FacturaItemRequest itemRequest : request.getItems()) {

                FacturaItem item = new FacturaItem();

                item.setDescription(itemRequest.getDescription());
                item.setQuantity(itemRequest.getQuantity());
                item.setUnitPrice(itemRequest.getUnitPrice());
                item.setTax(itemRequest.getTax());
                item.setTotal(itemRequest.getTotal());

                factura.addItem(item);
            }
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