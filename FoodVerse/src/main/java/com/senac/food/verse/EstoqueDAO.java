package com.senac.food.verse;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class EstoqueDAO {

    // ========================================================================
    // 1. MODELOS (Originais + Aliases de Compatibilidade)
    // ========================================================================

    public static class Unidade {
        private final String codigo;     // ex.: g, kg, ml, L, un, pct, cx
        private final String base;       // grupo base (g/ml/un)
        private final double fatorBase;  // multiplicador para a base (kg=1000 -> g)
        
        public Unidade(String codigo, String base, double fatorBase) {
            this.codigo = codigo; this.base = base; this.fatorBase = fatorBase;
        }
        
        public String getCodigo() { return codigo; }
        public String getBase()   { return base; }
        public double getFatorBase() { return fatorBase; }
        
        @Override
        public String toString() { return codigo; } // Importante para ComboBox
    }

    public static class CategoriaEstoque {
        private final Long id;
        private final String nome;
        private final boolean ativo;
        public CategoriaEstoque(Long id, String nome, boolean ativo) {
            this.id=id; this.nome=nome; this.ativo=ativo;
        }
        public Long getId() { return id; }
        public String getNome() { return nome; }
        public boolean isAtivo() { return ativo; }
    }

    public static class ItemEstoque {
        private Long id;
        private String nome;
        private String categoria;     // nome da categoria
        private String unidadePadrao; // un, g, ml...
        private boolean ativo = true;
        private double estoqueAtual;  // quantidade na unidade padrão
        private double custoMedio;    // custo médio atual (por unidade padrão)
        private Double fatorCxParaUn; // 1 cx/pct = X un (opcional)

        public ItemEstoque() {}
        public ItemEstoque(Long id, String nome, String categoria, String unidadePadrao,
                           boolean ativo, double estoqueAtual, double custoMedio, Double fatorCxParaUn) {
            this.id=id; this.nome=nome; this.categoria=categoria; this.unidadePadrao=unidadePadrao;
            this.ativo=ativo; this.estoqueAtual=estoqueAtual; this.custoMedio=custoMedio; this.fatorCxParaUn=fatorCxParaUn;
        }
        public Long getId() { return id; }
        public String getNome() { return nome; }
        public String getCategoria() { return categoria; }
        public String getUnidadePadrao() { return unidadePadrao; }
        public boolean isAtivo() { return ativo; }
        public double getEstoqueAtual() { return estoqueAtual; }
        public double getCustoMedio() { return custoMedio; }
        public Double getFatorCxParaUn() { return fatorCxParaUn; }
        
        // --- ALIAS DE COMPATIBILIDADE (Para TelaInicial funcionar) ---
        public double getQuantidadeAtual() { return estoqueAtual; }
    }

    // ========================================================================
    // 2. DADOS EM MEMÓRIA (SIMULAÇÃO DE BANCO)
    // ========================================================================
    
    private static final List<CategoriaEstoque> CATEGORIAS = new ArrayList<>();
    private static final Map<String, Unidade> UNIDADES = new LinkedHashMap<>();
    private static final List<ItemEstoque> ITENS = new ArrayList<>();
    private static final AtomicLong SEQ_ITEM = new AtomicLong(1);

    static {
        // Categorias padrão
        String[] cats = {
            "Carnes","Hortifruti","Laticínios","Secos e Molhados","Bebidas","Temperos/Especiarias",
            "Embalagens","Limpeza","Congelados","Padaria/Massas","Molhos/Conservas","Descartáveis",
            "Higiene","Mercearia"
        };
        long cid=1;
        for (String c : cats) CATEGORIAS.add(new CategoriaEstoque(cid++, c, true));

        // Unidades e bases (Configuração completa para conversão)
        UNIDADES.put("g",  new Unidade("g",  "g", 1.0));
        UNIDADES.put("kg", new Unidade("kg", "g", 1000.0));
        UNIDADES.put("ml", new Unidade("ml","ml", 1.0));
        UNIDADES.put("L",  new Unidade("L", "ml", 1000.0));
        UNIDADES.put("un", new Unidade("un","un", 1.0));
        UNIDADES.put("pct",new Unidade("pct","un", 0)); // conversão por item
        UNIDADES.put("cx", new Unidade("cx", "un", 0)); // conversão por item

        // Seeds de Itens (Dados iniciais para não abrir vazio)
        ITENS.add(new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Pão de hambúrguer", "Padaria/Massas", "un", true, 120, 1.20, 12.0));
        ITENS.add(new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Carne bovina 160g", "Carnes", "g", true, 25000, 0.045, null));
        ITENS.add(new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Queijo cheddar (fatia)", "Laticínios", "un", true, 300, 0.80, 24.0));
        ITENS.add(new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Alface", "Hortifruti", "g", true, 8000, 0.012, null));
        ITENS.add(new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Molho Caesar", "Molhos/Conservas", "ml", true, 5000, 0.010, null));
        ITENS.add(new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Croutons", "Mercearia", "g", true, 1500, 0.030, null));
        ITENS.add(new ItemEstoque(SEQ_ITEM.getAndIncrement(), "Coca-Cola Lata", "Bebidas", "un", true, 150, 2.50, 12.0));
    }

    // ========================================================================
    // 3. MÉTODOS DE SERVIÇO
    // ========================================================================

    // Listagens
    public List<CategoriaEstoque> listarCategorias(boolean apenasAtivas) {
        return CATEGORIAS.stream().filter(c -> !apenasAtivas || c.isAtivo()).collect(Collectors.toList());
    }

    public List<String> categoriasNomes() {
        return CATEGORIAS.stream().map(CategoriaEstoque::getNome).collect(Collectors.toList());
    }
    
    // Alias para o novo Dashboard
    public List<String> categoriasEstoque() {
        return categoriasNomes();
    }

    public List<Unidade> listarUnidades() { 
        return new ArrayList<>(UNIDADES.values()); 
    }

    public List<ItemEstoque> listarItens(String termo, String categoria, String status) {
        String t = termo == null ? "" : termo.trim().toLowerCase();
        boolean filtraCat = categoria != null && !categoria.equalsIgnoreCase("Todas") && !categoria.isEmpty();
        boolean filtraStatus = status != null && !status.equalsIgnoreCase("Todos");
        
        return ITENS.stream()
            .filter(i -> (String.valueOf(i.getId()).contains(t)) ||
                         i.getNome().toLowerCase().contains(t) ||
                         (i.getCategoria()!=null && i.getCategoria().toLowerCase().contains(t)))
            .filter(i -> !filtraCat || (i.getCategoria()!=null && i.getCategoria().equalsIgnoreCase(categoria)))
            .filter(i -> !filtraStatus || (status.equalsIgnoreCase("Ativos") ? i.isAtivo() : !i.isAtivo()))
            .sorted(Comparator.comparing(ItemEstoque::getNome, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }
    
    // --- ALIAS DE COMPATIBILIDADE (Para TelaInicial funcionar) ---
    public List<ItemEstoque> buscarTodos() {
        return new ArrayList<>(ITENS);
    }

    // Básicos
    public ItemEstoque buscarItemPorId(Long id) {
        return ITENS.stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
    }

    public boolean unidadeExiste(String cod) { return UNIDADES.containsKey(cod); }

    // Conversões simples
    public double converterQuantidade(double quantidade, String de, String para, ItemEstoque itemContexto) {
        if (de == null || para == null || de.equalsIgnoreCase(para)) return quantidade;
        Unidade uDe = UNIDADES.get(de);
        Unidade uPara = UNIDADES.get(para);
        if (uDe == null || uPara == null) return quantidade;

        // Mesmo grupo (ex.: g<->kg, ml<->L)
        if (Objects.equals(uDe.getBase(), uPara.getBase()) && uDe.getFatorBase() > 0 && uPara.getFatorBase() > 0) {
            double emBase = quantidade * uDe.getFatorBase();
            return emBase / uPara.getFatorBase();
        }

        // Conversões por item (cx/pct <-> un)
        if (itemContexto != null && itemContexto.getFatorCxParaUn() != null) {
            double f = itemContexto.getFatorCxParaUn();
            if (f > 0) {
                if (de.equals("cx") && para.equals("un")) return quantidade * f;
                if (de.equals("un") && para.equals("cx")) return quantidade / f;
                if (de.equals("pct") && para.equals("un")) return quantidade * f;
                if (de.equals("un") && para.equals("pct")) return quantidade / f;
            }
        }

        return quantidade;
    }
    
    // Métodos Mock para Salvar/Editar (evita erros em tempo de execução)
    public void salvarItem(ItemEstoque item) { 
        System.out.println("Simulação: Item salvo no ArrayList.");
        if(item.getId() == null) {
            // Em um caso real atribuiriamos ID, mas aqui o objeto já vem do construtor ou mock
        }
        if(!ITENS.contains(item)) ITENS.add(item);
    }
    
    public void atualizarItem(ItemEstoque item) {
        System.out.println("Simulação: Item atualizado.");
        // Como é referência em memória, setar os valores no objeto já atualiza na lista
    }
    
    public void excluirItem(Long id) {
        ITENS.removeIf(i -> i.getId().equals(id));
        System.out.println("Simulação: Item removido.");
    }
}