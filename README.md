# BD6DSM — E-commerce com Replicação MySQL

Atividade da disciplina **Computação em Nuvem 2 — FATEC**.  
Aplicação Java / Spring Boot com separação de leitura e escrita entre banco primário e réplica(s) MySQL.

Toda a configuração de conexão fica no arquivo `.env`.  
**Para trocar de ambiente basta alterar esse arquivo — nenhuma linha de código muda.**

---

## O que a aplicação faz

Ao iniciar, executa um **loop contínuo** (ciclo a cada 2 segundos) que simula o fluxo de um e-commerce:

| Passo | Operação | Banco |
|-------|----------|-------|
| 1 | Insere 1 cliente | **Primário** (escrita) |
| 2 | Insere 1 produto aleatório | **Primário** (escrita) |
| 3 | Cria 1 pedido com 1–3 itens | **Primário** (escrita) |
| 4.1 | Busca o pedido recém-criado por ID | **Réplica** (leitura) |
| 4.2 | Lista os itens do pedido | **Réplica** (leitura) |
| 4.3 | Histórico: últimos 5 pedidos do cliente | **Réplica** (leitura) |
| 4.4 | Relatório: COUNT, AVG e SUM de vendas | **Réplica** (leitura) |

O roteamento é automático: `@Transactional` → primário; `@Transactional(readOnly=true)` → réplica (round-robin quando há mais de uma).

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker e Docker Compose

---

## Executando localmente

Localmente, leitura e escrita apontam para o mesmo banco — não é necessário criar replicação local.

### 1. Suba o banco

```bash
docker compose up -d
```

Isso sobe um MySQL 8.0 na porta `3306` com o schema já aplicado (`init/schema.sql`).

### 2. Configure o `.env`

```bash
cp .env.example .env
```

O `.env.example` já vem pronto para uso local — primary e replica apontam para o mesmo banco:

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

### 3. Execute

```bash
mvn spring-boot:run
```

---

## Na apresentação (ambiente do professor)

O professor fornecerá o IP do banco em cloud na hora da apresentação.  
Basta atualizar o `.env` com os dados recebidos e rodar novamente:

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

```bash
mvn spring-boot:run
```

---

## Suporte a N réplicas

A aplicação suporta qualquer número de réplicas sem alteração de código.  
Basta listar os hosts em `DB_REPLICAS`, separados por vírgula:

```env
DB_REPLICAS=host1:3306,host2:3306,host3:3306
```

As leituras são distribuídas entre elas em **round-robin** automaticamente.

---

## Endpoints REST

Todos os endpoints fazem leitura na réplica.

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/pedidos/{id}` | Detalhes de um pedido |
| GET | `/clientes/{id}/pedidos` | Pedidos de um cliente |
| GET | `/produtos/baixo-estoque?limite=10` | Produtos com estoque abaixo do limite |
| GET | `/relatorios/vendas` | Total de pedidos, valor médio e valor total |

---

## Saída esperada no terminal

```
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

## Tecnologias

- Java 21
- Spring Boot 3.2 (Web, Data JPA, HikariCP)
- MySQL 8.0
- dotenv-java 3.0

---

## Observações

- O arquivo `.env` não deve ser versionado (já está no `.gitignore`).
- O campo `criado_por` nas tabelas usa o nome do grupo, conforme requisito da atividade.
