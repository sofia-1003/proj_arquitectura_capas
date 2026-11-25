package com.ejemplo.tienda.pedidos.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

// What the API RETURNS to the client.
public record OrderResponse(
        Long id,
        String clienteId,
        String estado,
        Instant createdAt,
        List<Line> lines,
        long total,
        String codigoExterno
) {

    public record Line(Long productId, String productName,
                       BigDecimal unitPrice, int quantity, BigDecimal subtotal) {
    }

    static OrderResponse from(Order order) {
        List<Line> lines = order.getLines().stream()
                .map(l -> new Line(l.productId(), l.productName(),
                        l.unitPrice(), l.quantity(), l.subtotal()))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getClienteId(),
                order.getEstado(),
                order.getCreatedAt(),
                lines,
                order.getTotal(),
                order.getCodigoExterno()
        );
    }
}
