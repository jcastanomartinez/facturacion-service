package com.facturacion.facturacion_service.e2e;

import com.facturacion.facturacion_service.client.DocumentServiceClient;
import com.facturacion.facturacion_service.support.JwtTestSupport;
import com.facturacion.facturacion_service.support.TestSecurityBeans;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate; // 👈 RESTAURADO: El paquete correcto de tu framework
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate; // 👈 RESTAURADO
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestSecurityBeans.class)
class FacturaFlowE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private DocumentServiceClient documentServiceClient;

    @Test
    void flujoCompleto_crearFactura_consultarla_yGenerarPdf() {
        String token = JwtTestSupport.validToken("test1@gmail.com", List.of("ROLE_USER"));

        Map<String, Object> nuevaFactura = Map.of(
                "invoiceNumber", "F-2026-E2E-001",
                "invoiceDate", "2026-09-29",
                "customerName", "Cliente E2E",
                "items", List.of(Map.of(
                        "description", "Servicio de prueba",
                        "quantity", 2,
                        "unitPrice", 100,
                        "tax", 21
                ))
        );

        ResponseEntity<Map> creada = restTemplate.exchange(
                "/facturas", HttpMethod.POST,
                new HttpEntity<>(nuevaFactura, authHeaders(token)), Map.class);

        assertThat(creada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Number id = (Number) creada.getBody().get("id");
        Number total = (Number) creada.getBody().get("total");
        assertThat(total.doubleValue()).isEqualTo(242.00);

        ResponseEntity<Map> consultada = restTemplate.exchange(
                "/facturas/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(consultada.getStatusCode()).isEqualTo(HttpStatus.OK);

        when(documentServiceClient.generateInvoice(any())).thenReturn(new byte[]{1, 2, 3, 4});

        ResponseEntity<byte[]> pdf = restTemplate.exchange(
                "/facturas/" + id + "/pdf", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), byte[].class);

        assertThat(pdf.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pdf.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(pdf.getBody()).containsExactly(1, 2, 3, 4);
    }

    @Test
    void sinToken_devuelve403() {
        ResponseEntity<String> respuesta = restTemplate.getForEntity("/facturas", String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void conTokenExpirado_devuelve403() {
        String token = JwtTestSupport.expiredToken("test1@gmail.com", List.of("ROLE_USER"));

        ResponseEntity<String> respuesta = restTemplate.exchange(
                "/facturas", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
