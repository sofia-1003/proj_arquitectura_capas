package com.ejemplo.tienda.pedidos.internal;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
class MuestraHoja1Runner implements ApplicationRunner {

    private final OrderController controller;
    private final ConfigurableApplicationContext context;

    MuestraHoja1Runner(OrderController controller, ConfigurableApplicationContext context) {
        this.controller = controller;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("muestra") || !args.getOptionValues("muestra").contains("hoja1")) {
            return;
        }

        // Fila 2: Teclado (stock=30, u=4, d=8%)
        // L1: C-001, u-1 = 3 -> CONFIRMADO, sin descuento -> total=360000
        // L2: C-002, u = 4 -> CONFIRMADO, 8% descuento -> total=441600
        // L3: C-001, stock - 2u + 2 = 24 -> RECHAZADO -> total=0
        // L4: C-003, stock - 2u + 1 = 23 -> CONFIRMADO, 8% descuento -> total=2539200
        List<OrderRequest> secuencia = List.of(
                new OrderRequest("C-001", List.of(new OrderRequest.Item(1L, 3))),
                new OrderRequest("C-002", List.of(new OrderRequest.Item(1L, 4))),
                new OrderRequest("C-001", List.of(new OrderRequest.Item(1L, 24))),
                new OrderRequest("C-003", List.of(new OrderRequest.Item(1L, 23)))
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