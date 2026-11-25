package com.ejemplo.tienda.catalogo;

/** Thrown when a product cannot be deleted because another module uses it. */
public class ProductInUseException extends RuntimeException {

    public ProductInUseException(Long id) {
        super("No se puede borrar el producto " + id + ": tiene pedidos asociados");
    }
}
