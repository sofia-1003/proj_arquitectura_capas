package com.ejemplo.tienda.catalogo.internal;

import com.ejemplo.tienda.catalogo.ProductDeletionGuard;
import com.ejemplo.tienda.catalogo.ProductInUseException;
import com.ejemplo.tienda.catalogo.ProductNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD use cases of the catalog. Used by the module's own controller. */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;
    private final List<ProductDeletionGuard> deletionGuards;

    // Spring injects every ProductDeletionGuard it finds, in any module.
    public ProductService(ProductRepository repository,
                          List<ProductDeletionGuard> deletionGuards) {
        this.repository = repository;
        this.deletionGuards = deletionGuards;
    }

    public List<Product> findAll(String q) {
        if (q == null || q.isBlank()) {
            return repository.findAll();
        }
        return repository.findByNameContainingIgnoreCase(q);
    }

    public Product findById(Long id) {
        return getOrThrow(id);
    }

    @Transactional
    public Product create(Product product) {
        return repository.save(product);
    }

    @Transactional
    public Product update(Long id, Product changes) {
        Product product = getOrThrow(id);
        product.update(changes.getName(), changes.getDescription(),
                changes.getPrice(), changes.getStock(), changes.getCategory());
        return product;
    }

    @Transactional
    public void delete(Long id) {
        Product product = getOrThrow(id);
        if (deletionGuards.stream().anyMatch(guard -> guard.isInUse(id))) {
            throw new ProductInUseException(id);
        }
        repository.delete(product);
    }

    private Product getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
