package com.ecommerce;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        carregarDotEnv();
        SpringApplication.run(EcommerceApplication.class, args);
    }

    /**
     * Carrega o arquivo .env (se existir) e exporta cada variavel como System Property,
     * tornando-as visíveis para o Spring com ${NOME_VAR}.
     * Variaveis de ambiente reais do SO tem prioridade sobre o .env.
     */
    private static void carregarDotEnv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(entry -> {
                if (System.getProperty(entry.getKey()) == null
                        && System.getenv(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
            System.out.println("[config] Arquivo .env carregado com sucesso.");
        } catch (Exception e) {
            System.out.println("[config] Sem arquivo .env — usando variaveis de ambiente do sistema.");
        }
    }
}
