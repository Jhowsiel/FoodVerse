package com.senac.food.verse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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

    private boolean semContextoOperacional() {
        SessionContext ctx = SessionContext.getInstance();
        return ctx.isAdmin() && ctx.getRestauranteEfetivo() <= 0;
    }

    private int restauranteEfetivo() {
        return SessionContext.getInstance().getRestauranteEfetivo();
    }

    private ItemEstoque montarItem(ResultSet rs) throws Exception {
        ItemEstoque item = new ItemEstoque(
                rs.getLong("ID_estoque"),
                rs.getString("nome"),
                rs.getString("categoria"),
                "un",
                rs.getDouble("estoque_atual"),
                0.0);
        item.setEstoqueMinimo(rs.getDouble("estoque_minimo"));
        item.setAtivo(rs.getBoolean("ativo"));
        return item;
    }

    private void registrarHistoricoLocal(ItemEstoque item, MovimentacaoEstoque mov) {
        mov.setNomeItemSnapshot(item != null ? item.getNome() : mov.getNomeItemSnapshot());
        mov.setDataMovimento(LocalDateTime.now());
        MOVS_MOCK.add(0, mov);
        while (MOVS_MOCK.size() > 100) {
            MOVS_MOCK.remove(MOVS_MOCK.size() - 1);
        }
    }

    public List<Unidade> listarUnidades() { 
        return UNIDADES_MOCK;
    }

    public List<String> categoriasNomes() {
        if (semContextoOperacional()) {
            return Collections.emptyList();
        }
        List<String> categorias = new ArrayList<>();
        String sql = "SELECT DISTINCT p.categoria FROM tb_produtos p " +
                     "JOIN tb_estoque e ON e.ID_produto = p.ID_produto " +
                     "WHERE p.categoria IS NOT NULL";
        int rid = restauranteEfetivo();
        if (rid > 0) {
            sql += " AND p.ID_restaurante = ?";
        }
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return ITENS_MOCK.stream().map(ItemEstoque::getCategoria).distinct().collect(Collectors.toList());
            try(PreparedStatement st = conn.prepareStatement(sql)) {
                if (rid > 0) {
                    st.setInt(1, rid);
                }
                try(ResultSet rs = st.executeQuery()) {
                while(rs.next()) categorias.add(rs.getString("categoria"));
            }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return categorias;
    }

    public List<ItemEstoque> listarItens(String termo, String categoria, String status) {
        if (semContextoOperacional()) {
            return Collections.emptyList();
        }
        List<ItemEstoque> itens = new ArrayList<>();
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) {
                String t = termo == null ? "" : termo.toLowerCase();
                boolean fCat = categoria != null && !categoria.equalsIgnoreCase("Todas");
                return ITENS_MOCK.stream().filter(i -> i.getNome().toLowerCase().contains(t))
                    .filter(i -> !fCat || i.getCategoria().equalsIgnoreCase(categoria))
                    .filter(i -> "Ativos".equalsIgnoreCase(status) ? i.isAtivo() : ("Inativos".equalsIgnoreCase(status) ? !i.isAtivo() : true))
                    .collect(Collectors.toList());
            }

            StringBuilder sql = new StringBuilder(
                "SELECT e.ID_estoque, p.nome_produto AS nome, p.categoria, " +
                "e.quantidade AS estoque_atual, e.estoque_minimo, p.disponivel AS ativo " +
                "FROM tb_estoque e JOIN tb_produtos p ON e.ID_produto = p.ID_produto " +
                "WHERE p.nome_produto LIKE ?"
            );
            int rid = SessionContext.getInstance().getRestauranteEfetivo();
            if (rid > 0) sql.append(" AND p.ID_restaurante = ?");
            if(categoria != null && !categoria.equalsIgnoreCase("Todas")) sql.append(" AND p.categoria = ?");
            if("Ativos".equalsIgnoreCase(status)) sql.append(" AND p.disponivel = 1");
            if("Inativos".equalsIgnoreCase(status)) sql.append(" AND p.disponivel = 0");

            try(PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int p = 1;
                ps.setString(p++, "%" + (termo==null?"":termo) + "%");
                if (rid > 0) ps.setInt(p++, rid);
                if(categoria != null && !categoria.equalsIgnoreCase("Todas")) ps.setString(p, categoria);
                
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        itens.add(montarItem(rs));
                    }
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return itens;
    }

    public ItemEstoque buscarItemPorId(Long id) {
        if (semContextoOperacional()) {
            return null;
        }
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) return ITENS_MOCK.stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
            
            String sql = "SELECT e.ID_estoque, p.nome_produto AS nome, p.categoria, " +
                         "e.quantidade AS estoque_atual, e.estoque_minimo, p.disponivel AS ativo " +
                         "FROM tb_estoque e JOIN tb_produtos p ON e.ID_produto = p.ID_produto " +
                         "WHERE e.ID_estoque = ?";
            int rid = restauranteEfetivo();
            if (rid > 0) {
                sql += " AND p.ID_restaurante = ?";
            }
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                if (rid > 0) {
                    ps.setInt(2, rid);
                }
                try(ResultSet rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return montarItem(rs);
                    }
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean salvarItem(ItemEstoque item) {
        if (semContextoOperacional()) {
            return false;
        }
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) {
                item.setId(SEQ_ITEM.getAndIncrement());
                ITENS_MOCK.add(item);
                return true;
            }
            int rid = restauranteEfetivo();
            if (rid <= 0) {
                return false;
            }
            String sqlProd = "INSERT INTO tb_produtos (ID_restaurante, nome_produto, categoria, disponivel) VALUES (?, ?, ?, ?)";
            try(PreparedStatement psProd = conn.prepareStatement(sqlProd, Statement.RETURN_GENERATED_KEYS)) {
                psProd.setInt(1, rid);
                psProd.setString(2, item.getNome());
                psProd.setString(3, item.getCategoria());
                psProd.setBoolean(4, item.isAtivo());
                psProd.executeUpdate();
                try(ResultSet gk = psProd.getGeneratedKeys()) {
                    if(gk.next()) {
                        long idProduto = gk.getLong(1);
                        String sqlEst = "INSERT INTO tb_estoque (ID_produto, quantidade, estoque_minimo, ultima_atualizacao) VALUES (?, ?, ?, GETDATE())";
                        try(PreparedStatement psEst = conn.prepareStatement(sqlEst, Statement.RETURN_GENERATED_KEYS)) {
                            psEst.setLong(1, idProduto);
                            psEst.setDouble(2, item.getEstoqueAtual());
                            psEst.setDouble(3, item.getEstoqueMinimo());
                            psEst.executeUpdate();
                            try(ResultSet gkEst = psEst.getGeneratedKeys()) {
                                if(gkEst.next()) {
                                    item.setId(gkEst.getLong(1));
                                }
                            }
                        }
                    }
                }
            }
            return true;
        } catch(Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean atualizarItem(ItemEstoque item) {
        if(item.getId() == null || semContextoOperacional()) return false;
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            if(conn == null) {
                for(int i=0; i<ITENS_MOCK.size(); i++) {
                    if(ITENS_MOCK.get(i).getId().equals(item.getId())) { ITENS_MOCK.set(i, item); return true; }
                }
                return false;
            }
            String sqlEst = "UPDATE tb_estoque SET estoque_minimo=?, ultima_atualizacao=GETDATE() WHERE ID_estoque=?";
            try(PreparedStatement ps = conn.prepareStatement(sqlEst)) {
                ps.setDouble(1, item.getEstoqueMinimo());
                ps.setLong(2, item.getId());
                ps.executeUpdate();
            }
            String sqlProd = "UPDATE tb_produtos SET nome_produto=?, categoria=?, disponivel=? " +
                             "WHERE ID_produto = (SELECT ID_produto FROM tb_estoque WHERE ID_estoque=?)";
            int rid = restauranteEfetivo();
            if (rid > 0) {
                sqlProd += " AND ID_restaurante = ?";
            }
            try(PreparedStatement ps = conn.prepareStatement(sqlProd)) {
                ps.setString(1, item.getNome());
                ps.setString(2, item.getCategoria());
                ps.setBoolean(3, item.isAtivo());
                ps.setLong(4, item.getId());
                if (rid > 0) {
                    ps.setInt(5, rid);
                }
                ps.executeUpdate();
            }
            return true;
        } catch(Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean registrarMovimentacao(MovimentacaoEstoque mov) {
        if (mov == null || mov.getItemId() == null || semContextoOperacional()) {
            return false;
        }
        ConexaoBanco cb = new ConexaoBanco();
        try(Connection conn = cb.abrirConexao()) {
            ItemEstoque item = buscarItemPorId(mov.getItemId());
            if (item == null) {
                return false;
            }
            double novoSaldo = item.getEstoqueAtual() + ("ENTRADA".equals(mov.getTipo()) ? mov.getQuantidade() : -mov.getQuantidade());
            if (novoSaldo < 0) {
                return false;
            }
            if(conn == null) {
                item.setEstoqueAtual(novoSaldo);
                registrarHistoricoLocal(item, mov);
                return true;
            }
            
            String sqlUpd = "UPDATE tb_estoque SET quantidade = quantidade + ?, ultima_atualizacao = GETDATE() WHERE ID_estoque = ?";
            if (!"ENTRADA".equals(mov.getTipo())) {
                sqlUpd = "UPDATE tb_estoque SET quantidade = quantidade - ?, ultima_atualizacao = GETDATE() " +
                        "WHERE ID_estoque = ? AND quantidade >= ?";
            }
            try(PreparedStatement ps = conn.prepareStatement(sqlUpd)) {
                ps.setDouble(1, mov.getQuantidade());
                ps.setLong(2, mov.getItemId());
                if (!"ENTRADA".equals(mov.getTipo())) {
                    ps.setDouble(3, mov.getQuantidade());
                }
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    return false;
                }
            }
            registrarHistoricoLocal(item, mov);
            return true;
        } catch(Exception e) { e.printStackTrace(); }
        return false;
    }

    public List<MovimentacaoEstoque> listarUltimasMovimentacoes() {
        return MOVS_MOCK.stream().limit(100).collect(Collectors.toList());
    }
}
