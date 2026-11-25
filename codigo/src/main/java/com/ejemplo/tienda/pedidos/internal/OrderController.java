package com.ejemplo.tienda.pedidos.internal;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderResponse> findAll() {
        return service.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id) {
        return OrderResponse.from(service.findById(id));
    }

    // RN-4: Total comprado por cliente
    @GetMapping("/clientes/{clienteId}/total")
    public Map<String, Object> totalPorCliente(@PathVariable String clienteId) {
        return Map.of(
                "clienteId", clienteId,
                "totalComprado", service.totalCompradoPorCliente(clienteId)
        );
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody OrderRequest request) {
        Order created = service.create(
                request.codigoExterno(),
                request.resolvedClienteId(),
                request.toRequestedItems()
        );
        OrderResponse response = OrderResponse.from(created);
        URI location = URI.create("/api/orders/" + response.id());
        return ResponseEntity.created(location).body(response);
    }
}