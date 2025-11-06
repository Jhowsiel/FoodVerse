package com.senac.food.verse;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


public class CardapioDAO {

    // MODELOS (internos para manter simples)
    public static class ReceitaItem {
        private Long itemEstoqueId; // futuro vínculo com o módulo de estoque
        private String itemNome;
        private String unidade; // ex.: g, kg, ml, un
        private double quantidade;

        public ReceitaItem() {}
        public ReceitaItem(Long itemEstoqueId, String itemNome, String unidade, double quantidade) {
            this.itemEstoqueId = itemEstoqueId;
            this.itemNome = itemNome;
            this.unidade = unidade;
            this.quantidade = quantidade;
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
        private String categoria; // ex.: Lanches, Pratos, Bebidas
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

    // "Banco" em memória
    private static final List<Prato> PRATOS = new ArrayList<>();
    private static final List<ProdutoVenda> PRODUTOS = new ArrayList<>();
    private static final AtomicLong SEQ_PRATO = new AtomicLong(1);
    private static final AtomicLong SEQ_PROD = new AtomicLong(1000);

    static {
        // Seeds de produtos
        PRODUTOS.add(new ProdutoVenda(SEQ_PROD.getAndIncrement(), "Coca-Cola Lata 350ml", "Bebidas", true, 7.00));
        PRODUTOS.add(new ProdutoVenda(SEQ_PROD.getAndIncrement(), "Água Mineral 500ml", "Bebidas", true, 4.00));
        PRODUTOS.add(new ProdutoVenda(SEQ_PROD.getAndIncrement(), "Bolo de Chocolate (fatia)", "Sobremesas", true, 9.50));

        // Seeds de pratos
        Prato p1 = new Prato(SEQ_PRATO.getAndIncrement(), "Hambúrguer Clássico", "Lanches", true, 24.90);
        p1.getIngredientes().add(new ReceitaItem(null, "Pão de hambúrguer", "un", 1));
        p1.getIngredientes().add(new ReceitaItem(null, "Carne bovina 160g", "g", 160));
        p1.getIngredientes().add(new ReceitaItem(null, "Queijo cheddar", "fatia", 1));
        PRATOS.add(p1);

        Prato p2 = new Prato(SEQ_PRATO.getAndIncrement(), "Salada Caesar", "Pratos", true, 29.90);
        p2.getIngredientes().add(new ReceitaItem(null, "Alface", "g", 120));
        p2.getIngredientes().add(new ReceitaItem(null, "Molho Caesar", "ml", 40));
        p2.getIngredientes().add(new ReceitaItem(null, "Croutons", "g", 30));
        PRATOS.add(p2);
    }

    // PRATOS
    public List<Prato> listarPratos(String termo, String categoria, String status) {
        String t = termo == null ? "" : termo.trim().toLowerCase();
        boolean filtraCategoria = categoria != null && !categoria.equalsIgnoreCase("Todas");
        boolean filtraStatus = status != null && !status.equalsIgnoreCase("Todos");

        return PRATOS.stream()
            .filter(p -> p.getNome().toLowerCase().contains(t)
                || (p.getCategoria() != null && p.getCategoria().toLowerCase().contains(t)))
            .filter(p -> !filtraCategoria || (p.getCategoria() != null && p.getCategoria().equalsIgnoreCase(categoria)))
            .filter(p -> !filtraStatus || (status.equalsIgnoreCase("Ativos") ? p.isAtivo() : !p.isAtivo()))
            .collect(Collectors.toList());
    }

    public Prato salvarPrato(Prato prato) {
        prato.setId(SEQ_PRATO.getAndIncrement());
        PRATOS.add(prato);
        return prato;
    }

    public void atualizarPrato(Prato prato) {
        if (prato.getId() == null) return;
        for (int i = 0; i < PRATOS.size(); i++) {
            if (PRATOS.get(i).getId().equals(prato.getId())) {
                PRATOS.set(i, prato);
                return;
            }
        }
    }

    public void excluirPrato(Long id) {
        PRATOS.removeIf(p -> p.getId().equals(id));
    }

    public Prato buscarPratoPorId(Long id) {
        return PRATOS.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    public List<String> categoriasPratos() {
        return PRATOS.stream()
            .map(Prato::getCategoria)
            .filter(c -> c != null && !c.isBlank())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    // PRODUTOS
    public List<ProdutoVenda> listarProdutos(String termo, String categoria, String status) {
        String t = termo == null ? "" : termo.trim().toLowerCase();
        boolean filtraCategoria = categoria != null && !categoria.equalsIgnoreCase("Todas");
        boolean filtraStatus = status != null && !status.equalsIgnoreCase("Todos");

        return PRODUTOS.stream()
            .filter(p -> p.getNome().toLowerCase().contains(t)
                || (p.getCategoria() != null && p.getCategoria().toLowerCase().contains(t)))
            .filter(p -> !filtraCategoria || (p.getCategoria() != null && p.getCategoria().equalsIgnoreCase(categoria)))
            .filter(p -> !filtraStatus || (status.equalsIgnoreCase("Ativos") ? p.isAtivo() : !p.isAtivo()))
            .collect(Collectors.toList());
    }

    public ProdutoVenda salvarProduto(ProdutoVenda p) {
        p.setId(SEQ_PROD.getAndIncrement());
        PRODUTOS.add(p);
        return p;
    }

    public void atualizarProduto(ProdutoVenda p) {
        for (int i = 0; i < PRODUTOS.size(); i++) {
            if (PRODUTOS.get(i).getId().equals(p.getId())) {
                PRODUTOS.set(i, p); return;
            }
        }
    }

    public void excluirProduto(Long id) {
        PRODUTOS.removeIf(p -> p.getId().equals(id));
    }

    public List<String> categoriasProdutos() {
        return PRODUTOS.stream()
            .map(ProdutoVenda::getCategoria)
            .filter(c -> c != null && !c.isBlank())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}