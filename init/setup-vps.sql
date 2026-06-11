-- ============================================================
-- Setup do banco aula-db na VPS (conforme atividade FATEC)
-- Execute como root no MySQL da VPS:
--   mysql -u root -p < init/setup-vps.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `aula-db`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `aula-db`;

-- Tabela: cliente
CREATE TABLE IF NOT EXISTS cliente (
    id INT AUTO_INCREMENT,
    nome VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL,
    criado_em DATETIME DEFAULT NOW(),
    criado_por VARCHAR(30) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE(email)
);

-- Tabela: produto
CREATE TABLE IF NOT EXISTS produto (
    id INT AUTO_INCREMENT,
    descricao VARCHAR(80) NOT NULL,
    categoria VARCHAR(30) NOT NULL,
    valor NUMERIC(15,2) NOT NULL,
    estoque INT NOT NULL,
    criado_em DATETIME DEFAULT NOW(),
    criado_por VARCHAR(30) NOT NULL,
    PRIMARY KEY(id)
);

-- Tabela: pedido
CREATE TABLE IF NOT EXISTS pedido (
    id INT AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    valor_total NUMERIC(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    criado_em DATETIME DEFAULT NOW(),
    criado_por VARCHAR(30) NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(cliente_id) REFERENCES cliente(id)
);

-- Tabela: pedido_item
CREATE TABLE IF NOT EXISTS pedido_item (
    id INT AUTO_INCREMENT,
    pedido_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL,
    valor_unitario NUMERIC(15,2) NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(pedido_id) REFERENCES pedido(id),
    FOREIGN KEY(produto_id) REFERENCES produto(id)
);

-- ============================================================
-- Usuario da aplicacao (ajuste usuario e senha antes de executar)
-- Descomente e edite as linhas abaixo:
-- ============================================================
-- CREATE USER IF NOT EXISTS 'bd6dsm'@'%' IDENTIFIED BY 'SUA_SENHA_FORTE';
-- GRANT ALL PRIVILEGES ON `aula-db`.* TO 'bd6dsm'@'%';
-- FLUSH PRIVILEGES;
