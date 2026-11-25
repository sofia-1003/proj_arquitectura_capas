package com.ejemplo.tienda.catalogo;

/** Thrown when a product id does not exist. Part of the public API. */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("No existe el producto con id " + id);
    }
}