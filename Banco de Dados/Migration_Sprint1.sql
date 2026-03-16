-- ============================================================
-- FoodVerse — Migration Sprint 1  (idempotente, T-SQL)
-- Banco: FoodVerseDB  |  SQL Server
-- Executar conectado ao banco FoodVerseDB
-- ============================================================

USE FoodVerseDB;
GO

-- ============================================================
-- WAVE 0: Criação das tabelas base (se ainda não existirem)
-- ============================================================

-- Tabela: tb_clientes
IF OBJECT_ID('dbo.tb_clientes', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_clientes (
        id_cliente      INT           IDENTITY(1,1) PRIMARY KEY,
        username        VARCHAR(50)   NULL,
        nome            VARCHAR(100)  NULL,
        email           VARCHAR(100)  NULL,
        telefone        VARCHAR(20)   NULL,
        cpf             VARCHAR(14)   NULL,
        senha           VARCHAR(255)  NULL,
        endereco        VARCHAR(255)  NULL,
        data_cadastro   DATETIME      NULL
    );
END
GO

-- Tabela: tb_restaurantes
IF OBJECT_ID('dbo.tb_restaurantes', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_restaurantes (
        ID_restaurante  INT           IDENTITY(1,1) PRIMARY KEY,
        nome            VARCHAR(100)  NULL,
        categoria       VARCHAR(50)   NULL,
        descricao       VARCHAR(255)  NULL,
        avaliacao       DECIMAL(3,2)  NULL,
        tempo_entrega   VARCHAR(20)   NULL,
        taxa_entrega    DECIMAL(10,2) NULL,
        cupom           VARCHAR(50)   NULL
    );
END
GO

-- Tabela: tb_funcionarios
IF OBJECT_ID('dbo.tb_funcionarios', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_funcionarios (
        ID_funcionario  INT           IDENTITY(1,1) PRIMARY KEY,
        nome            VARCHAR(100)  NULL,
        username        VARCHAR(50)   NULL,
        email           VARCHAR(100)  NULL,
        cargo           VARCHAR(50)   NULL,
        telefone        VARCHAR(20)   NULL,
        senha           VARCHAR(255)  NULL,
        status          VARCHAR(20)   NULL,
        data_cadastro   DATETIME      NULL
    );
END
GO

-- Tabela: tb_status_pedido
IF OBJECT_ID('dbo.tb_status_pedido', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_status_pedido (
        ID_status   INT          PRIMARY KEY,
        nome_status VARCHAR(50)  NULL
    );
END
GO

-- Tabela: tb_produtos
IF OBJECT_ID('dbo.tb_produtos', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_produtos (
        ID_produto    INT           IDENTITY(1,1) PRIMARY KEY,
        nome_produto  VARCHAR(100)  NULL,
        descricao     VARCHAR(255)  NULL,
        preco         DECIMAL(10,2) NULL,
        categoria     VARCHAR(50)   NULL,
        tempo_preparo INT           NULL,
        disponivel    BIT           NULL,
        destaque      BIT           NULL,
        data_criacao  DATETIME      NULL
    );
END
GO

-- Tabela: tb_estoque
IF OBJECT_ID('dbo.tb_estoque', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_estoque (
        ID_estoque         INT      IDENTITY(1,1) PRIMARY KEY,
        ID_produto         INT      NOT NULL,
        quantidade         INT      NULL,
        estoque_minimo     INT      NULL,
        ultima_atualizacao DATETIME NULL
    );
END
GO

-- Tabela: tb_nutricao
IF OBJECT_ID('dbo.tb_nutricao', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_nutricao (
        ID_nutricao INT         IDENTITY(1,1) PRIMARY KEY,
        ID_produto  INT         NOT NULL,
        kcal        INT         NULL,
        proteina    VARCHAR(20) NULL,
        carbo       VARCHAR(20) NULL,
        gordura     VARCHAR(20) NULL
    );
END
GO

-- Tabela: tb_pedidos
IF OBJECT_ID('dbo.tb_pedidos', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_pedidos (
        ID_pedido        INT           IDENTITY(1,1) PRIMARY KEY,
        ID_cliente       INT           NULL,
        ID_restaurante   INT           NULL,
        status_id        INT           NULL,
        data_pedido      DATETIME      NULL,
        endereco_entrega VARCHAR(255)  NULL,
        valor_total      DECIMAL(10,2) NULL
    );
END
GO

-- Tabela: tb_pedidos_produtos
IF OBJECT_ID('dbo.tb_pedidos_produtos', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_pedidos_produtos (
        ID_pedido      INT           NOT NULL,
        ID_produto     INT           NOT NULL,
        quantidade     INT           NULL,
        preco_unitario DECIMAL(10,2) NULL,
        CONSTRAINT PK_tb_pedidos_produtos PRIMARY KEY (ID_pedido, ID_produto)
    );
END
GO

-- Tabela: tb_reservas
IF OBJECT_ID('dbo.tb_reservas', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_reservas (
        ID_reserva     INT         IDENTITY(1,1) PRIMARY KEY,
        ID_cliente     INT         NULL,
        ID_restaurante INT         NULL,
        data_reserva   DATETIME    NULL,
        numero_pessoas INT         NULL,
        mesa           VARCHAR(10) NULL
    );
END
GO

-- Tabela: tb_pagamentos
IF OBJECT_ID('dbo.tb_pagamentos', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_pagamentos (
        ID_pagamento     INT           IDENTITY(1,1) PRIMARY KEY,
        ID_pedido        INT           NULL,
        metodo_pagamento VARCHAR(50)   NULL,
        valor            DECIMAL(10,2) NULL,
        data_pagamento   DATETIME      NULL
    );
END
GO

-- Tabela: tb_avaliacoes
IF OBJECT_ID('dbo.tb_avaliacoes', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_avaliacoes (
        ID_avaliacao   INT          IDENTITY(1,1) PRIMARY KEY,
        ID_cliente     INT          NULL,
        ID_restaurante INT          NULL,
        comentario     VARCHAR(255) NULL,
        nota           INT          NULL,
        data_avaliacao DATETIME     NULL
    );
END
GO

-- Tabela: tb_cupons
IF OBJECT_ID('dbo.tb_cupons', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_cupons (
        ID_cupom  INT          IDENTITY(1,1) PRIMARY KEY,
        codigo    VARCHAR(50)  NULL,
        desconto  DECIMAL(5,2) NULL,
        validade  DATE         NULL
    );
END
GO

-- Tabela: tb_fidelidade
IF OBJECT_ID('dbo.tb_fidelidade', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_fidelidade (
        ID_fidelidade INT           IDENTITY(1,1) PRIMARY KEY,
        ID_cliente    INT           NULL,
        pontos        INT           NULL,
        cashback      DECIMAL(10,2) NULL
    );
END
GO

-- Tabela: tb_mesas
IF OBJECT_ID('dbo.tb_mesas', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tb_mesas (
        ID_mesa        INT           IDENTITY(1,1) PRIMARY KEY,
        ID_restaurante INT           NOT NULL,
        nome           NVARCHAR(50)  NOT NULL,
        capacidade     INT           NOT NULL DEFAULT 4,
        ativa          BIT           NOT NULL DEFAULT 1
    );
    CREATE UNIQUE INDEX UX_tb_mesas_rest_nome ON dbo.tb_mesas (ID_restaurante, nome);
END
GO

-- ============================================================
-- WAVE 1: Adição de colunas (idempotente — Guard: sys.columns)
-- ============================================================

-- tb_restaurantes: imagem
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'imagem')
BEGIN
    ALTER TABLE tb_restaurantes ADD imagem VARCHAR(255) NULL;
END
GO

-- tb_restaurantes: banner
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'banner')
BEGIN
    ALTER TABLE tb_restaurantes ADD banner VARCHAR(255) NULL;
END
GO

-- tb_restaurantes: ativo
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'ativo')
BEGIN
    ALTER TABLE tb_restaurantes ADD ativo BIT NOT NULL DEFAULT 1;
END
GO

-- tb_restaurantes: aberto
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'aberto')
BEGIN
    ALTER TABLE tb_restaurantes ADD aberto BIT NOT NULL DEFAULT 1;
END
GO

-- Backfill para linhas anteriores à adição das colunas ativo/aberto
IF EXISTS ( SELECT 1 FROM tb_restaurantes WHERE ativo IS NULL OR aberto IS NULL )
BEGIN
    UPDATE tb_restaurantes
    SET ativo  = COALESCE(ativo, 1) , aberto = COALESCE(aberto, 1)
    WHERE ativo IS NULL OR aberto IS NULL;
END
GO

-- DEFAULT constraint: ativo
IF NOT EXISTS (SELECT 1 FROM sys.default_constraints WHERE name = 'DF_tb_restaurantes_ativo')
BEGIN
    ALTER TABLE tb_restaurantes ADD CONSTRAINT DF_tb_restaurantes_ativo DEFAULT 1 FOR ativo;
END
GO

-- DEFAULT constraint: aberto
IF NOT EXISTS (SELECT 1 FROM sys.default_constraints WHERE name = 'DF_tb_restaurantes_aberto')
BEGIN
    ALTER TABLE tb_restaurantes ADD CONSTRAINT DF_tb_restaurantes_aberto DEFAULT 1 FOR aberto;
END
GO

-- Forçar NOT NULL após backfill
ALTER TABLE tb_restaurantes ALTER COLUMN ativo BIT NOT NULL;
GO
ALTER TABLE tb_restaurantes ALTER COLUMN aberto BIT NOT NULL;
GO

-- tb_produtos: imagem
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'imagem')
BEGIN
    ALTER TABLE tb_produtos ADD imagem VARCHAR(255) NULL;
END
GO

-- tb_produtos: restricoes
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'restricoes')
BEGIN
    ALTER TABLE tb_produtos ADD restricoes VARCHAR(255) NULL;
END
GO

-- tb_funcionarios: ID_restaurante
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_funcionarios') AND name = 'ID_restaurante')
BEGIN
    ALTER TABLE tb_funcionarios ADD ID_restaurante INT NULL;
END
GO

-- tb_produtos: ID_restaurante
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'ID_restaurante')
BEGIN
    ALTER TABLE tb_produtos ADD ID_restaurante INT NULL;
END
GO

-- tb_pedidos: ID_restaurante
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_pedidos') AND name = 'ID_restaurante')
BEGIN
    ALTER TABLE tb_pedidos ADD ID_restaurante INT NULL;
END
GO

-- tb_pedidos: mesa (campo opcional para pedidos no salão)
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_pedidos') AND name = 'mesa')
BEGIN
    ALTER TABLE tb_pedidos ADD mesa VARCHAR(50) NULL;
END
GO

-- ============================================================
-- WAVE 2: tipo_produto, receitas e personalização
-- ============================================================

-- tb_produtos: tipo_produto ('VENDA' ou 'INSUMO')
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'tipo_produto')
BEGIN
    ALTER TABLE tb_produtos ADD tipo_produto VARCHAR(20) NOT NULL DEFAULT 'VENDA';
END
GO

-- Tabela: tb_receitas (BOM — Bill of Materials)
IF OBJECT_ID('dbo.tb_receitas', 'U') IS NULL
BEGIN
    CREATE TABLE tb_receitas (
        ID_receita       INT            IDENTITY(1,1) PRIMARY KEY,
        ID_produto_venda INT            NOT NULL,
        ID_insumo        INT            NOT NULL,
        quantidade       DECIMAL(10,4)  NOT NULL,
        unidade          VARCHAR(20)    NULL,
        ativo            BIT            NOT NULL DEFAULT 1
    );
END
GO

-- Tabela: tb_personalizacao_grupos
IF OBJECT_ID('dbo.tb_personalizacao_grupos', 'U') IS NULL
BEGIN
    CREATE TABLE tb_personalizacao_grupos (
        ID_grupo    INT          IDENTITY(1,1) PRIMARY KEY,
        ID_produto  INT          NOT NULL,
        nome        VARCHAR(100) NULL,
        obrigatorio BIT          NULL,
        max_opcoes  INT          NULL
    );
END
GO

-- Tabela: tb_personalizacao_opcoes
IF OBJECT_ID('dbo.tb_personalizacao_opcoes', 'U') IS NULL
BEGIN
    CREATE TABLE tb_personalizacao_opcoes (
        ID_opcao        INT           IDENTITY(1,1) PRIMARY KEY,
        ID_grupo        INT           NOT NULL,
        nome            VARCHAR(100)  NULL,
        preco_adicional DECIMAL(10,2) NULL
    );
END
GO

-- ============================================================
-- WAVE 3: Dados de referência — status de pedido
-- ============================================================

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

-- ============================================================
-- WAVE 4: Chaves estrangeiras (idempotente)
-- ============================================================

-- FK: tb_funcionarios → tb_restaurantes
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_funcionarios_tb_restaurantes')
BEGIN
    ALTER TABLE tb_funcionarios
    ADD CONSTRAINT FK_tb_funcionarios_tb_restaurantes FOREIGN KEY (ID_restaurante) REFERENCES tb_restaurantes (ID_restaurante) ON DELETE SET NULL;
END
GO

-- FK: tb_produtos → tb_restaurantes
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_produtos_tb_restaurantes')
BEGIN
    ALTER TABLE tb_produtos
    ADD CONSTRAINT FK_tb_produtos_tb_restaurantes FOREIGN KEY (ID_restaurante) REFERENCES tb_restaurantes (ID_restaurante) ON DELETE CASCADE;
END
GO

-- FK: tb_estoque → tb_produtos
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_estoque_tb_produtos')
BEGIN
    ALTER TABLE tb_estoque
    ADD CONSTRAINT FK_tb_estoque_tb_produtos FOREIGN KEY (ID_produto) REFERENCES tb_produtos (ID_produto) ON DELETE CASCADE;
END
GO

-- FK: tb_nutricao → tb_produtos
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_nutricao_tb_produtos')
BEGIN
    ALTER TABLE tb_nutricao
    ADD CONSTRAINT FK_tb_nutricao_tb_produtos FOREIGN KEY (ID_produto) REFERENCES tb_produtos (ID_produto) ON DELETE CASCADE;
END
GO

-- FK: tb_pedidos → tb_clientes
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_pedidos_tb_clientes')
BEGIN
    ALTER TABLE tb_pedidos
    ADD CONSTRAINT FK_tb_pedidos_tb_clientes FOREIGN KEY (ID_cliente) REFERENCES tb_clientes (id_cliente);
END
GO

-- FK: tb_pedidos → tb_restaurantes
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_pedidos_tb_restaurantes')
BEGIN
    ALTER TABLE tb_pedidos
    ADD CONSTRAINT FK_tb_pedidos_tb_restaurantes FOREIGN KEY (ID_restaurante) REFERENCES tb_restaurantes (ID_restaurante) ON DELETE CASCADE;
END
GO

-- FK: tb_pedidos → tb_status_pedido
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_pedidos_tb_status_pedido')
BEGIN
    ALTER TABLE tb_pedidos
    ADD CONSTRAINT FK_tb_pedidos_tb_status_pedido FOREIGN KEY (status_id) REFERENCES tb_status_pedido (ID_status);
END
GO

-- FK: tb_pedidos_produtos → tb_pedidos
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_pedidos_produtos_tb_pedidos')
BEGIN
    ALTER TABLE tb_pedidos_produtos
    ADD CONSTRAINT FK_tb_pedidos_produtos_tb_pedidos FOREIGN KEY (ID_pedido) REFERENCES tb_pedidos (ID_pedido) ON DELETE CASCADE;
END
GO

-- FK: tb_pedidos_produtos → tb_produtos
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_pedidos_produtos_tb_produtos')
BEGIN
    ALTER TABLE tb_pedidos_produtos
    ADD CONSTRAINT FK_tb_pedidos_produtos_tb_produtos FOREIGN KEY (ID_produto) REFERENCES tb_produtos (ID_produto);
END
GO

-- FK: tb_reservas → tb_clientes
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_reservas_tb_clientes')
BEGIN
    ALTER TABLE tb_reservas
    ADD CONSTRAINT FK_tb_reservas_tb_clientes FOREIGN KEY (ID_cliente) REFERENCES tb_clientes (id_cliente);
END
GO

-- FK: tb_reservas → tb_restaurantes
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_reservas_tb_restaurantes')
BEGIN
    ALTER TABLE tb_reservas
    ADD CONSTRAINT FK_tb_reservas_tb_restaurantes FOREIGN KEY (ID_restaurante) REFERENCES tb_restaurantes (ID_restaurante) ON DELETE CASCADE;
END
GO

-- FK: tb_pagamentos → tb_pedidos
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_pagamentos_tb_pedidos')
BEGIN
    ALTER TABLE tb_pagamentos
    ADD CONSTRAINT FK_tb_pagamentos_tb_pedidos FOREIGN KEY (ID_pedido) REFERENCES tb_pedidos (ID_pedido) ON DELETE CASCADE;
END
GO

-- FK: tb_avaliacoes → tb_clientes
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_avaliacoes_tb_clientes')
BEGIN
    ALTER TABLE tb_avaliacoes
    ADD CONSTRAINT FK_tb_avaliacoes_tb_clientes FOREIGN KEY (ID_cliente) REFERENCES tb_clientes (id_cliente);
END
GO

-- FK: tb_avaliacoes → tb_restaurantes
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_avaliacoes_tb_restaurantes')
BEGIN
    ALTER TABLE tb_avaliacoes
    ADD CONSTRAINT FK_tb_avaliacoes_tb_restaurantes FOREIGN KEY (ID_restaurante) REFERENCES tb_restaurantes (ID_restaurante) ON DELETE CASCADE;
END
GO

-- FK: tb_fidelidade → tb_clientes
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_fidelidade_tb_clientes')
BEGIN
    ALTER TABLE tb_fidelidade
    ADD CONSTRAINT FK_tb_fidelidade_tb_clientes FOREIGN KEY (ID_cliente) REFERENCES tb_clientes (id_cliente) ON DELETE CASCADE;
END
GO

-- FK: tb_receitas → tb_produtos (produto de venda)
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_receitas_venda')
BEGIN
    ALTER TABLE tb_receitas
    ADD CONSTRAINT FK_tb_receitas_venda FOREIGN KEY (ID_produto_venda) REFERENCES tb_produtos (ID_produto);
END
GO

-- FK: tb_receitas → tb_produtos (insumo)
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_receitas_insumo')
BEGIN
    ALTER TABLE tb_receitas
    ADD CONSTRAINT FK_tb_receitas_insumo FOREIGN KEY (ID_insumo) REFERENCES tb_produtos (ID_produto);
END
GO

-- FK: tb_personalizacao_grupos → tb_produtos
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_personalizacao_grupos_tb_produtos')
BEGIN
    ALTER TABLE tb_personalizacao_grupos
    ADD CONSTRAINT FK_tb_personalizacao_grupos_tb_produtos FOREIGN KEY (ID_produto) REFERENCES tb_produtos (ID_produto) ON DELETE CASCADE;
END
GO

-- FK: tb_personalizacao_opcoes → tb_personalizacao_grupos
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_personalizacao_opcoes_tb_grupos')
BEGIN
    ALTER TABLE tb_personalizacao_opcoes
    ADD CONSTRAINT FK_tb_personalizacao_opcoes_tb_grupos FOREIGN KEY (ID_grupo) REFERENCES tb_personalizacao_grupos (ID_grupo) ON DELETE CASCADE;
END
GO

-- FK: tb_mesas → tb_restaurantes
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_tb_mesas_tb_restaurantes')
BEGIN
    ALTER TABLE tb_mesas
    ADD CONSTRAINT FK_tb_mesas_tb_restaurantes FOREIGN KEY (ID_restaurante) REFERENCES tb_restaurantes (ID_restaurante) ON DELETE CASCADE;
END
GO
