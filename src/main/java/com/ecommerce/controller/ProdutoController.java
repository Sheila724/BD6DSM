package com.ecommerce.controller;

import com.ecommerce.entity.Produto;
import com.ecommerce.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    /** GET /produtos — lista todos os produtos (leitura na REPLICA) */
    @GetMapping
    public List<Produto> listarTodos() {
        return produtoService.listarTodos();
    }

    /** GET /produtos/baixo-estoque?limite=10 — produtos com estoque abaixo do limite */
    @GetMapping("/baixo-estoque")
    public List<Produto> listarBaixoEstoque(@RequestParam(defaultValue = "10") int limite) {
        return produtoService.listarBaixoEstoque(limite);
    }
}