package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Painel de Gestão de Usuários (Admin/Gerente)
 */
public class AprovacaoCadastrosPanel extends JPanel {

    // Dados vinculados à tabela
    private final List<Integer> funcionariosIds = new ArrayList<>();

    // Componentes principais
    private JLabel labelTitulo;
    private JScrollPane scrollPaneTabela;
    private JTable tabelaCadastros;

    // Ações
    private JButton btnNovo;
    private JButton btnEditar;
    private JButton btnAprovarSelecionados;
    private JButton btnRejeitarSelecionados;
    private JButton btnExcluirSelecionados;
    private JButton btnSelecionarTodos;
    private JButton btnLimparSelecao;
    private JButton btnRecarregar;

    // Filtros
    private JTextField txtBuscar;
    private JComboBox<String> cbFiltroStatus;
    private JComboBox<String> cbFiltroCargo;

    // Feedback
    private JLabel lblSelecionados;
    private JLabel lblStatusAcao;

    // Lateral "Últimos aprovados" (agora com JList)
    private JPanel painelUltimos;
    private JList<UltimoAprovado> listUltimos;
    private DefaultListModel<UltimoAprovado> modelUltimos;

    // Sorter para filtro/busca
    private TableRowSorter<DefaultTableModel> sorter;

    // Ícones (use os existentes no projeto; se não existir algum, há fallback textual em seleções)
    private final Icon icAdd      = IconLoader.load("/icons/add.png", 16, 16);
    private final Icon icEdit     = IconLoader.load("/icons/edit.png", 16, 16);
    private final Icon icDelete   = IconLoader.load("/icons/delete.png", 16, 16);
    private final Icon icApprove  = IconLoader.load("/icons/save.png", 16, 16);
    private final Icon icReject   = IconLoader.load("/icons/cancel.png", 16, 16);
    private final Icon icSelectAll= IconLoader.load("/icons/select_all.png", 16, 16);
    private final Icon icClear    = IconLoader.load("/icons/clear.png", 16, 16);
    private final Icon icRefresh  = IconLoader.load("/icons/refresh.png", 16, 16);
    private final Icon icOptions  = IconLoader.load("/icons/options.png", 16, 16);

    // Ícones de seleção (com fallback textual)
    private final Icon icCheckOn  = IconLoader.load("/icons/check_on.png", 16, 16);
    private final Icon icCheckOff = IconLoader.load("/icons/check_off.png", 16, 16);

    // Status "chips" ícones (placeholders possíveis)
    private final Icon icStatusOk = IconLoader.load("/icons/ok.png", 12, 12);
    private final Icon icStatusPen= IconLoader.load("/icons/pending.png", 12, 12);
    private final Icon icStatusRej= IconLoader.load("/icons/reject.png", 12, 12);

