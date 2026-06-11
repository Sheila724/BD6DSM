-- ============================================================
-- 2) USUARIO + PRIVILEGIOS — rodar dentro do MySQL na VPS
--
-- IMPORTANTE: troque 'SUA_SENHA_FORTE' antes de executar!
--
-- Entrar no MySQL:
--   mysql -u root -p
--
-- Ou executar direto no shell da VPS:
--   mysql -u root -p < init/02-usuario-e-privilegios.sql
-- ============================================================

-- Criar usuario com acesso remoto (% = qualquer IP)
CREATE USER IF NOT EXISTS 'bd6dsm'@'%' IDENTIFIED BY 'SUA_SENHA_FORTE';

-- Privilegios completos no database da atividade
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP
  ON `aula-db`.* TO 'bd6dsm'@'%';

-- Aplicar alteracoes
FLUSH PRIVILEGES;

-- Conferir usuario e permissoes
SHOW GRANTS FOR 'bd6dsm'@'%';
