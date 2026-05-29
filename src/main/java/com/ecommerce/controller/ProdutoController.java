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

    /**
     * GET /produtos/baixo-estoque?limite=10
     * Lista produtos com estoque abaixo ou igual ao limite (leitura na REPLICA).
     */
    @GetMapping("/baixo-estoque")
    public List<Produto> baixoEstoque(@RequestParam(defaultValue = "10") int limite) {
        return produtoService.listarBaixoEstoque(limite);
    }
}
