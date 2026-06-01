# BD6DSM — E-commerce com Replicação MySQL

Atividade da disciplina **Computação em Nuvem 2 — FATEC**.

Aplicação Java/Spring Boot com separação de leitura e escrita entre banco primário e réplica(s) MySQL.

Toda a configuração de conexão fica no arquivo `.env`.

> **Para trocar de ambiente basta alterar esse arquivo — nenhuma linha de código muda.**

---

## O que a aplicação faz

Ao iniciar, a aplicação executa um **loop contínuo** (ciclo a cada 2 segundos) que simula o fluxo de um e-commerce.

| Passo | Operação                                | Banco              |
| ----- | --------------------------------------- | ------------------ |
| 1     | Insere 1 cliente                        | Primário (escrita) |
| 2     | Insere 1 produto aleatório              | Primário (escrita) |
| 3     | Cria 1 pedido com 1–3 itens             | Primário (escrita) |
| 4.1   | Busca o pedido recém-criado por ID      | Réplica (leitura)  |
| 4.2   | Lista os itens do pedido                | Réplica (leitura)  |
| 4.3   | Histórico: últimos 5 pedidos do cliente | Réplica (leitura)  |
| 4.4   | Relatório: COUNT, AVG e SUM de vendas   | Réplica (leitura)  |

O roteamento é automático:

* `@Transactional` → banco primário
* `@Transactional(readOnly = true)` → réplica
* Múltiplas réplicas → distribuição em **round-robin**

---

# Front-end em Tempo Real

A aplicação possui uma interface web que atualiza automaticamente a cada **5 segundos**.

### Funcionalidades

* Dashboard com estatísticas em tempo real

  * Total de pedidos
  * Valor total vendido
  * Ticket médio

* Busca de pedido por ID

  * Cliente
  * Itens
  * Status
  * Valor total

* Histórico de pedidos por cliente

* Produtos com baixo estoque

  * Indicador crítico
  * Indicador baixo
  * Indicador normal

* Visualização completa das tabelas

  * Clientes
  * Produtos
  * Pedidos
  * Itens de pedidos

* Últimos pedidos em tempo real

* Indicador de leituras realizadas na réplica

### Acesso

```text
http://localhost:8080
```

---

## Pré-requisitos

* Java 21+
* Maven 3.9+
* Docker
* Docker Compose

---

## Executando Localmente

Localmente, leitura e escrita apontam para o mesmo banco.

Não é necessário configurar replicação.

### 1. Suba o banco

```bash
docker compose up -d
```

Será iniciado um MySQL 8.0 na porta `3306` com o schema já aplicado.

### 2. Configure o `.env`

```bash
cp .env.example .env
```

Configuração padrão:

```env
DB_PRIMARY_HOST=localhost
DB_PRIMARY_PORT=3306
DB_PRIMARY_DATABASE=aula-db
DB_PRIMARY_USERNAME=root
DB_PRIMARY_PASSWORD=teste

DB_REPLICAS=localhost:3306
DB_REPLICA_DATABASE=aula-db
DB_REPLICA_USERNAME=root
DB_REPLICA_PASSWORD=teste
```

### 3. Execute a aplicação

```bash
mvn spring-boot:run
```

### 4. Acesse o sistema

```text
http://localhost:8080
```

---

## Ambiente da Apresentação

O professor fornecerá os dados do banco hospedado em cloud.

Basta atualizar o arquivo `.env`:

```env
DB_PRIMARY_HOST=<ip-fornecido>
DB_PRIMARY_PORT=<porta>
DB_PRIMARY_DATABASE=aula-db
DB_PRIMARY_USERNAME=<usuario>
DB_PRIMARY_PASSWORD=<senha>

DB_REPLICAS=<ip-replica>:<porta>
DB_REPLICA_DATABASE=aula-db
DB_REPLICA_USERNAME=<usuario>
DB_REPLICA_PASSWORD=<senha>
```

Executar novamente:

```bash
mvn spring-boot:run
```

O front-end continuará funcionando normalmente exibindo os dados do ambiente cloud.

---

## Suporte a N Réplicas

A aplicação suporta qualquer quantidade de réplicas sem necessidade de alterar código.

Exemplo:

```env
DB_REPLICAS=host1:3306,host2:3306,host3:3306
```

As leituras serão distribuídas automaticamente utilizando **round-robin**.

---

## Endpoints REST

Todos os endpoints utilizam leitura na réplica.

| Método | Endpoint                            | Descrição                                    |
| ------ | ----------------------------------- | -------------------------------------------- |
| GET    | `/clientes`                         | Lista todos os clientes                      |
| GET    | `/clientes/{id}/pedidos`            | Lista os pedidos de um cliente               |
| GET    | `/produtos`                         | Lista todos os produtos                      |
| GET    | `/produtos/baixo-estoque?limite=10` | Produtos abaixo do estoque informado         |
| GET    | `/pedidos`                          | Lista todos os pedidos                       |
| GET    | `/pedidos/{id}`                     | Busca detalhes de um pedido                  |
| GET    | `/pedidos/itens`                    | Lista todos os itens de pedidos              |
| GET    | `/relatorios/vendas`                | Total de pedidos, ticket médio e faturamento |

---

## Exemplo de Saída no Terminal

```text
============================================================
  SISTEMA DE REPLICACAO DE BANCO DE DADOS
  Escrita → PRIMARY  |  Leitura → REPLICA(S)
============================================================

────────────────────────────────────────────────────────────
  CICLO 1
────────────────────────────────────────────────────────────

[ROTEAMENTO] Escrita → primary
  [INSERT] Cliente 1 | id=1 | cliente1@email.com

[ROTEAMENTO] Escrita → primary
  [INSERT] Produto: Monitor 1 | id=1 | R$850,00 | estoque=42

[ROTEAMENTO] Escrita → primary
  [INSERT] Pedido id=1 | cliente=Cliente 1 | total=R$850,00 | itens=1

  [LEITURA → REPLICA]

[ROTEAMENTO] Leitura (read-only) → replica-0
  4.1 Pedido 1: cliente=Cliente 1 | total=R$850,00 | status=FINALIZADO

[ROTEAMENTO] Leitura (read-only) → replica-0
  4.2 PedidoItem 1 — Monitor 1 x1  R$850,00

[ROTEAMENTO] Leitura (read-only) → replica-0
  4.3 Últimos 1 pedido(s) do cliente 1:
       Pedido 1 — R$850,00

[ROTEAMENTO] Leitura (read-only) → replica-0
  4.4 Relatório: 1 pedidos | média=R$850,00 | total=R$850,00
```

---

## Tecnologias Utilizadas

* Java 21
* Spring Boot 3.2
* Spring Data JPA
* Spring Web
* HikariCP
* MySQL 8.0
* dotenv-java 3.0
* Docker
* Docker Compose
* HTML
* CSS
* JavaScript

---

## Observações

* O arquivo `.env` não deve ser versionado.
* O `.gitignore` já está configurado para ignorá-lo.
* O campo `criado_por` utiliza o identificador do grupo **BD6DSM** conforme requisito da atividade.
* O front-end atualiza automaticamente a cada 5 segundos.
* Todas as consultas exibidas na interface são realizadas através da camada de leitura (réplica).
