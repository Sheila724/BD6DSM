# BD6DSM — E-commerce com Replicação MySQL

**Integrantes:**

* Sheila Alves
* Éllen Dias Farias
* Gabriel Abramovick
* Habbiner Soares de Andrade

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
* MySQL 8.0 (VPS ou ambiente do professor)
* Docker e Docker Compose *(opcional — alternativa local)*

---

## Executando com banco na VPS (recomendado)

Leitura e escrita apontam para o **mesmo banco na VPS** durante o desenvolvimento. Não é necessário configurar replicação real.

### Passo a passo completo

Consulte **[docs/SETUP-VPS.md](docs/SETUP-VPS.md)** — inclui:

1. Instalação/configuração do MySQL na VPS
2. Criação do database `aula-db` e tabelas (`init/setup-vps.sql`)
3. Usuário, firewall e teste de conexão remota
4. Configuração do `.env`
5. O que fazer no dia da apresentação

### Resumo rápido

**1. Na VPS — criar o banco:**

```bash
mysql -u root -p < init/setup-vps.sql
```

**2. Na sua máquina — configurar conexão:**

```bash
cp .env.example .env
```

Edite o `.env` com o IP da VPS:

```env
DB_PRIMARY_HOST=SEU_IP_VPS
DB_PRIMARY_PORT=3306
DB_PRIMARY_DATABASE=aula-db
DB_PRIMARY_USERNAME=bd6dsm
DB_PRIMARY_PASSWORD=SUA_SENHA

DB_REPLICAS=SEU_IP_VPS:3306
DB_REPLICA_DATABASE=aula-db
DB_REPLICA_USERNAME=bd6dsm
DB_REPLICA_PASSWORD=SUA_SENHA
```

**3. Executar a aplicação:**

```bash
mvn spring-boot:run
```

**4. Acessar (opcional):**

```text
http://localhost:8080
```

---

## Alternativa: Docker local (opcional)

Se preferir um MySQL local sem VPS:

```bash
docker compose up -d
cp .env.example .env
```

Configure o `.env` com `DB_PRIMARY_HOST=localhost` e `DB_REPLICAS=localhost:3306`.

---

## Ambiente da Apresentação

O professor fornecerá os IPs na hora. **Altere somente o `.env`:**

```env
DB_PRIMARY_HOST=<ip-primario>
DB_PRIMARY_PORT=3306
DB_PRIMARY_DATABASE=aula-db
DB_PRIMARY_USERNAME=<usuario>
DB_PRIMARY_PASSWORD=<senha>

DB_REPLICAS=<ip-replica-1>:3306,<ip-replica-2>:3306
DB_REPLICA_DATABASE=aula-db
DB_REPLICA_USERNAME=<usuario>
DB_REPLICA_PASSWORD=<senha>
```

Reinicie a aplicação:

```bash
mvn spring-boot:run
```

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
