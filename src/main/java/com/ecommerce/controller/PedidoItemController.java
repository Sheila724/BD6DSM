package com.ecommerce.controller;

import com.ecommerce.entity.PedidoItem;
import com.ecommerce.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pedidos/itens")
public class PedidoItemController {

    @Autowired
    private PedidoService pedidoService;

    /**
     * GET /pedidos/itens — lista todos os itens de pedidos (leitura na REPLICA)
     */
    @GetMapping
    public List<PedidoItem> listarTodos() {
        // Você precisa criar este método no PedidoService
        return pedidoService.listarTodosItens();
    }
}