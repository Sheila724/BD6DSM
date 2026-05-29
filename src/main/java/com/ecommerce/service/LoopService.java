package com.ecommerce.service;

import com.ecommerce.dto.RelatorioVendasDto;
import com.ecommerce.entity.Pedido;
import com.ecommerce.entity.PedidoItem;
import com.ecommerce.entity.Produto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Servico principal que executa o loop infinito de insercoes e consultas.
 *
 * A cada ciclo:
 *   1. Cadastra 1 cliente  (ESCRITA → PRIMARY)
 *   2. Cadastra 1 produto  (ESCRITA → PRIMARY)
 *   3. Cria 1 pedido com 1-3 produtos aleatorios (ESCRITA → PRIMARY)
 *   4. Faz 4 consultas na REPLICA (read-only → replica-N round-robin)
 */
@Service
public class LoopService {

    private static final String[] NOMES_PRODUTO = {
            "Notebook", "Mouse", "Teclado", "Monitor", "Headset",
            "Webcam", "Hub USB", "SSD", "Pendrive", "Carregador",
            "Placa de Video", "Processador", "Memoria RAM", "Fonte ATX"
    };

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private PedidoService pedidoService;

    private final Random random = new Random();
    private final List<Long> clienteIds = new ArrayList<>();
    private final List<Long> produtoIds = new ArrayList<>();
    private int ciclo = 0;

    @EventListener(ApplicationReadyEvent.class)
    public void iniciar() {
        new Thread(this::executarLoop, "loop-principal").start();
    }

    private void executarLoop() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  SISTEMA DE REPLICACAO DE BANCO DE DADOS");
        System.out.println("  Escrita → PRIMARY  |  Leitura → REPLICA(S)");
        System.out.println("=".repeat(60) + "\n");

        while (true) {
            ciclo++;
            System.out.println("\n" + "─".repeat(60));
            System.out.printf("  CICLO %d%n", ciclo);
            System.out.println("─".repeat(60));

            try {
                // ── Passo 1: Cadastrar cliente ──────────────────────────────
                int numCliente = clienteIds.size() + 1;
                var cliente = clienteService.cadastrar(
                        "Cliente " + numCliente,
                        "cliente" + numCliente + "@email.com");
                clienteIds.add(cliente.getId());

                // ── Passo 2: Cadastrar produto ──────────────────────────────
                String nomeProduto = NOMES_PRODUTO[random.nextInt(NOMES_PRODUTO.length)] + " " + ciclo;
                BigDecimal valor = BigDecimal.valueOf(50 + random.nextInt(2000));
                int estoque = 5 + random.nextInt(100);
                Produto produto = produtoService.cadastrar(nomeProduto, valor, estoque);
                produtoIds.add(produto.getId());

                // ── Passo 3: Criar pedido ───────────────────────────────────
                Long clienteEscolhido = clienteIds.get(random.nextInt(clienteIds.size()));
                int numItens = 1 + random.nextInt(Math.min(3, produtoIds.size()));
                List<Long> itensPedido = selecionarAleatorios(produtoIds, numItens);
                Pedido pedido = pedidoService.criarPedido(clienteEscolhido, itensPedido);

                // ── Passo 4: Consultas na REPLICA ───────────────────────────
                System.out.println("\n  [LEITURA → REPLICA]");

                // 4.1 Dados do pedido recém-criado
                pedidoService.buscarPorId(pedido.getId()).ifPresent(p ->
                        System.out.printf("  4.1 Pedido %d: cliente=%s | total=R$%.2f%n",
                                p.getId(), p.getCliente().getNome(), p.getValorTotal()));

                // 4.2 Itens do pedido
                List<PedidoItem> itens = pedidoService.listarItensDoPedido(pedido.getId());
                System.out.printf("  4.2 Itens do pedido %d:%n", pedido.getId());
                itens.forEach(item -> System.out.printf(
                        "       - %-30s x%d  R$%.2f%n",
                        item.getProduto().getDescricao(),
                        item.getQuantidade(),
                        item.getValorUnitario()));

                // 4.3 Ultimos 5 pedidos do cliente
                List<Pedido> ultimos = pedidoService.ultimos5PorCliente(clienteEscolhido);
                System.out.printf("  4.3 Ultimos %d pedido(s) do cliente %d:%n",
                        ultimos.size(), clienteEscolhido);
                ultimos.forEach(p -> System.out.printf(
                        "       Pedido %d — R$%.2f — %s%n",
                        p.getId(), p.getValorTotal(), p.getCriadoEm()));

                // 4.4 Relatorio geral de vendas
                RelatorioVendasDto rel = pedidoService.relatorioVendas();
                System.out.printf("  4.4 Relatorio: %d pedidos | media=R$%.2f | total=R$%.2f%n",
                        rel.getTotalPedidos(), rel.getValorMedio(), rel.getValorTotal());

                Thread.sleep(2_000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[loop] Encerrado.");
                break;
            } catch (Exception e) {
                System.err.printf("[loop] Erro no ciclo %d: %s%n", ciclo, e.getMessage());
                try {
                    Thread.sleep(2_000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private List<Long> selecionarAleatorios(List<Long> fonte, int quantidade) {
        List<Long> copia = new ArrayList<>(fonte);
        Collections.shuffle(copia, random);
        return copia.subList(0, Math.min(quantidade, copia.size()));
    }
}
