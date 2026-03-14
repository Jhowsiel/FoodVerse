-- ============================================================
-- Migration_Sprint1.sql
-- Migração incremental e idempotente — FoodVerse
-- Sprint 1: Suporte multi-restaurante, imagens, controle de
--           estoque por tipo e personalização de pratos.
-- Execute no SQL Server Management Studio ou via sqlcmd.
-- ============================================================

-- ============================================================
-- WAVE 1: Campos compartilhados e suporte multi-restaurante
-- ============================================================

-- 1. Vincular funcionários ao restaurante (NULL = admin global)
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_funcionarios') AND name = 'ID_restaurante')
    ALTER TABLE tb_funcionarios ADD ID_restaurante INT NULL;
GO

-- 2. Chave estrangeira (restaurante pode ser excluído sem perder funcionário)
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_funcionarios_tb_restaurantes')
    ALTER TABLE tb_funcionarios ADD CONSTRAINT FK_tb_funcionarios_tb_restaurantes FOREIGN KEY (ID_restaurante) REFERENCES tb_restaurantes (ID_restaurante) ON DELETE SET NULL;
GO

-- 3. Flag de restaurante ativo
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'ativo')
    ALTER TABLE tb_restaurantes ADD ativo BIT NOT NULL DEFAULT 1;
GO

-- 4. Flag de restaurante aberto/fechado
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'aberto')
    ALTER TABLE tb_restaurantes ADD aberto BIT NOT NULL DEFAULT 1;
GO

-- 5. Preencher NULLs existentes antes de reforçar NOT NULL
IF EXISTS (SELECT 1 FROM tb_restaurantes WHERE ativo IS NULL OR aberto IS NULL)
BEGIN
    UPDATE tb_restaurantes
    SET ativo = COALESCE(ativo, 1), aberto = COALESCE(aberto, 1)
    WHERE ativo IS NULL OR aberto IS NULL;
END
GO

-- 6. Reforçar NOT NULL após backfill
ALTER TABLE tb_restaurantes ALTER COLUMN ativo BIT NOT NULL;
GO
ALTER TABLE tb_restaurantes ALTER COLUMN aberto BIT NOT NULL;
GO

-- 7. Constraints DEFAULT para novas linhas
IF NOT EXISTS (SELECT 1 FROM sys.default_constraints WHERE name = 'DF_tb_restaurantes_ativo')
    ALTER TABLE tb_restaurantes ADD CONSTRAINT DF_tb_restaurantes_ativo DEFAULT 1 FOR ativo;
GO
IF NOT EXISTS (SELECT 1 FROM sys.default_constraints WHERE name = 'DF_tb_restaurantes_aberto')
    ALTER TABLE tb_restaurantes ADD CONSTRAINT DF_tb_restaurantes_aberto DEFAULT 1 FOR aberto;
GO

-- 8. Imagem de perfil do restaurante
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'imagem')
    ALTER TABLE tb_restaurantes ADD imagem VARCHAR(255) NULL;
GO

-- 9. Banner do restaurante
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'banner')
    ALTER TABLE tb_restaurantes ADD banner VARCHAR(255) NULL;
GO

-- 10. Imagem do produto/prato
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'imagem')
    ALTER TABLE tb_produtos ADD imagem VARCHAR(255) NULL;
GO

-- 11. Restrições alimentares do produto (ex.: "sem glúten, vegano")
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'restricoes')
    ALTER TABLE tb_produtos ADD restricoes VARCHAR(255) NULL;
GO

-- ============================================================
-- WAVE 2: Separação de insumos, receitas e personalização
-- ============================================================

-- 12. Tipo de produto: 'VENDA' (cardápio cliente) ou 'INSUMO' (estoque interno)
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'tipo_produto')
    ALTER TABLE tb_produtos ADD tipo_produto VARCHAR(20) NOT NULL DEFAULT 'VENDA';
GO

