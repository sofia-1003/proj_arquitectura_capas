package com.ejemplo.tienda.pedidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ejemplo.tienda.catalogo.CatalogApi;
import com.ejemplo.tienda.catalogo.ProductSummary;
import com.ejemplo.tienda.pedidos.internal.Order;
import com.ejemplo.tienda.pedidos.internal.OrderService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Starts ONLY the orders module (with its real database) and replaces the
 * catalog with a mock. The catalog's beans are never created.
 */
@ApplicationModuleTest
class OrderModuleTests {

    @Autowired
    OrderService service;

    @Autowired
    ApplicationContext context;

    @MockitoBean
    CatalogApi catalog;

    @Test
    void createsAndStoresAnOrderUsingTheCatalogApi() {
        ProductSummary teclado = new ProductSummary(1L, "Teclado", new BigDecimal("120000"));
        when(catalog.getProduct(1L)).thenReturn(teclado);
        when(catalog.reserveStock(1L, 2)).thenReturn(teclado);

        Order created = service.create("C-001", List.of(new OrderService.RequestedItem(1L, 2)));

        Order stored = service.findById(created.getId());
        assertThat(stored.getEstado()).isEqualTo("CONFIRMADO");
        assertThat(stored.getTotal()).isEqualTo(240000L);
        assertThat(stored.getLines().get(0).productName()).isEqualTo("Teclado");
    }

    @Test
    void theCatalogModuleIsNotPartOfThisTest() {
        assertThat(context.containsBean("productController")).isFalse();
        assertThat(context.containsBean("orderController")).isTrue();
    }
}