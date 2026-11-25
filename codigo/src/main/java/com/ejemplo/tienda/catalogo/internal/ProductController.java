package com.ejemplo.tienda.catalogo.internal;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // GET /api/products or GET /api/products?q=laptop
    @GetMapping
    public List<ProductResponse> findAll(
            @RequestParam(required = false) String q) {
        return service.findAll(q).stream()
                .map(ProductResponse::from)
                .toList();
    }

    // GET /api/products/1
    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return ProductResponse.from(service.findById(id));
    }

    // POST /api/products -> 201 Created + Location header
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request) {
        Product created = service.create(toProduct(request));
        ProductResponse response = ProductResponse.from(created);
        URI location = URI.create("/api/products/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    // PUT /api/products/1
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(service.update(id, toProduct(request)));
    }

    // DELETE /api/products/1 -> 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static Product toProduct(ProductRequest request) {
        return new Product(request.name(), request.description(),
                request.price(), request.stock(), request.category());
    }
}
