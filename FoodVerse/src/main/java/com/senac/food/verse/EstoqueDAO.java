package com.senac.food.verse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class EstoqueDAO {

    // ========================================================================
    // 1. MODELOS
    // ========================================================================

    public static class Unidade {
        private final String codigo;     // ex.: g, kg, ml, L, un
        private final String base;       // grupo base
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
        // Campos que faltavam:
        private double estoqueMinimo;
        private double custoMedio;
        private boolean ativo = true;
        private Double fatorCxParaUn; // Opcional, mantido para compatibilidade

        public ItemEstoque() {}
        
        public ItemEstoque(Long id, String nome, String categoria, String unidadePadrao, double estoqueAtual, double custoMedio) {
            this.id = id; this.nome = nome; this.categoria = categoria;
            this.unidadePadrao = unidadePadrao; this.estoqueAtual = estoqueAtual;
            this.custoMedio = custoMedio;
        }

        // Getters e Setters necessários para o Painel
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

    // Classe MovimentacaoEstoque (Estava faltando)
    public static class MovimentacaoEstoque {
        private Long id;
        private Long itemId;
        private String nomeItemSnapshot; // Para histórico se o item for deletado
        private String tipo; // "ENTRADA" ou "SAIDA"
        private double quantidade;
        private String observacao;
        private LocalDateTime dataMovimento;

        public MovimentacaoEstoque() {
            this.dataMovimento = LocalDateTime.now();
        }

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

    // ========================================================================
    // 2. DADOS EM MEMÓRIA (SIMULAÇÃO)
    // ========================================================================
    
    private static final List<ItemEstoque> ITENS = new ArrayList<>();
    private static final List<Unidade> UNIDADES = new ArrayList<>();
    private static final List<MovimentacaoEstoque> MOVIMENTACOES = new ArrayList<>();
    private static final AtomicLong SEQ_ITEM = new AtomicLong(1);
    private static final AtomicLong SEQ_MOV = new AtomicLong(1);

    static {
        // Unidades Padrão
        UNIDADES.add(new Unidade("kg", "g", 1000));
        UNIDADES.add(new Unidade("g", "g", 1));
        UNIDADES.add(new Unidade("L", "ml", 1000));
        UNIDADES.add(new Unidade("ml", "ml", 1));
        UNIDADES.add(new Unidade("un", "un", 1));
        UNIDADES.add(new Unidade("cx", "un", 1)); // Fator variável
        UNIDADES.add(new Unidade("pct", "un", 1));

        // Itens de Exemplo
        ItemEstoque i1 = new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Arroz Branco", "Mercearia", "kg", 50.0, 4.50);
        i1.setEstoqueMinimo(10.0);
        ITENS.add(i1);

        ItemEstoque i2 = new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Coca-Cola Lata", "Bebidas", "un", 120.0, 2.80);
        i2.setEstoqueMinimo(24.0);
        ITENS.add(i2);
        
        ItemEstoque i3 = new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Filé Mignon", "Carnes", "kg", 5.0, 69.90);
        i3.setEstoqueMinimo(8.0); // Estoque baixo para teste
        ITENS.add(i3);
    }

    // ========================================================================
    // 3. MÉTODOS DE SERVIÇO
    // ========================================================================

    public List<Unidade> listarUnidades() { return new ArrayList<>(UNIDADES); }

    public List<String> categoriasNomes() {
        return ITENS.stream().map(ItemEstoque::getCategoria)
                .filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList());
    }

    public List<ItemEstoque> listarItens(String termo, String categoria, String status) {
        String t = termo == null ? "" : termo.toLowerCase();
        boolean filtrarCat = categoria != null && !categoria.equalsIgnoreCase("Todas");
        
        return ITENS.stream()
            .filter(i -> i.getNome().toLowerCase().contains(t))
            .filter(i -> !filtrarCat || i.getCategoria().equalsIgnoreCase(categoria))
            .filter(i -> {
                if("Ativos".equalsIgnoreCase(status)) return i.isAtivo();
                if("Inativos".equalsIgnoreCase(status)) return !i.isAtivo();
                return true;
            })
            .collect(Collectors.toList());
    }

    public ItemEstoque buscarItemPorId(Long id) {
        return ITENS.stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
    }

    public void salvarItem(ItemEstoque item) {
        if(item.getId() == null) item.setId(SEQ_ITEM.getAndIncrement());
        ITENS.add(item);
    }

    public void atualizarItem(ItemEstoque item) {
        for(int i=0; i<ITENS.size(); i++) {
            if(ITENS.get(i).getId().equals(item.getId())) {
                ITENS.set(i, item);
                return;
            }
        }
    }

    // Métodos de Movimentação que faltavam
    public void registrarMovimentacao(MovimentacaoEstoque mov) {
        mov.setId(SEQ_MOV.getAndIncrement());
        if(mov.getDataMovimento() == null) mov.setDataMovimento(LocalDateTime.now());
        
        // Atualiza saldo do item
        ItemEstoque item = buscarItemPorId(mov.getItemId());
        if(item != null) {
            mov.setNomeItemSnapshot(item.getNome());
            if("ENTRADA".equals(mov.getTipo())) {
                item.setEstoqueAtual(item.getEstoqueAtual() + mov.getQuantidade());
            } else {
                item.setEstoqueAtual(item.getEstoqueAtual() - mov.getQuantidade());
            }
        }
        
        MOVIMENTACOES.add(0, mov); // Adiciona no início (mais recente)
    }

    public List<MovimentacaoEstoque> listarUltimasMovimentacoes() {
        return MOVIMENTACOES.stream().limit(100).collect(Collectors.toList());
    }
}