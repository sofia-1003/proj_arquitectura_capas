package com.ejemplo.tienda.pedidos.internal;

import com.ejemplo.tienda.catalogo.ProductDeletionGuard;
import org.springframework.stereotype.Component;

/**
 * Orders plugs into the catalog's extension point: a product that appears in
 * any order cannot be deleted. The dependency goes orders -> catalog, never
 * the other way around.
 */
@Component
class OrderProductDeletionGuard implements ProductDeletionGuard {

    private final OrderRepository orders;

    OrderProductDeletionGuard(OrderRepository orders) {
        this.orders = orders;
    }

    @Override
    public boolean isInUse(Long productId) {
        return orders.existsByProductId(productId);
    }
}
