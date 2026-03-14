package com.senac.food.verse;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaMigrationScriptTest {

    private String readMigration() throws IOException {
        Path migration = findMigrationPath();
        return Files.readString(migration);
    }

    private Path findMigrationPath() {
        Path cursor = Path.of("").toAbsolutePath().normalize();
        while (cursor != null) {
            Path candidate = cursor.resolve("Banco de Dados").resolve("Migration_Sprint1.sql");
            if (Files.exists(candidate)) {
                return candidate;
            }
            cursor = cursor.getParent();
        }
        throw new IllegalStateException("arquivo Migration_Sprint1.sql não encontrado a partir do diretório atual.");
    }

    private void assertSqlMatches(String sql, String regex) {
        assertTrue(Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(sql).find());
    }

    @Test
    void deveGarantirCamposMinimosCompartilhados() throws IOException {
        String sql = readMigration();

        assertSqlMatches(sql, "WHERE\\s+object_id\\s*=\\s*OBJECT_ID\\('tb_funcionarios'\\)\\s+AND\\s+name\\s*=\\s*'ID_restaurante'");
        assertSqlMatches(sql, "ALTER\\s+TABLE\\s+tb_funcionarios\\s+ADD\\s+ID_restaurante\\s+INT\\s+NULL\\s*;");
        assertSqlMatches(sql, "WHERE\\s+object_id\\s*=\\s*OBJECT_ID\\('tb_restaurantes'\\)\\s+AND\\s+name\\s*=\\s*'ativo'");
        assertSqlMatches(sql, "ALTER\\s+TABLE\\s+tb_restaurantes\\s+ADD\\s+ativo\\s+BIT\\s+NOT\\s+NULL\\s+DEFAULT\\s+1\\s*;");
        assertSqlMatches(sql, "WHERE\\s+object_id\\s*=\\s*OBJECT_ID\\('tb_restaurantes'\\)\\s+AND\\s+name\\s*=\\s*'aberto'");
        assertSqlMatches(sql, "ALTER\\s+TABLE\\s+tb_restaurantes\\s+ADD\\s+aberto\\s+BIT\\s+NOT\\s+NULL\\s+DEFAULT\\s+1\\s*;");
        assertSqlMatches(sql, "ALTER\\s+TABLE\\s+tb_restaurantes\\s+ADD\\s+imagem\\s+VARCHAR\\(255\\)\\s+NULL\\s*;");
        assertSqlMatches(sql, "ALTER\\s+TABLE\\s+tb_restaurantes\\s+ADD\\s+banner\\s+VARCHAR\\(255\\)\\s+NULL\\s*;");
        assertSqlMatches(sql, "ALTER\\s+TABLE\\s+tb_produtos\\s+ADD\\s+imagem\\s+VARCHAR\\(255\\)\\s+NULL\\s*;");
        assertSqlMatches(sql, "ALTER\\s+TABLE\\s+tb_produtos\\s+ADD\\s+restricoes\\s+VARCHAR\\(255\\)\\s+NULL\\s*;");
    }

    @Test
    void deveGarantirColunasBannerEImagemComGuardaIdempotente() throws IOException {
        String sql = readMigration();

        assertSqlMatches(sql,
                "WHERE\\s+object_id\\s*=\\s*OBJECT_ID\\('tb_restaurantes'\\)\\s+AND\\s+name\\s*=\\s*'banner'");
        assertSqlMatches(sql,
                "ALTER\\s+TABLE\\s+tb_restaurantes\\s+ADD\\s+banner\\s+VARCHAR\\(255\\)\\s+NULL\\s*;");
        assertSqlMatches(sql,
                "WHERE\\s+object_id\\s*=\\s*OBJECT_ID\\('tb_restaurantes'\\)\\s+AND\\s+name\\s*=\\s*'imagem'");
    }

    @Test
    void devePreservarCompatibilidadeComAdminGlobalERestauranteCompartilhado() throws IOException {
        String sql = readMigration();

        assertSqlMatches(sql, "ADD\\s+CONSTRAINT\\s+FK_tb_funcionarios_tb_restaurantes\\s+FOREIGN\\s+KEY\\s*\\(ID_restaurante\\)\\s+REFERENCES\\s+tb_restaurantes\\s*\\(ID_restaurante\\)\\s+ON\\s+DELETE\\s+SET\\s+NULL\\s*;");
        assertSqlMatches(sql, "IF\\s+EXISTS\\s*\\(\\s*SELECT\\s+1\\s+FROM\\s+tb_restaurantes\\s+WHERE\\s+ativo\\s+IS\\s+NULL\\s+OR\\s+aberto\\s+IS\\s+NULL\\s*\\)");
        assertSqlMatches(sql, "SET\\s+ativo\\s*=\\s*COALESCE\\(ativo,\\s*1\\)\\s*,\\s*aberto\\s*=\\s*COALESCE\\(aberto,\\s*1\\)");
        assertSqlMatches(sql, "ADD\\s+CONSTRAINT\\s+DF_tb_restaurantes_ativo\\s+DEFAULT\\s+1\\s+FOR\\s+ativo\\s*;");
        assertSqlMatches(sql, "ADD\\s+CONSTRAINT\\s+DF_tb_restaurantes_aberto\\s+DEFAULT\\s+1\\s+FOR\\s+aberto\\s*;");
        assertSqlMatches(sql, "ALTER\\s+TABLE\\s+tb_restaurantes\\s+ALTER\\s+COLUMN\\s+ativo\\s+BIT\\s+NOT\\s+NULL\\s*;");
        assertSqlMatches(sql, "ALTER\\s+TABLE\\s+tb_restaurantes\\s+ALTER\\s+COLUMN\\s+aberto\\s+BIT\\s+NOT\\s+NULL\\s*;");
    }

    @Test
    void deveGarantirWave2TipoProdutoEReceitas() throws IOException {
        String sql = readMigration();

        // tipo_produto column
        assertSqlMatches(sql, "WHERE\\s+object_id\\s*=\\s*OBJECT_ID\\('tb_produtos'\\)\\s+AND\\s+name\\s*=\\s*'tipo_produto'");
        assertSqlMatches(sql, "ALTER\\s+TABLE\\s+tb_produtos\\s+ADD\\s+tipo_produto\\s+VARCHAR\\(20\\)\\s+NOT\\s+NULL\\s+DEFAULT\\s+'VENDA'\\s*;");

        // tb_receitas table
        assertSqlMatches(sql, "CREATE\\s+TABLE\\s+tb_receitas");
        assertSqlMatches(sql, "ID_produto_venda\\s+INT\\s+NOT\\s+NULL");
        assertSqlMatches(sql, "ID_insumo\\s+INT\\s+NOT\\s+NULL");

        // personalization tables
        assertSqlMatches(sql, "CREATE\\s+TABLE\\s+tb_personalizacao_grupos");
        assertSqlMatches(sql, "CREATE\\s+TABLE\\s+tb_personalizacao_opcoes");

        // status seed data
        assertSqlMatches(sql, "INSERT\\s+INTO\\s+tb_status_pedido.*VALUES\\s*\\(1,\\s*'pendente'\\)");
        assertSqlMatches(sql, "INSERT\\s+INTO\\s+tb_status_pedido.*VALUES\\s*\\(6,\\s*'cancelado'\\)");
    }
}
