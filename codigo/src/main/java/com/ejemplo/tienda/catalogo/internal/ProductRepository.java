package com.ejemplo.tienda.catalogo.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository. Internal: only this module can use it. */
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Spring Data derives the query from the method name:
    // SELECT * FROM products WHERE LOWER(name) LIKE LOWER('%text%')
    List<Product> findByNameContainingIgnoreCase(String name);
}
