package com.ecommerce.service;

import com.ecommerce.dto.RelatorioVendasDto;
import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PedidoItemRepository pedidoItemRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    /**
     * Escrita — vai para o banco PRIMARIO.
     * Cria um pedido com os produtos informados (quantidade 1 de cada).
     */
    @Transactional
    public Pedido criarPedido(Long clienteId, List<Long> produtoIds) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente nao encontrado: " + clienteId));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);

        List<PedidoItem> itens = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (Long produtoId : produtoIds) {
            Produto produto = produtoRepository.findById(produtoId)
                    .orElseThrow(() -> new RuntimeException("Produto nao encontrado: " + produtoId));

            PedidoItem item = new PedidoItem();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(1);
            item.setValorUnitario(produto.getValor());

            itens.add(item);
            valorTotal = valorTotal.add(produto.getValor());
        }

        pedido.setValorTotal(valorTotal);
        pedido.setItens(itens);

        Pedido salvo = pedidoRepository.save(pedido);
        System.out.printf("[ESCRITA → PRIMARY] Pedido criado: id=%d | cliente=%s | total=R$%.2f | itens=%d%n",
                salvo.getId(), cliente.getNome(), salvo.getValorTotal(), salvo.getItens().size());
        return salvo;
    }

    /** Leitura — vai para a REPLICA. */
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PedidoItem> listarItensDoPedido(Long pedidoId) {
        return pedidoItemRepository.findByPedidoId(pedidoId);
    }

    @Transactional(readOnly = true)
    public List<Pedido> ultimos5PorCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdPaged(clienteId, PageRequest.of(0, 5));
    }

    @Transactional(readOnly = true)
    public RelatorioVendasDto relatorioVendas() {
        List<Object[]> resultados = pedidoRepository.findRelatorioVendas();
        if (resultados.isEmpty()) {
            return new RelatorioVendasDto(0L, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        Object[] r = resultados.get(0);
        long total  = r[0] != null ? ((Number) r[0]).longValue() : 0L;
        BigDecimal media = r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO;
        BigDecimal soma  = r[2] != null ? new BigDecimal(r[2].toString()) : BigDecimal.ZERO;
        return new RelatorioVendasDto(total, media, soma);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PedidoItem> listarTodosItens() {
        return pedidoItemRepository.findAll();
    }
}
