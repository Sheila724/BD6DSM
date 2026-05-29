package com.ecommerce.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * DataSource que roteia automaticamente:
 *   - Transacoes read-only  (@Transactional(readOnly=true)) → replica-0, replica-1, ..., replica-N (round-robin)
 *   - Transacoes de escrita (@Transactional)                → primary
 *
 * O numero de replicas e definido via construtor, permitindo N replicas sem alterar o codigo.
 */
public class ReplicaRoutingDataSource extends AbstractRoutingDataSource {

    private final int totalReplicas;
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    public ReplicaRoutingDataSource(int totalReplicas) {
        this.totalReplicas = totalReplicas;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

        if (isReadOnly && totalReplicas > 0) {
            int index = Math.abs(roundRobinCounter.getAndIncrement() % totalReplicas);
            String chave = "replica-" + index;
            System.out.printf("[ROTEAMENTO] Leitura (read-only) → %s%n", chave);
            return chave;
        }

        System.out.println("[ROTEAMENTO] Escrita → primary");
        return "primary";
    }
}
