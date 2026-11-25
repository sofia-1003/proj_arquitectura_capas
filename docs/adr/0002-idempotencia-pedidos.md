# ADR-0002 Implementación de idempotencia mediante `codigoExterno` (Q0)

Variante: Q0 / fila 2
Fecha: 2026-11-25 02:20
Estado: aceptada

## Contexto

Al evolucionar hacia la Hoja 2 bajo el atributo prioritario **Q0 (Consistencia)**, los clientes envían un identificador `codigoExterno` (como `W-1001`) y pueden reintentar la petición ante fallos de red. El sistema debe asegurar que una solicitud repetida con el mismo `codigoExterno` devuelva exactamente el mismo pedido (`id`, `estado` y `total`) sin volver a descontar el stock inicial de 30 Teclados ni alterar la secuencia de identificadores para los pedidos posteriores (`W-1002` y `W-1003`).

## Alternativas consideradas

* **Tabla independiente de claves de idempotencia:** descartada porque duplicaría almacenamiento y requeriría sincronizar dos entidades distintas para un modelo de dominio sencillo de un único producto (Teclado, stock 30) donde el propio `Order` ya persiste tanto los pedidos `CONFIRMADO` como los `RECHAZADO`.
* **Validación únicamente en el controlador REST (`OrderController`):** descartada porque dejaría desprotegida la lógica de negocio si `OrderService` es invocado desde otros puntos de entrada o pruebas de módulo, rompiendo la garantía transaccional de `Q0`.

## Decisión

Se incorpora el campo opcional `codigoExterno` en `Order` y en los DTO (`OrderRequest` y `OrderResponse`), resolviendo la deduplicación directamente al inicio del método transaccional `OrderService.create(...)` mediante `OrderRepository.findByCodigoExterno(...)` antes de validar o reservar inventario en `CatalogApi`.

## Consecuencias

**Se gana:** Idempotencia estricta tanto para pedidos `CONFIRMADO` como `RECHAZADO` dentro de una única transacción, total retrocompatibilidad con las peticiones sin `codigoExterno` de la Hoja 1 y aislamiento completo del módulo `catalogo` (que no requirió cambios en su API ni en su lógica de inventario).

**Se pierde:** Una consulta adicional de lectura por `codigoExterno` en cada creación de pedido que incluya dicho campo, además de que el contrato de `OrderService.create` y `OrderRequest` tuvo que ampliarse para propagar el identificador externo.

**Veredicto sobre ADR-0001:** Se confirmó lo previsto en *Se pierde*: la adaptación quedó contenida dentro de `pedidos.internal` sin tocar `CatalogApi` ni la lógica de stock. La predicción acertó en casi totalidad: se crearon exactamente las dos clases previstas (`DuplicateExternalCodeConflictException` y `MuestraHoja2Runner`), se modificaron las siete clases esperadas de `pedidos.internal`, no se tocó la fórmula de cálculo del total ni las pruebas de negocio de la Hoja 1, y como única desviación menor respecto a lo predicho se ajustó `DataSeeder.java` (`@Order(1)`) para garantizar el orden de carga inicial frente a `MuestraHoja2Runner`.