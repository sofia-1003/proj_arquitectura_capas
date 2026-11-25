package com.ejemplo.tienda.pedidos.internal;

public class DuplicateExternalCodeConflictException extends RuntimeException {

    public DuplicateExternalCodeConflictException(String codigoExterno) {
        super("El código externo '" + codigoExterno + "' ya existe asociado a un pedido con datos diferentes.");
    }
}