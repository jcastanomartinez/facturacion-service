package com.facturacion.facturacion_service.controllers;

import com.facturacion.facturacion_service.dtos.CrearFacturaRequest;
import com.facturacion.facturacion_service.entity.Factura;
import com.facturacion.facturacion_service.services.FacturaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class) // 👈 1. Usa Mockito nativo. Adiós @WebMvcTest y problemas de beans nulos
class FacturaControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock // 👈 2. Mock puro de Mockito (nunca será null)
    private FacturaService facturaService;

    @InjectMocks // 👈 3. Inyecta automáticamente el servicio mockeado en tu controlador real
    private FacturaController facturaController;

    @BeforeEach
    void setUp() {
        // 👈 4. Construye MockMvc de forma aislada apuntando solo al controlador
        this.mockMvc = MockMvcBuilders.standaloneSetup(facturaController).build();
    }

    @Test
    void getAllFacturas_devuelveListado() throws Exception {
        Factura factura = new Factura();
        factura.setId(1L);
        factura.setInvoiceNumber("F-2026-001");
        when(facturaService.getAllFacturas()).thenReturn(List.of(factura));

        mockMvc.perform(get("/facturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceNumber").value("F-2026-001"));
    }

    @Test
    void getFacturaById_existente_devuelve200() throws Exception {
        Factura factura = new Factura();
        factura.setId(1L);
        factura.setInvoiceNumber("F-2026-001");
        when(facturaService.getFacturaById(1L)).thenReturn(factura);

        mockMvc.perform(get("/facturas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceNumber").value("F-2026-001"));
    }

    @Test
    void getFacturaById_inexistente_devuelve404() throws Exception {
        when(facturaService.getFacturaById(99L))
                .thenThrow(new IllegalArgumentException("Factura no encontrada"));

        mockMvc.perform(get("/facturas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Factura no encontrada"));
    }

    @Test
    void createFactura_datosValidos_devuelve201() throws Exception {
        CrearFacturaRequest request = new CrearFacturaRequest();
        request.setInvoiceNumber("F-2026-002");
        request.setInvoiceDate(LocalDate.now());
        request.setItems(List.of());

        Factura creada = new Factura();
        creada.setId(2L);
        creada.setInvoiceNumber("F-2026-002");
        when(facturaService.crearFactura(any())).thenReturn(creada);

        mockMvc.perform(post("/facturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void createFactura_datosInvalidos_devuelve400() throws Exception {
        when(facturaService.crearFactura(any()))
                .thenThrow(new IllegalArgumentException("quantity debe ser mayor o igual que 0"));

        mockMvc.perform(post("/facturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("quantity debe ser mayor o igual que 0"));
    }

    @Test
    void deleteFactura_existente_devuelve204() throws Exception {
        when(facturaService.getFacturaById(1L)).thenReturn(new Factura());

        mockMvc.perform(delete("/facturas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteFactura_inexistente_devuelve404() throws Exception {
        when(facturaService.getFacturaById(99L))
                .thenThrow(new IllegalArgumentException("Factura no encontrada"));

        mockMvc.perform(delete("/facturas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void generarPdf_existente_devuelvePdfConCabeceras() throws Exception {
        Factura factura = new Factura();
        factura.setId(1L);
        factura.setInvoiceNumber("F-2026-001");
        when(facturaService.generarPdf(1L)).thenReturn(new byte[]{1, 2, 3});
        when(facturaService.getFacturaById(1L)).thenReturn(factura);

        mockMvc.perform(get("/facturas/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("F-2026-001.pdf")));
    }

    @Test
    void generarPdf_inexistente_devuelve404() throws Exception {
        when(facturaService.generarPdf(99L))
                .thenThrow(new IllegalArgumentException("Factura no encontrada"));

        mockMvc.perform(get("/facturas/99/pdf"))
                .andExpect(status().isNotFound());
    }
}
