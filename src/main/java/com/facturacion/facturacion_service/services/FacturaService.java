package com.facturacion.facturacion_service.services;

import com.facturacion.facturacion_service.client.DocumentServiceClient;
import com.facturacion.facturacion_service.dtos.ActualizarFacturaRequest;
import com.facturacion.facturacion_service.dtos.CrearFacturaRequest;
import com.facturacion.facturacion_service.dtos.FacturaItemRequest;
import com.facturacion.facturacion_service.dtos.InvoiceDocumentRequest;
import com.facturacion.facturacion_service.entity.Factura;
import com.facturacion.facturacion_service.entity.FacturaItem;
import com.facturacion.facturacion_service.repository.FacturaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class FacturaService {

    private static final int MONEY_SCALE = 2;

    private final FacturaRepository facturaRepository;
    private final DocumentServiceClient documentServiceClient;

    private final String defaultCompanyName;
    private final String defaultCompanyTaxId;
    private final String defaultCompanyAddress;
    private final String defaultCompanyEmail;

    public FacturaService(
            FacturaRepository facturaRepository,
            DocumentServiceClient documentServiceClient,
            @Value("${invoice.company.name:Mi Empresa}") String defaultCompanyName,
            @Value("${invoice.company.tax-id:B00000000}") String defaultCompanyTaxId,
            @Value("${invoice.company.address:Calle Principal 1, Madrid}") String defaultCompanyAddress,
            @Value("${invoice.company.email:facturacion@example.com}") String defaultCompanyEmail) {
        this.facturaRepository = facturaRepository;
        this.documentServiceClient = documentServiceClient;
        this.defaultCompanyName = defaultCompanyName;
        this.defaultCompanyTaxId = defaultCompanyTaxId;
        this.defaultCompanyAddress = defaultCompanyAddress;
        this.defaultCompanyEmail = defaultCompanyEmail;
    }

    public List<Factura> getAllFacturas() {
        return facturaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Factura getFacturaById(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(Long id) {
        Factura factura = getFacturaById(id);

        InvoiceDocumentRequest.Company company = new InvoiceDocumentRequest.Company(
                valueOrDefault(factura.getCompanyName(), defaultCompanyName),
                valueOrDefault(factura.getCompanyTaxId(), defaultCompanyTaxId),
                valueOrDefault(factura.getCompanyAddress(), defaultCompanyAddress),
                valueOrDefault(factura.getCompanyEmail(), defaultCompanyEmail)
        );

        InvoiceDocumentRequest.Customer customer = new InvoiceDocumentRequest.Customer(
                factura.getCustomerName(),
                factura.getCustomerTaxId(),
                factura.getCustomerAddress()
        );

        List<InvoiceDocumentRequest.InvoiceItem> items = factura.getItems()
                .stream()
                .map(item -> new InvoiceDocumentRequest.InvoiceItem(
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTax(),
                        item.getTotal()
                ))
                .toList();

        InvoiceDocumentRequest request = new InvoiceDocumentRequest(
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

    @Transactional
    public Factura crearFactura(CrearFacturaRequest request) {
        Factura factura = new Factura();

        factura.setInvoiceNumber(request.getInvoiceNumber());
        factura.setInvoiceDate(request.getInvoiceDate());

        factura.setCompanyName(valueOrDefault(request.getCompanyName(), defaultCompanyName));
        factura.setCompanyTaxId(valueOrDefault(request.getCompanyTaxId(), defaultCompanyTaxId));
        factura.setCompanyAddress(valueOrDefault(request.getCompanyAddress(), defaultCompanyAddress));
        factura.setCompanyEmail(valueOrDefault(request.getCompanyEmail(), defaultCompanyEmail));

        factura.setCustomerName(request.getCustomerName());
        factura.setCustomerTaxId(request.getCustomerTaxId());
        factura.setCustomerAddress(request.getCustomerAddress());
        factura.setEstado(request.getEstado());

        replaceItemsAndRecalculate(factura, request.getItems());

        return facturaRepository.save(factura);
    }

    @Transactional
    public Factura actualizarFactura(Long id, ActualizarFacturaRequest request) {
        Factura factura = getFacturaById(id);

        if (request.getInvoiceDate() != null) {
            factura.setInvoiceDate(request.getInvoiceDate());
        }

        if (request.getCompanyName() != null) factura.setCompanyName(request.getCompanyName());
        if (request.getCompanyTaxId() != null) factura.setCompanyTaxId(request.getCompanyTaxId());
        if (request.getCompanyAddress() != null) factura.setCompanyAddress(request.getCompanyAddress());
        if (request.getCompanyEmail() != null) factura.setCompanyEmail(request.getCompanyEmail());

        if (request.getCustomerName() != null) factura.setCustomerName(request.getCustomerName());
        if (request.getCustomerTaxId() != null) factura.setCustomerTaxId(request.getCustomerTaxId());
        if (request.getCustomerAddress() != null) factura.setCustomerAddress(request.getCustomerAddress());
        if (request.getEstado() != null) factura.setEstado(request.getEstado());

        // Si vienen líneas, se reemplazan y los totales se recalculan en servidor.
        // Los subtotal/taxTotal/total enviados por el cliente se ignoran.
        if (request.getItems() != null) {
            replaceItemsAndRecalculate(factura, request.getItems());
        } else {
            ensureCompanyDefaults(factura);
        }

        return facturaRepository.save(factura);
    }

    public void eliminarFactura(Long id) {
        if (!facturaRepository.existsById(id)) {
            throw new IllegalArgumentException("Factura no encontrada");
        }
        facturaRepository.deleteById(id);
    }

    private void replaceItemsAndRecalculate(Factura factura, List<FacturaItemRequest> requests) {
        factura.getItems().clear();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;

        if (requests != null) {
            for (FacturaItemRequest request : requests) {
                FacturaItem item = new FacturaItem();
                item.setDescription(request.getDescription());
                item.setQuantity(requireNonNegative(request.getQuantity(), "quantity"));
                item.setUnitPrice(requireNonNegative(request.getUnitPrice(), "unitPrice"));
                item.setTax(requireNonNegative(request.getTax(), "tax"));

                BigDecimal lineSubtotal = money(item.getQuantity().multiply(item.getUnitPrice()));
                BigDecimal lineTax = money(lineSubtotal.multiply(item.getTax())
                        .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
                BigDecimal lineTotal = money(lineSubtotal.add(lineTax));

                item.setTotal(lineTotal);
                factura.addItem(item);

                subtotal = subtotal.add(lineSubtotal);
                taxTotal = taxTotal.add(lineTax);
            }
        }

        factura.setSubtotal(money(subtotal));
        factura.setTaxTotal(money(taxTotal));
        factura.setTotal(money(subtotal.add(taxTotal)));
        ensureCompanyDefaults(factura);
    }

    private void ensureCompanyDefaults(Factura factura) {
        factura.setCompanyName(valueOrDefault(factura.getCompanyName(), defaultCompanyName));
        factura.setCompanyTaxId(valueOrDefault(factura.getCompanyTaxId(), defaultCompanyTaxId));
        factura.setCompanyAddress(valueOrDefault(factura.getCompanyAddress(), defaultCompanyAddress));
        factura.setCompanyEmail(valueOrDefault(factura.getCompanyEmail(), defaultCompanyEmail));
    }

    private BigDecimal requireNonNegative(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(field + " debe ser mayor o igual que 0");
        }
        return value;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
