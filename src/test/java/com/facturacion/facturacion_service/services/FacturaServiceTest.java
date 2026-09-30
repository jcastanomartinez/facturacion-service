package com.facturacion.facturacion_service.services;

import com.facturacion.facturacion_service.client.DocumentServiceClient;
import com.facturacion.facturacion_service.dtos.ActualizarFacturaRequest;
import com.facturacion.facturacion_service.dtos.CrearFacturaRequest;
import com.facturacion.facturacion_service.dtos.FacturaItemRequest;
import com.facturacion.facturacion_service.entity.Factura;
import com.facturacion.facturacion_service.repository.FacturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitario puro: sin contexto de Spring, sin base de datos.
 * Se prueba la lógica de negocio de FacturaService de forma plana e infalible para Maven.
 */
@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private DocumentServiceClient documentServiceClient;

    private FacturaService facturaService;

    @BeforeEach
    void setUp() {
        facturaService = new FacturaService(
                facturaRepository,
                documentServiceClient,
                "Mi Empresa",
                "B00000000",
                "Calle Principal 1, Madrid",
                "facturacion@example.com"
        );
    }

    // =====================================================================
    // PRUEBAS DE: crearFactura
    // =====================================================================

    @Test
    @DisplayName("crearFactura - calcula subtotal, IVA y total a partir de las líneas")
    void calculaTotalesCorrectamente() {
        CrearFacturaRequest request = new CrearFacturaRequest();
        request.setInvoiceNumber("F-2026-001");
        request.setInvoiceDate(LocalDate.of(2026, 9, 29));
        request.setCustomerName("Cliente SL");

        FacturaItemRequest item1 = new FacturaItemRequest();
        item1.setDescription("Consultoría");
        item1.setQuantity(new BigDecimal("10"));
        item1.setUnitPrice(new BigDecimal("50.00"));
        item1.setTax(new BigDecimal("21"));

        FacturaItemRequest item2 = new FacturaItemRequest();
        item2.setDescription("Licencia");
        item2.setQuantity(new BigDecimal("2"));
        item2.setUnitPrice(new BigDecimal("100.00"));
        item2.setTax(new BigDecimal("21"));

        request.setItems(List.of(item1, item2));

        when(facturaRepository.save(any(Factura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Factura resultado = facturaService.crearFactura(request);

        assertThat(resultado.getSubtotal()).isEqualByComparingTo("700.00");
        assertThat(resultado.getTaxTotal()).isEqualByComparingTo("147.00");
        assertThat(resultado.getTotal()).isEqualByComparingTo("847.00");
        assertThat(resultado.getItems()).hasSize(2);
        assertThat(resultado.getItems().get(0).getFactura()).isSameAs(resultado);
    }

    @Test
    @DisplayName("crearFactura - aplica los datos de empresa por defecto cuando no se informan")
    void aplicaDefaultsDeEmpresa() {
        CrearFacturaRequest request = new CrearFacturaRequest();
        request.setInvoiceNumber("F-2026-002");
        request.setInvoiceDate(LocalDate.now());
        request.setItems(List.of());

        when(facturaRepository.save(any(Factura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Factura resultado = facturaService.crearFactura(request);

        assertThat(resultado.getCompanyName()).isEqualTo("Mi Empresa");
        assertThat(resultado.getCompanyTaxId()).isEqualTo("B00000000");
        assertThat(resultado.getCompanyEmail()).isEqualTo("facturacion@example.com");
    }

    @Test
    @DisplayName("crearFactura - respeta los datos de empresa cuando sí se informan")
    void respetaDatosDeEmpresaInformados() {
        CrearFacturaRequest request = new CrearFacturaRequest();
        request.setInvoiceNumber("F-2026-003");
        request.setInvoiceDate(LocalDate.now());
        request.setCompanyName("Otra Empresa SL");
        request.setItems(List.of());

        when(facturaRepository.save(any(Factura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Factura resultado = facturaService.crearFactura(request);

        assertThat(resultado.getCompanyName()).isEqualTo("Otra Empresa SL");
        assertThat(resultado.getCompanyTaxId()).isEqualTo("B00000000");
    }

    @Test
    @DisplayName("crearFactura - rechaza una línea con cantidad negativa")
    void rechazaCantidadNegativa() {
        CrearFacturaRequest request = new CrearFacturaRequest();
        request.setInvoiceNumber("F-2026-004");
        request.setInvoiceDate(LocalDate.now());

        FacturaItemRequest item = new FacturaItemRequest();
        item.setDescription("Producto");
        item.setQuantity(new BigDecimal("-1"));
        item.setUnitPrice(BigDecimal.TEN);
        item.setTax(BigDecimal.ZERO);
        request.setItems(List.of(item));

        assertThatThrownBy(() -> facturaService.crearFactura(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity");

        verify(facturaRepository, never()).save(any());
    }

    // =====================================================================
    // PRUEBAS DE: getFacturaById
    // =====================================================================

    @Test
    @DisplayName("getFacturaById - lanza IllegalArgumentException si no existe")
    void lanzaExcepcionSiNoExiste() {
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facturaService.getFacturaById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Factura no encontrada");
    }

    @Test
    @DisplayName("getFacturaById - devuelve la factura si existe")
    void devuelveFacturaSiExiste() {
        Factura factura = new Factura();
        factura.setId(1L);
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));

        Factura resultado = facturaService.getFacturaById(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
    }

    // =====================================================================
    // PRUEBAS DE: actualizarFactura
    // =====================================================================

    @Test
    @DisplayName("actualizarFactura - mantiene los valores existentes cuando el campo llega a null")
    void mantieneValoresExistentesSiNoSeEnvian() {
        Factura existente = new Factura();
        existente.setId(1L);
        existente.setCustomerName("Cliente original");
        existente.setCompanyName("Mi Empresa");
        existente.setEstado("PENDIENTE");

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(facturaRepository.save(any(Factura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ActualizarFacturaRequest request = new ActualizarFacturaRequest();
        request.setEstado("PAGADA");

        Factura resultado = facturaService.actualizarFactura(1L, request);

        assertThat(resultado.getEstado()).isEqualTo("PAGADA");
        assertThat(resultado.getCustomerName()).isEqualTo("Cliente original");
    }
}
