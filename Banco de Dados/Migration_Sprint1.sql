-- =============================================================================
-- FoodVerse — Migration Sprint 1
-- Migração idempotente: adiciona colunas, constraints e tabelas auxiliares.
-- Compatível com SQL Server 2019+.
-- Execute uma vez por ambiente; re-executar não causa erros.
-- =============================================================================

-- =============================================================================
-- WAVE 1 — Tabelas base
-- =============================================================================

-- tb_restaurantes
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_restaurantes')
BEGIN
    CREATE TABLE tb_restaurantes (
        ID_restaurante  INT          NOT NULL IDENTITY(1,1) PRIMARY KEY,
        nome            VARCHAR(150) NOT NULL,
        categoria       VARCHAR(100) NULL,
        descricao       VARCHAR(500) NULL,
        tempo_entrega   VARCHAR(50)  NULL,
        taxa_entrega    DECIMAL(6,2) NULL
    );
END;

-- tb_clientes
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_clientes')
BEGIN
    CREATE TABLE tb_clientes (
        id_cliente  INT          NOT NULL IDENTITY(1,1) PRIMARY KEY,
        username    VARCHAR(150) NOT NULL,
        email       VARCHAR(254) NULL,
        telefone    VARCHAR(20)  NULL,
        senha       VARCHAR(128) NULL
    );
END;

-- tb_funcionarios
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_funcionarios')
BEGIN
    CREATE TABLE tb_funcionarios (
        ID_funcionario  INT          NOT NULL IDENTITY(1,1) PRIMARY KEY,
        nome            VARCHAR(150) NOT NULL,
        username        VARCHAR(150) NOT NULL UNIQUE,
        senha           VARCHAR(128) NOT NULL,
        cargo           VARCHAR(50)  NOT NULL,
        email           VARCHAR(254) NULL,
        status          VARCHAR(20)  NOT NULL DEFAULT 'ativo'
    );
END;

-- tb_produtos
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_produtos')
BEGIN
    CREATE TABLE tb_produtos (
        ID_produto      INT            NOT NULL IDENTITY(1,1) PRIMARY KEY,
        ID_restaurante  INT            NULL,
        nome_produto    VARCHAR(200)   NOT NULL,
        categoria       VARCHAR(100)   NULL,
        descricao       VARCHAR(500)   NULL,
        preco           DECIMAL(10,2)  NOT NULL DEFAULT 0,
        disponivel      BIT            NOT NULL DEFAULT 1,
        tempo_preparo   INT            NULL DEFAULT 0
    );
END;

-- tb_estoque
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_estoque')
BEGIN
    CREATE TABLE tb_estoque (
        ID_estoque      INT            NOT NULL IDENTITY(1,1) PRIMARY KEY,
        ID_produto      INT            NULL,
        ID_restaurante  INT            NULL,
        quantidade      DECIMAL(12,3)  NOT NULL DEFAULT 0,
        estoque_minimo  DECIMAL(12,3)  NOT NULL DEFAULT 0
    );
END;

-- tb_status_pedido
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_status_pedido')
BEGIN
    CREATE TABLE tb_status_pedido (
        ID_status   INT          NOT NULL PRIMARY KEY,
        nome_status VARCHAR(50)  NOT NULL UNIQUE
    );
END;

-- tb_pedidos
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_pedidos')
BEGIN
    CREATE TABLE tb_pedidos (
        ID_pedido         INT            NOT NULL IDENTITY(1,1) PRIMARY KEY,
        ID_restaurante    INT            NULL,
        ID_cliente        INT            NULL,
        status_id         INT            NOT NULL DEFAULT 1,
        valor_total       DECIMAL(10,2)  NOT NULL DEFAULT 0,
        data_pedido       DATETIME       NOT NULL DEFAULT GETDATE(),
        endereco_entrega  VARCHAR(500)   NULL,
        mesa              VARCHAR(30)    NULL,
        observacao        VARCHAR(500)   NULL
    );
