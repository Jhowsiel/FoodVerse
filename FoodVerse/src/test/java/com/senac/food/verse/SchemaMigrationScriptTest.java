package com.senac.food.verse;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaMigrationScriptTest {

    private String readMigration() throws IOException {
        Path migration = Path.of("..", "Banco de Dados", "Migration_Sprint1.sql");
        return Files.readString(migration);
    }

    @Test
    void deveGarantirCamposMinimosCompartilhados() throws IOException {
        String sql = readMigration();

        assertTrue(sql.contains("tb_funcionarios ADD ID_restaurante INT NULL"));
        assertTrue(sql.contains("tb_restaurantes ADD ativo BIT NOT NULL DEFAULT 1"));
        assertTrue(sql.contains("tb_restaurantes ADD aberto BIT NOT NULL DEFAULT 1"));
        assertTrue(sql.contains("tb_restaurantes ADD imagem VARCHAR(255) NULL"));
        assertTrue(sql.contains("tb_produtos ADD imagem VARCHAR(255) NULL"));
    }

    @Test
    void devePreservarCompatibilidadeComAdminGlobalERestauranteCompartilhado() throws IOException {
        String sql = readMigration();

        assertTrue(sql.contains("ALTER TABLE tb_funcionarios ALTER COLUMN ID_restaurante INT NULL"));
        assertTrue(sql.contains("ON DELETE SET NULL"));
        assertTrue(sql.contains("DEFAULT 1 FOR ativo"));
        assertTrue(sql.contains("DEFAULT 1 FOR aberto"));
        assertTrue(sql.contains("Admin (cargo = 'Admin') pode manter ID_restaurante = NULL"));
    }
}
