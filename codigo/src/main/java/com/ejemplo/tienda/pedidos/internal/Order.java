package com.ejemplo.tienda.pedidos.internal;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// "orders" because ORDER is a reserved word in SQL.
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_externo", unique = true)
    private String codigoExterno;

    @Column(nullable = false)
    private String clienteId;

    @Column(nullable = false)
    private String estado; // CONFIRMADO o RECHAZADO

    @Column(nullable = false)
    private long total;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // EAGER: an order is always used together with its lines, and with
    // open-in-view=false there is no session left to load them lazily later.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderLine> lines = new ArrayList<>();

    protected Order() {
    }

    public Order(String clienteId, String estado, long total) {
        this.clienteId = clienteId;
        this.estado = estado;
        this.total = total;
    }

    public Order(String codigoExterno, String clienteId, String estado, long total) {
        this.codigoExterno = (codigoExterno == null || codigoExterno.isBlank()) ? null : codigoExterno.trim();
        this.clienteId = (clienteId == null || clienteId.isBlank()) ? "C-001" : clienteId;
        this.estado = estado;
        this.total = total;
    }

    public void addLine(OrderLine line) {
        lines.add(line);
    }

    public Long getId() {
        return id;
    }

    public String getCodigoExterno() {
        return codigoExterno;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getEstado() {
        return estado;
    }

    public long getTotal() {
        return total;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderLine> getLines() {
        return List.copyOf(lines);
    }
}
