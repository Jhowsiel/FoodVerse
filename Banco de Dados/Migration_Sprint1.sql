-- ============================================================
-- Sprint 1 — Schema Mínimo: Multi-restaurante
-- Execute no banco FoodVerseDB existente
-- ============================================================

USE FoodVerseDB;
GO

-- 1. Adicionar campos de controle ao tb_restaurantes
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

-- 2. Migração de dados: todos os restaurantes existentes ficam ativo=1, aberto=1
UPDATE tb_restaurantes SET ativo = 1, aberto = 1 WHERE ativo IS NULL OR aberto IS NULL;
GO

-- 3. tb_funcionarios já possui ID_restaurante (nullable).
--    Garantir que funcionários Admin (cargo = 'Admin') podem ter ID_restaurante = NULL.
--    Para os demais, ID_restaurante já deveria estar preenchido.
--    Estratégia conservadora: manter NULLs existentes (sem FK break).
PRINT 'Schema Sprint 1 aplicado com sucesso.';
GO
