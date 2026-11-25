package com.ejemplo.tienda.pedidos.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * One line of an order. It keeps a SNAPSHOT of the product name and price:
 * the order must not change if the catalog changes tomorrow. It stores the
 * product id as a plain number, never a JPA relation to the catalog's entity.
 */
@Embeddable
public record OrderLine(
        @Column(nullable = false) Long productId,
        @Column(nullable = false, length = 120) String productName,
        @Column(nullable = false, precision = 10, scale = 2) BigDecimal unitPrice,
        @Column(nullable = false) int quantity
) {

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
