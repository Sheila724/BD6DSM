package com.ecommerce.repository;

import com.ecommerce.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("SELECT p FROM Produto p WHERE p.estoque <= :limite ORDER BY p.estoque ASC")
    List<Produto> findBaixoEstoque(@Param("limite") int limite);
}
