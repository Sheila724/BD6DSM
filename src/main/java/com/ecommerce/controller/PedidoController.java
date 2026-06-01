package com.ecommerce.controller;

import com.ecommerce.entity.Pedido;
import com.ecommerce.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    /** GET /pedidos — lista todos os pedidos (leitura na REPLICA) */
    @GetMapping
    public List<Pedido> listarTodos() {
        return pedidoService.listarTodos();
    }

    /** GET /pedidos/{id} — busca um pedido específico */
    @GetMapping("/{id}")
    public Pedido buscarPedido(@PathVariable Long id) {
        return pedidoService.buscarPorId(id).orElse(null);
    }
}