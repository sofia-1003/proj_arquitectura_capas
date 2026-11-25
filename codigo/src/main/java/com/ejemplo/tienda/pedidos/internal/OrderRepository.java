package com.ejemplo.tienda.pedidos.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select count(o) > 0 from Order o join o.lines l where l.productId = :productId")
    boolean existsByProductId(@Param("productId") Long productId);

    List<Order> findByClienteIdAndEstado(String clienteId, String estado);
    Optional<Order> findByCodigoExterno(String codigoExterno);
}