END;

-- tb_pedidos_produtos
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_pedidos_produtos')
BEGIN
    CREATE TABLE tb_pedidos_produtos (
        ID_pedido_produto  INT            NOT NULL IDENTITY(1,1) PRIMARY KEY,
        ID_pedido          INT            NOT NULL,
        ID_produto         INT            NOT NULL,
        quantidade         INT            NOT NULL DEFAULT 1,
        preco_unitario     DECIMAL(10,2)  NULL
    );
END;

-- tb_pagamentos
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_pagamentos')
BEGIN
    CREATE TABLE tb_pagamentos (
        ID_pagamento        INT            NOT NULL IDENTITY(1,1) PRIMARY KEY,
        ID_pedido           INT            NOT NULL,
        metodo_pagamento    VARCHAR(50)    NULL,
        valor               DECIMAL(10,2)  NULL
    );
END;

-- tb_reservas
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_reservas')
BEGIN
    CREATE TABLE tb_reservas (
        ID_reserva      INT      NOT NULL IDENTITY(1,1) PRIMARY KEY,
        ID_cliente      INT      NULL,
        ID_restaurante  INT      NULL,
        data_reserva    DATETIME NOT NULL,
        numero_pessoas  INT      NOT NULL DEFAULT 1,
        mesa            VARCHAR(30) NULL,
        status          VARCHAR(20) NOT NULL DEFAULT 'RESERVADA'
    );
END;

-- =============================================================================
-- WAVE 1 — Colunas adicionais em tabelas existentes
-- =============================================================================

-- tb_funcionarios.ID_restaurante
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_funcionarios') AND name = 'ID_restaurante'
)
BEGIN
    ALTER TABLE tb_funcionarios ADD ID_restaurante INT NULL;
END;

-- tb_restaurantes.ativo
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'ativo'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD ativo BIT NOT NULL DEFAULT 1;
END;

-- tb_restaurantes.aberto
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'aberto'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD aberto BIT NOT NULL DEFAULT 1;
END;

-- tb_restaurantes.imagem
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'imagem'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD imagem VARCHAR(255) NULL;
END;

-- tb_restaurantes.banner
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'banner'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD banner VARCHAR(255) NULL;
END;

-- tb_produtos.imagem
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'imagem'
)
BEGIN
    ALTER TABLE tb_produtos ADD imagem VARCHAR(255) NULL;
END;

-- tb_produtos.restricoes
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'restricoes'
)
BEGIN
    ALTER TABLE tb_produtos ADD restricoes VARCHAR(255) NULL;
END;

-- =============================================================================
-- WAVE 1 — Normalização dos campos ativo/aberto (dados anteriores podem ser NULL)
-- =============================================================================

IF EXISTS (
    SELECT 1 FROM tb_restaurantes WHERE ativo IS NULL OR aberto IS NULL
)
BEGIN
    UPDATE tb_restaurantes
    SET ativo = COALESCE(ativo, 1) , aberto = COALESCE(aberto, 1)
    WHERE ativo IS NULL OR aberto IS NULL;
END;

-- Garantir NOT NULL após normalização
IF EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'ativo' AND is_nullable = 1
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM sys.default_constraints WHERE name = 'DF_tb_restaurantes_ativo')
    BEGIN
        ALTER TABLE tb_restaurantes ADD CONSTRAINT DF_tb_restaurantes_ativo DEFAULT 1 FOR ativo;
    END;
    ALTER TABLE tb_restaurantes ALTER COLUMN ativo BIT NOT NULL;
END;

IF EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'aberto' AND is_nullable = 1
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM sys.default_constraints WHERE name = 'DF_tb_restaurantes_aberto')
    BEGIN
        ALTER TABLE tb_restaurantes ADD CONSTRAINT DF_tb_restaurantes_aberto DEFAULT 1 FOR aberto;
    END;
    ALTER TABLE tb_restaurantes ALTER COLUMN aberto BIT NOT NULL;
