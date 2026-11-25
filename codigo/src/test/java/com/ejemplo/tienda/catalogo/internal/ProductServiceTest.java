package com.ejemplo.tienda.catalogo.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ejemplo.tienda.catalogo.ProductDeletionGuard;
import com.ejemplo.tienda.catalogo.ProductInUseException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductDeletionGuard guard; // stands for "some other module"

    private Product existing() {
        return new Product("Mouse", "Bluetooth", new BigDecimal("24.90"), 5, "electronica");
    }

    @Test
    void deleteRemovesTheProductWhenNoModuleUsesIt() {
        Product product = existing();
        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(guard.isInUse(1L)).thenReturn(false);

        new ProductService(repository, List.of(guard)).delete(1L);

        verify(repository).delete(product);
    }

    @Test
    void deleteIsRefusedWhenAnotherModuleUsesTheProduct() {
        when(repository.findById(1L)).thenReturn(Optional.of(existing()));
        when(guard.isInUse(1L)).thenReturn(true);

        assertThatThrownBy(() -> new ProductService(repository, List.of(guard)).delete(1L))
                .isInstanceOf(ProductInUseException.class)
                .hasMessageContaining("1");
        verify(repository, never()).delete(any());
    }
}
