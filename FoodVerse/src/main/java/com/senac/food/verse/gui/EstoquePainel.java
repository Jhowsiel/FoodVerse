package com.senac.food.verse.gui;

import com.senac.food.verse.EstoqueDAO;
import com.senac.food.verse.EstoqueDAO.ItemEstoque;
import com.senac.food.verse.EstoqueDAO.MovimentacaoEstoque;
import com.senac.food.verse.EstoqueDAO.Unidade;
import com.senac.food.verse.SessionContext;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EstoquePainel extends JPanel {

    private final EstoqueDAO dao = new EstoqueDAO();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final SessionContext sessionContext = SessionContext.getInstance();
    private final boolean podeEditar = PermissionChecker.canEditInventory(sessionContext);
    private final boolean possuiContextoRestaurante = PermissionChecker.hasOperationalRestaurantContext(sessionContext);
    private static final String[] CATEGORIAS_ESTOQUE_PADRAO = {
        "Carnes", "Aves", "Peixes/Frutos do Mar", "Laticínios", "Hortifruti", 
        "Mercearia Seca", "Bebidas", "Embalagens", "Limpeza", "Temperos/Especiarias"
    };

    private JTabbedPane tabs;
    private JTextField txtBusca;
    private JComboBox<String> cbCategoria;
    private JComboBox<String> cbStatus;
    private JTable tblItens;
    private DefaultTableModel modelItens;
    private JLabel lblStatusGeral;
    private JButton btnNovo;
    private JButton btnMovimentar;
    private JButton btnEditar;
    private final Map<Long, ItemEstoque> itensVisiveis = new HashMap<>();

    private JTable tblHistorico;
    private DefaultTableModel modelHistorico;

    public EstoquePainel() {
        setLayout(new BorderLayout());
        UIConstants.stylePanel(this);

        add(criarHeader(), BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(UIConstants.FONT_BOLD);
        tabs.setForeground(UIConstants.FG_LIGHT);
        tabs.setBackground(UIConstants.BG_DARK);

        tabs.addTab("Inventário", criarAbaItens());
        tabs.addTab("Histórico de Movimentações", criarAbaHistorico());

        add(tabs, BorderLayout.CENTER);

        carregarCombos();
        atualizarTabelas();
        
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) atualizarHistorico();
        });
    }

    public static String formatarQuantidade(double qtd, String un) {
        if (un == null) un = "un";
        if (un.equalsIgnoreCase("un") || un.equalsIgnoreCase("cx") || un.equalsIgnoreCase("lata") || qtd % 1 == 0) {
            return String.format(Locale.US, "%.0f %s", qtd, un);
        } else {
            return String.format(Locale.US, "%.2f %s", qtd, un);
        }
    }

    private JPanel criarHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel tituloPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tituloPanel.setOpaque(false);
        
        JLabel lbl = new JLabel("Controle de Estoque");
        lbl.setFont(UIConstants.FONT_TITLE);
        lbl.setForeground(UIConstants.FG_LIGHT);
        lbl.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.STORE, 32, UIConstants.FG_LIGHT));
        lbl.setIconTextGap(15);
        tituloPanel.add(lbl);

        lblStatusGeral = new JLabel("Carregando...");
        lblStatusGeral.setFont(UIConstants.FONT_BOLD);
        lblStatusGeral.setForeground(UIConstants.FG_MUTED);
        lblStatusGeral.setBorder(new EmptyBorder(10, 0, 0, 20));

        p.add(tituloPanel, BorderLayout.WEST);
        p.add(lblStatusGeral, BorderLayout.EAST);
        return p;
    }

    private JPanel criarAbaItens() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        txtBusca = new JTextField(20);
        UIConstants.styleField(txtBusca);
        txtBusca.putClientProperty("JTextField.placeholderText", "Buscar insumo...");
        txtBusca.addActionListener(e -> atualizarTabelaItens());

        cbCategoria = new JComboBox<>();
        UIConstants.styleCombo(cbCategoria);

        cbStatus = new JComboBox<>(new String[]{"Todos", "Ativos", "Inativos", "Baixo Estoque"});
        UIConstants.styleCombo(cbStatus);

        JButton btnBuscar = criarBotaoIcone(GoogleMaterialDesignIcons.SEARCH, UIConstants.BG_DARK_ALT, "Filtrar");
        btnBuscar.addActionListener(e -> atualizarTabelaItens());

        toolbar.add(txtBusca);
        toolbar.add(cbCategoria);
        toolbar.add(cbStatus);
        toolbar.add(btnBuscar);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        btnNovo = new JButton("Novo Item");
        btnNovo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ADD_BOX, 18, UIConstants.SEL_FG));
        UIConstants.stylePrimary(btnNovo);
        btnNovo.addActionListener(e -> novoItem());

        btnMovimentar = new JButton("Entrada / Saída"); 
        btnMovimentar.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SWAP_VERT, 18, UIConstants.FG_LIGHT));
        UIConstants.styleSecondary(btnMovimentar);
        btnMovimentar.addActionListener(e -> movimentarEstoqueSelecionado());

        // CORREÇÃO: Ícone adicionado ao botão Editar
        btnEditar = new JButton("Editar Item");
        btnEditar.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.EDIT, 18, UIConstants.FG_LIGHT));
        UIConstants.styleSecondary(btnEditar);
        btnEditar.addActionListener(e -> editarItem());

        if (!podeEditar) {
            btnNovo.setVisible(false);
            btnMovimentar.setVisible(false);
            btnEditar.setVisible(false);
        }

        actionPanel.add(btnNovo);
        actionPanel.add(btnMovimentar);
        actionPanel.add(btnEditar);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(toolbar, BorderLayout.WEST);
        topContainer.add(actionPanel, BorderLayout.EAST);

        String[] cols = {"ID", "Insumo / Item", "Categoria", "Em Estoque", "Estoque Mín.", "Status"};
        modelItens = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            // CORREÇÃO: Removido o override do getColumnClass para garantir que nosso Custom Renderer seja lido.
        };

        tblItens = new JTable(modelItens);
        UIConstants.styleTable(tblItens);
        configurarRenderersItens();

        tblItens.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && podeEditar) editarItem();
            }
        });

        p.add(topContainer, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tblItens);
        UIConstants.styleScrollPane(scroll);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private JPanel criarAbaHistorico() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel headerHist = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerHist.setOpaque(false);
        JLabel lblInfo = new JLabel("Histórico persistente de entradas e saídas");
        lblInfo.setForeground(UIConstants.FG_MUTED);
        lblInfo.setFont(UIConstants.FONT_REGULAR);
        JButton btnRefresh = criarBotaoIcone(GoogleMaterialDesignIcons.REFRESH, UIConstants.BG_DARK_ALT, "Atualizar Lista");
        btnRefresh.addActionListener(e -> atualizarHistorico());
        
        headerHist.add(btnRefresh);
        headerHist.add(lblInfo);
        
        p.add(headerHist, BorderLayout.NORTH);

        String[] cols = {"Data/Hora", "Item", "Operação", "Qtd Movimentada", "Motivo/Obs"};
        modelHistorico = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        tblHistorico = new JTable(modelHistorico);
        UIConstants.styleTable(tblHistorico);
        
        tblHistorico.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if(!isSelected) c.setBackground(row % 2 == 0 ? UIConstants.BG_DARK : UIConstants.ALT_ROW);
                setHorizontalAlignment(CENTER);

                if(column == 2) { 
                    String tipo = (String) value;
                    if("ENTRADA".equals(tipo)) setForeground(UIConstants.SUCCESS_GREEN);
                    else if("SAIDA".equals(tipo)) setForeground(UIConstants.PRIMARY_RED);
                    else setForeground(UIConstants.FG_LIGHT);
                    setFont(UIConstants.FONT_BOLD);
                } else if(column == 3) {
                    setForeground(UIConstants.FG_LIGHT); setFont(UIConstants.FONT_BOLD);
                } else {
                    setForeground(UIConstants.FG_LIGHT);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tblHistorico);
        UIConstants.styleScrollPane(scroll);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private void carregarCombos() {
        cbCategoria.removeAllItems();
        cbCategoria.addItem("Todas");
        dao.categoriasNomes().forEach(cbCategoria::addItem);
    }

    private void atualizarTabelas() {
        atualizarTabelaItens();
        atualizarHistorico();
    }

    private void atualizarTabelaItens() {
        if (!possuiContextoRestaurante) {
            itensVisiveis.clear(); modelItens.setRowCount(0);
            lblStatusGeral.setText("Selecione um restaurante para operar o estoque");
            lblStatusGeral.setForeground(UIConstants.FG_MUTED);
            return;
        }

        String termo = txtBusca.getText();
        String cat = (String) cbCategoria.getSelectedItem();
        String status = (String) cbStatus.getSelectedItem();

        String statusDAO = "Todos";
        if("Ativos".equals(status)) statusDAO = "Ativos";
        if("Inativos".equals(status)) statusDAO = "Inativos";

        List<ItemEstoque> lista = dao.listarItens(termo, cat, statusDAO);
        
        modelItens.setRowCount(0);
        itensVisiveis.clear();
        int criticos = 0;

        for (ItemEstoque item : lista) {
            boolean isBaixo = item.getEstoqueAtual() <= item.getEstoqueMinimo();
            if ("Baixo Estoque".equals(status) && !isBaixo) continue;
            
            if(isBaixo && item.isAtivo()) criticos++;
            itensVisiveis.put(item.getId(), item);

            modelItens.addRow(new Object[]{
                    item.getId(),
                    item.getNome(),
                    item.getCategoria(),
                    item.getEstoqueAtual(),
                    item.getEstoqueMinimo(),
                    item.isAtivo() ? (isBaixo ? "Crítico" : "Ativo") : "Inativo"
            });
        }
        
        if(criticos > 0) {
            lblStatusGeral.setText("⚠ " + criticos + " itens exigem reposição");
            lblStatusGeral.setForeground(UIConstants.PRIMARY_RED);
        } else if (!podeEditar) {
            lblStatusGeral.setText("Consulta liberada para este perfil");
            lblStatusGeral.setForeground(UIConstants.FG_MUTED);
        } else {
            lblStatusGeral.setText("Estoque Saudável");
            lblStatusGeral.setForeground(UIConstants.SUCCESS_GREEN);
        }
    }

    private void atualizarHistorico() {
        modelHistorico.setRowCount(0);
        List<MovimentacaoEstoque> movs = dao.listarUltimasMovimentacoes();
        if(movs != null) {
            for(MovimentacaoEstoque m : movs) {
                ItemEstoque item = dao.buscarItemPorId(m.getItemId());
                String un = item != null ? item.getUnidadePadrao() : "un";
                modelHistorico.addRow(new Object[]{
                    m.getDataMovimento().format(DATE_FMT),
                    m.getNomeItemSnapshot(),
                    m.getTipo(),
                    formatarQuantidade(m.getQuantidade(), un),
                    m.getObservacao()
                });
            }
        }
    }

    private void novoItem() {
        if (!podeEditar) { Toast.show(this, "Acesso somente leitura.", Toast.Type.WARNING); return; }
        ItemDialog d = new ItemDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if (d.getResultado() != null) {
            if (dao.salvarItem(d.getResultado())) {
                carregarCombos(); atualizarTabelaItens(); Toast.show(this, "Item cadastrado com sucesso!", Toast.Type.SUCCESS);
            } else Toast.show(this, "Falha ao cadastrar item.", Toast.Type.ERROR);
        }
    }

    private void editarItem() {
        if (!podeEditar) { Toast.show(this, "Acesso somente leitura.", Toast.Type.WARNING); return; }
        int row = tblItens.getSelectedRow();
        if (row < 0) { Toast.show(this, "Selecione um item.", Toast.Type.WARNING); return; }
        Long id = (Long) tblItens.getValueAt(row, 0);
        ItemEstoque item = itensVisiveis.getOrDefault(id, dao.buscarItemPorId(id));

        ItemDialog d = new ItemDialog(SwingUtilities.getWindowAncestor(this), item);
        d.setVisible(true);
        if (d.getResultado() != null) {
            d.getResultado().setId(id);
            if (dao.atualizarItem(d.getResultado())) {
                carregarCombos(); atualizarTabelaItens(); Toast.show(this, "Item atualizado!", Toast.Type.SUCCESS);
            } else Toast.show(this, "Falha ao atualizar.", Toast.Type.ERROR);
        }
    }

    private void movimentarEstoqueSelecionado() {
        if (!podeEditar) { Toast.show(this, "Acesso negado.", Toast.Type.WARNING); return; }
        int row = tblItens.getSelectedRow();
        if (row < 0) { Toast.show(this, "Selecione um item.", Toast.Type.WARNING); return; }
        Long id = (Long) tblItens.getValueAt(row, 0);
        ItemEstoque item = itensVisiveis.getOrDefault(id, dao.buscarItemPorId(id));
        
        MovimentacaoDialog d = new MovimentacaoDialog(SwingUtilities.getWindowAncestor(this), item);
        d.setVisible(true);
        if(d.isConfirmado()) {
            if (dao.registrarMovimentacao(d.getMovimentacao())) {
                atualizarTabelas();
                Toast.show(this, "Ajuste registrado com sucesso!", Toast.Type.SUCCESS);
            } else Toast.show(this, "Falha no ajuste. Verifique saldo.", Toast.Type.ERROR);
        }
    }

    // --- DIALOG DE NOVO/EDITAR ITEM ---
    private class ItemDialog extends JDialog {
        private JTextField txtNome;
        private JComboBox<String> cbCat;
        private JComboBox<Unidade> cbUnidade;
        private JSpinner spEstoqueMin;
        private JSpinner spEstoqueInicial;
        private JCheckBox chkAtivo;
        private ItemEstoque resultado;
        private final ItemEstoque base;

        public ItemDialog(Window owner, ItemEstoque base) {
            super(owner, base == null ? "Novo Insumo/Produto" : "Editar Insumo/Produto", ModalityType.APPLICATION_MODAL);
            this.base = base;
            getContentPane().setBackground(UIConstants.BG_DARK);
            setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UIConstants.BG_DARK);
            form.setBorder(new EmptyBorder(20, 30, 20, 30));
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(10, 5, 10, 5); 

            gc.gridy = 0; gc.gridx = 0; gc.weightx = 0; form.add(criarLabel("Nome:"), gc);
            gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = 2;
            txtNome = new JTextField(); UIConstants.styleField(txtNome);
            form.add(txtNome, gc);

            gc.gridy++; gc.gridx = 0; gc.weightx = 0; gc.gridwidth = 1; form.add(criarLabel("Categoria:"), gc);
            gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = 2;
            cbCat = new JComboBox<>(); UIConstants.styleCombo(cbCat);
            cbCat.setEditable(true);
            for(String s : CATEGORIAS_ESTOQUE_PADRAO) cbCat.addItem(s);
            for(String s : dao.categoriasNomes()) if(((DefaultComboBoxModel)cbCat.getModel()).getIndexOf(s) == -1) cbCat.addItem(s);
            form.add(cbCat, gc);

            gc.gridy++; gc.gridx = 0; gc.weightx = 0; gc.gridwidth = 1; form.add(criarLabel("Unid. Medida:"), gc);
            gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = 2;
            cbUnidade = new JComboBox<>(); UIConstants.styleCombo(cbUnidade);
            dao.listarUnidades().forEach(cbUnidade::addItem);
            form.add(cbUnidade, gc);

            gc.gridy++; gc.gridx = 0; gc.weightx = 0; gc.gridwidth = 1; 
            form.add(criarLabel(base == null ? "Estoque Inicial:" : "Em Estoque:"), gc);
            gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = 2;
            if (base == null) {
                // CORREÇÃO: SetEditor para permitir visualizar e digitar decimais
                spEstoqueInicial = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.5));
                spEstoqueInicial.setEditor(new JSpinner.NumberEditor(spEstoqueInicial, "0.00"));
                UIConstants.styleSpinner(spEstoqueInicial);
                form.add(spEstoqueInicial, gc);
            } else {
                JLabel lblAtual = new JLabel(formatarQuantidade(base.getEstoqueAtual(), base.getUnidadePadrao()));
                lblAtual.setForeground(UIConstants.FG_LIGHT); lblAtual.setFont(UIConstants.FONT_BOLD);
                form.add(lblAtual, gc);
            }

            gc.gridy++; gc.gridx = 0; gc.weightx = 0; gc.gridwidth = 1; 
            form.add(criarLabel("Estoque Mínimo:"), gc);
            gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = 2;
            // CORREÇÃO: SetEditor aplicado
            spEstoqueMin = new JSpinner(new SpinnerNumberModel(base != null ? base.getEstoqueMinimo() : 0.0, 0.0, 10000.0, 0.5));
            spEstoqueMin.setEditor(new JSpinner.NumberEditor(spEstoqueMin, "0.00"));
            UIConstants.styleSpinner(spEstoqueMin);
            form.add(spEstoqueMin, gc);

            gc.gridy++; gc.gridx = 0; gc.gridwidth = 3; gc.insets = new Insets(20, 5, 5, 5);
            chkAtivo = new JCheckBox("Insumo ativo nas operações");
            chkAtivo.setForeground(UIConstants.FG_LIGHT); chkAtivo.setOpaque(false); chkAtivo.setSelected(true);
            form.add(chkAtivo, gc);
            
            add(form, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.setBackground(UIConstants.BG_DARK);
            JButton btnCancel = new JButton("Cancelar"); UIConstants.styleSecondary(btnCancel);
            btnCancel.addActionListener(e -> dispose());
            JButton btnSave = new JButton("Salvar Insumo"); UIConstants.styleSuccess(btnSave);
            btnSave.addActionListener(e -> salvar());
            footer.add(btnCancel); footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);

            if (base != null) {
                txtNome.setText(base.getNome());
                cbCat.setSelectedItem(base.getCategoria());
                chkAtivo.setSelected(base.isAtivo());
                for(int i=0; i<cbUnidade.getItemCount(); i++) {
                    if(cbUnidade.getItemAt(i).getCodigo().equalsIgnoreCase(base.getUnidadePadrao())) {
                        cbUnidade.setSelectedIndex(i); break;
                    }
                }
            }

            pack(); setSize(450, 480); setLocationRelativeTo(owner);
        }

        private void salvar() {
            if (txtNome.getText().trim().isEmpty()) { Toast.show(this, "Nome obrigatório", Toast.Type.WARNING); return; }
            resultado = new ItemEstoque();
            resultado.setNome(txtNome.getText().trim());
            resultado.setCategoria(cbCat.getSelectedItem() != null ? cbCat.getSelectedItem().toString().trim() : "Geral");
            Unidade u = (Unidade) cbUnidade.getSelectedItem();
            resultado.setUnidadePadrao(u != null ? u.getCodigo() : "un");
            resultado.setEstoqueAtual(base == null ? ((Number)spEstoqueInicial.getValue()).doubleValue() : base.getEstoqueAtual());
            resultado.setEstoqueMinimo(((Number)spEstoqueMin.getValue()).doubleValue());
            resultado.setAtivo(chkAtivo.isSelected());
            dispose();
        }

        public ItemEstoque getResultado() { return resultado; }
    }

    // --- DIALOG DE MOVIMENTAÇÃO DE ESTOQUE (CORREÇÃO LAYOUT E UX) ---
    private class MovimentacaoDialog extends JDialog {
        private JRadioButton rbEntrada, rbSaida;
        private JSpinner spQtd;
        private JTextArea txtObs;
        private JLabel lblPrevisaoSaldo;
        private MovimentacaoEstoque movimentacao;
        private ItemEstoque item;
        private boolean confirmado = false;

        public MovimentacaoDialog(Window owner, ItemEstoque item) {
            super(owner, "Registrar Entrada ou Saída", ModalityType.APPLICATION_MODAL);
            this.item = item;
            getContentPane().setBackground(UIConstants.BG_DARK);
            setLayout(new BorderLayout());

            // --- PAINEL NORTE (Formulário Fixo) ---
            JPanel pnlTop = new JPanel(new GridBagLayout());
            pnlTop.setBackground(UIConstants.BG_DARK);
            pnlTop.setBorder(new EmptyBorder(15, 20, 5, 20));
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(10, 0, 10, 0);
            
            gc.gridx = 0; gc.gridy = 0;
            JLabel lblItem = new JLabel(item.getNome());
            lblItem.setFont(UIConstants.FONT_TITLE); lblItem.setForeground(UIConstants.PRIMARY_RED);
            pnlTop.add(lblItem, gc);
            
            gc.gridy++;
            JLabel lblAtual = new JLabel("Em Estoque: " + formatarQuantidade(item.getEstoqueAtual(), item.getUnidadePadrao()));
            lblAtual.setForeground(UIConstants.FG_MUTED); pnlTop.add(lblAtual, gc);

            gc.gridy++; pnlTop.add(criarLabel("Qual operação deseja registrar?"), gc);
            
            gc.gridy++;
            JPanel pTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0)); pTipo.setOpaque(false);
            ButtonGroup bg = new ButtonGroup();
            rbEntrada = new JRadioButton("Entrada (+)"); rbSaida = new JRadioButton("Saída (-)");
            styleRadio(rbEntrada); styleRadio(rbSaida);
            bg.add(rbEntrada); bg.add(rbSaida); rbEntrada.setSelected(true);
            pTipo.add(rbEntrada); pTipo.add(rbSaida);
            pnlTop.add(pTipo, gc);

            gc.gridy++; pnlTop.add(criarLabel("Quantidade a movimentar:"), gc);
            
            gc.gridy++;
            JPanel pQtd = new JPanel(new BorderLayout(10,0)); pQtd.setOpaque(false);
            
            // CORREÇÃO FLOAT: NumberEditor obriga aceitar decimais visualmente
            spQtd = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 10000.0, 0.5));
            spQtd.setEditor(new JSpinner.NumberEditor(spQtd, "0.00"));
            UIConstants.styleSpinner(spQtd);
            
            JLabel lblUnidade = new JLabel(item.getUnidadePadrao()); lblUnidade.setForeground(UIConstants.FG_LIGHT);
            pQtd.add(spQtd, BorderLayout.CENTER); pQtd.add(lblUnidade, BorderLayout.EAST);
            pnlTop.add(pQtd, gc);

            gc.gridy++;
            lblPrevisaoSaldo = new JLabel(); lblPrevisaoSaldo.setFont(UIConstants.FONT_BOLD);
            pnlTop.add(lblPrevisaoSaldo, gc);

            add(pnlTop, BorderLayout.NORTH);

            // --- PAINEL CENTRO (Responsivo para Motivo) ---
            JPanel pnlCenter = new JPanel(new BorderLayout(0, 5));
            pnlCenter.setBackground(UIConstants.BG_DARK);
            pnlCenter.setBorder(new EmptyBorder(10, 20, 10, 20));
            pnlCenter.add(criarLabel("Motivo / Observação (Opcional):"), BorderLayout.NORTH);
            txtObs = new JTextArea();
            txtObs.setLineWrap(true); txtObs.setWrapStyleWord(true);
            txtObs.setBackground(UIConstants.CARD_DARK); txtObs.setForeground(UIConstants.FG_LIGHT);
            txtObs.setBorder(BorderFactory.createLineBorder(UIConstants.GRID_DARK));
            pnlCenter.add(new JScrollPane(txtObs), BorderLayout.CENTER);
            
            add(pnlCenter, BorderLayout.CENTER);

            // --- PAINEL SUL (Botões) ---
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.setBackground(UIConstants.BG_DARK);
            JButton btnCancel = new JButton("Cancelar"); UIConstants.styleSecondary(btnCancel);
            btnCancel.addActionListener(e -> dispose());
            JButton btnConfirm = new JButton("Confirmar Ajuste"); UIConstants.styleSuccess(btnConfirm);
            btnConfirm.addActionListener(e -> confirmar());
            footer.add(btnCancel); footer.add(btnConfirm);
            add(footer, BorderLayout.SOUTH);

            rbEntrada.addActionListener(e -> atualizarPrevisaoSaldo());
            rbSaida.addActionListener(e -> atualizarPrevisaoSaldo());
            spQtd.addChangeListener(e -> atualizarPrevisaoSaldo());
            atualizarPrevisaoSaldo();

            pack(); setSize(450, 480); setLocationRelativeTo(owner);
        }

        private void styleRadio(JRadioButton rb) { rb.setOpaque(false); rb.setForeground(UIConstants.FG_LIGHT); rb.setFocusPainted(false); }

        private void confirmar() {
            double qtd = ((Number) spQtd.getValue()).doubleValue();
            if(qtd <= 0) { Toast.show(this, "A quantidade deve ser maior que zero.", Toast.Type.WARNING); return; }
            if(rbSaida.isSelected() && item.getEstoqueAtual() < qtd) { Toast.show(this, "Saldo insuficiente!", Toast.Type.ERROR); return; }

            movimentacao = new MovimentacaoEstoque();
            movimentacao.setItemId(item.getId());
            movimentacao.setTipo(rbEntrada.isSelected() ? "ENTRADA" : "SAIDA");
            movimentacao.setQuantidade(qtd);
            movimentacao.setObservacao(txtObs.getText());
            confirmado = true; dispose();
        }

        private void atualizarPrevisaoSaldo() {
            double qtd = ((Number) spQtd.getValue()).doubleValue();
            double saldoPrevisto = item.getEstoqueAtual() + (rbEntrada.isSelected() ? qtd : -qtd);
            lblPrevisaoSaldo.setText("Saldo previsto após a operação: " + formatarQuantidade(saldoPrevisto, item.getUnidadePadrao()));
            lblPrevisaoSaldo.setForeground(saldoPrevisto < 0 ? UIConstants.PRIMARY_RED : UIConstants.SUCCESS_GREEN);
        }

        public boolean isConfirmado() { return confirmado; }
        public MovimentacaoEstoque getMovimentacao() { return movimentacao; }
    }

    private void configurarRenderersItens() {
        // CORREÇÃO: Aplica a todos os tipos de coluna para sobrescrever o JTable.NumberRenderer
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                Long itemId = (Long) table.getValueAt(row, 0);
                ItemEstoque itemReal = itensVisiveis.get(itemId);
                String unidade = itemReal != null && itemReal.getUnidadePadrao() != null ? itemReal.getUnidadePadrao() : "un";

                double estoqueAtual = itemReal != null ? itemReal.getEstoqueAtual() : 0.0;
                double estoqueMinimo = itemReal != null ? itemReal.getEstoqueMinimo() : 0.0;
                boolean critico = itemReal != null && estoqueAtual <= estoqueMinimo;

                // CORREÇÃO: Sinalização Vermelha da Tabela (Item 7)
                if (!isSelected) {
                    if (critico && itemReal.isAtivo()) {
                        c.setBackground(new Color(65, 25, 25)); // Fundo vermelho sutil para itens críticos
                    } else {
                        c.setBackground(row % 2 == 0 ? UIConstants.BG_DARK : UIConstants.ALT_ROW);
                    }
                }
                
                setHorizontalAlignment(column >= 3 ? CENTER : LEFT); 
                setIcon(null); 

                if (column == 3 || column == 4) { // Em Estoque / Estoque Mín.
                    double valor = value instanceof Number ? ((Number) value).doubleValue() : 0.0;
                    setText(formatarQuantidade(valor, unidade));
                    
                    if (critico && itemReal.isAtivo()) {
                        setForeground(UIConstants.FG_LIGHT); setFont(UIConstants.FONT_BOLD);
                        if (column == 3) { 
                            // CORREÇÃO: Ícone padrão garantido
                            setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ERROR, 14, UIConstants.PRIMARY_RED));
                        }
                    } else {
                        setForeground(UIConstants.FG_LIGHT); setFont(UIConstants.FONT_REGULAR);
                    }
                } else if (column == 5) { // Status
                    if ("Crítico".equals(value)) {
                        setForeground(UIConstants.PRIMARY_RED); setFont(UIConstants.FONT_BOLD);
                        setText("⚠ Crítico");
                    } else if ("Ativo".equals(value)) {
                        setForeground(UIConstants.SUCCESS_GREEN); setFont(UIConstants.FONT_BOLD);
                    } else {
                        setForeground(UIConstants.FG_MUTED); setFont(UIConstants.FONT_REGULAR);
                    }
                } else {
                    setForeground(UIConstants.FG_LIGHT); setFont(UIConstants.FONT_REGULAR);
                }
                return c;
            }
        };

        tblItens.setDefaultRenderer(Object.class, customRenderer);
        tblItens.setDefaultRenderer(Double.class, customRenderer);
        tblItens.setDefaultRenderer(String.class, customRenderer);
        tblItens.setDefaultRenderer(Long.class, customRenderer);
    }

    private JLabel criarLabel(String txt) {
        JLabel l = new JLabel(txt); l.setForeground(UIConstants.FG_MUTED); l.setFont(UIConstants.FONT_BOLD); return l;
    }

    private JButton criarBotaoIcone(GoogleMaterialDesignIcons icone, Color bg, String tooltip) {
        JButton b = new JButton(IconFontSwing.buildIcon(icone, 18, UIConstants.FG_LIGHT));
        b.setBackground(bg); b.setBorder(BorderFactory.createLineBorder(UIConstants.GRID_DARK));
        b.setFocusPainted(false); b.setPreferredSize(new Dimension(40, 35));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setToolTipText(tooltip);
        return b;
    }
}