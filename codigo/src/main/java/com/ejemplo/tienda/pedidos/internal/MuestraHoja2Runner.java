package com.ejemplo.tienda.pedidos.internal;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class MuestraHoja2Runner implements CommandLineRunner {

    private final OrderController controller;
    private final ConfigurableApplicationContext context;

    public MuestraHoja2Runner(
            OrderController controller,
            ConfigurableApplicationContext context) {
        this.controller = controller;
        this.context = context;
    }

    @Override
    public void run(String... args) {
        if (!Arrays.asList(args).contains("--muestra=hoja2")) {
            return;
        }

        // Muestra Q0 - Fila 2 (Teclado: stock=30, u=4, d=8%, T=441600)
        // L1: C-001, W-1001, cantidad u=4 -> PEDIDO 1 CONFIRMADO total=441600 (stock queda en 26)
        // L2: C-001, W-1001, cantidad u=4 -> PEDIDO 1 CONFIRMADO total=441600 (stock sigue en 26)
        // L3: C-002, W-1002, cantidad stock-u=26 -> PEDIDO 2 CONFIRMADO total=2870400 (stock queda en 0)
        // L4: C-003, W-1003, cantidad 1 -> PEDIDO 3 RECHAZADO total=0 (stock agotado)
        List<OrderRequest> secuencia = List.of(
                new OrderRequest("W-1001", "C-001", List.of(new OrderRequest.Item(1L, 4))),
                new OrderRequest("W-1001", "C-001", List.of(new OrderRequest.Item(1L, 4))),
                new OrderRequest("W-1002", "C-002", List.of(new OrderRequest.Item(1L, 26))),
                new OrderRequest("W-1003", "C-003", List.of(new OrderRequest.Item(1L, 1)))
        );

        for (OrderRequest req : secuencia) {
            OrderResponse res = controller.create(req).getBody();
            if (res != null) {
                System.out.println("PEDIDO " + res.id() + " " + res.estado() + " total=" + res.total());
            }
        }

        SpringApplication.exit(context, () -> 0);
    }
}