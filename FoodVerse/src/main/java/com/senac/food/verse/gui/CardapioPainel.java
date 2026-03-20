package com.senac.food.verse.gui;

import com.senac.food.verse.CardapioDAO;
import com.senac.food.verse.CardapioDAO.Prato;
import com.senac.food.verse.CardapioDAO.ProdutoVenda;
import com.senac.food.verse.CardapioDAO.ReceitaItem;
import com.senac.food.verse.EstoqueDAO;
import com.senac.food.verse.EstoqueDAO.ItemEstoque;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardapioPainel extends JPanel {

    private static final String PRODUTO_IMAGES_DIR = "media" + File.separator + "produtos";

    private final CardapioDAO dao = new CardapioDAO();
    private final EstoqueDAO estoqueDAO = new EstoqueDAO();
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    // Componentes Globais
    private JTabbedPane tabs;
    
    // ABA PRATOS
    private JTextField txtBuscaPratos;
    private JComboBox<String> cbCategoriaPratos;
    private JComboBox<String> cbStatusPratos;
    private JTable tblPratos;
    private DefaultTableModel pratosModel;

    // ABA PRODUTOS
    private JTextField txtBuscaProdutos;
    private JComboBox<String> cbCategoriaProdutos;
    private JComboBox<String> cbStatusProdutos;
    private JTable tblProdutos;
    private DefaultTableModel produtosModel;

    // Listas Padrão
    private static final String[] CATEGORIAS_PRATOS_PADRAO = {
        "Lanches","Pratos","Bebidas","Sobremesas","Entradas","Acompanhamentos","Combos","Kids"
    };
    private static final String[] CATEGORIAS_PRODUTOS_PADRAO = {
        "Bebidas","Sobremesas","Outros","Mercearia","Descartáveis"
    };

    public CardapioPainel() {
        setLayout(new BorderLayout());
        UIConstants.stylePanel(this);

        add(criarHeader(), BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(UIConstants.FONT_BOLD);
        tabs.setForeground(UIConstants.FG_LIGHT);
        tabs.setBackground(UIConstants.BG_DARK);
        
        tabs.addTab("Pratos & Refeições", criarAbaPratos());
        tabs.addTab("Produtos de Venda", criarAbaProdutos());
        
        add(tabs, BorderLayout.CENTER);

        carregarCombos();
        atualizarTabelas();
    }

    private JPanel criarHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(0, 10, 10, 10));
        
        JLabel lbl = new JLabel("Gestão de Cardápio");
        lbl.setFont(UIConstants.FONT_TITLE);
        lbl.setForeground(UIConstants.FG_LIGHT);
        lbl.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.RESTAURANT_MENU, 32, UIConstants.FG_LIGHT));
        lbl.setIconTextGap(15);
        p.add(lbl);
        return p;
    }

    private JPanel criarAbaPratos() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        txtBuscaPratos = new JTextField(20);
        UIConstants.styleField(txtBuscaPratos);
        txtBuscaPratos.putClientProperty("JTextField.placeholderText", "Buscar prato...");
        txtBuscaPratos.addActionListener(e -> atualizarTabelaPratos());

        cbCategoriaPratos = new JComboBox<>();
        UIConstants.styleCombo(cbCategoriaPratos);

        cbStatusPratos = new JComboBox<>(new String[]{"Todos", "Ativos", "Inativos"});
        UIConstants.styleCombo(cbStatusPratos);

        JButton btnBuscar = criarBotaoIcone(GoogleMaterialDesignIcons.SEARCH, UIConstants.BG_DARK_ALT, "Filtrar Resultados");
        btnBuscar.addActionListener(e -> atualizarTabelaPratos());

        toolbar.add(txtBuscaPratos);
        toolbar.add(cbCategoriaPratos);
        toolbar.add(cbStatusPratos);
        toolbar.add(btnBuscar);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnNovo = new JButton("Novo Prato");
        btnNovo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ADD, 18, UIConstants.SEL_FG));
        UIConstants.stylePrimary(btnNovo);
        btnNovo.addActionListener(e -> novoPrato());

        JButton btnEditar = new JButton("Editar");
        UIConstants.styleSecondary(btnEditar);
        btnEditar.addActionListener(e -> editarPrato());
        
        JButton btnExcluir = new JButton("Excluir");
        UIConstants.styleDanger(btnExcluir);
        btnExcluir.addActionListener(e -> excluirPrato());

        actionPanel.add(btnNovo);
        actionPanel.add(btnEditar);
        actionPanel.add(btnExcluir);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(toolbar, BorderLayout.WEST);
        topContainer.add(actionPanel, BorderLayout.EAST);

        String[] cols = {"ID", "Nome", "Categoria", "Preço", "Status", "Ingredientes"};
        pratosModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Long.class : String.class; }
        };
        tblPratos = new JTable(pratosModel);
        UIConstants.styleTable(tblPratos);
        configurarRenderers(tblPratos);
        
        tblPratos.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tblPratos.rowAtPoint(e.getPoint());
                int col = tblPratos.columnAtPoint(e.getPoint());
                if (row >= 0) {
                    if (col == 4) toggleStatusPrato(row);
                    else if(e.getClickCount() == 2) editarPrato();
                }
            }
        });

        p.add(topContainer, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tblPratos);
        UIConstants.styleScrollPane(scroll);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private JPanel criarAbaProdutos() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        txtBuscaProdutos = new JTextField(20);
        UIConstants.styleField(txtBuscaProdutos);
        txtBuscaProdutos.putClientProperty("JTextField.placeholderText", "Buscar produto...");
        txtBuscaProdutos.addActionListener(e -> atualizarTabelaProdutos());

        cbCategoriaProdutos = new JComboBox<>();
        UIConstants.styleCombo(cbCategoriaProdutos);

        cbStatusProdutos = new JComboBox<>(new String[]{"Todos", "Ativos", "Inativos"});
        UIConstants.styleCombo(cbStatusProdutos);

        JButton btnBuscar = criarBotaoIcone(GoogleMaterialDesignIcons.SEARCH, UIConstants.BG_DARK_ALT, "Filtrar");
        btnBuscar.addActionListener(e -> atualizarTabelaProdutos());

        toolbar.add(txtBuscaProdutos);
        toolbar.add(cbCategoriaProdutos);
        toolbar.add(cbStatusProdutos);
        toolbar.add(btnBuscar);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnNovo = new JButton("Novo Produto");
        btnNovo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ADD, 18, UIConstants.SEL_FG));
        UIConstants.stylePrimary(btnNovo);
        btnNovo.addActionListener(e -> novoProduto());

        JButton btnEditar = new JButton("Editar");
        UIConstants.styleSecondary(btnEditar);
        btnEditar.addActionListener(e -> editarProduto());

        JButton btnExcluir = new JButton("Excluir");
        UIConstants.styleDanger(btnExcluir);
        btnExcluir.addActionListener(e -> excluirProduto());

        actionPanel.add(btnNovo);
        actionPanel.add(btnEditar);
        actionPanel.add(btnExcluir);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(toolbar, BorderLayout.WEST);
        topContainer.add(actionPanel, BorderLayout.EAST);

        String[] cols = {"ID", "Nome", "Categoria", "Preço", "Status"};
        produtosModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Long.class : String.class; }
        };
        tblProdutos = new JTable(produtosModel);
        UIConstants.styleTable(tblProdutos);
        configurarRenderers(tblProdutos);

        tblProdutos.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tblProdutos.rowAtPoint(e.getPoint());
                int col = tblProdutos.columnAtPoint(e.getPoint());
                if (row >= 0) {
                    if (col == 4) toggleStatusProduto(row);
                    else if(e.getClickCount() == 2) editarProduto();
                }
            }
        });

        p.add(topContainer, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tblProdutos);
        UIConstants.styleScrollPane(scroll);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private void carregarCombos() {
        List<String> catsPratos = Stream.concat(Arrays.stream(CATEGORIAS_PRATOS_PADRAO), dao.categoriasPratos().stream())
                .filter(s -> s != null && !s.isBlank()).distinct().sorted().collect(Collectors.toList());
        cbCategoriaPratos.removeAllItems();
        cbCategoriaPratos.addItem("Todas");
        catsPratos.forEach(cbCategoriaPratos::addItem);

        List<String> catsProds = Stream.concat(Arrays.stream(CATEGORIAS_PRODUTOS_PADRAO), dao.categoriasProdutos().stream())
                .filter(s -> s != null && !s.isBlank()).distinct().sorted().collect(Collectors.toList());
        cbCategoriaProdutos.removeAllItems();
        cbCategoriaProdutos.addItem("Todas");
        catsProds.forEach(cbCategoriaProdutos::addItem);
    }

    private void atualizarTabelas() {
        atualizarTabelaPratos();
        atualizarTabelaProdutos();
    }

    private void atualizarTabelaPratos() {
        String termo = txtBuscaPratos.getText();
        String cat = (String) cbCategoriaPratos.getSelectedItem();
        String st = (String) cbStatusPratos.getSelectedItem();
        
        List<Prato> lista = dao.listarPratos(termo, cat, st);
        pratosModel.setRowCount(0);
        for(Prato p : lista) {
            pratosModel.addRow(new Object[]{
                p.getId(), p.getNome(), p.getCategoria(), 
                CURRENCY.format(p.getPreco()), 
                p.isAtivo() ? "Ativo" : "Inativo",
                p.getIngredientes().size() + " itens"
            });
        }
    }

    private void atualizarTabelaProdutos() {
        String termo = txtBuscaProdutos.getText();
        String cat = (String) cbCategoriaProdutos.getSelectedItem();
        String st = (String) cbStatusProdutos.getSelectedItem();
        
        List<ProdutoVenda> lista = dao.listarProdutos(termo, cat, st);
        produtosModel.setRowCount(0);
        for(ProdutoVenda p : lista) {
            produtosModel.addRow(new Object[]{
                p.getId(), p.getNome(), p.getCategoria(), 
                CURRENCY.format(p.getPreco()), 
                p.isAtivo() ? "Ativo" : "Inativo"
            });
        }
    }

    private void toggleStatusPrato(int row) {
        Long id = (Long) tblPratos.getValueAt(row, 0);
        Prato p = dao.buscarPratoPorId(id);
        if(p != null) {
            p.setAtivo(!p.isAtivo());
            dao.atualizarPrato(p);
            atualizarTabelaPratos();
            Toast.show(this, p.getNome() + (p.isAtivo() ? " Ativado" : " Desativado"), Toast.Type.INFO);
        }
    }

    private void toggleStatusProduto(int row) {
        Long id = (Long) tblProdutos.getValueAt(row, 0);
        ProdutoVenda pv = dao.buscarProdutoPorId(id);
        if (pv == null) return;
        pv.setAtivo(!pv.isAtivo());
        dao.atualizarProduto(pv);
        atualizarTabelaProdutos();
        Toast.show(this, pv.getNome() + (pv.isAtivo() ? " ativado" : " desativado"), Toast.Type.INFO);
    }

    private void novoPrato() {
        PratoDialog d = new PratoDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if(d.getResultado() != null) {
            Prato saved = dao.salvarPrato(d.getResultado());
            d.salvarNutricao(saved.getId());
            carregarCombos();
            atualizarTabelaPratos();
            Toast.show(this, "Prato cadastrado!", Toast.Type.SUCCESS);
        }
    }

    private void editarPrato() {
        int row = tblPratos.getSelectedRow();
        if(row < 0) { Toast.show(this, "Selecione um prato para editar.", Toast.Type.WARNING); return; }
        Long id = (Long) tblPratos.getValueAt(row, 0);
        Prato p = dao.buscarPratoPorId(id);
        PratoDialog d = new PratoDialog(SwingUtilities.getWindowAncestor(this), p);
        d.setVisible(true);
        if(d.getResultado() != null) {
            d.getResultado().setId(id);
            dao.atualizarPrato(d.getResultado());
            d.salvarNutricao(id);
            carregarCombos();
            atualizarTabelaPratos();
            Toast.show(this, "Alterações salvas!", Toast.Type.SUCCESS);
        }
    }

    private void excluirPrato() {
        int row = tblPratos.getSelectedRow();
        if(row < 0) { Toast.show(this, "Selecione um prato para excluir.", Toast.Type.WARNING); return; }
        Long id = (Long) tblPratos.getValueAt(row, 0);
        UIConstants.showConfirmDialog(this, "Confirmar exclusão", "Excluir este prato permanentemente?", () -> {
            dao.excluirPrato(id);
            atualizarTabelaPratos();
            Toast.show(this, "Prato removido.", Toast.Type.ERROR);
        });
    }

    private void novoProduto() {
        ProdutoDialog d = new ProdutoDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if(d.getResultado() != null) {
            dao.salvarProduto(d.getResultado());
            carregarCombos();
            atualizarTabelaProdutos();
            Toast.show(this, "Produto cadastrado!", Toast.Type.SUCCESS);
        }
    }

    private void editarProduto() {
        int row = tblProdutos.getSelectedRow();
        if(row < 0) { Toast.show(this, "Selecione um produto.", Toast.Type.WARNING); return; }
        Long id = (Long) tblProdutos.getValueAt(row, 0);
        ProdutoVenda p = dao.buscarProdutoPorId(id);
        if(p == null) return;
        ProdutoDialog d = new ProdutoDialog(SwingUtilities.getWindowAncestor(this), p);
        d.setVisible(true);
        if(d.getResultado() != null) {
            d.getResultado().setId(id);
            dao.atualizarProduto(d.getResultado());
            carregarCombos();
            atualizarTabelaProdutos();
            Toast.show(this, "Produto atualizado!", Toast.Type.SUCCESS);
        }
    }

    private void excluirProduto() {
        int row = tblProdutos.getSelectedRow();
        if(row < 0) { Toast.show(this, "Selecione um produto para excluir.", Toast.Type.WARNING); return; }
        Long id = (Long) tblProdutos.getValueAt(row, 0);
        UIConstants.showConfirmDialog(this, "Confirmar exclusão", "Excluir este produto?", () -> {
            dao.excluirProduto(id);
            atualizarTabelaProdutos();
            Toast.show(this, "Produto removido.", Toast.Type.ERROR);
        });
    }


    // --- DIALOG PRATO ---
    private static final String[] RESTRICOES_OPCOES = {
        "Sem glúten", "Sem lactose", "Vegano", "Vegetariano", "Sem oleaginosas", "Sem açúcar"
    };

    private class PratoDialog extends JDialog {
        private JTextField txtNome;
        private JTextArea txtDescricao;
        private JComboBox<String> cbCategoria;
        private JSpinner spPreco;
        private JSpinner spTempoPreparo;
        private JCheckBox chkAtivo;
        private JTextField txtImagem;
        
        private DefaultTableModel ingModel;
        private JTable tblIng;
        
        private JSpinner spKcal;
        private JTextField txtProteina;
        private JTextField txtCarbo;
        private JTextField txtGordura;
        private final JCheckBox[] chkRestricoes = new JCheckBox[RESTRICOES_OPCOES.length];
        
        private Prato resultado;

        public PratoDialog(Window owner, Prato base) {
            super(owner, base == null ? "Novo Prato / Refeição" : "Editar Prato", ModalityType.APPLICATION_MODAL);
            getContentPane().setBackground(UIConstants.BG_DARK);
            setLayout(new BorderLayout());

            JTabbedPane tabs = new JTabbedPane();
            tabs.setBackground(UIConstants.BG_DARK);
            tabs.setForeground(UIConstants.FG_LIGHT);
            tabs.setFont(UIConstants.FONT_BOLD);

            // ================= ABA 1: INFORMAÇÕES GERAIS =================
            JPanel pnlGeral = new JPanel(new GridBagLayout());
            pnlGeral.setBackground(UIConstants.BG_DARK);
            pnlGeral.setBorder(new EmptyBorder(15, 20, 15, 20));
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(8, 5, 8, 5);
            gc.gridx=0; gc.gridy=0;

            pnlGeral.add(criarLabel("Nome:"), gc);
            gc.gridx=1; gc.weightx=1.0;
            txtNome = new JTextField(); UIConstants.styleField(txtNome);
            pnlGeral.add(txtNome, gc);

            gc.gridx=0; gc.gridy=1; gc.weightx=0; pnlGeral.add(criarLabel("Categoria:"), gc);
            gc.gridx=1;
            cbCategoria = new JComboBox<>(); UIConstants.styleCombo(cbCategoria);
            cbCategoria.setEditable(true); // Autonomia para nova categoria
            for(String s : CATEGORIAS_PRATOS_PADRAO) cbCategoria.addItem(s);
            for(String s : dao.categoriasPratos()) if(((DefaultComboBoxModel)cbCategoria.getModel()).getIndexOf(s) == -1) cbCategoria.addItem(s);
            pnlGeral.add(cbCategoria, gc);

            gc.gridx=0; gc.gridy=2; gc.weightx=0; pnlGeral.add(criarLabel("Preço (R$):"), gc);
            gc.gridx=1;
            JPanel pPreco = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); pPreco.setOpaque(false);
            spPreco = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.5));
            UIConstants.styleSpinner(spPreco); spPreco.setPreferredSize(new Dimension(100, 35));
            pPreco.add(spPreco);
            pPreco.add(Box.createHorizontalStrut(20));
            pPreco.add(criarLabel("Preparo (min):"));
            pPreco.add(Box.createHorizontalStrut(5));
            spTempoPreparo = new JSpinner(new SpinnerNumberModel(0, 0, 180, 1));
            UIConstants.styleSpinner(spTempoPreparo); spTempoPreparo.setPreferredSize(new Dimension(70, 35));
            pPreco.add(spTempoPreparo);
            pnlGeral.add(pPreco, gc);

            gc.gridx=0; gc.gridy=3; gc.weightx=0; pnlGeral.add(criarLabel("Imagem:"), gc);
            gc.gridx=1;
            JPanel pImg = new JPanel(new BorderLayout(8, 0)); pImg.setOpaque(false);
            txtImagem = new JTextField(); UIConstants.styleField(txtImagem);
            txtImagem.setEditable(true); // Permitir URL
            txtImagem.putClientProperty("JTextField.placeholderText", "Ex: https://... ou clique em Escolher");
            pImg.add(txtImagem, BorderLayout.CENTER);
            JButton btnEscolherImg = new JButton("Escolher..."); UIConstants.styleSecondary(btnEscolherImg);
            btnEscolherImg.addActionListener(e -> selecionarImagem(txtImagem));
            pImg.add(btnEscolherImg, BorderLayout.EAST);
            pnlGeral.add(pImg, gc);

            gc.gridx=0; gc.gridy=4; gc.weightx=0; pnlGeral.add(criarLabel("Descrição:"), gc);
            gc.gridx=1;
            txtDescricao = new JTextArea(3, 20);
            txtDescricao.setLineWrap(true); txtDescricao.setWrapStyleWord(true);
            txtDescricao.setBackground(UIConstants.BG_DARK_ALT); txtDescricao.setForeground(UIConstants.FG_LIGHT);
            txtDescricao.setBorder(BorderFactory.createLineBorder(UIConstants.GRID_DARK));
            pnlGeral.add(new JScrollPane(txtDescricao), gc);

            gc.gridx=1; gc.gridy=5;
            chkAtivo = new JCheckBox("Prato Ativo/Disponível no Cardápio");
            chkAtivo.setForeground(UIConstants.FG_LIGHT); chkAtivo.setOpaque(false); chkAtivo.setSelected(true);
            pnlGeral.add(chkAtivo, gc);

            tabs.addTab("Informações Gerais", pnlGeral);


            // ================= ABA 2: FICHA TÉCNICA =================
            JPanel pnlFicha = new JPanel(new BorderLayout(10, 10));
            pnlFicha.setBackground(UIConstants.BG_DARK);
            pnlFicha.setBorder(new EmptyBorder(15, 15, 15, 15));

            JPanel headerFicha = new JPanel(new FlowLayout(FlowLayout.LEFT)); headerFicha.setOpaque(false);
            JLabel lblInfoFicha = new JLabel("Adicione os insumos que compõem este prato para baixa automática.");
            lblInfoFicha.setForeground(UIConstants.FG_MUTED);
            headerFicha.add(lblInfoFicha);
            pnlFicha.add(headerFicha, BorderLayout.NORTH);

            String[] iCols = {"ID Produto", "Insumo", "Un.", "Qtd. Padrão"};
            ingModel = new DefaultTableModel(iCols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            tblIng = new JTable(ingModel); UIConstants.styleTable(tblIng);
            JScrollPane scrollIng = new JScrollPane(tblIng); UIConstants.styleScrollPane(scrollIng);
            pnlFicha.add(scrollIng, BorderLayout.CENTER);

            JPanel pIngBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); pIngBtns.setOpaque(false);
            JButton btnAddIng = new JButton("Adicionar Insumo"); UIConstants.styleSecondary(btnAddIng);
            btnAddIng.addActionListener(e -> abrirSeletorIngredientes());
            JButton btnRemIng = new JButton("Remover"); UIConstants.styleSecondary(btnRemIng); 
            btnRemIng.addActionListener(e -> {
                int r = tblIng.getSelectedRow();
                if(r >= 0) ingModel.removeRow(r); else Toast.show(this, "Selecione um ingrediente.", Toast.Type.WARNING);
            });
            pIngBtns.add(btnAddIng); pIngBtns.add(btnRemIng);
            pnlFicha.add(pIngBtns, BorderLayout.SOUTH);

            tabs.addTab("Ficha Técnica (Ingredientes)", pnlFicha);


            // ================= ABA 3: NUTRIÇÃO & RESTRIÇÕES =================
            JPanel pnlNutri = new JPanel(new GridBagLayout());
            pnlNutri.setBackground(UIConstants.BG_DARK);
            pnlNutri.setBorder(new EmptyBorder(15, 20, 15, 20));
            GridBagConstraints gn = new GridBagConstraints();
            gn.fill = GridBagConstraints.HORIZONTAL; gn.insets = new Insets(10, 5, 10, 5);
            gn.gridx=0; gn.gridy=0;

            pnlNutri.add(criarLabel("Calorias (kcal):"), gn); gn.gridx=1;
            spKcal = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
            UIConstants.styleSpinner(spKcal); pnlNutri.add(spKcal, gn);

            gn.gridx=0; gn.gridy=1; pnlNutri.add(criarLabel("Proteína (g):"), gn); gn.gridx=1;
            txtProteina = new JTextField(); UIConstants.styleField(txtProteina); pnlNutri.add(txtProteina, gn);

            gn.gridx=0; gn.gridy=2; pnlNutri.add(criarLabel("Carboidrato (g):"), gn); gn.gridx=1;
            txtCarbo = new JTextField(); UIConstants.styleField(txtCarbo); pnlNutri.add(txtCarbo, gn);

            gn.gridx=0; gn.gridy=3; pnlNutri.add(criarLabel("Gordura (g):"), gn); gn.gridx=1;
            txtGordura = new JTextField(); UIConstants.styleField(txtGordura); pnlNutri.add(txtGordura, gn);

            gn.gridx=0; gn.gridy=4; gn.gridwidth=2; gn.insets = new Insets(20, 5, 5, 5);
            JLabel lblRestr = new JLabel("Restrições Alimentares:");
            lblRestr.setForeground(UIConstants.FG_LIGHT); lblRestr.setFont(UIConstants.FONT_BOLD);
            pnlNutri.add(lblRestr, gn);

            gn.gridy=5; gn.insets = new Insets(0, 5, 10, 5);
            JPanel pRestr = new JPanel(new GridLayout(2, 3, 10, 10)); pRestr.setOpaque(false);
            for(int i = 0; i < RESTRICOES_OPCOES.length; i++) {
                chkRestricoes[i] = new JCheckBox(RESTRICOES_OPCOES[i]);
                chkRestricoes[i].setForeground(UIConstants.FG_LIGHT); chkRestricoes[i].setOpaque(false);
                pRestr.add(chkRestricoes[i]);
            }
            pnlNutri.add(pRestr, gn);

            tabs.addTab("Nutrição & Dietas", pnlNutri);

            add(tabs, BorderLayout.CENTER);

            // ================= FOOTER (BOTÕES) =================
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.setBackground(UIConstants.BG_DARK);
            footer.setBorder(new EmptyBorder(10, 20, 15, 20));

            JButton btnCancel = new JButton("Cancelar"); UIConstants.styleSecondary(btnCancel);
            btnCancel.addActionListener(e -> dispose());
            JButton btnSave = new JButton("Salvar Prato"); UIConstants.styleSuccess(btnSave);
            btnSave.addActionListener(e -> salvar());

            footer.add(btnCancel); footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);

            if(base != null) carregarDados(base);

            configurarDialogResponsivo(this, owner, 580, 500);
        }

        private void abrirSeletorIngredientes() {
            JDialog d = new JDialog(this, "Selecionar do Estoque", true);
            d.getContentPane().setBackground(UIConstants.BG_DARK);
            d.setLayout(new BorderLayout());

            DefaultTableModel tm = new DefaultTableModel(new String[]{"ID Produto", "Insumo", "Un.", "Estoque Atual"}, 0);
            JTable t = new JTable(tm); UIConstants.styleTable(t);
            
            for(ItemEstoque ie : estoqueDAO.listarItens("", "Todas", "Ativos")) {
                if(ie.getProdutoId() != null) {
                    tm.addRow(new Object[]{ie.getProdutoId(), ie.getNome(), ie.getUnidadePadrao(), EstoquePainel.formatarQuantidade(ie.getEstoqueAtual(), ie.getUnidadePadrao())});
                }
            }

            d.add(new JScrollPane(t), BorderLayout.CENTER);

            JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            pBottom.setBackground(UIConstants.BG_DARK);
            JLabel lQtd = new JLabel("Qtd na receita (ex: 0.050 para 50g):"); 
            lQtd.setForeground(UIConstants.FG_LIGHT);
            
            JSpinner sQtd = new JSpinner(new SpinnerNumberModel(1.0, 0.001, 1000.0, 0.010));
            sQtd.setEditor(new JSpinner.NumberEditor(sQtd, "0.000"));
            sQtd.setPreferredSize(new Dimension(100, 30));
            UIConstants.styleSpinner(sQtd);
            
            JButton bAdd = new JButton("Adicionar"); UIConstants.styleSuccess(bAdd);
            bAdd.addActionListener(e -> {
                int r = t.getSelectedRow();
                if(r >= 0) {
                    ingModel.addRow(new Object[]{
                        t.getValueAt(r, 0), 
                        t.getValueAt(r, 1), 
                        t.getValueAt(r, 2), 
                        sQtd.getValue()
                    });
                    d.dispose();
                } else Toast.show(d, "Selecione um item", Toast.Type.WARNING);
            });
            pBottom.add(lQtd); pBottom.add(sQtd); pBottom.add(bAdd);
            d.add(pBottom, BorderLayout.SOUTH);
            configurarDialogResponsivo(d, this, 550, 450); d.setVisible(true);
        }

        private void salvar() {
            if(txtNome.getText().trim().isEmpty()) { Toast.show(this, "Nome obrigatório", Toast.Type.WARNING); return; }
            double preco = (Double)spPreco.getValue();
            if(preco < 0.01) { Toast.show(this, "Preço deve ser maior que R$ 0,00", Toast.Type.WARNING); return; }

            resultado = new Prato();
            resultado.setNome(txtNome.getText().trim());
            resultado.setDescricao(txtDescricao.getText().trim());
            resultado.setCategoria(cbCategoria.getSelectedItem() != null ? cbCategoria.getSelectedItem().toString() : "");
            resultado.setPreco(preco);
            resultado.setTempoPreparo((Integer)spTempoPreparo.getValue());
            resultado.setAtivo(chkAtivo.isSelected());

            String imgInput = txtImagem.getText().trim();
            resultado.setImagem(imgInput.isEmpty() ? null : imgInput);
            
            List<String> tags = new ArrayList<>();
            for(int i = 0; i < RESTRICOES_OPCOES.length; i++) if(chkRestricoes[i].isSelected()) tags.add(RESTRICOES_OPCOES[i]);
            resultado.setRestricoes(tags.isEmpty() ? null : String.join(",", tags));

            for(int i=0; i<ingModel.getRowCount(); i++) {
                Long id = (Long) ingModel.getValueAt(i, 0);
                String nome = (String) ingModel.getValueAt(i, 1);
                String un = (String) ingModel.getValueAt(i, 2);
                Double qtd = (Double) ingModel.getValueAt(i, 3);
                resultado.getIngredientes().add(new ReceitaItem(id, nome, un, qtd));
            }
            dispose();
        }

        private void carregarDados(Prato p) {
            txtNome.setText(p.getNome());
            if(p.getDescricao() != null) txtDescricao.setText(p.getDescricao());
            cbCategoria.setSelectedItem(p.getCategoria());
            spPreco.setValue(p.getPreco());
            spTempoPreparo.setValue(p.getTempoPreparo());
            chkAtivo.setSelected(p.isAtivo());
            
            if(p.getImagem() != null) txtImagem.setText(p.getImagem());

            for(ReceitaItem ri : p.getIngredientes()) {
                ingModel.addRow(new Object[]{ri.getItemEstoqueId(), ri.getItemNome(), ri.getUnidade(), ri.getQuantidade()});
            }
            
            if(p.getRestricoes() != null && !p.getRestricoes().isEmpty()) {
                Set<String> tags = new HashSet<>(Arrays.asList(p.getRestricoes().split(",")));
                for(int i = 0; i < RESTRICOES_OPCOES.length; i++) chkRestricoes[i].setSelected(tags.contains(RESTRICOES_OPCOES[i].trim()));
            }
            
            if(p.getId() != null) {
                CardapioDAO.Nutricao nut = dao.buscarNutricao(p.getId());
                if(nut != null) {
                    if(nut.getKcal() != null) spKcal.setValue(nut.getKcal());
                    if(nut.getProteina() != null) txtProteina.setText(nut.getProteina());
                    if(nut.getCarbo() != null) txtCarbo.setText(nut.getCarbo());
                    if(nut.getGordura() != null) txtGordura.setText(nut.getGordura());
                }
            }
        }

        void salvarNutricao(Long produtoId) {
            if(produtoId == null) return;
            dao.salvarOuAtualizarNutricao(produtoId, (Integer)spKcal.getValue(), txtProteina.getText().trim(), txtCarbo.getText().trim(), txtGordura.getText().trim());
        }

        public Prato getResultado() { return resultado; }
    }
    

    // --- DIALOG PRODUTO ---
    private class ProdutoDialog extends JDialog {
        private JTextField txtNome;
        private JTextArea txtDescricao;
        private JComboBox<String> cbCategoria;
        private JSpinner spPreco;
        private JCheckBox chkAtivo;
        private JTextField txtImagem;
        private ProdutoVenda resultado;
        private final JCheckBox[] chkRestr = new JCheckBox[RESTRICOES_OPCOES.length];

        public ProdutoDialog(Window owner, ProdutoVenda base) {
            super(owner, base == null ? "Novo Produto Simples" : "Editar Produto", ModalityType.APPLICATION_MODAL);
            getContentPane().setBackground(UIConstants.BG_DARK);
            setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UIConstants.BG_DARK);
            form.setBorder(new EmptyBorder(20, 20, 20, 20));
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0; gc.insets = new Insets(6, 0, 4, 0);
            gc.gridx = 0; gc.gridy = 0;

            form.add(criarLabel("Nome do Produto (Bebida, Sobremesa, etc):"), gc); gc.gridy++;
            txtNome = new JTextField(); UIConstants.styleField(txtNome); form.add(txtNome, gc);

            gc.gridy++; form.add(criarLabel("Categoria:"), gc); gc.gridy++;
            cbCategoria = new JComboBox<>(CATEGORIAS_PRODUTOS_PADRAO);
            UIConstants.styleCombo(cbCategoria); cbCategoria.setEditable(true);
            form.add(cbCategoria, gc);

            gc.gridy++; form.add(criarLabel("Descrição:"), gc); gc.gridy++;
            txtDescricao = new JTextArea(2, 20); txtDescricao.setLineWrap(true); txtDescricao.setWrapStyleWord(true);
            txtDescricao.setBackground(UIConstants.BG_DARK_ALT); txtDescricao.setForeground(UIConstants.FG_LIGHT);
            txtDescricao.setBorder(BorderFactory.createLineBorder(UIConstants.GRID_DARK));
            form.add(new JScrollPane(txtDescricao), gc);

            gc.gridy++; form.add(criarLabel("Preço de Venda:"), gc); gc.gridy++;
            JPanel pRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); pRow.setOpaque(false);
            spPreco = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.5));
            UIConstants.styleSpinner(spPreco); spPreco.setPreferredSize(new Dimension(120, 35));
            pRow.add(spPreco); pRow.add(Box.createHorizontalStrut(20));
            chkAtivo = new JCheckBox("Produto Ativo"); chkAtivo.setForeground(UIConstants.FG_LIGHT);
            chkAtivo.setOpaque(false); chkAtivo.setSelected(true); pRow.add(chkAtivo);
            form.add(pRow, gc);

            gc.gridy++; form.add(criarLabel("Imagem (URL da web ou Arquivo Local):"), gc); gc.gridy++;
            txtImagem = new JTextField(); UIConstants.styleField(txtImagem);
            txtImagem.setEditable(true);
            txtImagem.putClientProperty("JTextField.placeholderText", "Ex: https://... ou clique em Escolher");
            JPanel pImg = new JPanel(new BorderLayout(8, 0)); pImg.setOpaque(false);
            pImg.add(txtImagem, BorderLayout.CENTER);
            JButton btnEscolherImg = new JButton("Escolher..."); UIConstants.styleSecondary(btnEscolherImg);
            btnEscolherImg.addActionListener(e -> selecionarImagem(txtImagem));
            pImg.add(btnEscolherImg, BorderLayout.EAST);
            form.add(pImg, gc);

            gc.gridy++; form.add(criarLabel("Restrições Alimentares:"), gc); gc.gridy++;
            JPanel pRestr = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2)); pRestr.setOpaque(false);
            for(int i = 0; i < RESTRICOES_OPCOES.length; i++) {
                chkRestr[i] = new JCheckBox(RESTRICOES_OPCOES[i]);
                chkRestr[i].setForeground(UIConstants.FG_LIGHT); chkRestr[i].setOpaque(false);
                pRestr.add(chkRestr[i]);
            }
            form.add(pRestr, gc);

            JScrollPane scrollForm = new JScrollPane(form);
            scrollForm.setBorder(null); scrollForm.setOpaque(false); scrollForm.getViewport().setOpaque(false);
            add(scrollForm, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.setBackground(UIConstants.BG_DARK); footer.setBorder(new EmptyBorder(10, 20, 15, 20));
            JButton btnCancel = new JButton("Cancelar"); UIConstants.styleSecondary(btnCancel);
            btnCancel.addActionListener(e -> dispose());
            JButton btnSave = new JButton("Salvar Produto"); UIConstants.styleSuccess(btnSave);
            
            btnSave.addActionListener(e -> {
                if(txtNome.getText().trim().isEmpty()) { Toast.show(this, "Nome é obrigatório", Toast.Type.WARNING); return; }
                double preco = (Double)spPreco.getValue();
                if(preco < 0.01) { Toast.show(this, "Preço inválido", Toast.Type.WARNING); return; }
                resultado = new ProdutoVenda(null, txtNome.getText().trim(),
                        cbCategoria.getSelectedItem().toString(), chkAtivo.isSelected(), preco);
                resultado.setDescricao(txtDescricao.getText().trim());
                
                String imgInput = txtImagem.getText().trim();
                resultado.setImagem(imgInput.isEmpty() ? null : imgInput);

                List<String> tags = new ArrayList<>();
                for(int i = 0; i < RESTRICOES_OPCOES.length; i++) if(chkRestr[i].isSelected()) tags.add(RESTRICOES_OPCOES[i]);
                resultado.setRestricoes(tags.isEmpty() ? null : String.join(",", tags));
                dispose();
            });

            footer.add(btnCancel); footer.add(btnSave); add(footer, BorderLayout.SOUTH);

            if(base != null) {
                txtNome.setText(base.getNome()); cbCategoria.setSelectedItem(base.getCategoria());
                spPreco.setValue(base.getPreco()); chkAtivo.setSelected(base.isAtivo());
                if(base.getDescricao() != null) txtDescricao.setText(base.getDescricao());
                
                if(base.getImagem() != null) txtImagem.setText(base.getImagem());

                if(base.getRestricoes() != null && !base.getRestricoes().isEmpty()) {
                    Set<String> rtags = new HashSet<>(Arrays.asList(base.getRestricoes().split(",")));
                    for(int i = 0; i < RESTRICOES_OPCOES.length; i++) chkRestr[i].setSelected(rtags.contains(RESTRICOES_OPCOES[i].trim()));
                }
            }
            configurarDialogResponsivo(this, owner, 540, 560);
        }
        public ProdutoVenda getResultado() { return resultado; }
    }
   
    // UTILS
    private JLabel criarLabel(String texto) {
        JLabel l = new JLabel(texto); l.setForeground(UIConstants.FG_MUTED); l.setFont(UIConstants.FONT_BOLD); return l;
    }

    private void configurarDialogResponsivo(JDialog dialog, Component owner, int minWidth, int minHeight) {
        dialog.pack();
        dialog.setMinimumSize(new Dimension(minWidth, minHeight));
        int largura = Math.max(dialog.getWidth(), minWidth);
        int altura = Math.max(dialog.getHeight(), minHeight);
        if (owner != null) {
            Dimension base = owner.getSize();
            if (base.width > 0 && base.height > 0) {
                largura = Math.min(largura, (int) (base.width * 0.95));
                altura = Math.min(altura, (int) (base.height * 0.95));
            }
        }
        dialog.setSize(largura, altura);
        dialog.setLocationRelativeTo(owner);
    }

    private void selecionarImagem(JTextField campo) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar imagem do produto");
        chooser.setFileFilter(new FileNameExtensionFilter("Imagens (JPG, PNG, GIF, WEBP)", "jpg", "jpeg", "png", "gif", "webp"));
        chooser.setAcceptAllFileFilterUsed(false);
        if(chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File origem = chooser.getSelectedFile();
        try {
            String nomeArquivo = origem.toPath().getFileName().toString();
            String nomeFinal = System.currentTimeMillis() + "_" + nomeArquivo;
            Path dir = Path.of(PRODUTO_IMAGES_DIR);
            Files.createDirectories(dir);
            Path destino = dir.resolve(nomeFinal);
            Files.copy(origem.toPath(), destino);
            campo.setText(PRODUTO_IMAGES_DIR + File.separator + nomeFinal);
        } catch(IOException ex) {
            Toast.show(this, "Erro ao copiar imagem: " + ex.getMessage(), Toast.Type.ERROR);
        }
    }

    private JButton criarBotaoIcone(GoogleMaterialDesignIcons icone, Color bg, String tooltip) {
        JButton b = new JButton(IconFontSwing.buildIcon(icone, 18, UIConstants.FG_LIGHT));
        b.setBackground(bg); b.setBorder(BorderFactory.createLineBorder(UIConstants.GRID_DARK));
        b.setFocusPainted(false); b.setPreferredSize(new Dimension(40, 35));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setToolTipText(tooltip); return b;
    }

    private void configurarRenderers(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? UIConstants.BG_DARK : UIConstants.ALT_ROW);
                setHorizontalAlignment(column == 0 ? CENTER : LEFT);
                
                if (column == 4) {
                    JLabel l = (JLabel) c; l.setHorizontalAlignment(CENTER); l.setFont(UIConstants.FONT_BOLD);
                    if ("Ativo".equals(value)) { l.setForeground(UIConstants.SUCCESS_GREEN); l.setText("● Ativo"); }
                    else { l.setForeground(UIConstants.FG_MUTED); l.setText("● Inativo"); }
                    l.setToolTipText("Clique para alterar o status");
                    l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else if (column == 3) setHorizontalAlignment(RIGHT);
                
                return c;
            }
        });
    }
}
