package com.ejemplo.tienda.catalogo;

import java.math.BigDecimal;

/** The part of a product that other modules are allowed to know. */
public record ProductSummary(Long id, String name, BigDecimal price) {
}