-- ============================================================
-- Sprint 1 — Schema Mínimo: Multi-restaurante
-- Execute no banco FoodVerseDB existente
-- ============================================================

USE FoodVerseDB;
GO

-- 1. Garantir vínculo por restaurante em tb_funcionarios
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_funcionarios') AND name = 'ID_restaurante'
)
BEGIN
    ALTER TABLE tb_funcionarios ADD ID_restaurante INT NULL;
    PRINT 'Coluna ID_restaurante adicionada em tb_funcionarios.';
END
ELSE
    PRINT 'Coluna ID_restaurante já existe em tb_funcionarios.';
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE parent_object_id = OBJECT_ID('tb_funcionarios')
      AND referenced_object_id = OBJECT_ID('tb_restaurantes')
)
BEGIN
    ALTER TABLE tb_funcionarios
        ADD CONSTRAINT FK_tb_funcionarios_tb_restaurantes
        FOREIGN KEY (ID_restaurante)
        REFERENCES tb_restaurantes(ID_restaurante)
        ON DELETE SET NULL;
    PRINT 'FK de tb_funcionarios para tb_restaurantes adicionada.';
END
ELSE
    PRINT 'FK de tb_funcionarios para tb_restaurantes já existe.';
GO

-- 2. Garantir campos de imagem compatíveis com Java e Django
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'imagem'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD imagem VARCHAR(255) NULL;
    PRINT 'Coluna imagem adicionada em tb_restaurantes.';
END
ELSE
    PRINT 'Coluna imagem já existe em tb_restaurantes.';
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_produtos') AND name = 'imagem'
)
BEGIN
    ALTER TABLE tb_produtos ADD imagem VARCHAR(255) NULL;
    PRINT 'Coluna imagem adicionada em tb_produtos.';
END
ELSE
    PRINT 'Coluna imagem já existe em tb_produtos.';
GO

-- 3. Adicionar campos de controle ao tb_restaurantes
--    ativo   (Admin global): controla se o restaurante existe/participa do marketplace
--    aberto  (Gerente):      controla se está aceitando pedidos agora

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'ativo'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD ativo BIT NOT NULL DEFAULT 1;
    PRINT 'Coluna ativo adicionada em tb_restaurantes.';
END
ELSE
    PRINT 'Coluna ativo já existe em tb_restaurantes.';
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'aberto'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD aberto BIT NOT NULL DEFAULT 1;
    PRINT 'Coluna aberto adicionada em tb_restaurantes.';
END
ELSE
    PRINT 'Coluna aberto já existe em tb_restaurantes.';
GO

-- 4. Migração de dados: normaliza apenas valores NULL de ativo/aberto para 1
IF EXISTS (
    SELECT 1
    FROM tb_restaurantes
    WHERE ativo IS NULL OR aberto IS NULL
)
BEGIN
    UPDATE tb_restaurantes
       SET ativo = COALESCE(ativo, 1),
           aberto = COALESCE(aberto, 1)
     WHERE ativo IS NULL OR aberto IS NULL;
    PRINT 'Dados legados de ativo/aberto normalizados.';
END
ELSE
    PRINT 'Nenhum dado legado de ativo/aberto precisava de normalização.';
GO

IF EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes')
      AND name = 'ativo'
      AND is_nullable = 1
)
BEGIN
    ALTER TABLE tb_restaurantes ALTER COLUMN ativo BIT NOT NULL;
    PRINT 'Coluna ativo ajustada para NOT NULL.';
END
ELSE
    PRINT 'Coluna ativo já está como NOT NULL.';
GO

IF EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes')
      AND name = 'aberto'
      AND is_nullable = 1
)
BEGIN
    ALTER TABLE tb_restaurantes ALTER COLUMN aberto BIT NOT NULL;
    PRINT 'Coluna aberto ajustada para NOT NULL.';
END
ELSE
    PRINT 'Coluna aberto já está como NOT NULL.';
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.default_constraints dc
    INNER JOIN sys.columns c
        ON c.object_id = dc.parent_object_id
       AND c.column_id = dc.parent_column_id
    WHERE dc.parent_object_id = OBJECT_ID('tb_restaurantes')
      AND c.name = 'ativo'
)
BEGIN
    ALTER TABLE tb_restaurantes
        ADD CONSTRAINT DF_tb_restaurantes_ativo DEFAULT 1 FOR ativo;
    PRINT 'Default de ativo configurado.';
END
ELSE
    PRINT 'Default de ativo já existe.';
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.default_constraints dc
    INNER JOIN sys.columns c
        ON c.object_id = dc.parent_object_id
       AND c.column_id = dc.parent_column_id
    WHERE dc.parent_object_id = OBJECT_ID('tb_restaurantes')
      AND c.name = 'aberto'
)
BEGIN
    ALTER TABLE tb_restaurantes
        ADD CONSTRAINT DF_tb_restaurantes_aberto DEFAULT 1 FOR aberto;
    PRINT 'Default de aberto configurado.';
END
ELSE
    PRINT 'Default de aberto já existe.';
GO

-- 5. Compatibilidade operacional
--    Admin (cargo = 'Admin') pode manter ID_restaurante = NULL.
--    Para os demais, a correção do vínculo deve ser feita com base no restaurante real.
--    Estratégia conservadora: não inventar relacionamento para dados legados.
PRINT 'Schema Sprint 1 aplicado com sucesso.';
GO
