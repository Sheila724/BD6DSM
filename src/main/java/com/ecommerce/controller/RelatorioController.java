package com.ecommerce.controller;

import com.ecommerce.dto.RelatorioVendasDto;
import com.ecommerce.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private PedidoService pedidoService;

    /**
     * GET /relatorios/vendas
     * Retorna total de pedidos, valor medio e valor total vendido (leitura na REPLICA).
     */
    @GetMapping("/vendas")
    public RelatorioVendasDto relatorioVendas() {
        return pedidoService.relatorioVendas();
    }
}