    public AprovacaoCadastrosPanel() {
        UIConstants.applyDarkDefaults();
        setOpaque(true);
        setBackground(UIConstants.BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        montarUI();
        configurarTabela();
        configurarAcoes();
        carregarCargos();
        carregarUsuarios(null, null, null); // todos
        carregarUltimosAprovados();
        iniciarAtualizacaoAutomatica();
    }

    /* ------------------------------- UI ------------------------------------ */

    private void montarUI() {
        // Título
        labelTitulo = new JLabel("Gestão de Usuários", IconLoader.load("/icons/dish.png", 20, 20), SwingConstants.LEADING);
        labelTitulo.setFont(UIConstants.ARIAL_16_B);
        labelTitulo.setForeground(UIConstants.FG_LIGHT);

        // Linha 1: Busca + Filtros (alinhados)
        JLabel lblBusca = new JLabel("Buscar (nome/e-mail):");
        lblBusca.setFont(UIConstants.ARIAL_12_B);
        lblBusca.setForeground(UIConstants.FG_LIGHT);

        txtBuscar = new JTextField();
        UIConstants.styleField(txtBuscar);
        txtBuscar.setToolTipText("Digite para filtrar por nome ou e-mail (Enter para aplicar)");
        txtBuscar.setColumns(24);

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(UIConstants.ARIAL_12_B);
        lblStatus.setForeground(UIConstants.FG_LIGHT);

        cbFiltroStatus = new JComboBox<>(new String[]{"Todos", "Pendente", "Aprovado", "Rejeitado", "Temporario"});
        UIConstants.styleCombo(cbFiltroStatus);
        cbFiltroStatus.setPreferredSize(new Dimension(150, 28));

        JLabel lblCargo = new JLabel("Cargo:");
        lblCargo.setFont(UIConstants.ARIAL_12_B);
        lblCargo.setForeground(UIConstants.FG_LIGHT);

        cbFiltroCargo = new JComboBox<>(new String[]{"Todos"});
        UIConstants.styleCombo(cbFiltroCargo);
        cbFiltroCargo.setPreferredSize(new Dimension(180, 28));

        // Linha 2: Ações
        btnNovo = new JButton("Novo Usuário", icAdd);
        UIConstants.stylePrimary(btnNovo);

        btnEditar = new JButton("Editar", icEdit);
        UIConstants.styleSecondary(btnEditar);

        btnAprovarSelecionados = new JButton("Aprovar", icApprove);
        UIConstants.styleSuccess(btnAprovarSelecionados);

        btnRejeitarSelecionados = new JButton("Rejeitar", icReject);
        UIConstants.styleSecondary(btnRejeitarSelecionados);

        btnExcluirSelecionados = new JButton("Excluir", icDelete);
        UIConstants.styleSecondary(btnExcluirSelecionados);

        btnSelecionarTodos = new JButton("Selecionar Todos", icSelectAll);
        UIConstants.styleSecondary(btnSelecionarTodos);

        btnLimparSelecao = new JButton("Limpar Seleção", icClear);
        UIConstants.styleSecondary(btnLimparSelecao);

        btnRecarregar = new JButton("Recarregar", icRefresh);
        UIConstants.styleSecondary(btnRecarregar);

        lblSelecionados = new JLabel("Selecionados: 0");
        lblSelecionados.setFont(UIConstants.ARIAL_12_B);
        lblSelecionados.setForeground(UIConstants.FG_MUTED);

        lblStatusAcao = new JLabel(" ");
        lblStatusAcao.setFont(UIConstants.ARIAL_12_B);
        lblStatusAcao.setForeground(UIConstants.FG_MUTED);

        // Tabela e scroll
        tabelaCadastros = new JTable();
        scrollPaneTabela = new JScrollPane(tabelaCadastros);
        scrollPaneTabela.setBorder(null);
        scrollPaneTabela.getViewport().setBackground(UIConstants.BG_DARK);

        // Painel "Últimos aprovados" – COMPACTO com JList (evita espaçamento bugado)
        painelUltimos = new JPanel();
        painelUltimos.setBackground(UIConstants.CARD_DARK);
        painelUltimos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.GRID_DARK),
                new EmptyBorder(10, 10, 10, 10)
        ));
        painelUltimos.setPreferredSize(new Dimension(300, 100));
        painelUltimos.setLayout(new BorderLayout(6, 6));

        JLabel lblUltimos = new JLabel("Últimos aprovados");
        lblUltimos.setFont(UIConstants.ARIAL_12_B);
        lblUltimos.setForeground(UIConstants.FG_LIGHT);
        painelUltimos.add(lblUltimos, BorderLayout.NORTH);

        modelUltimos = new DefaultListModel<>();
        listUltimos = new JList<>(modelUltimos);
        listUltimos.setBackground(UIConstants.CARD_DARK);
        listUltimos.setForeground(UIConstants.FG_LIGHT);
        listUltimos.setFixedCellHeight(46); // altura fixa p/ evitar variação e espaços
        listUltimos.setCellRenderer(new UltimosRenderer());
        JScrollPane spUltimos = new JScrollPane(listUltimos);
        spUltimos.setBorder(null);
        spUltimos.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spUltimos.getViewport().setBackground(UIConstants.CARD_DARK);
        painelUltimos.add(spUltimos, BorderLayout.CENTER);

        // Header superior com GridBag (alinha corretamente tudo na mesma linha)
        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        // Linha 1: Busca + Status + Cargo
        gc.gridx = 0; gc.gridy = 0; header.add(lblBusca, gc);
        gc.gridx = 1; header.add(txtBuscar, gc);
        gc.gridx = 2; header.add(lblStatus, gc);
        gc.gridx = 3; header.add(cbFiltroStatus, gc);
        gc.gridx = 4; header.add(lblCargo, gc);
        gc.gridx = 5; header.add(cbFiltroCargo, gc);
        gc.gridx = 6; gc.weightx = 1; header.add(Box.createHorizontalGlue(), gc);
        gc.weightx = 0;

        // Linha 2: Ações
        gc.gridy = 1; gc.gridx = 0; header.add(btnNovo, gc);
        gc.gridx = 1; header.add(btnEditar, gc);
        gc.gridx = 2; header.add(btnAprovarSelecionados, gc);
        gc.gridx = 3; header.add(btnRejeitarSelecionados, gc);
        gc.gridx = 4; header.add(btnExcluirSelecionados, gc);
        gc.gridx = 5; header.add(btnSelecionarTodos, gc);
        gc.gridx = 6; header.add(btnLimparSelecao, gc);
        gc.gridx = 7; header.add(btnRecarregar, gc);
        gc.gridx = 8; header.add(lblSelecionados, gc);
        gc.gridx = 9; header.add(lblStatusAcao, gc);

        // Painel central (header + tabela)
        JPanel esquerda = new JPanel();
        esquerda.setOpaque(false);
        GroupLayout gl = new GroupLayout(esquerda);
        esquerda.setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);
        gl.setHorizontalGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(labelTitulo)
                .addComponent(header)
                .addComponent(scrollPaneTabela)
        );
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addComponent(labelTitulo)
                .addComponent(header, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(scrollPaneTabela, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setLayout(new BorderLayout(10, 10));
        add(esquerda, BorderLayout.CENTER);
        add(painelUltimos, BorderLayout.EAST);
    }

    private void configurarTabela() {
        String[] colunas = {"", "ID", "Nome", "Username", "E-mail", "Cargo", "Telefone", "Data Registro", "Status", "Ações"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 0 || col == 9; }
            @Override public Class<?> getColumnClass(int columnIndex) { return columnIndex == 0 ? Boolean.class : String.class; }
        };
        tabelaCadastros.setModel(modelo);

        // Tabela
        tabelaCadastros.setRowHeight(36); // ligeiramente acima do conteúdo
        tabelaCadastros.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tabelaCadastros.setSelectionBackground(UIConstants.SEL_BG);
        tabelaCadastros.setSelectionForeground(UIConstants.SEL_FG);
        tabelaCadastros.setGridColor(UIConstants.GRID_DARK);
        tabelaCadastros.setShowVerticalLines(false);
        tabelaCadastros.setShowHorizontalLines(true);
        tabelaCadastros.setBackground(UIConstants.BG_DARK);
        tabelaCadastros.setForeground(UIConstants.FG_LIGHT);
        tabelaCadastros.setAutoCreateRowSorter(true);

        // Cabeçalho estilizado e ALINHADO por coluna
        JTableHeader th = tabelaCadastros.getTableHeader();
        th.setDefaultRenderer(new HeaderRenderer(new int[]{
                SwingConstants.CENTER, // ""
                SwingConstants.CENTER, // ID
                SwingConstants.LEFT,   // Nome
                SwingConstants.LEFT,   // Username
                SwingConstants.LEFT,   // E-mail
                SwingConstants.CENTER, // Cargo
                SwingConstants.CENTER, // Telefone
                SwingConstants.CENTER, // Data
                SwingConstants.CENTER, // Status
                SwingConstants.CENTER  // Ações
        }));
        th.setPreferredSize(new Dimension(th.getPreferredSize().width, 34));
        th.setReorderingAllowed(false);

        // Larguras
        TableColumnModel cm = tabelaCadastros.getColumnModel();
        cm.getColumn(0).setPreferredWidth(44);  // seleção
        cm.getColumn(1).setPreferredWidth(70);  // ID
        cm.getColumn(2).setPreferredWidth(220); // Nome
        cm.getColumn(3).setPreferredWidth(160); // Username
        cm.getColumn(4).setPreferredWidth(240); // E-mail
        cm.getColumn(5).setPreferredWidth(150); // Cargo
        cm.getColumn(6).setPreferredWidth(140); // Telefone
        cm.getColumn(7).setPreferredWidth(150); // Data
        cm.getColumn(8).setPreferredWidth(120); // Status
        cm.getColumn(9).setPreferredWidth(120); // Ações

        // Renderers alinhados por coluna (evita desalinhamento)
        cm.getColumn(0).setCellRenderer(new SelectIconRenderer());
        cm.getColumn(0).setCellEditor(new SelectIconEditor(new JCheckBox()));

        cm.getColumn(1).setCellRenderer(new BodyCellRenderer(SwingConstants.CENTER));
        cm.getColumn(2).setCellRenderer(new BodyCellRenderer(SwingConstants.LEFT));
        cm.getColumn(3).setCellRenderer(new BodyCellRenderer(SwingConstants.LEFT));
        cm.getColumn(4).setCellRenderer(new BodyCellRenderer(SwingConstants.LEFT));
        cm.getColumn(5).setCellRenderer(new BodyCellRenderer(SwingConstants.CENTER));
        cm.getColumn(6).setCellRenderer(new BodyCellRenderer(SwingConstants.CENTER));
        cm.getColumn(7).setCellRenderer(new BodyCellRenderer(SwingConstants.CENTER));
        cm.getColumn(8).setCellRenderer(new StatusChipRenderer());

        cm.getColumn(9).setCellRenderer(new ActionButtonRenderer());
        cm.getColumn(9).setCellEditor(new ActionButtonEditor(new JCheckBox()));

        // Sorter p/ busca
        sorter = new TableRowSorter<>(modelo);
        tabelaCadastros.setRowSorter(sorter);

        // Duplo clique para editar
        tabelaCadastros.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int vr = tabelaCadastros.getSelectedRow();
                    if (vr >= 0) abrirDialogoEditar(vr);
                }
            }
        });
    }

    private void configurarAcoes() {
        // Buscar
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { aplicarFiltroBusca(); }
        });

        // Filtros
        cbFiltroStatus.addActionListener(e -> recarregarLista());
        cbFiltroCargo.addActionListener(e -> recarregarLista());

        // Recarregar
        btnRecarregar.addActionListener(e -> {
            recarregarLista();
            carregarUltimosAprovados();
            feedback("Dados recarregados", UIConstants.FG_MUTED);
        });

        // Seleção com feedback visual
        btnSelecionarTodos.addActionListener(e -> {
            selecionarTodas(true);
            feedback("Todos selecionados", new Color(25, 135, 84));
        });
        btnLimparSelecao.addActionListener(e -> {
            selecionarTodas(false);
            feedback("Seleção limpa", new Color(255, 193, 7));
        });

        // CRUD/Ações
        btnNovo.addActionListener(e -> abrirDialogoNovo());
        btnEditar.addActionListener(e -> editarSelecionado());
        btnAprovarSelecionados.addActionListener(e -> alterarStatusSelecionados("aprovado"));
        btnRejeitarSelecionados.addActionListener(e -> alterarStatusSelecionados("rejeitado"));
        btnExcluirSelecionados.addActionListener(e -> excluirSelecionados());

        // Atalho focar busca
        InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "focusSearch");
        am.put("focusSearch", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { txtBuscar.requestFocusInWindow(); } });
    }

    private void feedback(String texto, Color cor) {
        lblStatusAcao.setText(texto);
        lblStatusAcao.setForeground(cor);
        new Timer(1600, e -> {
            lblStatusAcao.setText(" ");
            ((Timer)e.getSource()).stop();
        }).start();
    }

    /* ------------------------- Busca/Filtro -------------------------------- */

    private void aplicarFiltroBusca() {
        String termo = txtBuscar.getText().trim();
        if (termo.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(termo), 2, 4)); // Nome e E-mail
        }
    }

    private void recarregarLista() {
        String status = cbFiltroStatus.getSelectedItem() != null ? String.valueOf(cbFiltroStatus.getSelectedItem()) : "Todos";
        String cargo  = cbFiltroCargo.getSelectedItem() != null ? String.valueOf(cbFiltroCargo.getSelectedItem())  : "Todos";
        String termo  = txtBuscar.getText().trim();
        carregarUsuarios("Todos".equals(status) ? null : status.toLowerCase(),
                         "Todos".equals(cargo)  ? null : cargo,
                         termo.isEmpty() ? null : termo);
    }

    private void selecionarTodas(boolean v) {
        DefaultTableModel m = (DefaultTableModel) tabelaCadastros.getModel();
        for (int i = 0; i < m.getRowCount(); i++) m.setValueAt(v, i, 0);
        atualizarContadorSelecionados();
    }

    /* ------------------------- Dados --------------------------------------- */

    private void limparTabela() {
        DefaultTableModel modelo = (DefaultTableModel) tabelaCadastros.getModel();
        modelo.setRowCount(0);
        funcionariosIds.clear();
        atualizarContadorSelecionados();
    }

    private void carregarCargos() {
        cbFiltroCargo.removeAllItems();
        cbFiltroCargo.addItem("Todos");

        ConexaoBanco banco = new ConexaoBanco();
        try (Connection conn = banco.abrirConexao();
             PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT role FROM tb_funcionarios WHERE role IS NOT NULL AND role <> '' ORDER BY role");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cbFiltroCargo.addItem(rs.getString("role"));
            }
        } catch (SQLException ex) {
            // Mantém apenas "Todos" se falhar
        }
    }

    private void carregarUsuarios(String status, String cargo, String termoBusca) {
        limparTabela();

        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = banco.abrirConexao();

            StringBuilder sql = new StringBuilder(
                    "SELECT userID, name, userName, email, role, phone, registrationDate, status " +
                    "FROM tb_funcionarios WHERE 1=1"
            );
            List<Object> params = new ArrayList<>();
            if (status != null) {
                sql.append(" AND LOWER(status) = ?");
                params.add(status.toLowerCase());
            }
            if (cargo != null) {
                sql.append(" AND role = ?");
                params.add(cargo);
            }
            if (termoBusca != null) {
                sql.append(" AND (LOWER(name) LIKE ? OR LOWER(email) LIKE ?)");
                String like = "%" + termoBusca.toLowerCase() + "%";
                params.add(like); params.add(like);
            }
            sql.append(" ORDER BY registrationDate DESC");

            stmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            rs = stmt.executeQuery();

            DefaultTableModel m = (DefaultTableModel) tabelaCadastros.getModel();
            while (rs.next()) {
                int id = rs.getInt("userID");
                String nome = n(rs.getString("name"));
                String uname= n(rs.getString("userName"));
                String email= n(rs.getString("email"));
                String role = n(rs.getString("role"));
                String phone= n(rs.getString("phone"));
                String data = n(rs.getString("registrationDate"));
                String st   = traduzStatus(n(rs.getString("status")));

                funcionariosIds.add(id);
                m.addRow(new Object[]{false, String.valueOf(id), nome, uname, email, role, phone, data, st, "Opções"});
            }

            aplicarFiltroBusca();
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao carregar usuários", ex);
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuários:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) banco.fecharConexao();
            } catch (SQLException ignored) { }
        }
    }

    private void carregarUltimosAprovados() {
        // Usa JList com modelo -> sem espaçamento vertical exagerado
        modelUltimos.clear();

        ConexaoBanco banco = new ConexaoBanco();
        try (Connection conn = banco.abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT TOP 5 name, role, registrationDate " +
                     "FROM tb_funcionarios WHERE status = 'aprovado' ORDER BY registrationDate DESC");
             ResultSet rs = stmt.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                count++;
                String nome  = n(rs.getString("name"));
                String role  = n(rs.getString("role"));
                String data  = n(rs.getString("registrationDate"));
                modelUltimos.addElement(new UltimoAprovado(nome, role, data));
            }

            if (count == 0) {
                // quando não há dados, mostra um aviso simples
                modelUltimos.addElement(new UltimoAprovado("Nenhum aprovado recente.", "", ""));
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao carregar últimos aprovados", ex);
            modelUltimos.clear();
            modelUltimos.addElement(new UltimoAprovado("Erro ao carregar.", "", ""));
        }
        listUltimos.repaint();
    }

    private void iniciarAtualizacaoAutomatica() {
        new Timer(60000, e -> {
            recarregarLista();
            carregarUltimosAprovados();
        }).start();
    }

    /* ------------------------- Ações Persistência --------------------------- */

    private void editarSelecionado() {
        int vr = tabelaCadastros.getSelectedRow();
        if (vr < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha para editar.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        abrirDialogoEditar(vr);
    }

    private void abrirDialogoEditar(int viewRow) {
        int mr = tabelaCadastros.convertRowIndexToModel(viewRow);
        int id = funcionariosIds.get(mr);
        DefaultTableModel m = (DefaultTableModel) tabelaCadastros.getModel();

        String nome  = String.valueOf(m.getValueAt(mr, 2));
        String uname = String.valueOf(m.getValueAt(mr, 3));
        String email = String.valueOf(m.getValueAt(mr, 4));
        String role  = String.valueOf(m.getValueAt(mr, 5));
        String phone = String.valueOf(m.getValueAt(mr, 6));
        String data  = String.valueOf(m.getValueAt(mr, 7));
        String status= String.valueOf(m.getValueAt(mr, 8));

        UsuarioDialog dlg = new UsuarioDialog(SwingUtilities.getWindowAncestor(this), "Editar Usuário", id, nome, uname, email, role, phone, data, status);
        dlg.setVisible(true);
        if (dlg.salvou) recarregarLista();
    }

    private void abrirDialogoNovo() {
        UsuarioDialog dlg = new UsuarioDialog(SwingUtilities.getWindowAncestor(this), "Novo Usuário", null, "", "", "", "", "", "", "Pendente");
        dlg.setVisible(true);
        if (dlg.salvou) {
            carregarCargos();
            recarregarLista();
        }
    }

    private void alterarStatusSelecionados(String novoStatus) {
        List<Integer> ids = getSelecionados();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum cadastro selecionado.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String titulo = "aprovado".equals(novoStatus) ? "Aprovar" : "Rejeitar";
        int ok = JOptionPane.showConfirmDialog(this, titulo + " " + ids.size() + " cadastro(s)?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = true;
        try {
            conn = banco.abrirConexao();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("UPDATE tb_funcionarios SET status = ? WHERE userID = ?");
            for (Integer id : ids) { stmt.setString(1, novoStatus); stmt.setInt(2, id); stmt.addBatch(); }
            int[] res = stmt.executeBatch();
            conn.commit();
            for (int r : res) if (r != 1) { sucesso = false; break; }
        } catch (SQLException ex) {
            sucesso = false;
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) { }
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao atualizar status", ex);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) { conn.setAutoCommit(true); banco.fecharConexao(); }
            } catch (SQLException ignored) { }
        }
        if (sucesso) {
            JOptionPane.showMessageDialog(this, "Status atualizado!");
            recarregarLista();
            if ("aprovado".equals(novoStatus)) carregarUltimosAprovados();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar status.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirSelecionados() {
        List<Integer> ids = getSelecionados();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum cadastro selecionado.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Excluir " + ids.size() + " cadastro(s)?", "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = true;
        try {
            conn = banco.abrirConexao();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("DELETE FROM tb_funcionarios WHERE userID = ?");
            for (Integer id : ids) { stmt.setInt(1, id); stmt.addBatch(); }
            int[] res = stmt.executeBatch();
            conn.commit();
            for (int r : res) if (r != 1) { sucesso = false; break; }
        } catch (SQLException ex) {
            sucesso = false;
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) { }
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao excluir", ex);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) { conn.setAutoCommit(true); banco.fecharConexao(); }
            } catch (SQLException ignored) { }
        }
        if (sucesso) {
            JOptionPane.showMessageDialog(this, "Cadastro(s) excluído(s)!");
            carregarCargos();
            recarregarLista();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao excluir.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Integer> getSelecionados() {
        List<Integer> ids = new ArrayList<>();
        DefaultTableModel m = (DefaultTableModel) tabelaCadastros.getModel();
        for (int i = 0; i < m.getRowCount(); i++) {
            if (Boolean.TRUE.equals(m.getValueAt(i, 0))) ids.add(funcionariosIds.get(i));
        }
        return ids;
    }

    /* ------------------------- Renderers/Editors ---------------------------- */

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private final int[] aligns;
        HeaderRenderer(int[] aligns) {
            this.aligns = aligns;
            setOpaque(true);
            setBackground(UIConstants.HEADER_DARK);
            setForeground(UIConstants.FG_LIGHT);
            setFont(UIConstants.ARIAL_12_B);
            setBorder(new EmptyBorder(6, 8, 6, 8));
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int align = (aligns != null && column < aligns.length) ? aligns[column] : LEFT;
            setHorizontalAlignment(align);
            return this;
        }
    }

    private static class BodyCellRenderer extends DefaultTableCellRenderer {
        private final int align;
        BodyCellRenderer(int align) { this.align = align; setOpaque(true); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? UIConstants.BG_DARK : UIConstants.ALT_ROW);
                setForeground(UIConstants.FG_LIGHT);
            }
            setHorizontalAlignment(align);
            setBorder(new EmptyBorder(6, 8, 6, 8));
            return this;
        }
    }

    // Seleção com ícone (fallback textual se ícone não existir)
    private class SelectIconRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, col);
            boolean sel = value instanceof Boolean && (Boolean) value;
            l.setHorizontalAlignment(CENTER);
            l.setOpaque(true);
            l.setBackground(!isSelected ? (row % 2 == 0 ? UIConstants.BG_DARK : UIConstants.ALT_ROW) : table.getSelectionBackground());
            l.setForeground(!isSelected ? UIConstants.FG_LIGHT : table.getSelectionForeground());
            if (icCheckOn != null && icCheckOff != null) {
                l.setIcon(sel ? icCheckOn : icCheckOff);
                l.setText(null);
            } else {
                l.setIcon(null);
                l.setText(sel ? "✓" : "○");
                l.setFont(UIConstants.ARIAL_14);
            }
            l.setToolTipText(sel ? "Selecionado" : "Clique para selecionar");
            return l;
        }
    }

    private class SelectIconEditor extends DefaultCellEditor {
        private final JLabel renderer = new JLabel("", SwingConstants.CENTER);
        public SelectIconEditor(JCheckBox cb) {
            super(cb);
            renderer.setOpaque(true);
            renderer.addMouseListener(new MouseAdapter() {
                @Override public void mouseReleased(MouseEvent e) {
                    int row = tabelaCadastros.getEditingRow();
                    if (row >= 0) {
                        int mr = tabelaCadastros.convertRowIndexToModel(row);
                        DefaultTableModel m = (DefaultTableModel) tabelaCadastros.getModel();
                        Boolean cur = (Boolean) m.getValueAt(mr, 0);
                        m.setValueAt(!Boolean.TRUE.equals(cur), mr, 0);
                        stopCellEditing();
                        atualizarContadorSelecionados();
                    }
                }
            });
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            boolean sel = value instanceof Boolean && (Boolean) value;
            if (icCheckOn != null && icCheckOff != null) {
                renderer.setIcon(sel ? icCheckOn : icCheckOff);
                renderer.setText(null);
            } else {
                renderer.setIcon(null);
                renderer.setText(sel ? "✓" : "○");
                renderer.setFont(UIConstants.ARIAL_14);
            }
            renderer.setBackground(table.getSelectionBackground());
            renderer.setForeground(table.getSelectionForeground());
            return renderer;
        }
        @Override public Object getCellEditorValue() { return null; }
    }

    // Status "chip" (não preenche a linha toda)
    private class StatusChipRenderer extends BodyCellRenderer {
        StatusChipRenderer() { super(SwingConstants.CENTER); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, col);
            String v = String.valueOf(value).toLowerCase();
            Color bg = UIConstants.CARD_DARK;
            Color fg = UIConstants.FG_LIGHT;
            Icon  ic = icStatusPen;
            if (v.contains("aprov")) { bg = new Color(25, 135, 84); fg = Color.WHITE; ic = icStatusOk; }
            else if (v.contains("rej")) { bg = new Color(180, 30, 30); fg = Color.WHITE; ic = icStatusRej; }
            else if (v.contains("temp") || v.contains("pend")) { bg = new Color(255, 193, 7); fg = Color.BLACK; ic = icStatusPen; }

            l.setText(" " + String.valueOf(value) + " ");
            l.setIcon(ic);
            l.setIconTextGap(6);
            l.setHorizontalAlignment(CENTER);
            l.setBorder(new EmptyBorder(4, 10, 4, 10));
            if (!selected) {
                l.setBackground(bg);
                l.setForeground(fg);
            }
            return l;
        }
    }

    // Botão "Opções"
    private class ActionButtonRenderer extends JButton implements TableCellRenderer {
        ActionButtonRenderer() { UIConstants.styleSecondary(this); setText("Opções"); setIcon(icOptions); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(UIConstants.CARD_DARK);
                setForeground(UIConstants.FG_LIGHT);
            }
            return this;
        }
    }
    private class ActionButtonEditor extends DefaultCellEditor {
        private final JButton btn;
        private int currentRow = -1;
        ActionButtonEditor(JCheckBox cb) {
            super(cb);
            btn = new JButton("Opções", icOptions);
            UIConstants.styleSecondary(btn);
            btn.addActionListener(e -> fireEditingStopped());
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = tabelaCadastros.convertRowIndexToModel(row);
            return btn;
        }
        @Override public Object getCellEditorValue() {
            if (currentRow >= 0) abrirDialogoAcoes(currentRow);
            return "Opções";
        }
    }

    // Diálogo de ações centralizado e com informações organizadas
    private void abrirDialogoAcoes(int modelRow) {
        int id = funcionariosIds.get(modelRow);
        DefaultTableModel m = (DefaultTableModel) tabelaCadastros.getModel();
        String nome  = String.valueOf(m.getValueAt(modelRow, 2));
        String uname = String.valueOf(m.getValueAt(modelRow, 3));
        String email = String.valueOf(m.getValueAt(modelRow, 4));
        String role  = String.valueOf(m.getValueAt(modelRow, 5));
        String phone = String.valueOf(m.getValueAt(modelRow, 6));
        String data  = String.valueOf(m.getValueAt(modelRow, 7));
        String status= String.valueOf(m.getValueAt(modelRow, 8));

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Ações do usuário", Dialog.ModalityType.APPLICATION_MODAL);
        d.setSize(560, 300);
        d.setMinimumSize(new Dimension(560, 300));
        d.setLocationRelativeTo(this);
        d.setResizable(false);

        JPanel c = new JPanel(new BorderLayout(10, 10));
        c.setBackground(UIConstants.BG_DARK);
        c.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Form organizado Nome:, E-mail:, Cargo: ...
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        int y = 0;
        addRow(form, gc, y++, "Nome:", nome);
        addRow(form, gc, y++, "Username:", uname);
        addRow(form, gc, y++, "E-mail:", email);
        addRow(form, gc, y++, "Cargo:", role);
        addRow(form, gc, y++, "Telefone:", phone);
        addRow(form, gc, y++, "Data de Registro:", data);
        addRow(form, gc, y,   "Status:", status);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);

        JButton editar = new JButton("Editar", icEdit);
        UIConstants.styleSecondary(editar);
        editar.addActionListener(e -> {
            d.dispose();
            abrirDialogoEditar(tabelaCadastros.convertRowIndexToView(modelRow));
        });

        JButton aprovar = new JButton("Aprovar", icApprove);
        UIConstants.styleSuccess(aprovar);
        aprovar.addActionListener(e -> {
            if (alterarStatusUnico(id, "aprovado")) {
                JOptionPane.showMessageDialog(d, "Usuário aprovado!");
                d.dispose();
                recarregarLista();
                carregarUltimosAprovados();
            } else {
                JOptionPane.showMessageDialog(d, "Erro ao aprovar.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton rejeitar = new JButton("Rejeitar", icReject);
        UIConstants.styleSecondary(rejeitar);
        rejeitar.addActionListener(e -> {
            if (alterarStatusUnico(id, "rejeitado")) {
                JOptionPane.showMessageDialog(d, "Usuário rejeitado!");
                d.dispose();
                recarregarLista();
            } else {
                JOptionPane.showMessageDialog(d, "Erro ao rejeitar.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton excluir = new JButton("Excluir", icDelete);
        UIConstants.styleSecondary(excluir);
        excluir.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(d, "Excluir este usuário?", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                if (excluirUnico(id)) {
                    JOptionPane.showMessageDialog(d, "Usuário excluído!");
                    d.dispose();
                    carregarCargos();
                    recarregarLista();
                } else {
                    JOptionPane.showMessageDialog(d, "Erro ao excluir.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton fechar = new JButton("Fechar");
        UIConstants.styleSecondary(fechar);
        fechar.addActionListener(e -> d.dispose());

        btns.add(fechar);
        btns.add(excluir);
        btns.add(rejeitar);
        btns.add(aprovar);
        btns.add(editar);

        c.add(form, BorderLayout.CENTER);
        c.add(btns, BorderLayout.SOUTH);

        d.setContentPane(c);
        d.setVisible(true);
    }

    private void addRow(JPanel panel, GridBagConstraints gc, int y, String rotulo, String valor) {
        JLabel l = new JLabel(rotulo);
        l.setFont(UIConstants.ARIAL_12_B);
        l.setForeground(UIConstants.FG_LIGHT);
        JLabel v = new JLabel(n(valor));
        v.setFont(UIConstants.ARIAL_12);
        v.setForeground(UIConstants.FG_LIGHT);

        gc.gridx = 0; gc.gridy = y; panel.add(l, gc);
        gc.gridx = 1; panel.add(v, gc);
    }

    private boolean alterarStatusUnico(int id, String novo) {
        ConexaoBanco banco = new ConexaoBanco();
        try (Connection conn = banco.abrirConexao();
             PreparedStatement ps = conn.prepareStatement("UPDATE tb_funcionarios SET status = ? WHERE userID = ?")) {
            ps.setString(1, novo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            return false;
        }
    }

    private boolean excluirUnico(int id) {
        ConexaoBanco banco = new ConexaoBanco();
        try (Connection conn = banco.abrirConexao();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_funcionarios WHERE userID = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            return false;
        }
    }

    /* ------------------------- Dialog de CRUD ------------------------------- */

    private class UsuarioDialog extends JDialog {
        boolean salvou = false;

        private final Integer id; // null para novo
        private JTextField txtNome;
        private JTextField txtUserName;
        private JTextField txtEmail;
        private JTextField txtTelefone;
        private JComboBox<String> cbCargo;
        private JComboBox<String> cbStatus;
        private JPasswordField txtSenha; // opcional para edição

        UsuarioDialog(Window owner, String titulo,
                      Integer id, String nome, String uname, String email, String role,
                      String phone, String data, String status) {
            super(owner, titulo, ModalityType.APPLICATION_MODAL);
            this.id = id;
            setSize(560, 420);
            setLocationRelativeTo(owner);
            setResizable(false);
            montar(nome, uname, email, role, phone, status);
        }

        private void montar(String nome, String uname, String email, String role, String phone, String status) {
            JPanel c = new JPanel(new GridBagLayout());
            c.setBackground(UIConstants.BG_DARK);
            c.setBorder(new EmptyBorder(16,16,16,16));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6, 6, 6, 6);
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1;

            int y = 0;

            txtNome = new JTextField(nome);
            UIConstants.styleField(txtNome);
            addFormRow(c, gc, y++, "Nome:", txtNome);

            txtUserName = new JTextField(uname);
            UIConstants.styleField(txtUserName);
            addFormRow(c, gc, y++, "Username:", txtUserName);

            txtEmail = new JTextField(email);
            UIConstants.styleField(txtEmail);
            addFormRow(c, gc, y++, "E-mail:", txtEmail);

            txtTelefone = new JTextField(phone);
            UIConstants.styleField(txtTelefone);
            addFormRow(c, gc, y++, "Telefone:", txtTelefone);

            cbCargo = new JComboBox<>();
            UIConstants.styleCombo(cbCargo);
            carregarCargosCombo(cbCargo, role);
            addFormRow(c, gc, y++, "Cargo:", cbCargo);

            cbStatus = new JComboBox<>(new String[]{"Pendente", "Aprovado", "Rejeitado", "Temporario"});
            UIConstants.styleCombo(cbStatus);
            cbStatus.setSelectedItem(status == null || status.isBlank() ? "Pendente" : traduzStatus(status));
            addFormRow(c, gc, y++, "Status:", cbStatus);

            txtSenha = new JPasswordField();
            UIConstants.styleField(txtSenha);
            addFormRow(c, gc, y++, id == null ? "Senha:" : "Senha (deixe vazio para não alterar):", txtSenha);

            JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            botoes.setOpaque(false);

            JButton cancelar = new JButton("Cancelar");
            UIConstants.styleSecondary(cancelar);
            cancelar.addActionListener(e -> dispose());

            JButton salvar = new JButton("Salvar", icApprove);
            UIConstants.styleSuccess(salvar);
            salvar.addActionListener(e -> salvarUsuario());

            botoes.add(cancelar);
            botoes.add(salvar);

            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setBackground(UIConstants.BG_DARK);
            wrap.add(c, BorderLayout.CENTER);
            wrap.add(botoes, BorderLayout.SOUTH);

            setContentPane(wrap);
        }

        private void addFormRow(JPanel p, GridBagConstraints gc, int y, String label, JComponent comp) {
            JLabel l = new JLabel(label);
            l.setFont(UIConstants.ARIAL_12_B);
            l.setForeground(UIConstants.FG_LIGHT);

            gc.gridx = 0; gc.gridy = y; gc.weightx = 0; p.add(l, gc);
            gc.gridx = 1; gc.weightx = 1; p.add(comp, gc);
        }

        private void carregarCargosCombo(JComboBox<String> combo, String selected) {
            combo.removeAllItems();
            // Carrega cargos do banco
            ConexaoBanco banco = new ConexaoBanco();
            List<String> cargos = new ArrayList<>();
            try (Connection conn = banco.abrirConexao();
                 PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT role FROM tb_funcionarios WHERE role IS NOT NULL AND role <> '' ORDER BY role");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) cargos.add(rs.getString("role"));
            } catch (SQLException ignored) { }
            // Defaults caso não haja
            if (cargos.isEmpty()) {
                cargos.add("Cozinheiro");
                cargos.add("Entregador");
                cargos.add("Gerente");
                cargos.add("Admin");
            }
            for (String r: cargos) combo.addItem(r);
            if (selected != null && !selected.isBlank()) combo.setSelectedItem(selected);
        }

        private void salvarUsuario() {
            String nome  = txtNome.getText().trim();
            String uname = txtUserName.getText().trim();
            String email = txtEmail.getText().trim();
            String role  = (String) cbCargo.getSelectedItem();
            String phone = txtTelefone.getText().trim();
            String status = (String) cbStatus.getSelectedItem();
            String senha  = new String(txtSenha.getPassword());

            if (nome.isBlank() || uname.isBlank() || email.isBlank() || role == null || role.isBlank()) {
                JOptionPane.showMessageDialog(this, "Preencha Nome, Username, E-mail e Cargo.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ConexaoBanco banco = new ConexaoBanco();
            boolean ok = false;

            if (id == null) {
                // INSERT
                try (Connection conn = banco.abrirConexao();
                     PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO tb_funcionarios (name, userName, email, role, phone, password, registrationDate, status) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, CONVERT(VARCHAR(10), GETDATE(), 103), ?)")) {
                    ps.setString(1, nome);
                    ps.setString(2, uname);
                    ps.setString(3, email);
                    ps.setString(4, role);
                    ps.setString(5, phone);
                    ps.setString(6, senha == null ? "" : senha);
                    ps.setString(7, status.toLowerCase());
                    ok = ps.executeUpdate() > 0;
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao inserir usuário", ex);
                }
            } else {
                // UPDATE
                StringBuilder sql = new StringBuilder(
                        "UPDATE tb_funcionarios SET name=?, userName=?, email=?, role=?, phone=?, status=?"
                );
                boolean alterarSenha = senha != null && !senha.isBlank();
                if (alterarSenha) sql.append(", password=?");
                sql.append(" WHERE userID=?");

                try (Connection conn = banco.abrirConexao();
                     PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                    int i = 1;
                    ps.setString(i++, nome);
                    ps.setString(i++, uname);
                    ps.setString(i++, email);
                    ps.setString(i++, role);
                    ps.setString(i++, phone);
                    ps.setString(i++, status.toLowerCase());
                    if (alterarSenha) ps.setString(i++, senha);
                    ps.setInt(i, id);
                    ok = ps.executeUpdate() > 0;
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao atualizar usuário", ex);
                }
            }

            if (ok) {
                JOptionPane.showMessageDialog(this, "Usuário salvo com sucesso!");
                salvou = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao salvar usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /* ------------------------- Utils --------------------------------------- */

    private void atualizarContadorSelecionados() {
        int n = 0;
        DefaultTableModel m = (DefaultTableModel) tabelaCadastros.getModel();
        for (int i = 0; i < m.getRowCount(); i++) if (Boolean.TRUE.equals(m.getValueAt(i, 0))) n++;
        lblSelecionados.setText("Selecionados: " + n);
    }

    private static String n(String s) { return s == null ? "" : s; }
    private static String traduzStatus(String s) {
        if (s == null) return "";
        String v = s.trim().toLowerCase();
        return switch (v) {
            case "pendente" -> "Pendente";
            case "aprovado" -> "Aprovado";
            case "rejeitado" -> "Rejeitado";
            case "temporario" -> "Temporario";
            default -> s;
        };
    }

    /* ------------------------- Model/Renderer últimos aprovados ------------- */

    private static class UltimoAprovado {
        final String nome;
        final String cargo;
        final String data;
        UltimoAprovado(String n, String c, String d){ this.nome=n; this.cargo=c; this.data=d; }
    }

    private static class UltimosRenderer extends JPanel implements ListCellRenderer<UltimoAprovado> {
        private final JLabel lblNome = new JLabel();
        private final JLabel lblSub  = new JLabel();

        UltimosRenderer(){
            setLayout(new BorderLayout(4,2));
            setOpaque(true);
            lblNome.setFont(UIConstants.ARIAL_12_B);
            lblNome.setForeground(UIConstants.FG_LIGHT);
            lblSub.setFont(UIConstants.ARIAL_12);
            lblSub.setForeground(UIConstants.FG_MUTED);
            setBorder(new EmptyBorder(4, 6, 4, 6));
            add(lblNome, BorderLayout.NORTH);
            add(lblSub, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends UltimoAprovado> list, UltimoAprovado value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) return this;
            lblNome.setText(value.nome);
            if (value.cargo == null || value.cargo.isBlank()) {
                lblSub.setText(value.data == null ? "" : value.data);
            } else {
                lblSub.setText(value.cargo + (value.data == null || value.data.isBlank() ? "" : "  •  " + value.data));
            }
            setBackground(isSelected ? UIConstants.HEADER_DARK : UIConstants.CARD_DARK);
            return this;
        }
    }
}