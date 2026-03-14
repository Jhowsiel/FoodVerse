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
-- 5. tb_produtos: garantir coluna restricoes (tags alimentares, ex.: "Sem glúten,Vegano")
-- -------------------------------------------------------------------------
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'restricoes'
)
BEGIN
    ALTER TABLE tb_produtos ADD restricoes VARCHAR(255) NULL;
END
GO

-- ============================================================================
-- Wave 2 — Domain & Operation Consistency
-- ============================================================================

-- -------------------------------------------------------------------------
-- 6. tb_produtos: tipo_produto (VENDA | INSUMO), default VENDA
-- -------------------------------------------------------------------------
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'tipo_produto'
)
BEGIN
    ALTER TABLE tb_produtos ADD tipo_produto VARCHAR(20) NOT NULL DEFAULT 'VENDA';
END
GO

-- Preencher VENDA em linhas existentes que tenham NULL
UPDATE tb_produtos SET tipo_produto = 'VENDA' WHERE tipo_produto IS NULL;
GO

-- -------------------------------------------------------------------------
-- 7. tb_receitas: BOM (Bill of Materials) — prato consome N insumos
-- -------------------------------------------------------------------------
IF NOT EXISTS (
    SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID('tb_receitas') AND type = 'U'
)
BEGIN
    CREATE TABLE tb_receitas (
        ID_receita      INT IDENTITY(1,1) PRIMARY KEY,
        ID_produto_venda INT NOT NULL,
        ID_insumo        INT NOT NULL,
        quantidade       DECIMAL(10,3) NOT NULL DEFAULT 1,
        unidade          VARCHAR(10)  NULL,
        ativo            BIT NOT NULL DEFAULT 1,
        CONSTRAINT FK_receita_produto FOREIGN KEY (ID_produto_venda)
            REFERENCES tb_produtos(ID_produto),
        CONSTRAINT FK_receita_insumo FOREIGN KEY (ID_insumo)
            REFERENCES tb_produtos(ID_produto)
    );
END
GO

-- -------------------------------------------------------------------------
-- 8. tb_personalizacao_grupos + tb_personalizacao_opcoes (schema only)
-- -------------------------------------------------------------------------
IF NOT EXISTS (
    SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID('tb_personalizacao_grupos') AND type = 'U'
)
BEGIN
    CREATE TABLE tb_personalizacao_grupos (
        ID_grupo    INT IDENTITY(1,1) PRIMARY KEY,
        ID_produto  INT NOT NULL,
        nome        VARCHAR(100) NOT NULL,
        obrigatorio BIT NOT NULL DEFAULT 0,
        max_itens   INT NOT NULL DEFAULT 1,
        ativo       BIT NOT NULL DEFAULT 1,
        CONSTRAINT FK_pers_grupo_produto FOREIGN KEY (ID_produto)
            REFERENCES tb_produtos(ID_produto)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID('tb_personalizacao_opcoes') AND type = 'U'
)
BEGIN
    CREATE TABLE tb_personalizacao_opcoes (
        ID_opcao        INT IDENTITY(1,1) PRIMARY KEY,
        ID_grupo        INT NOT NULL,
        nome            VARCHAR(100) NOT NULL,
        preco_adicional DECIMAL(10,2) NOT NULL DEFAULT 0,
        ativo           BIT NOT NULL DEFAULT 1,
        CONSTRAINT FK_pers_opcao_grupo FOREIGN KEY (ID_grupo)
            REFERENCES tb_personalizacao_grupos(ID_grupo)
    );
END
GO

-- -------------------------------------------------------------------------
-- 9. Garantir dados-mestre de tb_status_pedido (IDs fixos)
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

-- ============================================================================
-- Fim da Migration_Sprint1.sql (Wave 2)
-- ============================================================================
