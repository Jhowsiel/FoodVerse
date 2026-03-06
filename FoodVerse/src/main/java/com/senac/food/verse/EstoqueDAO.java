package com.senac.food.verse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class EstoqueDAO {

    public static class Unidade {
        private final String codigo;
        private final String base;
        private final double fatorBase;  
        
        public Unidade(String codigo, String base, double fatorBase) {
            this.codigo = codigo; this.base = base; this.fatorBase = fatorBase;
        }
        public String getCodigo() { return codigo; }
        public String getBase()   { return base; }
        public double getFatorBase() { return fatorBase; }
        @Override public String toString() { return codigo; }
    }

    public static class ItemEstoque {
        private Long id;
        private String nome;
        private String categoria;
        private String unidadePadrao;
        private double estoqueAtual;
        private double estoqueMinimo;
        private double custoMedio;
        private boolean ativo = true;
        private Double fatorCxParaUn; 

        public ItemEstoque() {}
        public ItemEstoque(Long id, String nome, String categoria, String unidadePadrao, double estoqueAtual, double custoMedio) {
            this.id = id; this.nome = nome; this.categoria = categoria;
            this.unidadePadrao = unidadePadrao; this.estoqueAtual = estoqueAtual;
            this.custoMedio = custoMedio;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public String getUnidadePadrao() { return unidadePadrao; }
        public void setUnidadePadrao(String unidadePadrao) { this.unidadePadrao = unidadePadrao; }
        public double getEstoqueAtual() { return estoqueAtual; }
        public void setEstoqueAtual(double estoqueAtual) { this.estoqueAtual = estoqueAtual; }
        public double getEstoqueMinimo() { return estoqueMinimo; }
        public void setEstoqueMinimo(double estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }
        public double getCustoMedio() { return custoMedio; }
        public void setCustoMedio(double custoMedio) { this.custoMedio = custoMedio; }
        public boolean isAtivo() { return ativo; }
        public void setAtivo(boolean ativo) { this.ativo = ativo; }
        public Double getFatorCxParaUn() { return fatorCxParaUn; }
        public void setFatorCxParaUn(Double fatorCxParaUn) { this.fatorCxParaUn = fatorCxParaUn; }
    }

    public static class MovimentacaoEstoque {
        private Long id;
        private Long itemId;
        private String nomeItemSnapshot; 
        private String tipo; 
        private double quantidade;
        private String observacao;
        private LocalDateTime dataMovimento;

        public MovimentacaoEstoque() { this.dataMovimento = LocalDateTime.now(); }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public String getNomeItemSnapshot() { return nomeItemSnapshot; }
        public void setNomeItemSnapshot(String nomeItemSnapshot) { this.nomeItemSnapshot = nomeItemSnapshot; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public double getQuantidade() { return quantidade; }
        public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
        public String getObservacao() { return observacao; }
        public void setObservacao(String observacao) { this.observacao = observacao; }
        public LocalDateTime getDataMovimento() { return dataMovimento; }
        public void setDataMovimento(LocalDateTime dataMovimento) { this.dataMovimento = dataMovimento; }
    }

    // Cache / Fallback para modo Offline
    private static final List<ItemEstoque> ITENS_MOCK = new ArrayList<>();
    private static final List<Unidade> UNIDADES_MOCK = new ArrayList<>();
    private static final List<MovimentacaoEstoque> MOVS_MOCK = new ArrayList<>();
    private static final AtomicLong SEQ_ITEM = new AtomicLong(1);

    static {
        UNIDADES_MOCK.add(new Unidade("kg", "g", 1000));
        UNIDADES_MOCK.add(new Unidade("g", "g", 1));
        UNIDADES_MOCK.add(new Unidade("L", "ml", 1000));
        UNIDADES_MOCK.add(new Unidade("ml", "ml", 1));
        UNIDADES_MOCK.add(new Unidade("un", "un", 1));

        ItemEstoque i1 = new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Arroz Branco", "Mercearia", "kg", 50.0, 4.50);
        i1.setEstoqueMinimo(10.0); ITENS_MOCK.add(i1);
        ItemEstoque i2 = new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Coca-Cola Lata", "Bebidas", "un", 120.0, 2.80);
        i2.setEstoqueMinimo(24.0); ITENS_MOCK.add(i2);
    }

    public List<Unidade> listarUnidades() { 
        List<Unidade> unidades = new ArrayList<>();
        String sql = "SELECT codigo_unidade, base, fator_base FROM tb_unidades_medida";
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return UNIDADES_MOCK;
            try(Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while(rs.next()) unidades.add(new Unidade(rs.getString("codigo_unidade"), rs.getString("base"), rs.getDouble("fator_base")));
            }
        } catch(Exception e) { e.printStackTrace(); }
        return unidades.isEmpty() ? UNIDADES_MOCK : unidades;
    }

    public List<String> categoriasNomes() {
        List<String> categorias = new ArrayList<>();
        String sql = "SELECT DISTINCT categoria FROM tb_itens_estoque WHERE categoria IS NOT NULL";
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return ITENS_MOCK.stream().map(ItemEstoque::getCategoria).distinct().collect(Collectors.toList());
            try(Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while(rs.next()) categorias.add(rs.getString("categoria"));
            }
        } catch(Exception e) { e.printStackTrace(); }
        return categorias;
    }

    public List<ItemEstoque> listarItens(String termo, String categoria, String status) {
        List<ItemEstoque> itens = new ArrayList<>();
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) {
                // Filtro em memória
                String t = termo == null ? "" : termo.toLowerCase();
                boolean fCat = categoria != null && !categoria.equalsIgnoreCase("Todas");
                return ITENS_MOCK.stream().filter(i -> i.getNome().toLowerCase().contains(t))
                    .filter(i -> !fCat || i.getCategoria().equalsIgnoreCase(categoria))
                    .filter(i -> "Ativos".equalsIgnoreCase(status) ? i.isAtivo() : ("Inativos".equalsIgnoreCase(status) ? !i.isAtivo() : true))
                    .collect(Collectors.toList());
            }

            StringBuilder sql = new StringBuilder("SELECT * FROM tb_itens_estoque WHERE nome LIKE ?");
            if(categoria != null && !categoria.equalsIgnoreCase("Todas")) sql.append(" AND categoria = ?");
            if("Ativos".equalsIgnoreCase(status)) sql.append(" AND ativo = 1");
            if("Inativos".equalsIgnoreCase(status)) sql.append(" AND ativo = 0");

            try(PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setString(1, "%" + (termo==null?"":termo) + "%");
                if(categoria != null && !categoria.equalsIgnoreCase("Todas")) ps.setString(2, categoria);
                
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        ItemEstoque i = new ItemEstoque(rs.getLong("ID_item_estoque"), rs.getString("nome"), 
                                rs.getString("categoria"), rs.getString("unidade_padrao"), 
                                rs.getDouble("estoque_atual"), rs.getDouble("custo_medio"));
                        i.setEstoqueMinimo(rs.getDouble("estoque_minimo"));
                        i.setAtivo(rs.getBoolean("ativo"));
                        itens.add(i);
                    }
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return itens;
    }

    public ItemEstoque buscarItemPorId(Long id) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return ITENS_MOCK.stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
            
            String sql = "SELECT * FROM tb_itens_estoque WHERE ID_item_estoque = ?";
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                try(ResultSet rs = ps.executeQuery()) {
                    if(rs.next()) {
                        ItemEstoque i = new ItemEstoque(rs.getLong("ID_item_estoque"), rs.getString("nome"), 
                                rs.getString("categoria"), rs.getString("unidade_padrao"), 
                                rs.getDouble("estoque_atual"), rs.getDouble("custo_medio"));
                        i.setEstoqueMinimo(rs.getDouble("estoque_minimo"));
                        i.setAtivo(rs.getBoolean("ativo"));
                        return i;
                    }
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }

    public void salvarItem(ItemEstoque item) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) {
                item.setId(SEQ_ITEM.getAndIncrement()); ITENS_MOCK.add(item); return;
            }
            String sql = "INSERT INTO tb_itens_estoque (nome, categoria, unidade_padrao, estoque_atual, estoque_minimo, custo_medio, ativo) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getNome()); ps.setString(2, item.getCategoria());
                ps.setString(3, item.getUnidadePadrao()); ps.setDouble(4, item.getEstoqueAtual());
                ps.setDouble(5, item.getEstoqueMinimo()); ps.setDouble(6, item.getCustoMedio());
                ps.setBoolean(7, item.isAtivo());
                ps.executeUpdate();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    public void atualizarItem(ItemEstoque item) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) {
                for(int i=0; i<ITENS_MOCK.size(); i++) {
                    if(ITENS_MOCK.get(i).getId().equals(item.getId())) { ITENS_MOCK.set(i, item); return; }
                }
                return;
            }
            String sql = "UPDATE tb_itens_estoque SET nome=?, categoria=?, unidade_padrao=?, estoque_minimo=?, custo_medio=?, ativo=? WHERE ID_item_estoque=?";
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getNome()); ps.setString(2, item.getCategoria());
                ps.setString(3, item.getUnidadePadrao()); ps.setDouble(4, item.getEstoqueMinimo());
                ps.setDouble(5, item.getCustoMedio()); ps.setBoolean(6, item.isAtivo());
                ps.setLong(7, item.getId());
                ps.executeUpdate();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    public void registrarMovimentacao(MovimentacaoEstoque mov) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) {
                // Lógica Offline
                ItemEstoque item = buscarItemPorId(mov.getItemId());
                if(item != null) {
                    mov.setNomeItemSnapshot(item.getNome());
                    item.setEstoqueAtual(item.getEstoqueAtual() + ("ENTRADA".equals(mov.getTipo()) ? mov.getQuantidade() : -mov.getQuantidade()));
                }
                MOVS_MOCK.add(0, mov); return;
            }
            
            // Grava Movimentação
            String sqlMov = "INSERT INTO tb_movimentacoes_estoque (ID_item_estoque, nome_item_snapshot, tipo, quantidade, observacao, data_movimento) VALUES (?, ?, ?, ?, ?, GETDATE())";
            try(PreparedStatement ps = conn.prepareStatement(sqlMov)) {
                ps.setLong(1, mov.getItemId()); ps.setString(2, mov.getNomeItemSnapshot() != null ? mov.getNomeItemSnapshot() : "");
                ps.setString(3, mov.getTipo()); ps.setDouble(4, mov.getQuantidade()); ps.setString(5, mov.getObservacao());
                ps.executeUpdate();
            }
            
            // Atualiza Saldo no Estoque
            String op = "ENTRADA".equals(mov.getTipo()) ? "+" : "-";
            String sqlUpd = "UPDATE tb_itens_estoque SET estoque_atual = estoque_atual " + op + " ? WHERE ID_item_estoque = ?";
            try(PreparedStatement ps = conn.prepareStatement(sqlUpd)) {
                ps.setDouble(1, mov.getQuantidade()); ps.setLong(2, mov.getItemId());
                ps.executeUpdate();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    public List<MovimentacaoEstoque> listarUltimasMovimentacoes() {
        List<MovimentacaoEstoque> movs = new ArrayList<>();
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return MOVS_MOCK.stream().limit(100).collect(Collectors.toList());
            
            String sql = "SELECT TOP 100 * FROM tb_movimentacoes_estoque ORDER BY data_movimento DESC";
            try(Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while(rs.next()) {
                    MovimentacaoEstoque m = new MovimentacaoEstoque();
                    m.setId(rs.getLong("ID_movimentacao"));
                    m.setItemId(rs.getLong("ID_item_estoque"));
                    m.setNomeItemSnapshot(rs.getString("nome_item_snapshot"));
                    m.setTipo(rs.getString("tipo"));
                    m.setQuantidade(rs.getDouble("quantidade"));
                    m.setObservacao(rs.getString("observacao"));
                    Timestamp ts = rs.getTimestamp("data_movimento");
                    if(ts != null) m.setDataMovimento(ts.toLocalDateTime());
                    movs.add(m);
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return movs;
    }
}