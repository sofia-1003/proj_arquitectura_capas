package com.ejemplo.tienda.catalogo;

/** Thrown when a reservation asks for more units than the product has. */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long id, int requested, int available) {
        super("Stock insuficiente para el producto " + id
                + ": pedido " + requested + ", disponible " + available);
    }
}