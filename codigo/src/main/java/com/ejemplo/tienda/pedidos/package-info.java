/**
 * Orders module: creates and queries orders.
 *
 * <p>It declares, in writing, the only module it may depend on: the catalog.
 * Any other dependency makes the structure test fail.
 */
@ApplicationModule(displayName = "Pedidos", allowedDependencies = "catalogo")
package com.ejemplo.tienda.pedidos;

import org.springframework.modulith.ApplicationModule;
