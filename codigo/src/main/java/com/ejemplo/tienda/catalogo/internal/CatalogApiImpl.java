package com.ejemplo.tienda.catalogo.internal;

import com.ejemplo.tienda.catalogo.CatalogApi;
import com.ejemplo.tienda.catalogo.InsufficientStockException;
import com.ejemplo.tienda.catalogo.ProductNotFoundException;
import com.ejemplo.tienda.catalogo.ProductSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The module's door. Implements the public {@link CatalogApi} and translates
 * the internal entity into the public {@link ProductSummary}.
 */
@Service
@Transactional
class CatalogApiImpl implements CatalogApi {

    private final ProductRepository repository;

    CatalogApiImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSummary getProduct(Long productId) {
        return toSummary(find(productId));
    }

    @Override
    @Transactional(noRollbackFor = InsufficientStockException.class)
    public ProductSummary reserveStock(Long productId, int quantity) {
        Product product = find(productId);
        product.reserve(quantity);
        return toSummary(product);
    }

    private Product find(Long productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private static ProductSummary toSummary(Product product) {
        return new ProductSummary(product.getId(), product.getName(), product.getPrice());
    }
}