-- 13. Tabela de receitas (BOM — liga prato de venda aos insumos utilizados)
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'tb_receitas')
    CREATE TABLE tb_receitas (
        ID_receita      INT           IDENTITY(1,1) NOT NULL,
        ID_produto_venda INT          NOT NULL,
        ID_insumo       INT           NOT NULL,
        quantidade      DECIMAL(10,3) NOT NULL DEFAULT 1,
        unidade         VARCHAR(20)   NULL,
        CONSTRAINT PK_tb_receitas PRIMARY KEY (ID_receita),
        CONSTRAINT FK_tb_receitas_produto FOREIGN KEY (ID_produto_venda)
            REFERENCES tb_produtos (ID_produto) ON DELETE CASCADE,
        CONSTRAINT FK_tb_receitas_insumo  FOREIGN KEY (ID_insumo)
            REFERENCES tb_produtos (ID_produto)
    );
GO

-- 14. Grupos de personalização (ex.: "Ponto da carne", "Adicionais")
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'tb_personalizacao_grupos')
    CREATE TABLE tb_personalizacao_grupos (
        ID_grupo        INT          IDENTITY(1,1) NOT NULL,
        ID_produto      INT          NOT NULL,
        nome_grupo      VARCHAR(100) NOT NULL,
        obrigatorio     BIT          NOT NULL DEFAULT 0,
        multipla_escolha BIT         NOT NULL DEFAULT 0,
        CONSTRAINT PK_tb_personalizacao_grupos PRIMARY KEY (ID_grupo),
        CONSTRAINT FK_tb_personalizacao_grupos_produto FOREIGN KEY (ID_produto)
            REFERENCES tb_produtos (ID_produto) ON DELETE CASCADE
    );
GO

-- 15. Opções dentro de cada grupo de personalização
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'tb_personalizacao_opcoes')
    CREATE TABLE tb_personalizacao_opcoes (
        ID_opcao        INT          IDENTITY(1,1) NOT NULL,
        ID_grupo        INT          NOT NULL,
        nome_opcao      VARCHAR(100) NOT NULL,
        preco_adicional DECIMAL(10,2) NOT NULL DEFAULT 0.00,
        disponivel      BIT          NOT NULL DEFAULT 1,
        CONSTRAINT PK_tb_personalizacao_opcoes PRIMARY KEY (ID_opcao),
        CONSTRAINT FK_tb_personalizacao_opcoes_grupo FOREIGN KEY (ID_grupo)
            REFERENCES tb_personalizacao_grupos (ID_grupo) ON DELETE CASCADE
    );
GO

-- 16. Tabela de informações nutricionais por produto
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'tb_nutricao')
    CREATE TABLE tb_nutricao (
        ID_nutricao INT           IDENTITY(1,1) NOT NULL,
        ID_produto  INT           NOT NULL,
        kcal        DECIMAL(8,2)  NULL,
        proteina    DECIMAL(8,2)  NULL,
        carbo       DECIMAL(8,2)  NULL,
        gordura     DECIMAL(8,2)  NULL,
        CONSTRAINT PK_tb_nutricao PRIMARY KEY (ID_nutricao),
        CONSTRAINT FK_tb_nutricao_produto FOREIGN KEY (ID_produto)
            REFERENCES tb_produtos (ID_produto) ON DELETE CASCADE
    );
GO

-- ============================================================
-- WAVE 2: Dados essenciais de status de pedido
-- ============================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'tb_status_pedido')
    CREATE TABLE tb_status_pedido (
        ID_status   INT         NOT NULL,
        nome_status VARCHAR(50) NOT NULL,
        CONSTRAINT PK_tb_status_pedido PRIMARY KEY (ID_status)
    );
GO

IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 1)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (1, 'pendente');
GO
IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 2)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (2, 'em preparo');
GO
IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 3)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (3, 'pronto');
GO
IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 4)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (4, 'em rota');
GO
IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 5)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (5, 'concluido');
GO
IF NOT EXISTS (SELECT 1 FROM tb_status_pedido WHERE ID_status = 6)
    INSERT INTO tb_status_pedido (ID_status, nome_status) VALUES (6, 'cancelado');
GO
