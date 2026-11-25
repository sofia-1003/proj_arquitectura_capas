package com.ejemplo.tienda.pedidos.internal;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("No existe el pedido con id " + id);
    }
}