END;

-- =============================================================================
-- WAVE 1 — FK: tb_funcionarios → tb_restaurantes
-- =============================================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_funcionarios_tb_restaurantes'
)
BEGIN
    ALTER TABLE tb_funcionarios
    ADD CONSTRAINT FK_tb_funcionarios_tb_restaurantes FOREIGN KEY (ID_restaurante)
    REFERENCES tb_restaurantes (ID_restaurante) ON DELETE SET NULL;
END;

-- =============================================================================
-- WAVE 2 — tipo_produto, receitas e personalização
-- =============================================================================

-- tb_produtos.tipo_produto
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'tipo_produto'
)
BEGIN
    ALTER TABLE tb_produtos ADD tipo_produto VARCHAR(20) NOT NULL DEFAULT 'VENDA';
END;


-- tb_receitas (Bill of Materials: receita de pratos → insumos)
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_receitas')
BEGIN
    CREATE TABLE tb_receitas (
        ID_receita        INT            NOT NULL IDENTITY(1,1) PRIMARY KEY,
        ID_produto_venda  INT            NOT NULL,
        ID_insumo         INT            NOT NULL,
        quantidade        DECIMAL(12,4)  NOT NULL DEFAULT 1,
        unidade           VARCHAR(10)    NOT NULL DEFAULT 'un',
        ativo             BIT            NOT NULL DEFAULT 1,
        CONSTRAINT FK_receitas_produto_venda FOREIGN KEY (ID_produto_venda)
            REFERENCES tb_produtos (ID_produto) ON DELETE NO ACTION,
        CONSTRAINT FK_receitas_insumo FOREIGN KEY (ID_insumo)
            REFERENCES tb_produtos (ID_produto) ON DELETE NO ACTION,
        CONSTRAINT UQ_receita_produto_insumo UNIQUE (ID_produto_venda, ID_insumo)
    );
END;

-- tb_personalizacao_grupos (grupos de personalização de prato, ex: "Adicionais", "Remoções")
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_personalizacao_grupos')
BEGIN
    CREATE TABLE tb_personalizacao_grupos (
        ID_grupo        INT          NOT NULL IDENTITY(1,1) PRIMARY KEY,
        ID_produto      INT          NOT NULL,
        nome_grupo      VARCHAR(100) NOT NULL,
        obrigatorio     BIT          NOT NULL DEFAULT 0,
        multiplo        BIT          NOT NULL DEFAULT 1,
        min_escolhas    INT          NOT NULL DEFAULT 0,
        max_escolhas    INT          NULL,
        ativo           BIT          NOT NULL DEFAULT 1,
        CONSTRAINT FK_pers_grupo_produto FOREIGN KEY (ID_produto)
            REFERENCES tb_produtos (ID_produto) ON DELETE CASCADE
    );
END;

-- tb_personalizacao_opcoes (opções de cada grupo)
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tb_personalizacao_opcoes')
BEGIN
    CREATE TABLE tb_personalizacao_opcoes (
        ID_opcao         INT            NOT NULL IDENTITY(1,1) PRIMARY KEY,
        ID_grupo         INT            NOT NULL,
        nome_opcao       VARCHAR(150)   NOT NULL,
        preco_adicional  DECIMAL(8,2)   NOT NULL DEFAULT 0,
        ativo            BIT            NOT NULL DEFAULT 1,
        CONSTRAINT FK_pers_opcao_grupo FOREIGN KEY (ID_grupo)
            REFERENCES tb_personalizacao_grupos (ID_grupo) ON DELETE CASCADE
    );
END;

-- =============================================================================
-- WAVE 2 — Dados mestres de status de pedido
-- =============================================================================

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

-- =============================================================================
-- FIM DA MIGRAÇÃO
-- =============================================================================
