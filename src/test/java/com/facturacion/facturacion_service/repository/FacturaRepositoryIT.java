package com.facturacion.facturacion_service.repository;

import com.facturacion.facturacion_service.entity.Factura;
import com.facturacion.facturacion_service.entity.FacturaItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración REAL: levanta un Postgres real en Docker (Testcontainers)
 * y ejecuta el repositorio contra él, en vez de contra H2 o un mock.
 *
 * "IT" en el nombre (en vez de "Test") es la convención habitual para que estos
 * tests, más lentos porque arrancan un contenedor, se puedan excluir fácilmente
 * de una ejecución rápida (p.ej. con el failsafe-plugin, que solo aquellos que
 * acaban en *IT).
 *
 * Requiere Docker corriendo en la máquina que ejecuta los tests (tu equipo o
 * el agente de Jenkins, que ya tiene acceso a /var/run/docker.sock).
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FacturaRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private FacturaRepository facturaRepository;

    @Test
    void guardaFacturaConLineasYLasRecupera() {
        Factura factura = facturaDeEjemplo("F-2026-100");
        FacturaItem item = new FacturaItem();
        item.setDescription("Servicio de consultoría");
        item.setQuantity(new BigDecimal("1.00"));
        item.setUnitPrice(new BigDecimal("500.00"));
        item.setTax(new BigDecimal("21.00"));
        item.setTotal(new BigDecimal("605.00"));
        factura.addItem(item);

        Factura guardada = facturaRepository.save(factura);
        facturaRepository.flush();

        Factura recuperada = facturaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(recuperada.getItems()).hasSize(1);
        assertThat(recuperada.getItems().get(0).getDescription()).isEqualTo("Servicio de consultoría");
        // el lado inverso de la relación debe quedar cargado correctamente
        assertThat(recuperada.getItems().get(0).getFactura().getId()).isEqualTo(recuperada.getId());
    }

    @Test
    void borrarUnaLineaDeLaListaLaEliminaDeBaseDeDatos_orphanRemoval() {
        Factura factura = facturaDeEjemplo("F-2026-101");
        FacturaItem item1 = itemDeEjemplo("Item 1");
        FacturaItem item2 = itemDeEjemplo("Item 2");
        factura.addItem(item1);
        factura.addItem(item2);
        Factura guardada = facturaRepository.saveAndFlush(factura);

        guardada.removeItem(guardada.getItems().get(0));
        facturaRepository.saveAndFlush(guardada);

        Factura recuperada = facturaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(recuperada.getItems()).hasSize(1);
    }

    @Test
    void numeroDeFacturaDuplicado_violaConstraintUnique() {
        facturaRepository.saveAndFlush(facturaDeEjemplo("F-2026-DUP"));

        Factura duplicada = facturaDeEjemplo("F-2026-DUP");

        assertThatThrownBy(() -> facturaRepository.saveAndFlush(duplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Factura facturaDeEjemplo(String invoiceNumber) {
        Factura factura = new Factura();
        factura.setInvoiceNumber(invoiceNumber);
        factura.setInvoiceDate(LocalDate.now());
        factura.setSubtotal(BigDecimal.ZERO);
        factura.setTaxTotal(BigDecimal.ZERO);
        factura.setTotal(BigDecimal.ZERO);
        return factura;
    }

    private FacturaItem itemDeEjemplo(String descripcion) {
        FacturaItem item = new FacturaItem();
        item.setDescription(descripcion);
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(BigDecimal.TEN);
        item.setTax(BigDecimal.ZERO);
        item.setTotal(BigDecimal.TEN);
        return item;
    }
}
