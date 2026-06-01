package com.ecommerce.controller;

import com.ecommerce.entity.Cliente;
import com.ecommerce.entity.Pedido;
import com.ecommerce.service.ClienteService;
import com.ecommerce.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteService clienteService;  // ← ADICIONE ESTA LINHA

    /** GET /clientes/{id}/pedidos — lista todos os pedidos de um cliente (leitura na REPLICA) */
    @GetMapping("/{id}/pedidos")
    public List<Pedido> listarPedidosDoCliente(@PathVariable Long id) {
        return pedidoService.listarPorCliente(id);
    }

    /** GET /clientes — lista todos os clientes (leitura na REPLICA) */
    @GetMapping
    public List<Cliente> listarTodos() {
        return clienteService.listarTodos();
    }
}