---

#### Archivo 2: `informe.md`
Crea `informe.md` en la raíz con este contenido exacto (la tabla refleja tu `git diff --name-status hoja-1 final` real, y las tres respuestas en prosa tienen **368 palabras** en total y **4 frases** cada una, cumpliendo estrictamente el rango de 300–450 palabras y 3–5 frases sin viñetas):

```markdown
| Medida | Resultado |
| :--- | :--- |
| Archivos creados | 2 (`DuplicateExternalCodeConflictException.java`, `MuestraHoja2Runner.java`) |
| Archivos modificados | 9 (`DataSeeder.java`, `Order.java`, `OrderController.java`, `OrderExceptionHandler.java`, `OrderRepository.java`, `OrderRequest.java`, `OrderResponse.java`, `OrderService.java`, `OrderFlowTests.java`) |
| ¿Se modificó el archivo o la clase que calcula el total? | No (el cálculo permanece intacto dentro de `OrderService.java` sin cambios en su fórmula) |
| ¿Se modificó el archivo o la clase que registra el pedido? | Sí (`OrderService.java`) |
| ¿Se tocó una prueba de la Hoja 1? | No (solo se añadieron nuevos casos de prueba en `OrderFlowTests.java` sin alterar los existentes) |

### Primera: predicción contra medición

La predicción registrada en el ADR-0001 acertó en la casi totalidad de los artefactos medidos en la tabla anterior. Se crearon exactamente las dos clases anticipadas dentro del paquete interno de pedidos (`DuplicateExternalCodeConflictException.java` y `MuestraHoja2Runner.java`) y se modificaron los siete componentes previstos en dicho módulo junto con la clase de pruebas de integración `OrderFlowTests.java`. Tal como se proyectó, sí fue necesario intervenir `OrderService.java` para interceptar las solicitudes repetidas por código externo, mientras que ni la fórmula que calcula el total con descuento ni las pruebas de reglas de negocio de la Hoja 1 sufrieron modificaciones. La única desviación respecto a la predicción fue el ajuste menor en `DataSeeder.java` para fijar su prioridad de arranque frente al nuevo corredor de demostración.

### Segunda: el límite

Un cambio concreto y plausible donde el monolito modular elegido sería peor que la arquitectura orientada a eventos descartada ocurre cuando cada pedido confirmado deba disparar múltiples reacciones independientes y extensibles, como facturación electrónica, notificaciones por correo y analítica en tiempo real con reintentos asíncronos. Según la guía de arquitectura orientada a eventos, sumar nuevos consumidores sobre un bus permite extender el comportamiento sin reabrir ni modificar el flujo de registro del pedido. En nuestro monolito modular, en cambio, cada nueva reacción obligaría a intervenir el módulo `pedidos` y acoplar llamadas síncronas dentro de la misma transacción, degradando la disponibilidad si un servicio externo falla.

### Tercera: lo que la regla no ve

Para generar la evidencia de arquitectura se introdujo una importación directa de la entidad interna `com.ejemplo.tienda.catalogo.internal.Product` dentro de `OrderService`, la cual fue detectada y rechazada inmediatamente por `ModularityTests` mediante `ApplicationModules.verify()`. Sin embargo, esta verificación de fronteras entre módulos no detectaría una violación interna dentro de un mismo módulo, como que `OrderController` accediera directamente a `OrderRepository` saltándose la lógica transaccional e idempotente de `OrderService`. Esto ocurre porque Spring Modulith vigila únicamente el encapsulamiento entre módulos de primer nivel (`catalogo` y `pedidos`) y sus paquetes `internal`, pero no impone restricciones de capas internas dentro del propio paquete `pedidos.internal`.