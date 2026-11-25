package com.ejemplo.tienda.catalogo.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ejemplo.tienda.catalogo.InsufficientStockException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

// Pure unit test of the business rule: no Spring, no database.
class ProductTest {

    private Product productWithStock(int stock) {
        return new Product("Mouse", "Bluetooth", new BigDecimal("24.90"), stock, "electronica");
    }

    @Test
    void reserveTakesUnitsOutOfStock() {
        Product product = productWithStock(10);

        product.reserve(3);

        assertThat(product.getStock()).isEqualTo(7);
    }

    @Test
    void reserveAllowsTakingExactlyTheRemainingStock() {
        Product product = productWithStock(2);

        product.reserve(2);

        assertThat(product.getStock()).isZero();
    }

    @Test
    void reserveFailsAndKeepsStockWhenThereIsNotEnough() {
        Product product = productWithStock(2);

        assertThatThrownBy(() -> product.reserve(3))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("pedido 3, disponible 2");
        assertThat(product.getStock()).isEqualTo(2);
    }
}
