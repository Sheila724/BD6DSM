# Passo a passo — Banco MySQL na VPS

Guia para criar o banco conforme a atividade e conectar a aplicacao **sem usar Docker**.

---

## Visao geral

| Ambiente | Escrita (primary) | Leitura (replica) |
|----------|-------------------|-------------------|
| **Desenvolvimento na VPS** | Seu IP VPS | **Mesmo** IP VPS |
| **Apresentacao (professor)** | IP primario fornecido | IP(s) replica fornecido(s) |

A aplicacao ja suporta os dois cenarios — basta alterar o arquivo `.env`.

---

## Parte 1 — Preparar o MySQL na VPS

### 1. Conectar na VPS via SSH

```bash
ssh usuario@SEU_IP_VPS
```

### 2. Verificar se o MySQL esta instalado

```bash
mysql --version
```

Se nao estiver instalado (Ubuntu/Debian):

```bash
sudo apt update
sudo apt install -y mysql-server
sudo systemctl enable mysql
sudo systemctl start mysql
```

### 3. Permitir conexao remota (da sua maquina local)

Edite o arquivo de configuracao do MySQL:

```bash
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
```

Altere ou confirme:

```ini
bind-address = 0.0.0.0
```

Reinicie o MySQL:

```bash
sudo systemctl restart mysql
```

### 4. Liberar a porta 3306 no firewall da VPS

```bash
sudo ufw allow 3306/tcp
sudo ufw reload
```

> No painel do provedor (Hostinger, DigitalOcean, AWS etc.), libere a porta **3306** no firewall/security group se existir regra externa.

### 5. Criar o banco e as tabelas

Na VPS, envie o projeto ou copie apenas o script:

```bash
# Opcao A: se o repo ja esta na VPS
mysql -u root -p < init/setup-vps.sql

# Opcao B: copiar da sua maquina local
scp init/setup-vps.sql usuario@SEU_IP_VPS:/tmp/
ssh usuario@SEU_IP_VPS "mysql -u root -p < /tmp/setup-vps.sql"
```

O script cria o database **`aula-db`** e as tabelas:

- `cliente`
- `produto`
- `pedido`
- `pedido_item`

### 6. Criar usuario da aplicacao

Entre no MySQL:

```bash
mysql -u root -p
```

Execute (troque a senha):

```sql
CREATE USER IF NOT EXISTS 'bd6dsm'@'%' IDENTIFIED BY 'SUA_SENHA_FORTE';
GRANT ALL PRIVILEGES ON `aula-db`.* TO 'bd6dsm'@'%';
FLUSH PRIVILEGES;
EXIT;
```

### 7. Testar conexao remota (da sua maquina local)

```bash
mysql -h SEU_IP_VPS -P 3306 -u bd6dsm -p aula-db
```

Se conectar, o banco esta pronto.

Comando util para listar tabelas:

```sql
SHOW TABLES;
```

---

## Parte 2 — Configurar a aplicacao

### 1. Criar o arquivo `.env`

Na raiz do projeto:

```bash
cp .env.example .env
```

### 2. Preencher com os dados da VPS

Edite o `.env`:

```env
DB_PRIMARY_HOST=203.0.113.50
DB_PRIMARY_PORT=3306
DB_PRIMARY_DATABASE=aula-db
DB_PRIMARY_USERNAME=bd6dsm
DB_PRIMARY_PASSWORD=SUA_SENHA_FORTE

DB_REPLICAS=203.0.113.50:3306
DB_REPLICA_DATABASE=aula-db
DB_REPLICA_USERNAME=bd6dsm
DB_REPLICA_PASSWORD=SUA_SENHA_FORTE

PORT=8080
```

> **Importante:** em desenvolvimento, `DB_REPLICAS` aponta para o **mesmo IP** da VPS. Escrita e leitura usam o mesmo banco, simulando o cenario local da atividade.

### 3. Executar a aplicacao

```bash
mvn spring-boot:run
```

### 4. Conferir no terminal

Voce deve ver:

```text
[config] Banco primario: 203.0.113.50:3306/aula-db
[config] Replica 0: 203.0.113.50:3306/aula-db
[config] Arquivo .env carregado com sucesso.

[ROTEAMENTO] Escrita → primary
[ROTEAMENTO] Leitura (read-only) → replica-0
```

Acesse opcionalmente: `http://localhost:8080`

---

## Parte 3 — Dia da apresentacao

Quando o professor passar os IPs na hora, **altere somente o `.env`**:

```env
DB_PRIMARY_HOST=IP_PRIMARIO_PROFESSOR
DB_PRIMARY_PORT=3306
DB_PRIMARY_DATABASE=aula-db
DB_PRIMARY_USERNAME=usuario_fornecido
DB_PRIMARY_PASSWORD=senha_fornecida

DB_REPLICAS=IP_REPLICA_1:3306,IP_REPLICA_2:3306
DB_REPLICA_DATABASE=aula-db
DB_REPLICA_USERNAME=usuario_fornecido
DB_REPLICA_PASSWORD=senha_fornecida
```

Reinicie a aplicacao:

```bash
mvn spring-boot:run
```

Nenhuma alteracao de codigo e necessaria.

---

## Parte 4 — Docker (opcional)

O `docker-compose.yml` permanece no repositorio como alternativa local. Para usa-lo em vez da VPS:

```bash
docker compose up -d
```

E configure o `.env` com `DB_PRIMARY_HOST=localhost` e `DB_REPLICAS=localhost:3306`.

---

## Solucao de problemas

| Erro | Possivel causa | Acao |
|------|----------------|------|
| `Connection refused` | MySQL parado ou firewall | Verificar `systemctl status mysql` e porta 3306 |
| `Access denied` | Usuario/senha incorretos | Conferir `.env` e `GRANT` no MySQL |
| `Communications link failure` | IP bloqueado | Liberar firewall VPS + provedor |
| Tabelas nao existem | Script nao executado | Rodar `init/setup-vps.sql` |
| Leitura nao acha pedido recém-criado | Lag de replicacao real | Normal em cloud; local na mesma VPS nao ocorre |

---

## Checklist rapido

- [ ] MySQL 8 rodando na VPS
- [ ] Porta 3306 liberada
- [ ] Database `aula-db` criado
- [ ] Tabelas criadas (`init/setup-vps.sql`)
- [ ] Usuario `bd6dsm` com permissao
- [ ] Conexao remota testada com `mysql -h ...`
- [ ] Arquivo `.env` configurado
- [ ] App sobe com `mvn spring-boot:run`
- [ ] Console mostra ciclos de escrita e leitura
