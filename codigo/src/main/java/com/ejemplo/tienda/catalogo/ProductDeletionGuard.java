package com.ejemplo.tienda.catalogo;

/**
 * Extension point owned by the catalog: any module may implement it to say
 * that a product is in use and must not be deleted. The catalog never needs
 * to know who implements it, so the dependency arrow still points INTO the
 * catalog.
 */
public interface ProductDeletionGuard {

    boolean isInUse(Long productId);
}
