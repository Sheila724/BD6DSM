package com.ecommerce.controller;

import com.ecommerce.entity.Pedido;
import com.ecommerce.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private PedidoService pedidoService;

    /** GET /clientes/{id}/pedidos — lista todos os pedidos de um cliente (leitura na REPLICA) */
    @GetMapping("/{id}/pedidos")
    public List<Pedido> listarPedidosDoCliente(@PathVariable Long id) {
        return pedidoService.listarPorCliente(id);
    }
}
