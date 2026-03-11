package com.senac.food.verse.gui;

import com.senac.food.verse.EstoqueDAO;
import com.senac.food.verse.EstoqueDAO.ItemEstoque;
import com.senac.food.verse.EstoqueDAO.MovimentacaoEstoque;
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

    private JTabbedPane tabs;

    // --- ABA 1: ITENS ---
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

    // --- ABA 2: HISTÓRICO ---
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

        tabs.addTab("Inventário Atual", criarAbaItens());
        tabs.addTab("Ajustes da Sessão", criarAbaHistorico());

        add(tabs, BorderLayout.CENTER);

        carregarCombos();
        atualizarTabelas();
        
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) atualizarHistorico();
        });
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
        // CORREÇÃO: Ícone INVENTORY -> STORE (pois INVENTORY pode faltar em versões antigas)
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
        btnNovo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ADD_BOX, 18, Color.WHITE));
        UIConstants.stylePrimary(btnNovo);
        btnNovo.addActionListener(e -> novoItem());

        btnMovimentar = new JButton("Ajuste Rápido"); 
        btnMovimentar.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SWAP_VERT, 18, UIConstants.FG_LIGHT));
        UIConstants.styleSecondary(btnMovimentar);
        btnMovimentar.setToolTipText("Lançar entrada ou saída de estoque");
        btnMovimentar.addActionListener(e -> movimentarEstoqueSelecionado());

        btnEditar = new JButton("Editar");
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

        String[] cols = {"ID", "Nome", "Categoria", "Atual", "Mínimo", "Status"};
        modelItens = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Long.class : (c == 3 || c == 4 ? Double.class : String.class);
            }
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
        JLabel lblInfo = new JLabel("Últimos ajustes registrados neste terminal");
        lblInfo.setForeground(UIConstants.FG_MUTED);
        lblInfo.setFont(UIConstants.FONT_REGULAR);
        JButton btnRefresh = criarBotaoIcone(GoogleMaterialDesignIcons.REFRESH, UIConstants.BG_DARK_ALT, "Atualizar Lista");
        btnRefresh.addActionListener(e -> atualizarHistorico());
        
        headerHist.add(btnRefresh);
        headerHist.add(lblInfo);
        
        p.add(headerHist, BorderLayout.NORTH);

        String[] cols = {"Data/Hora", "Item", "Tipo", "Qtd", "Motivo/Obs"};
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
                
                if(column == 2) { 
                    String tipo = (String) value;
                    if("ENTRADA".equals(tipo)) setForeground(UIConstants.SUCCESS_GREEN);
                    else if("SAIDA".equals(tipo)) setForeground(UIConstants.DANGER_RED);
                    else setForeground(UIConstants.FG_LIGHT);
                    setFont(UIConstants.FONT_BOLD);
                } else {
                    setForeground(UIConstants.FG_LIGHT);
                }
                setHorizontalAlignment(CENTER);
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
            itensVisiveis.clear();
            modelItens.setRowCount(0);
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
            lblStatusGeral.setText("⚠ " + criticos + " itens com estoque crítico");
            lblStatusGeral.setForeground(UIConstants.DANGER_RED);
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
                modelHistorico.addRow(new Object[]{
                    m.getDataMovimento().format(DATE_FMT),
                    m.getNomeItemSnapshot(),
                    m.getTipo(),
                    m.getQuantidade(),
                    m.getObservacao()
                });
            }
        }
    }

    private void novoItem() {
        if (!podeEditar) {
            Toast.show(this, "Seu perfil possui acesso somente leitura ao estoque.", Toast.Type.WARNING);
            return;
        }
        ItemDialog d = new ItemDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if (d.getResultado() != null) {
            if (dao.salvarItem(d.getResultado())) {
                carregarCombos();
                atualizarTabelaItens();
                Toast.show(this, "Item cadastrado com sucesso!", Toast.Type.SUCCESS);
            } else {
                Toast.show(this, "Não foi possível cadastrar o item no restaurante atual.", Toast.Type.ERROR);
            }
        }
    }

    private void editarItem() {
        if (!podeEditar) {
            Toast.show(this, "Seu perfil possui acesso somente leitura ao estoque.", Toast.Type.WARNING);
            return;
        }
        int row = tblItens.getSelectedRow();
        if (row < 0) {
            Toast.show(this, "Selecione um item para editar.", Toast.Type.WARNING);
            return;
        }
        Long id = (Long) tblItens.getValueAt(row, 0);
        ItemEstoque item = itensVisiveis.getOrDefault(id, dao.buscarItemPorId(id));

        ItemDialog d = new ItemDialog(SwingUtilities.getWindowAncestor(this), item);
        d.setVisible(true);
        if (d.getResultado() != null) {
            d.getResultado().setId(id);
            if (dao.atualizarItem(d.getResultado())) {
                carregarCombos();
                atualizarTabelaItens();
                Toast.show(this, "Item atualizado!", Toast.Type.SUCCESS);
            } else {
                Toast.show(this, "Não foi possível salvar as alterações do item.", Toast.Type.ERROR);
            }
        }
    }

    private void movimentarEstoqueSelecionado() {
        if (!podeEditar) {
            Toast.show(this, "Somente gerente ou admin em contexto podem ajustar o estoque.", Toast.Type.WARNING);
            return;
        }
        int row = tblItens.getSelectedRow();
        if (row < 0) {
            Toast.show(this, "Selecione um item para movimentar.", Toast.Type.WARNING);
            return;
        }
        Long id = (Long) tblItens.getValueAt(row, 0);
        ItemEstoque item = itensVisiveis.getOrDefault(id, dao.buscarItemPorId(id));
        
        MovimentacaoDialog d = new MovimentacaoDialog(SwingUtilities.getWindowAncestor(this), item);
        d.setVisible(true);
        if(d.isConfirmado()) {
            if (dao.registrarMovimentacao(d.getMovimentacao())) {
                atualizarTabelas();
                String tipo = d.getMovimentacao().getTipo(); 
                Toast.show(this, (tipo.equals("ENTRADA") ? "Entrada" : "Saída") + " registrada!", Toast.Type.SUCCESS);
            } else {
                Toast.show(this, "Ajuste não realizado. Verifique o saldo e o restaurante em contexto.", Toast.Type.ERROR);
            }
        }
    }

    private class ItemDialog extends JDialog {
        private JTextField txtNome;
        private JComboBox<String> cbCat;
        private JSpinner spEstoqueMin;
        private JSpinner spEstoqueInicial;
        private JCheckBox chkAtivo;
        private ItemEstoque resultado;
        private final ItemEstoque base;

        public ItemDialog(Window owner, ItemEstoque base) {
            super(owner, base == null ? "Novo Item de Estoque" : "Editar Item", ModalityType.APPLICATION_MODAL);
            this.base = base;
            setSize(500, 520);
            setLocationRelativeTo(owner);
            getContentPane().setBackground(UIConstants.BG_DARK);
            setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UIConstants.BG_DARK);
            form.setBorder(new EmptyBorder(20, 30, 20, 30));
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(8, 0, 8, 0);
            gc.gridx = 0; gc.gridy = 0;

            form.add(criarLabel("Nome do insumo:"), gc);
            gc.gridy++;
            txtNome = new JTextField(); UIConstants.styleField(txtNome);
            form.add(txtNome, gc);

            gc.gridy++;
            form.add(criarLabel("Categoria:"), gc);
            gc.gridy++;
            cbCat = new JComboBox<>(); UIConstants.styleCombo(cbCat);
            cbCat.setEditable(true);
            dao.categoriasNomes().forEach(cbCat::addItem);
            form.add(cbCat, gc);

            gc.gridy++;
            JPanel pNum = new JPanel(new GridLayout(1, 2, 20, 0));
            pNum.setOpaque(false);

            JPanel pAtual = new JPanel(new BorderLayout());
            pAtual.setOpaque(false);
            pAtual.add(criarLabel(base == null ? "Estoque inicial:" : "Estoque atual:"), BorderLayout.NORTH);
            if (base == null) {
                spEstoqueInicial = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000.0, 1.0));
                UIConstants.styleSpinner(spEstoqueInicial);
                pAtual.add(spEstoqueInicial, BorderLayout.CENTER);
            } else {
                JLabel lblAtual = new JLabel(String.format(Locale.US, "%.0f unidades", base.getEstoqueAtual()));
                lblAtual.setForeground(UIConstants.FG_LIGHT);
                lblAtual.setFont(UIConstants.FONT_BOLD);
                pAtual.add(lblAtual, BorderLayout.CENTER);
            }
            
            JPanel pMin = new JPanel(new BorderLayout()); pMin.setOpaque(false);
            pMin.add(criarLabel("Estoque Mínimo:"), BorderLayout.NORTH);
            spEstoqueMin = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000.0, 1.0));
            UIConstants.styleSpinner(spEstoqueMin);
            pMin.add(spEstoqueMin, BorderLayout.CENTER);

            pNum.add(pAtual);
            pNum.add(pMin);
            form.add(pNum, gc);

            gc.gridy++;
            gc.insets = new Insets(20, 0, 0, 0);
            chkAtivo = new JCheckBox("Item Ativo para uso em receitas");
            chkAtivo.setForeground(UIConstants.FG_LIGHT);
            chkAtivo.setOpaque(false);
            chkAtivo.setSelected(true);
            form.add(chkAtivo, gc);
            
            add(form, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.setBackground(UIConstants.BG_DARK);
            JButton btnCancel = new JButton("Cancelar"); UIConstants.styleSecondary(btnCancel);
            btnCancel.addActionListener(e -> dispose());
            JButton btnSave = new JButton("Salvar Item"); UIConstants.styleSuccess(btnSave);
            btnSave.addActionListener(e -> salvar());
            footer.add(btnCancel); footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);

            if (base != null) {
                txtNome.setText(base.getNome());
                cbCat.setSelectedItem(base.getCategoria());
                spEstoqueMin.setValue(base.getEstoqueMinimo());
                chkAtivo.setSelected(base.isAtivo());
            }
        }

        private void salvar() {
            if (txtNome.getText().trim().isEmpty()) {
                Toast.show(this, "Nome é obrigatório", Toast.Type.WARNING);
                return;
            }
            if (cbCat.getSelectedItem() == null || cbCat.getSelectedItem().toString().trim().isEmpty()) {
                Toast.show(this, "Informe uma categoria para facilitar a operação.", Toast.Type.WARNING);
                return;
            }
            resultado = new ItemEstoque();
            resultado.setNome(txtNome.getText().trim());
            resultado.setCategoria(cbCat.getSelectedItem().toString().trim());
            resultado.setUnidadePadrao("un");
            resultado.setEstoqueAtual(base == null ? ((Number)spEstoqueInicial.getValue()).doubleValue() : base.getEstoqueAtual());
            resultado.setEstoqueMinimo(((Number)spEstoqueMin.getValue()).doubleValue());
            resultado.setAtivo(chkAtivo.isSelected());
            dispose();
        }

        public ItemEstoque getResultado() { return resultado; }
    }

    private class MovimentacaoDialog extends JDialog {
        private JRadioButton rbEntrada, rbSaida;
        private JSpinner spQtd;
        private JTextArea txtObs;
        private JLabel lblItem, lblUnidade, lblPrevisaoSaldo;
        private MovimentacaoEstoque movimentacao;
        private ItemEstoque item;
        private boolean confirmado = false;

        public MovimentacaoDialog(Window owner, ItemEstoque item) {
            super(owner, "Ajuste de Estoque", ModalityType.APPLICATION_MODAL);
            this.item = item;
            setSize(450, 400);
            setLocationRelativeTo(owner);
            getContentPane().setBackground(UIConstants.BG_DARK);
            setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UIConstants.BG_DARK);
            form.setBorder(new EmptyBorder(15, 20, 15, 20));
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(10, 0, 5, 0);
            gc.gridx = 0; gc.gridy = 0;

            lblItem = new JLabel(item.getNome());
            lblItem.setFont(UIConstants.FONT_TITLE);
            lblItem.setForeground(UIConstants.PRIMARY_RED);
            form.add(lblItem, gc);
            
            gc.gridy++;
            JLabel lblAtual = new JLabel("Estoque Atual: " + item.getEstoqueAtual() + " " + item.getUnidadePadrao());
            lblAtual.setForeground(UIConstants.FG_MUTED);
            form.add(lblAtual, gc);

            gc.gridy++;
            form.add(criarLabel("Tipo de Movimentação:"), gc);
            
            gc.gridy++;
            JPanel pTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            pTipo.setOpaque(false);
            ButtonGroup bg = new ButtonGroup();
            rbEntrada = new JRadioButton("Entrada (Compra)");
            rbSaida = new JRadioButton("Saída (Uso/Perda)");
            styleRadio(rbEntrada); styleRadio(rbSaida);
            bg.add(rbEntrada); bg.add(rbSaida);
            rbEntrada.setSelected(true);
            pTipo.add(rbEntrada); pTipo.add(rbSaida);
            form.add(pTipo, gc);

            gc.gridy++;
            form.add(criarLabel("Quantidade:"), gc);
            
            gc.gridy++;
            JPanel pQtd = new JPanel(new BorderLayout(10,0));
            pQtd.setOpaque(false);
            spQtd = new JSpinner(new SpinnerNumberModel(1.0, 0.001, 10000.0, 1.0));
            UIConstants.styleSpinner(spQtd);
            lblUnidade = new JLabel(item.getUnidadePadrao());
            lblUnidade.setForeground(UIConstants.FG_LIGHT);
            pQtd.add(spQtd, BorderLayout.CENTER);
            pQtd.add(lblUnidade, BorderLayout.EAST);
            form.add(pQtd, gc);

            gc.gridy++;
            lblPrevisaoSaldo = new JLabel();
            lblPrevisaoSaldo.setFont(UIConstants.FONT_BOLD);
            form.add(lblPrevisaoSaldo, gc);

            gc.gridy++;
            form.add(criarLabel("Motivo / Observação:"), gc);
            
            gc.gridy++;
            gc.weighty = 1.0; gc.fill = GridBagConstraints.BOTH;
            txtObs = new JTextArea();
            txtObs.setBackground(UIConstants.CARD_DARK);
            txtObs.setForeground(UIConstants.FG_LIGHT);
            txtObs.setBorder(BorderFactory.createLineBorder(UIConstants.GRID_DARK));
            JScrollPane scrollObs = new JScrollPane(txtObs);
            UIConstants.styleScrollPane(scrollObs);
            form.add(scrollObs, gc);

            add(form, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.setBackground(UIConstants.BG_DARK);
            JButton btnCancel = new JButton("Cancelar"); UIConstants.styleSecondary(btnCancel);
            btnCancel.addActionListener(e -> dispose());
            JButton btnConfirm = new JButton("Confirmar"); UIConstants.styleSuccess(btnConfirm);
            btnConfirm.addActionListener(e -> confirmar());
            footer.add(btnCancel); footer.add(btnConfirm);
            add(footer, BorderLayout.SOUTH);

            rbEntrada.addActionListener(e -> atualizarPrevisaoSaldo());
            rbSaida.addActionListener(e -> atualizarPrevisaoSaldo());
            spQtd.addChangeListener(e -> atualizarPrevisaoSaldo());
            atualizarPrevisaoSaldo();
        }

        private void styleRadio(JRadioButton rb) {
            rb.setOpaque(false);
            rb.setForeground(UIConstants.FG_LIGHT);
            rb.setFocusPainted(false);
        }

        private void confirmar() {
            double qtd = ((Number) spQtd.getValue()).doubleValue();
            if(qtd <= 0) {
                Toast.show(this, "Quantidade inválida", Toast.Type.WARNING);
                return;
            }
            
            if(rbSaida.isSelected() && item.getEstoqueAtual() < qtd) {
                 Toast.show(this, "Saldo insuficiente em estoque!", Toast.Type.ERROR);
                 return;
            }

            movimentacao = new MovimentacaoEstoque();
            movimentacao.setItemId(item.getId());
            movimentacao.setTipo(rbEntrada.isSelected() ? "ENTRADA" : "SAIDA");
            movimentacao.setQuantidade(qtd);
            movimentacao.setObservacao(txtObs.getText());
            
            confirmado = true;
            dispose();
        }

        private void atualizarPrevisaoSaldo() {
            double qtd = ((Number) spQtd.getValue()).doubleValue();
            double saldoPrevisto = item.getEstoqueAtual() + (rbEntrada.isSelected() ? qtd : -qtd);
            boolean negativo = saldoPrevisto < 0;
            lblPrevisaoSaldo.setText(String.format(Locale.US, "Saldo após ajuste: %.2f %s", saldoPrevisto, item.getUnidadePadrao()));
            lblPrevisaoSaldo.setForeground(negativo ? UIConstants.DANGER_RED : UIConstants.SUCCESS_GREEN);
        }

        public boolean isConfirmado() { return confirmado; }
        public MovimentacaoEstoque getMovimentacao() { return movimentacao; }
    }

    private void configurarRenderersItens() {
        tblItens.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? UIConstants.BG_DARK : UIConstants.ALT_ROW);
                
                setHorizontalAlignment(column >= 3 ? CENTER : LEFT); 

                double estoqueAtual = ((Number) table.getValueAt(row, 3)).doubleValue();
                double estoqueMinimo = ((Number) table.getValueAt(row, 4)).doubleValue();
                boolean critico = estoqueAtual <= estoqueMinimo;

                if (column == 3 || column == 4) {
                    setForeground(critico ? UIConstants.DANGER_RED : UIConstants.FG_LIGHT);
                    setFont(critico ? UIConstants.FONT_BOLD : UIConstants.FONT_REGULAR);
                    setToolTipText(critico ? "Abaixo do mínimo configurado" : null);
                } else if (column == 5) { 
                    if ("Crítico".equals(value)) {
                        setForeground(UIConstants.DANGER_RED);
                    } else if ("Ativo".equals(value)) {
                        setForeground(UIConstants.SUCCESS_GREEN);
                    } else {
                        setForeground(UIConstants.FG_MUTED);
                    }
                } else {
                    setForeground(UIConstants.FG_LIGHT);
                }
                
                return c;
            }
        });
    }

    private JLabel criarLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(UIConstants.FG_MUTED);
        l.setFont(UIConstants.FONT_BOLD);
        return l;
    }

    private JButton criarBotaoIcone(GoogleMaterialDesignIcons icone, Color bg, String tooltip) {
        JButton b = new JButton(IconFontSwing.buildIcon(icone, 18, UIConstants.FG_LIGHT));
        b.setBackground(bg);
        b.setBorder(BorderFactory.createLineBorder(UIConstants.GRID_DARK));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(40, 35));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setToolTipText(tooltip);
        return b;
    }
}
