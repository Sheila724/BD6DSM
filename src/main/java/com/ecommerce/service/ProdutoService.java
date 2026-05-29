package com.ecommerce.service;

import com.ecommerce.entity.Produto;
import com.ecommerce.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    /**
     * Escrita — vai para o banco PRIMARIO.
     */
    @Transactional
    public Produto cadastrar(String descricao, BigDecimal valor, int estoque) {
        Produto produto = new Produto();
        produto.setDescricao(descricao);
        produto.setValor(valor);
        produto.setEstoque(estoque);
        Produto salvo = produtoRepository.save(produto);
        System.out.printf("[ESCRITA → PRIMARY] Produto cadastrado: id=%d | descricao=%s | valor=R$%.2f | estoque=%d%n",
                salvo.getId(), salvo.getDescricao(), salvo.getValor(), salvo.getEstoque());
        return salvo;
    }

    /**
     * Leitura — vai para a REPLICA.
     */
    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Produto> listarBaixoEstoque(int limite) {
        return produtoRepository.findBaixoEstoque(limite);
    }
}
