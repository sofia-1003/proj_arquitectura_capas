package com.ejemplo.tienda.pedidos.internal;

import com.ejemplo.tienda.catalogo.CatalogApi;
import com.ejemplo.tienda.catalogo.InsufficientStockException;
import com.ejemplo.tienda.catalogo.ProductSummary;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    // Parámetros Fila 2: Teclado (umbral u = 4, descuento d = 8%)
    private static final int UMBRAL_VOLUMEN = 4;
    private static final int DESCUENTO_PORCENTAJE = 8;

    private final OrderRepository repository;
    private final CatalogApi catalog;

    public OrderService(OrderRepository repository, CatalogApi catalog) {
        this.repository = repository;
        this.catalog = catalog;
    }

    public List<Order> findAll() {
        return repository.findAll();
    }

    public Order findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    // RN-4: Total comprado por cliente (suma de totales de pedidos CONFIRMADO)
    public long totalCompradoPorCliente(String clienteId) {
        return repository.findByClienteIdAndEstado(clienteId, "CONFIRMADO").stream()
                .mapToLong(Order::getTotal)
                .sum();
    }

    @Transactional(noRollbackFor = InsufficientStockException.class)
    public Order create(List<RequestedItem> items) {
        return create(null, "C-001", items);
    }

    @Transactional(noRollbackFor = InsufficientStockException.class)
    public Order create(String clienteId, List<RequestedItem> items) {
        return create(null, clienteId, items);
    }

    @Transactional(noRollbackFor = InsufficientStockException.class)
    public Order create(String codigoExterno, String clienteId, List<RequestedItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos una línea");
        }

        String clienteNormalizado = (clienteId == null || clienteId.isBlank()) ? "C-001" : clienteId;
        String codigoNormalizado = (codigoExterno == null || codigoExterno.isBlank()) ? null : codigoExterno.trim();

        // RN-5: Validar cantidad > 0 y existencia del producto ANTES de crear/asignar id
        for (RequestedItem item : items) {
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
            }
            catalog.getProduct(item.productId());
        }

        // Q0: Verificar idempotencia por codigoExterno si fue suministrado
        if (codigoNormalizado != null) {
            Optional<Order> existenteOpt = repository.findByCodigoExterno(codigoNormalizado);
            if (existenteOpt.isPresent()) {
                Order existente = existenteOpt.get();
                if (mismoContenido(existente, clienteNormalizado, items)) {
                    return existente;
                }
                throw new DuplicateExternalCodeConflictException(codigoNormalizado);
            }
        }

        // Intentar reservar stock (RN-3)
        try {
            long totalPedido = 0L;
            Order order = new Order(codigoNormalizado, clienteNormalizado, "CONFIRMADO", 0L);

            for (RequestedItem item : items) {
                ProductSummary product = catalog.reserveStock(item.productId(), item.quantity());
                order.addLine(new OrderLine(
                        product.id(), product.name(), product.price(), item.quantity()));

                long precioEntero = product.price().longValue();
                totalPedido += calcularTotalLinea(item.quantity(), precioEntero);
            }

            Order confirmado = new Order(codigoNormalizado, clienteNormalizado, "CONFIRMADO", totalPedido);
            order.getLines().forEach(confirmado::addLine);
            return repository.save(confirmado);

        } catch (InsufficientStockException ex) {
            // RN-3: Stock insuficiente -> RECHAZADO, total=0, recibe id y no consume stock
            Order rechazado = new Order(codigoNormalizado, clienteNormalizado, "RECHAZADO", 0L);
            for (RequestedItem item : items) {
                ProductSummary product = catalog.getProduct(item.productId());
                rechazado.addLine(new OrderLine(
                        product.id(), product.name(), product.price(), item.quantity()));
            }
            return repository.save(rechazado);
        }
    }

    private boolean mismoContenido(Order existente, String clienteId, List<RequestedItem> items) {
        if (!existente.getClienteId().equals(clienteId)) {
            return false;
        }
        List<OrderLine> lineas = existente.getLines();
        if (lineas.size() != items.size()) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            OrderLine l = lineas.get(i);
            RequestedItem r = items.get(i);
            if (!l.productId().equals(r.productId()) || l.quantity() != r.quantity()) {
                return false;
            }
        }
        return true;
    }

    // RN-1 y RN-2: Si cantidad >= 4 aplica 8% descuento; si no, 0%
    public long calcularTotalLinea(int cantidad, long precioUnitario) {
        int descuento = (cantidad >= UMBRAL_VOLUMEN) ? DESCUENTO_PORCENTAJE : 0;
        return (cantidad * precioUnitario * (100 - descuento)) / 100;
    }

    public record RequestedItem(Long productId, int quantity) {
    }
}