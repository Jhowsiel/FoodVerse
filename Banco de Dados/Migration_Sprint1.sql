-- ============================================================
-- Sprint 1 — Schema mínimo: multi-restaurante compatível
-- Execute no banco FoodVerseDB existente
-- ============================================================

USE FoodVerseDB;
GO

-- 1. Garantir os dois controles no restaurante
--    ativo  = Admin global controla participação na plataforma
--    aberto = Gerente/Admin em contexto controla operação temporária

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('tb_restaurantes') AND name = 'ativo'
)
BEGIN
    ALTER TABLE tb_restaurantes ADD ativo BIT NOT NULL CONSTRAINT DF_tb_restaurantes_ativo DEFAULT 1;
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
    ALTER TABLE tb_restaurantes ADD aberto BIT NOT NULL CONSTRAINT DF_tb_restaurantes_aberto DEFAULT 1;
    PRINT 'Coluna aberto adicionada em tb_restaurantes.';
END
ELSE
    PRINT 'Coluna aberto já existe em tb_restaurantes.';
GO

UPDATE tb_restaurantes
SET ativo = ISNULL(ativo, 1),
    aberto = ISNULL(aberto, 1)
WHERE ativo IS NULL OR aberto IS NULL;
GO

-- 2. Garantir vínculo funcionário -> restaurante
--    ID_restaurante pode ficar NULL apenas para Admin global.

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
    WHERE name = 'FK_tb_funcionarios_tb_restaurantes'
)
BEGIN
    ALTER TABLE tb_funcionarios
    ADD CONSTRAINT FK_tb_funcionarios_tb_restaurantes
        FOREIGN KEY (ID_restaurante)
        REFERENCES tb_restaurantes(ID_restaurante)
        ON DELETE SET NULL;
    PRINT 'FK FK_tb_funcionarios_tb_restaurantes criada.';
END
ELSE
    PRINT 'FK FK_tb_funcionarios_tb_restaurantes já existe.';
GO

-- 3. Migração de dados conservadora
--    a) Admin permanece sem restaurante
UPDATE tb_funcionarios
SET ID_restaurante = NULL
WHERE cargo = 'Admin';
GO

--    b) Se existir exatamente 1 restaurante, vincula automaticamente os não-admin sem restaurante
DECLARE @restaurantes_existentes INT = (SELECT COUNT(*) FROM tb_restaurantes);
DECLARE @restaurante_unico INT = (
    SELECT TOP 1 ID_restaurante
    FROM tb_restaurantes
    ORDER BY ID_restaurante
);

IF @restaurantes_existentes = 1
BEGIN
    UPDATE tb_funcionarios
    SET ID_restaurante = @restaurante_unico
    WHERE cargo <> 'Admin'
      AND ID_restaurante IS NULL;

    PRINT 'Funcionários não-admin vinculados automaticamente ao único restaurante existente.';
END
ELSE
BEGIN
    PRINT 'Migração conservadora: vínculos ambíguos NÃO foram atribuídos automaticamente.';
    PRINT 'Revise funcionários não-admin sem restaurante antes de exigir o vínculo em todas as operações.';
END
GO

-- 4. Regra futura: somente Admin pode permanecer com restaurante NULL.
--    WITH NOCHECK preserva bases legadas com pendências, mas passa a validar novos INSERT/UPDATE.
IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_tb_funcionarios_admin_restaurante'
)
BEGIN
    ALTER TABLE tb_funcionarios WITH NOCHECK
    ADD CONSTRAINT CK_tb_funcionarios_admin_restaurante
    CHECK (
        cargo = 'Admin'
        OR ID_restaurante IS NOT NULL
    );

    PRINT 'Constraint CK_tb_funcionarios_admin_restaurante criada.';
END
ELSE
    PRINT 'Constraint CK_tb_funcionarios_admin_restaurante já existe.';
GO

PRINT 'Schema Sprint 1 aplicado com sucesso.';
GO
