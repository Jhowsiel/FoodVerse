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
        private String descricao;
        private String imagem;
        private int tempoPreparo;
        private String tipoProduto = "VENDA";
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
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public String getImagem() { return imagem; }
        public void setImagem(String imagem) { this.imagem = imagem; }
        public int getTempoPreparo() { return tempoPreparo; }
        public void setTempoPreparo(int tempoPreparo) { this.tempoPreparo = tempoPreparo; }
        public String getTipoProduto() { return tipoProduto; }
        public void setTipoProduto(String tipoProduto) { this.tipoProduto = tipoProduto; }
        public List<ReceitaItem> getIngredientes() { return ingredientes; }
        private String restricoes;
        public String getRestricoes() { return restricoes; }
        public void setRestricoes(String restricoes) { this.restricoes = restricoes; }
    }

    public static class ProdutoVenda {
        private Long id;
        private String nome;
        private String categoria;
        private boolean ativo = true;
        private double preco;
        private String descricao;
        private String imagem;

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
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public String getImagem() { return imagem; }
        public void setImagem(String imagem) { this.imagem = imagem; }
        private String restricoes;
        public String getRestricoes() { return restricoes; }
        public void setRestricoes(String restricoes) { this.restricoes = restricoes; }
    }
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

            StringBuilder sql = new StringBuilder("SELECT * FROM tb_produtos WHERE nome_produto LIKE ? AND COALESCE(tipo_produto, 'VENDA') = 'VENDA'");
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
                        pr.setDescricao(rs.getString("descricao"));
                        pr.setImagem(rs.getString("imagem"));
                        pr.setTempoPreparo(rs.getInt("tempo_preparo"));
                        try { pr.setRestricoes(rs.getString("restricoes")); } catch(Exception ignored) {}
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

            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            String sql = "INSERT INTO tb_produtos (ID_restaurante, nome_produto, categoria, descricao, preco, disponivel, imagem, tempo_preparo, restricoes, tipo_produto) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'VENDA')";
            try(PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setObject(1, rid > 0 ? rid : null);
                ps.setString(2, prato.getNome());
                ps.setString(3, prato.getCategoria());
                ps.setString(4, prato.getDescricao());
                ps.setDouble(5, prato.getPreco());
                ps.setBoolean(6, prato.isAtivo());
                ps.setString(7, prato.getImagem());
                ps.setInt(8, prato.getTempoPreparo());
                ps.setString(9, prato.getRestricoes());
                ps.executeUpdate();
                
                try(ResultSet rs = ps.getGeneratedKeys()) {
                    if(rs.next()) prato.setId(rs.getLong(1));
                }
            }
            // Persist recipe ingredients
            salvarReceita(conn, prato.getId(), prato.getIngredientes());
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
            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            StringBuilder sql = new StringBuilder("UPDATE tb_produtos SET nome_produto=?, categoria=?, descricao=?, preco=?, disponivel=?, imagem=?, tempo_preparo=?, restricoes=? WHERE ID_produto=?");
            if (rid > 0) sql.append(" AND ID_restaurante = ?");
            try(PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setString(1, prato.getNome());
                ps.setString(2, prato.getCategoria());
                ps.setString(3, prato.getDescricao());
                ps.setDouble(4, prato.getPreco());
                ps.setBoolean(5, prato.isAtivo());
                ps.setString(6, prato.getImagem());
                ps.setInt(7, prato.getTempoPreparo());
                ps.setString(8, prato.getRestricoes());
                ps.setLong(9, prato.getId());
                if (rid > 0) ps.setInt(10, rid);
                ps.executeUpdate();
            }
            // Update recipe ingredients
            salvarReceita(conn, prato.getId(), prato.getIngredientes());
        } catch(Exception e) { e.printStackTrace(); }
    }

    public void excluirPrato(Long id) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) { PRATOS_MOCK.removeIf(p -> p.getId().equals(id)); return; }
            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            StringBuilder sql = new StringBuilder("DELETE FROM tb_produtos WHERE ID_produto=?");
            if (rid > 0) sql.append(" AND ID_restaurante = ?");
            try(PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setLong(1, id);
                if (rid > 0) ps.setInt(2, rid);
                ps.executeUpdate();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    public Prato buscarPratoPorId(Long id) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return PRATOS_MOCK.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
            
            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            StringBuilder sql = new StringBuilder("SELECT * FROM tb_produtos WHERE ID_produto=?");
            if (rid > 0) sql.append(" AND ID_restaurante = ?");
            try(PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setLong(1, id);
                if (rid > 0) ps.setInt(2, rid);
                try(ResultSet rs = ps.executeQuery()) {
                    if(rs.next()) {
                        Prato pr = new Prato(rs.getLong("ID_produto"), rs.getString("nome_produto"),
                                    rs.getString("categoria"), rs.getBoolean("disponivel"), rs.getDouble("preco"));
                        pr.setDescricao(rs.getString("descricao"));
                        pr.setImagem(rs.getString("imagem"));
                        pr.setTempoPreparo(rs.getInt("tempo_preparo"));
                        try { pr.setRestricoes(rs.getString("restricoes")); } catch(Exception ignored) {}
                        // Load recipe ingredients
                        carregarReceita(conn, pr);
                        return pr;
                    }
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
            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            String sql = "SELECT DISTINCT categoria FROM tb_produtos WHERE categoria IS NOT NULL AND COALESCE(tipo_produto, 'VENDA') = 'VENDA'"
                    + (rid > 0 ? " AND ID_restaurante = ?" : "");
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                if (rid > 0) ps.setInt(1, rid);
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) cat.add(rs.getString(1));
                }
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

            StringBuilder sql = new StringBuilder("SELECT * FROM tb_produtos WHERE nome_produto LIKE ? AND COALESCE(tipo_produto, 'VENDA') = 'VENDA'");
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
                        ProdutoVenda pv = new ProdutoVenda(rs.getLong("ID_produto"), rs.getString("nome_produto"),
                                rs.getString("categoria"), rs.getBoolean("disponivel"), rs.getDouble("preco"));
                        pv.setDescricao(rs.getString("descricao"));
                        pv.setImagem(rs.getString("imagem"));
                        try { pv.setRestricoes(rs.getString("restricoes")); } catch(Exception ignored) {}
                        prods.add(pv);
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

            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            String sql = "INSERT INTO tb_produtos (ID_restaurante, nome_produto, categoria, descricao, preco, disponivel, imagem, restricoes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try(PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setObject(1, rid > 0 ? rid : null);
                ps.setString(2, p.getNome());
                ps.setString(3, p.getCategoria());
                ps.setString(4, p.getDescricao());
                ps.setDouble(5, p.getPreco());
                ps.setBoolean(6, p.isAtivo());
                ps.setString(7, p.getImagem());
                ps.setString(8, p.getRestricoes());
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
            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            StringBuilder sql = new StringBuilder("UPDATE tb_produtos SET nome_produto=?, categoria=?, descricao=?, preco=?, disponivel=?, imagem=?, restricoes=? WHERE ID_produto=?");
            if (rid > 0) sql.append(" AND ID_restaurante = ?");
            try(PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setString(1, p.getNome());
                ps.setString(2, p.getCategoria());
                ps.setString(3, p.getDescricao());
                ps.setDouble(4, p.getPreco());
                ps.setBoolean(5, p.isAtivo());
                ps.setString(6, p.getImagem());
                ps.setString(7, p.getRestricoes());
                ps.setLong(8, p.getId());
                if (rid > 0) ps.setInt(9, rid);
                ps.executeUpdate();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    public void excluirProduto(Long id) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) { PRODUTOS_MOCK.removeIf(p -> p.getId().equals(id)); return; }
            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            StringBuilder sql = new StringBuilder("DELETE FROM tb_produtos WHERE ID_produto=?");
            if (rid > 0) sql.append(" AND ID_restaurante = ?");
            try(PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setLong(1, id);
                if (rid > 0) ps.setInt(2, rid);
                ps.executeUpdate();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    public ProdutoVenda buscarProdutoPorId(Long id) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return PRODUTOS_MOCK.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            StringBuilder sql = new StringBuilder("SELECT * FROM tb_produtos WHERE ID_produto=?");
            if (rid > 0) sql.append(" AND ID_restaurante = ?");
            try(PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setLong(1, id);
                if (rid > 0) ps.setInt(2, rid);
                try(ResultSet rs = ps.executeQuery()) {
                    if(rs.next()) {
                        ProdutoVenda pv = new ProdutoVenda(rs.getLong("ID_produto"), rs.getString("nome_produto"),
                                rs.getString("categoria"), rs.getBoolean("disponivel"), rs.getDouble("preco"));
                        pv.setDescricao(rs.getString("descricao"));
                        pv.setImagem(rs.getString("imagem"));
                        try { pv.setRestricoes(rs.getString("restricoes")); } catch(Exception ignored) {}
                        return pv;
                    }
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }

    public List<String> categoriasProdutos() {
        List<String> cat = new ArrayList<>();
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return PRODUTOS_MOCK.stream().map(ProdutoVenda::getCategoria).distinct().collect(Collectors.toList());
            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            String sql = "SELECT DISTINCT categoria FROM tb_produtos WHERE categoria IS NOT NULL AND COALESCE(tipo_produto, 'VENDA') = 'VENDA'"
                    + (rid > 0 ? " AND ID_restaurante = ?" : "");
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                if (rid > 0) ps.setInt(1, rid);
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) cat.add(rs.getString(1));
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return cat;
    }

    // ================== NUTRIÇÃO ==================

    public static class Nutricao {
        private Long id;
        private Long produtoId;
        private Integer kcal;
        private String proteina;
        private String carbo;
        private String gordura;

        public Nutricao() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProdutoId() { return produtoId; }
        public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
        public Integer getKcal() { return kcal; }
        public void setKcal(Integer kcal) { this.kcal = kcal; }
        public String getProteina() { return proteina; }
        public void setProteina(String proteina) { this.proteina = proteina; }
        public String getCarbo() { return carbo; }
        public void setCarbo(String carbo) { this.carbo = carbo; }
        public String getGordura() { return gordura; }
        public void setGordura(String gordura) { this.gordura = gordura; }
    }

    public Nutricao buscarNutricao(Long produtoId) {
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return null;
            String sql = "SELECT * FROM tb_nutricao WHERE ID_produto = ?";
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, produtoId);
                try(ResultSet rs = ps.executeQuery()) {
                    if(rs.next()) {
                        Nutricao n = new Nutricao();
                        n.setId(rs.getLong("ID_nutricao"));
                        n.setProdutoId(produtoId);
                        n.setKcal(rs.getObject("kcal") != null ? rs.getInt("kcal") : null);
                        n.setProteina(rs.getString("proteina"));
                        n.setCarbo(rs.getString("carbo"));
                        n.setGordura(rs.getString("gordura"));
                        return n;
                    }
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }

    public void salvarOuAtualizarNutricao(Long produtoId, Integer kcal, String proteina, String carbo, String gordura) {
        if(produtoId == null) return;
        boolean vazio = (kcal == null || kcal == 0)
                && (proteina == null || proteina.isEmpty())
                && (carbo == null || carbo.isEmpty())
                && (gordura == null || gordura.isEmpty());
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return;
            Nutricao existente = buscarNutricao(produtoId);
            if(vazio) {
                if(existente != null) {
                    try(PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_nutricao WHERE ID_nutricao = ?")) {
                        ps.setLong(1, existente.getId());
                        ps.executeUpdate();
                    }
                }
                return;
            }
            if(existente != null) {
                try(PreparedStatement ps = conn.prepareStatement("UPDATE tb_nutricao SET kcal=?, proteina=?, carbo=?, gordura=? WHERE ID_nutricao=?")) {
                    ps.setObject(1, kcal);
                    ps.setString(2, proteina);
                    ps.setString(3, carbo);
                    ps.setString(4, gordura);
                    ps.setLong(5, existente.getId());
                    ps.executeUpdate();
                }
            } else {
                try(PreparedStatement ps = conn.prepareStatement("INSERT INTO tb_nutricao (ID_produto, kcal, proteina, carbo, gordura) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setLong(1, produtoId);
                    ps.setObject(2, kcal);
                    ps.setString(3, proteina);
                    ps.setString(4, carbo);
                    ps.setString(5, gordura);
                    ps.executeUpdate();
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    // ================== RECEITA (BOM) ==================

    private void carregarReceita(Connection conn, Prato prato) {
        if(prato == null || prato.getId() == null) return;
        try {
            String sql = "SELECT r.ID_insumo, p.nome_produto, r.unidade, r.quantidade " +
                         "FROM tb_receitas r JOIN tb_produtos p ON r.ID_insumo = p.ID_produto " +
                         "WHERE r.ID_produto_venda = ? AND r.ativo = 1";
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, prato.getId());
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        prato.getIngredientes().add(new ReceitaItem(
                            rs.getLong("ID_insumo"),
                            rs.getString("nome_produto"),
                            rs.getString("unidade"),
                            rs.getDouble("quantidade")
                        ));
                    }
                }
            }
        } catch(Exception e) {
            System.out.println("Aviso: Erro ao carregar receita do produto " + prato.getId() + ": " + e.getMessage());
        }
    }

    private void salvarReceita(Connection conn, Long produtoVendaId, List<ReceitaItem> ingredientes) {
        if(produtoVendaId == null) return;
        try {
            // Remove old recipe entries
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_receitas WHERE ID_produto_venda = ?")) {
                ps.setLong(1, produtoVendaId);
                ps.executeUpdate();
            }
            // Insert new entries
            if(ingredientes != null && !ingredientes.isEmpty()) {
                String sql = "INSERT INTO tb_receitas (ID_produto_venda, ID_insumo, quantidade, unidade) VALUES (?, ?, ?, ?)";
                try(PreparedStatement ps = conn.prepareStatement(sql)) {
                    for(ReceitaItem ri : ingredientes) {
                        ps.setLong(1, produtoVendaId);
                        ps.setLong(2, ri.getItemEstoqueId());
                        ps.setDouble(3, ri.getQuantidade());
                        ps.setString(4, ri.getUnidade());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        } catch(Exception e) {
            System.out.println("Aviso: Erro ao salvar receita do produto " + produtoVendaId + ": " + e.getMessage());
        }
    }

    /**
     * Returns the recipe (BOM) for a product by its ID.
     * Used by PedidoDAO for stock deduction.
     */
    public List<ReceitaItem> buscarReceitaPorProduto(Long produtoId) {
        List<ReceitaItem> items = new ArrayList<>();
        if(produtoId == null) return items;
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return items;
            String sql = "SELECT r.ID_insumo, p.nome_produto, r.unidade, r.quantidade " +
                         "FROM tb_receitas r JOIN tb_produtos p ON r.ID_insumo = p.ID_produto " +
                         "WHERE r.ID_produto_venda = ? AND r.ativo = 1";
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, produtoId);
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        items.add(new ReceitaItem(
                            rs.getLong("ID_insumo"),
                            rs.getString("nome_produto"),
                            rs.getString("unidade"),
                            rs.getDouble("quantidade")
                        ));
                    }
                }
            }
        } catch(Exception ignored) {}
        return items;
    }
}