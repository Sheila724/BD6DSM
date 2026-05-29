package com.ecommerce.repository;

import com.ecommerce.entity.Pedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId ORDER BY p.criadoEm DESC")
    List<Pedido> findByClienteId(@Param("clienteId") Long clienteId);

    // Usado para buscar os ultimos N pedidos do cliente (ultimos5PorCliente usa PageRequest.of(0, 5))
    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId ORDER BY p.criadoEm DESC")
    List<Pedido> findByClienteIdPaged(@Param("clienteId") Long clienteId, Pageable pageable);

    // Retorna [COUNT, AVG(valorTotal), SUM(valorTotal)]
    @Query("SELECT COUNT(p), AVG(p.valorTotal), SUM(p.valorTotal) FROM Pedido p")
    List<Object[]> findRelatorioVendas();
}
