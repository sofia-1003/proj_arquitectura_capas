# ADR-0001 Elección de arquitectura monolito modular para la tienda

Variante: Q0 / fila 2
Fecha: 2026-11-24 23:30
Estado: aceptada

## Contexto

La tienda requiere gestionar un producto único inicial (Teclado, precio 120.000, stock inicial 30, umbral de volumen $u = 4$ y descuento $d = 8\%$) y procesar pedidos que descuentan inventario según las reglas RN-1 a RN-5. Dado que el atributo prioritario de la pareja es **Q0 (Consistencia)**, el sistema debe garantizar que pedidos repetidos por reintentos de red con el mismo `codigoExterno` no dupliquen el registro ni descuenten el stock de 30 unidades dos veces, manteniendo los pedidos rechazados con su identificador consecutivo.

## Alternativas consideradas

* **Arquitectura por capas (`capas/catalogo`):** descartada porque no incorpora verificación automática de fronteras entre módulos ni dominio de pedidos, lo que aumenta el riesgo de mezclar el control de stock y la deduplicación de `codigoExterno` directamente en controladores o servicios acoplados para un stock acotado de 30 unidades.
* **Arquitectura hexagonal (`hexagonal/catalogo`):** descartada porque introduce múltiples puertos, adaptadores y mapeadores adicionales cuando la carpeta base ni siquiera incluye pedidos, añadiendo complejidad estructural innecesaria para proteger la consistencia transaccional de un catálogo de un solo producto con umbral $u = 4$.
* **Arquitectura orientada a eventos (`eventos/tienda`):** descartada porque la comunicación asíncrona sobre RabbitMQ introduce consistencia eventual, obligando a diseñar mecanismos complejos de compensación y deduplicación distribuida cuando la consistencia fuerte inmediata (`Q0`) sobre un stock reducido de 30 unidades se resuelve de forma atómica en una sola transacción local.

## Decisión

Se elige la **Arquitectura monolito modular** (`monilito modular/tienda`) sobre Spring Boot y Spring Modulith, organizando el sistema en dos módulos encapsulados (`catalogo` y `pedidos`) que ejecutan la creación del pedido y la reserva de stock dentro de una misma transacción ACID, respaldada por base de datos en memoria H2 para garantizar arranques limpios deterministas.

## Consecuencias

**Se gana:** Consistencia transaccional inmediata entre el registro del pedido y el descuento de stock, fronteras modulares verificadas automáticamente mediante `ModularityTests` (`ApplicationModules.verify()`) y un punto de partida que ya incluye la estructura base de ambos módulos.

**Se pierde:** La posibilidad de escalar o desplegar `catalogo` y `pedidos` de forma independiente, además de requerir adaptar la lógica existente de `pedidos` para soportar estados (`CONFIRMADO`/`RECHAZADO`), descuentos por volumen y persistencia de pedidos rechazados.

**Predicción del costo de la Hoja 2:** Para absorber el escenario Q0 esperamos crear dos clases dentro de `com.ejemplo.tienda.pedidos.internal`: una excepción de conflicto por código externo (`DuplicateExternalCodeConflictException`) y una clase de ejecución para la segunda muestra (`MuestraHoja2Runner`). Esperamos modificar `Order.java`, `OrderRepository.java`, `OrderRequest.java`, `OrderResponse.java`, `OrderController.java`, `OrderService.java` y `OrderExceptionHandler.java` dentro de `pedidos.internal`, además de actualizar `OrderFlowTests.java` y `OrderServiceTest.java`. Estimamos que **sí** se modificará la clase que registra el pedido (`OrderService.java`), que **no** se modificará la lógica que calcula el total (permanece encapsulada sin cambios en la fórmula de descuento), y que **no** se editarán las aserciones de las pruebas unitarias de reglas de negocio de la Hoja 1.