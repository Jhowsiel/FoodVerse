-- ============================================================================
-- Migration_Sprint1.sql — FoodVerse Schema Migration (Sprint 1)
-- Migração idempotente: pode ser executada várias vezes sem erros.
-- Adiciona colunas compartilhadas entre Java Swing e Django ao FoodVerseDB.
-- ============================================================================

USE FoodVerseDB;
GO

-- -------------------------------------------------------------------------
-- 1. tb_funcionarios: garantir coluna ID_restaurante (FK)
-- -------------------------------------------------------------------------
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_funcionarios') AND name = 'ID_restaurante'
)
BEGIN
    ALTER TABLE tb_funcionarios ADD ID_restaurante INT NULL;
END
GO

-- Adicionar FK com ON DELETE SET NULL (idempotente)
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_funcionarios_tb_restaurantes'
)
BEGIN
    ALTER TABLE tb_funcionarios
        ADD CONSTRAINT FK_tb_funcionarios_tb_restaurantes
        FOREIGN KEY (ID_restaurante) REFERENCES tb_restaurantes(ID_restaurante) ON DELETE SET NULL;
END
GO

-- -------------------------------------------------------------------------
-- 2. tb_restaurantes: garantir colunas ativo, aberto, imagem e banner
-- -------------------------------------------------------------------------

-- 2a. ativo
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'ativo'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD ativo BIT NOT NULL DEFAULT 1;
END
GO

-- 2b. aberto
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'aberto'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD aberto BIT NOT NULL DEFAULT 1;
END
GO

-- 2c. imagem
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'imagem'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD imagem VARCHAR(255) NULL;
END
GO

-- 2d. banner
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'banner'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD banner VARCHAR(255) NULL;
END
GO

-- -------------------------------------------------------------------------
-- 3. Preencher valores default para ativo/aberto em linhas existentes
-- -------------------------------------------------------------------------
IF EXISTS (SELECT 1 FROM tb_restaurantes WHERE ativo IS NULL OR aberto IS NULL)
BEGIN
    UPDATE tb_restaurantes
    SET ativo = COALESCE(ativo, 1), aberto = COALESCE(aberto, 1);
END
GO

-- Garantir constraints de default (idempotente)
IF NOT EXISTS (
    SELECT 1 FROM sys.default_constraints
    WHERE parent_object_id = OBJECT_ID('tb_restaurantes') AND name = 'DF_tb_restaurantes_ativo'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD CONSTRAINT DF_tb_restaurantes_ativo DEFAULT 1 FOR ativo;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.default_constraints
    WHERE parent_object_id = OBJECT_ID('tb_restaurantes') AND name = 'DF_tb_restaurantes_aberto'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD CONSTRAINT DF_tb_restaurantes_aberto DEFAULT 1 FOR aberto;
END
GO

-- Garantir NOT NULL
ALTER TABLE tb_restaurantes ALTER COLUMN ativo BIT NOT NULL;
GO

ALTER TABLE tb_restaurantes ALTER COLUMN aberto BIT NOT NULL;
GO

-- -------------------------------------------------------------------------
-- 4. tb_produtos: garantir coluna imagem
-- -------------------------------------------------------------------------
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'imagem'
)
BEGIN
    ALTER TABLE tb_produtos ADD imagem VARCHAR(255) NULL;
END
GO

-- -------------------------------------------------------------------------
-- 5. tb_status_pedido: garantir dados mestres de status
--    Ambos os sistemas dependem desses registros:
--    • Django: get_object_or_404(TbStatusPedido, id_status=1)
--    • Java:   SELECT ID_status FROM tb_status_pedido WHERE nome_status = ?
-- -------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 1)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (1, 'pendente');

IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 2)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (2, 'em preparo');

IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 3)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (3, 'pronto');

IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 4)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (4, 'em rota');

IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 5)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (5, 'concluido');

IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 6)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (6, 'cancelado');
GO

-- -------------------------------------------------------------------------
-- 6. tb_pedidos_produtos: garantir coluna preco_unitario
--    Armazena o preço no momento da compra para consistência entre
--    Django (criação do pedido) e Java (leitura dos itens).
-- -------------------------------------------------------------------------
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_pedidos_produtos') AND name = 'preco_unitario'
)
BEGIN
    ALTER TABLE tb_pedidos_produtos ADD preco_unitario DECIMAL(10,2) NULL;
END
GO

-- ============================================================================
-- Fim da Migration_Sprint1.sql
-- ============================================================================
