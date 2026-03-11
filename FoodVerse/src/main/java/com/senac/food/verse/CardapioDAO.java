package com.senac.food.verse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class CardapioDAO {

    public static class ReceitaItem {
        private Long itemEstoqueId; 
        private String itemNome;
        private String unidade; 
        private double quantidade;

        public ReceitaItem() {}
        public ReceitaItem(Long itemEstoqueId, String itemNome, String unidade, double quantidade) {
            this.itemEstoqueId = itemEstoqueId; this.itemNome = itemNome;
            this.unidade = unidade; this.quantidade = quantidade;
        }
        public Long getItemEstoqueId() { return itemEstoqueId; }
        public void setItemEstoqueId(Long itemEstoqueId) { this.itemEstoqueId = itemEstoqueId; }
        public String getItemNome() { return itemNome; }
        public void setItemNome(String itemNome) { this.itemNome = itemNome; }
        public String getUnidade() { return unidade; }
        public void setUnidade(String unidade) { this.unidade = unidade; }
        public double getQuantidade() { return quantidade; }
        public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
    }

    public static class Prato {
        private Long id;
        private String nome;
        private String categoria; 
        private boolean ativo = true;
        private double preco;
        private final List<ReceitaItem> ingredientes = new ArrayList<>();

        public Prato() {}
        public Prato(Long id, String nome, String categoria, boolean ativo, double preco) {
            this.id = id; this.nome = nome; this.categoria = categoria; this.ativo = ativo; this.preco = preco;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public boolean isAtivo() { return ativo; }
        public void setAtivo(boolean ativo) { this.ativo = ativo; }
        public double getPreco() { return preco; }
        public void setPreco(double preco) { this.preco = preco; }
        public List<ReceitaItem> getIngredientes() { return ingredientes; }
    }

    public static class ProdutoVenda {
        private Long id;
        private String nome;
        private String categoria;
        private boolean ativo = true;
        private double preco;

        public ProdutoVenda() {}
        public ProdutoVenda(Long id, String nome, String categoria, boolean ativo, double preco) {
            this.id = id; this.nome = nome; this.categoria = categoria; this.ativo = ativo; this.preco = preco;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public boolean isAtivo() { return ativo; }
        public void setAtivo(boolean ativo) { this.ativo = ativo; }
        public double getPreco() { return preco; }
        public void setPreco(double preco) { this.preco = preco; }
    }

    // --- MOCKS (Offline Mode) ---
    private static final List<Prato> PRATOS_MOCK = new ArrayList<>();
    private static final List<ProdutoVenda> PRODUTOS_MOCK = new ArrayList<>();
    private static final AtomicLong SEQ_ID = new AtomicLong(1000);

    static {
        PRODUTOS_MOCK.add(new ProdutoVenda(SEQ_ID.getAndIncrement(), "Coca-Cola Lata 350ml", "Bebidas", true, 7.00));
        Prato p1 = new Prato(SEQ_ID.getAndIncrement(), "Hambúrguer Clássico", "Lanches", true, 24.90);
        PRATOS_MOCK.add(p1);
    }

    // ================== PRATOS (is_prato = 1) ==================

    public List<Prato> listarPratos(String termo, String categoria, String status) {
        List<Prato> pratos = new ArrayList<>();
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) {
                String t = termo == null ? "" : termo.toLowerCase();
                return PRATOS_MOCK.stream()
                    .filter(p -> p.getNome().toLowerCase().contains(t))
                    .filter(p -> categoria == null || "Todas".equals(categoria) || categoria.equals(p.getCategoria()))
                    .collect(Collectors.toList());
            }

            StringBuilder sql = new StringBuilder("SELECT * FROM tb_produtos WHERE nome_produto LIKE ?");
            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            if (rid > 0) sql.append(" AND ID_restaurante = ?");
            if(categoria != null && !categoria.equalsIgnoreCase("Todas")) sql.append(" AND categoria = ?");
            if("Ativos".equalsIgnoreCase(status)) sql.append(" AND disponivel = 1");
            if("Inativos".equalsIgnoreCase(status)) sql.append(" AND disponivel = 0");

            try(PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int p = 1;
                ps.setString(p++, "%" + (termo==null?"":termo) + "%");
                if (rid > 0) ps.setInt(p++, rid);
                if(categoria != null && !categoria.equalsIgnoreCase("Todas")) ps.setString(p, categoria);
                
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        Prato pr = new Prato(rs.getLong("ID_produto"), rs.getString("nome_produto"),
                                rs.getString("categoria"), rs.getBoolean("disponivel"), rs.getDouble("preco"));
                        pratos.add(pr);
                    }
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return pratos;
    }

    public Prato salvarPrato(Prato prato) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) { prato.setId(SEQ_ID.getAndIncrement()); PRATOS_MOCK.add(prato); return prato; }

            String sql = "INSERT INTO tb_produtos (nome_produto, categoria, preco, disponivel) VALUES (?, ?, ?, ?)";
            try(PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, prato.getNome()); ps.setString(2, prato.getCategoria());
                ps.setDouble(3, prato.getPreco()); ps.setBoolean(4, prato.isAtivo());
                ps.executeUpdate();
                
                try(ResultSet rs = ps.getGeneratedKeys()) {
                    if(rs.next()) prato.setId(rs.getLong(1));
                }
            }
            // (Para expandir: Fazer um FOR para salvar os ingredientes na tb_receitas_prato)
        } catch(Exception e) { e.printStackTrace(); }
        return prato;
    }

    public void atualizarPrato(Prato prato) {
        if(prato.getId() == null) return;
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) {
                for(int i=0; i<PRATOS_MOCK.size(); i++) if(PRATOS_MOCK.get(i).getId().equals(prato.getId())) { PRATOS_MOCK.set(i, prato); break; }
                return;
            }
            String sql = "UPDATE tb_produtos SET nome_produto=?, categoria=?, preco=?, disponivel=? WHERE ID_produto=?";
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, prato.getNome()); ps.setString(2, prato.getCategoria());
                ps.setDouble(3, prato.getPreco()); ps.setBoolean(4, prato.isAtivo());
                ps.setLong(5, prato.getId());
                ps.executeUpdate();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    public void excluirPrato(Long id) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) { PRATOS_MOCK.removeIf(p -> p.getId().equals(id)); return; }
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_produtos WHERE ID_produto=?")) {
                ps.setLong(1, id); ps.executeUpdate();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    public Prato buscarPratoPorId(Long id) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return PRATOS_MOCK.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
            
            String sql = "SELECT * FROM tb_produtos WHERE ID_produto=?";
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                try(ResultSet rs = ps.executeQuery()) {
                    if(rs.next()) return new Prato(rs.getLong("ID_produto"), rs.getString("nome_produto"),
                                rs.getString("categoria"), rs.getBoolean("disponivel"), rs.getDouble("preco"));
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }

    public List<String> categoriasPratos() {
        List<String> cat = new ArrayList<>();
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return PRATOS_MOCK.stream().map(Prato::getCategoria).distinct().collect(Collectors.toList());
            try(Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT DISTINCT categoria FROM tb_produtos WHERE categoria IS NOT NULL")) {
                while(rs.next()) cat.add(rs.getString(1));
            }
        } catch(Exception e) { e.printStackTrace(); }
        return cat;
    }

    // ================== PRODUTOS VENDA (is_prato = 0) ==================

    public List<ProdutoVenda> listarProdutos(String termo, String categoria, String status) {
        List<ProdutoVenda> prods = new ArrayList<>();
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) {
                String t = termo == null ? "" : termo.toLowerCase();
                return PRODUTOS_MOCK.stream().filter(p -> p.getNome().toLowerCase().contains(t))
                    .filter(p -> categoria == null || "Todas".equals(categoria) || categoria.equals(p.getCategoria()))
                    .collect(Collectors.toList());
            }

            StringBuilder sql = new StringBuilder("SELECT * FROM tb_produtos WHERE nome_produto LIKE ?");
            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            if (rid > 0) sql.append(" AND ID_restaurante = ?");
            if(categoria != null && !categoria.equalsIgnoreCase("Todas")) sql.append(" AND categoria = ?");
            if("Ativos".equalsIgnoreCase(status)) sql.append(" AND disponivel = 1");
            if("Inativos".equalsIgnoreCase(status)) sql.append(" AND disponivel = 0");

            try(PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int p = 1;
                ps.setString(p++, "%" + (termo==null?"":termo) + "%");
                if (rid > 0) ps.setInt(p++, rid);
                if(categoria != null && !categoria.equalsIgnoreCase("Todas")) ps.setString(p, categoria);
                
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        prods.add(new ProdutoVenda(rs.getLong("ID_produto"), rs.getString("nome_produto"),
                                rs.getString("categoria"), rs.getBoolean("disponivel"), rs.getDouble("preco")));
                    }
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return prods;
    }

    public ProdutoVenda salvarProduto(ProdutoVenda p) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) { p.setId(SEQ_ID.getAndIncrement()); PRODUTOS_MOCK.add(p); return p; }

            String sql = "INSERT INTO tb_produtos (nome_produto, categoria, preco, disponivel) VALUES (?, ?, ?, ?)";
            try(PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getNome()); ps.setString(2, p.getCategoria());
                ps.setDouble(3, p.getPreco()); ps.setBoolean(4, p.isAtivo());
                ps.executeUpdate();
                try(ResultSet rs = ps.getGeneratedKeys()) { if(rs.next()) p.setId(rs.getLong(1)); }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return p;
    }

    public void atualizarProduto(ProdutoVenda p) {
        if(p.getId() == null) return;
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) {
                for(int i=0; i<PRODUTOS_MOCK.size(); i++) if(PRODUTOS_MOCK.get(i).getId().equals(p.getId())) { PRODUTOS_MOCK.set(i, p); break; }
                return;
            }
            String sql = "UPDATE tb_produtos SET nome_produto=?, categoria=?, preco=?, disponivel=? WHERE ID_produto=?";
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, p.getNome()); ps.setString(2, p.getCategoria());
                ps.setDouble(3, p.getPreco()); ps.setBoolean(4, p.isAtivo()); ps.setLong(5, p.getId());
                ps.executeUpdate();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    public void excluirProduto(Long id) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) { PRODUTOS_MOCK.removeIf(p -> p.getId().equals(id)); return; }
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_produtos WHERE ID_produto=?")) {
                ps.setLong(1, id); ps.executeUpdate();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    public List<String> categoriasProdutos() {
        List<String> cat = new ArrayList<>();
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return PRODUTOS_MOCK.stream().map(ProdutoVenda::getCategoria).distinct().collect(Collectors.toList());
            try(Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT DISTINCT categoria FROM tb_produtos WHERE categoria IS NOT NULL")) {
                while(rs.next()) cat.add(rs.getString(1));
            }
        } catch(Exception e) { e.printStackTrace(); }
        return cat;
    }
}