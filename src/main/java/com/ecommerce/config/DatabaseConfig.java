package com.ecommerce.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configura o roteamento de DataSource:
 *   - 1 banco primario para escrita
 *   - N replicas para leitura (definidas em DB_REPLICAS como "host:porta,host:porta,...")
 *
 * Para adicionar mais replicas basta alterar a variavel DB_REPLICAS — nenhuma linha de codigo muda.
 *
 * Fluxo:
 *   DataSource (@Primary) = LazyConnectionDataSourceProxy
 *     → ReplicaRoutingDataSource
 *         → "primary"    (HikariDataSource → banco principal)
 *         → "replica-0"  (HikariDataSource → primeira replica)
 *         → "replica-1"  (HikariDataSource → segunda replica)
 *         → ...
 *
 * O LazyConnectionDataSourceProxy e necessario para que o roteamento seja feito APOS o
 * Spring iniciar a transacao (@Transactional), garantindo que isCurrentTransactionReadOnly()
 * retorne o valor correto no momento em que a conexao e adquirida.
 */
@Configuration
public class DatabaseConfig {

    // ── Primário ──────────────────────────────────────────────────────────────
    @Value("${db.primary.host}")
    private String primaryHost;

    @Value("${db.primary.port}")
    private int primaryPort;

    @Value("${db.primary.database}")
    private String primaryDatabase;

    @Value("${db.primary.username}")
    private String primaryUsername;

    @Value("${db.primary.password}")
    private String primaryPassword;

    // ── Réplicas ──────────────────────────────────────────────────────────────
    @Value("${db.replicas}")
    private String replicasConfig;   // ex.: "localhost:3307" ou "h1:3307,h2:3308,h3:3309"

    @Value("${db.replica.database}")
    private String replicaDatabase;

    @Value("${db.replica.username}")
    private String replicaUsername;

    @Value("${db.replica.password}")
    private String replicaPassword;

    // ─────────────────────────────────────────────────────────────────────────

    private DataSource criarHikari(String host, int port, String database,
                                   String username, String password, String poolName) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(String.format(
                "jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo",
                host, port, database));
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setPoolName(poolName);
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(1);
        ds.setConnectionTimeout(30_000);
        return ds;
    }

    private DataSource primaryDataSource() {
        System.out.printf("[config] Banco primario: %s:%d/%s%n", primaryHost, primaryPort, primaryDatabase);
        return criarHikari(primaryHost, primaryPort, primaryDatabase,
                primaryUsername, primaryPassword, "pool-primary");
    }

    private List<DataSource> replicaDataSources() {
        List<DataSource> fontes = new ArrayList<>();
        String[] entradas = replicasConfig.split(",");
        for (int i = 0; i < entradas.length; i++) {
            String entrada = entradas[i].trim();
            String[] partes = entrada.split(":");
            String host = partes[0];
            int port = partes.length > 1 ? Integer.parseInt(partes[1]) : 3306;
            System.out.printf("[config] Replica %d: %s:%d/%s%n", i, host, port, replicaDatabase);
            fontes.add(criarHikari(host, port, replicaDatabase,
                    replicaUsername, replicaPassword, "pool-replica-" + i));
        }
        return fontes;
    }

    @Bean("routingDataSource")
    public DataSource routingDataSource() {
        DataSource primary = primaryDataSource();
        List<DataSource> replicas = replicaDataSources();

        Map<Object, Object> alvos = new HashMap<>();
        alvos.put("primary", primary);
        for (int i = 0; i < replicas.size(); i++) {
            alvos.put("replica-" + i, replicas.get(i));
        }

        System.out.printf("[config] Total de replicas configuradas: %d%n", replicas.size());

        ReplicaRoutingDataSource routing = new ReplicaRoutingDataSource(replicas.size());
        routing.setTargetDataSources(alvos);
        routing.setDefaultTargetDataSource(primary);
        routing.afterPropertiesSet();
        return routing;
    }

    /**
     * Bean @Primary utilizado pelo Spring Boot para JPA e TransactionManager.
     * O LazyConnectionDataSourceProxy adia a obtencao da conexao ate o primeiro SQL,
     * garantindo que o roteamento leia isCurrentTransactionReadOnly() corretamente.
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        return new LazyConnectionDataSourceProxy(routingDataSource());
    }
}
