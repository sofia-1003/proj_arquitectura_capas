package com.ejemplo.tienda.pedidos.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ejemplo.tienda.catalogo.CatalogApi;
import com.ejemplo.tienda.catalogo.InsufficientStockException;
import com.ejemplo.tienda.catalogo.ProductSummary;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private CatalogApi catalog;

    @InjectMocks
    private OrderService service;

    @Test
    void rn1_y_rn2_sinDescuentoPorDebajoDelUmbral() {
        ProductSummary teclado = new ProductSummary(1L, "Teclado", new BigDecimal("120000"));
        when(catalog.getProduct(1L)).thenReturn(teclado);
        when(catalog.reserveStock(1L, 3)).thenReturn(teclado);
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Fila 2: u - 1 = 3 unidades -> 0% descuento -> 3 * 120000 = 360000
        Order order = service.create("C-001", List.of(new OrderService.RequestedItem(1L, 3)));

        assertThat(order.getEstado()).isEqualTo("CONFIRMADO");
        assertThat(order.getTotal()).isEqualTo(360000L);
    }

    @Test
    void rn1_y_rn2_conDescuentoAlAlcanzarElUmbral() {
        ProductSummary teclado = new ProductSummary(1L, "Teclado", new BigDecimal("120000"));
        when(catalog.getProduct(1L)).thenReturn(teclado);
        when(catalog.reserveStock(1L, 4)).thenReturn(teclado);
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Fila 2: u = 4 unidades -> 8% descuento -> 4 * 120000 * 92 / 100 = 441600
        Order order = service.create("C-002", List.of(new OrderService.RequestedItem(1L, 4)));

        assertThat(order.getEstado()).isEqualTo("CONFIRMADO");
        assertThat(order.getTotal()).isEqualTo(441600L);
    }

    @Test
    void rn3_pedidoSinStockQuedaRechazadoConTotalCeroYSeGuarda() {
        ProductSummary teclado = new ProductSummary(1L, "Teclado", new BigDecimal("120000"));
        when(catalog.getProduct(1L)).thenReturn(teclado);
        when(catalog.reserveStock(1L, 35)).thenThrow(new InsufficientStockException(1L, 35, 30));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = service.create("C-001", List.of(new OrderService.RequestedItem(1L, 35)));

        assertThat(order.getEstado()).isEqualTo("RECHAZADO");
        assertThat(order.getTotal()).isEqualTo(0L);
        verify(repository).save(any(Order.class));
    }

    @Test
    void rn5_cantidadCeroONegativaNoSeGuarda() {
        assertThatThrownBy(() ->
                service.create("C-001", List.of(new OrderService.RequestedItem(1L, 0))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void findByIdThrowsWhenTheOrderDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- Pruebas Hoja 2: Q0 Idempotencia por codigoExterno ---

    @Test
    void q0_reintentoConMismoCodigoExternoYMismoContenidoDevuelveExistenteSinReservarDoble() {
        ProductSummary teclado = new ProductSummary(1L, "Teclado", new BigDecimal("120000"));
        when(catalog.getProduct(1L)).thenReturn(teclado);
        when(catalog.reserveStock(1L, 2)).thenReturn(teclado);
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        var items = List.of(new OrderService.RequestedItem(1L, 2));
        Order primero = service.create("EXT-2026-001", "C-001", items);

        when(repository.findByCodigoExterno("EXT-2026-001")).thenReturn(Optional.of(primero));

        Order reintento = service.create("EXT-2026-001", "C-001", items);

        assertThat(reintento).isSameAs(primero);
        assertThat(reintento.getCodigoExterno()).isEqualTo("EXT-2026-001");
        assertThat(reintento.getTotal()).isEqualTo(240000L);
        verify(catalog, times(1)).reserveStock(1L, 2);
        verify(repository, times(1)).save(any(Order.class));
    }

    @Test
    void q0_reintentoConMismoCodigoExternoYDistintoContenidoLanzaConflicto() {
        Order existente = new Order("EXT-2026-001", "C-001", "CONFIRMADO", 240000L);
        existente.addLine(new OrderLine(1L, "Teclado", new BigDecimal("120000"), 2));

        ProductSummary teclado = new ProductSummary(1L, "Teclado", new BigDecimal("120000"));
        when(catalog.getProduct(1L)).thenReturn(teclado);
        when(repository.findByCodigoExterno("EXT-2026-001")).thenReturn(Optional.of(existente));

        var itemsDistintos = List.of(new OrderService.RequestedItem(1L, 4));

        assertThatThrownBy(() -> service.create("EXT-2026-001", "C-001", itemsDistintos))
                .isInstanceOf(DuplicateExternalCodeConflictException.class)
                .hasMessageContaining("EXT-2026-001");

        verify(catalog, never()).reserveStock(anyLong(), anyInt());
        verify(repository, never()).save(any(Order.class));
    }
}