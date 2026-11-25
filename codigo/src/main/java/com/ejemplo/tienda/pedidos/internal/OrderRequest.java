package com.ejemplo.tienda.pedidos.internal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// What the client SENDS to create an order.
public record OrderRequest(
        String codigoExterno,
        String clienteId,

        @NotEmpty(message = "El pedido debe tener al menos un producto")
        List<@Valid Item> items
) {
    public OrderRequest(List<Item> items) {
        this(null,"C-001", items);
    }

    public OrderRequest(String clienteId, List<Item> items) {
        this(null, clienteId, items);
    }

    public record Item(
            @NotNull(message = "El producto es obligatorio")
            Long productId,

            @Min(value = 1, message = "La cantidad debe ser al menos 1")
            int quantity
    ) {
    }

    String resolvedClienteId() {
        return (clienteId == null || clienteId.isBlank()) ? "C-001" : clienteId;
    }

    List<OrderService.RequestedItem> toRequestedItems() {
        return items.stream()
                .map(item -> new OrderService.RequestedItem(item.productId(), item.quantity()))
                .toList();
    }
}
