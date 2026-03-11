package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import com.senac.food.verse.SessionContext;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Sprint 3 — Painel de Gestão de Restaurantes (Admin Global).
 * Permite listar, criar, editar, ativar/inativar (ativo) e ver status operacional (aberto)
 * de todos os restaurantes na plataforma.
 */
public class AdminRestaurantesPanel extends JPanel {

    private final JFrame parentFrame;

    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JTextField txtBusca;

    // Colunas da tabela
    private static final String[] COLUNAS = {
        "ID", "Nome", "Categoria", "Avaliação", "Plataforma", "Status", "Ações"
    };

    public AdminRestaurantesPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        UIConstants.stylePanel(this);

        add(criarHeader(), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(0, 20));
        corpo.setBackground(UIConstants.BG_DARK);
        corpo.setBorder(new EmptyBorder(10, 20, 10, 20));
        corpo.add(criarToolbar(), BorderLayout.NORTH);
        corpo.add(criarTabela(), BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);

        carregarDados("");
    }

    // -------------------------------------------------------------------------
    // Header
    // -------------------------------------------------------------------------
    private JPanel criarHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(15, 10, 15, 10));

        JLabel titulo = new JLabel("Gestão de Restaurantes");
        titulo.setFont(UIConstants.FONT_TITLE);
        titulo.setForeground(UIConstants.FG_LIGHT);
        titulo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.STORE_MALL_DIRECTORY, 32, UIConstants.PRIMARY_RED));
        titulo.setIconTextGap(15);
        p.add(titulo, BorderLayout.WEST);

        JButton btnNovo = new JButton("Novo Restaurante");
        UIConstants.stylePrimary(btnNovo);
        btnNovo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ADD, 18, Color.WHITE));
        btnNovo.addActionListener(e -> abrirDialogRestaurante(null));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnNovo);
        p.add(btnPanel, BorderLayout.EAST);

        return p;
    }

    // -------------------------------------------------------------------------
    // Toolbar de busca
    // -------------------------------------------------------------------------
    private JPanel criarToolbar() {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        txtBusca = new JTextField();
        txtBusca.setPreferredSize(new Dimension(300, 38));
        UIConstants.styleField(txtBusca);
        txtBusca.putClientProperty("JTextField.placeholderText", "Buscar por nome ou categoria...");
        txtBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { carregarDados(txtBusca.getText().trim()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { carregarDados(txtBusca.getText().trim()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { carregarDados(txtBusca.getText().trim()); }
        });

        JButton btnAtualizar = new JButton("Atualizar");
        UIConstants.styleSecondary(btnAtualizar);
        btnAtualizar.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.REFRESH, 18, UIConstants.FG_LIGHT));
        btnAtualizar.addActionListener(e -> carregarDados(txtBusca.getText().trim()));

        p.add(txtBusca, BorderLayout.WEST);
        p.add(btnAtualizar, BorderLayout.EAST);
        return p;
    }

    // -------------------------------------------------------------------------
    // Tabela
    // -------------------------------------------------------------------------
    private JScrollPane criarTabela() {
        modeloTabela = new DefaultTableModel(COLUNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        tabela = new JTable(modeloTabela);
        UIConstants.styleTable(tabela);
        tabela.getColumnModel().getColumn(0).setMaxWidth(50);
        tabela.getColumnModel().getColumn(4).setMaxWidth(100);
        tabela.getColumnModel().getColumn(5).setMaxWidth(100);
        tabela.getColumnModel().getColumn(6).setMinWidth(200);

        tabela.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        tabela.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        tabela.getColumnModel().getColumn(6).setCellRenderer(new AcoesRenderer());
        tabela.getColumnModel().getColumn(6).setCellEditor(new AcoesEditor());

        tabela.setRowHeight(46);

        JScrollPane scroll = new JScrollPane(tabela);
        UIConstants.styleScrollPane(scroll);
        return scroll;
    }

    // -------------------------------------------------------------------------
    // Carregar dados
    // -------------------------------------------------------------------------
    private void carregarDados(String filtro) {
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() {
                List<Object[]> lista = new ArrayList<>();
                ConexaoBanco cb = new ConexaoBanco();
                try (Connection conn = cb.abrirConexao()) {
                    if (conn == null) return lista;
                    String sql = "SELECT ID_restaurante, nome, categoria, avaliacao, ativo, aberto "
                               + "FROM tb_restaurantes WHERE (nome LIKE ? OR categoria LIKE ?) "
                               + "ORDER BY nome";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        String like = "%" + filtro + "%";
                        ps.setString(1, like);
                        ps.setString(2, like);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                lista.add(new Object[]{
                                    rs.getInt("ID_restaurante"),
                                    rs.getString("nome"),
                                    rs.getString("categoria"),
                                    rs.getString("avaliacao"),
                                    rs.getBoolean("ativo") ? "Ativo" : "Inativo",
                                    rs.getBoolean("aberto") ? "Aberto" : "Fechado",
                                    "ACOES"
                                });
                            }
                        }
                    }
                } catch (SQLException ex) {
                    System.err.println("Erro ao carregar restaurantes: " + ex.getMessage());
                }
                return lista;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> lista = get();
                    modeloTabela.setRowCount(0);
                    for (Object[] row : lista) modeloTabela.addRow(row);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // -------------------------------------------------------------------------
    // Diálogo de criação/edição de restaurante
    // -------------------------------------------------------------------------
    private void abrirDialogRestaurante(Integer idRestaurante) {
        JDialog dialog = new JDialog(parentFrame, idRestaurante == null ? "Novo Restaurante" : "Editar Restaurante", true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIConstants.BG_DARK_ALT);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 0);
        gc.weightx = 1.0;

        JTextField txtNome        = new JTextField(); UIConstants.styleField(txtNome);
        JTextField txtCategoria   = new JTextField(); UIConstants.styleField(txtCategoria);
        JTextField txtDescricao   = new JTextField(); UIConstants.styleField(txtDescricao);
        JTextField txtTempo       = new JTextField(); UIConstants.styleField(txtTempo);
        JTextField txtTaxa        = new JTextField(); UIConstants.styleField(txtTaxa);
        JCheckBox  chkAtivo       = new JCheckBox("Ativo na Plataforma");
        chkAtivo.setForeground(UIConstants.FG_LIGHT);
        chkAtivo.setBackground(UIConstants.BG_DARK_ALT);
        chkAtivo.setFont(UIConstants.FONT_REGULAR);
        chkAtivo.setSelected(true);

        // Se editando, carregar dados
        if (idRestaurante != null) {
            carregarDadosRestaurante(idRestaurante, txtNome, txtCategoria, txtDescricao, txtTempo, txtTaxa, chkAtivo);
        }

        int gridy = 0;
        gc.gridx = 0; gc.gridy = gridy++;
        JLabel lbl = new JLabel(idRestaurante == null ? "Novo Restaurante" : "Editar Restaurante");
        lbl.setFont(UIConstants.FONT_TITLE); lbl.setForeground(UIConstants.FG_LIGHT);
        panel.add(lbl, gc);

        gc.gridy = gridy++; panel.add(criarLabelForm("Nome *"), gc);
        gc.gridy = gridy++; panel.add(txtNome, gc);
        gc.gridy = gridy++; panel.add(criarLabelForm("Categoria"), gc);
        gc.gridy = gridy++; panel.add(txtCategoria, gc);
        gc.gridy = gridy++; panel.add(criarLabelForm("Descrição"), gc);
        gc.gridy = gridy++; panel.add(txtDescricao, gc);

        JPanel rowTempoTaxa = new JPanel(new GridLayout(1, 2, 10, 0));
        rowTempoTaxa.setOpaque(false);
        JPanel colTempo = new JPanel(new BorderLayout(0, 6));
        colTempo.setOpaque(false);
        colTempo.add(criarLabelForm("Tempo Entrega"), BorderLayout.NORTH);
        colTempo.add(txtTempo, BorderLayout.CENTER);
        JPanel colTaxa = new JPanel(new BorderLayout(0, 6));
        colTaxa.setOpaque(false);
        colTaxa.add(criarLabelForm("Taxa Entrega (R$)"), BorderLayout.NORTH);
        colTaxa.add(txtTaxa, BorderLayout.CENTER);
        rowTempoTaxa.add(colTempo); rowTempoTaxa.add(colTaxa);
        gc.gridy = gridy++; panel.add(rowTempoTaxa, gc);

        gc.gridy = gridy++; panel.add(chkAtivo, gc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnCancelar = new JButton("Cancelar");
        UIConstants.styleSecondary(btnCancelar);
        btnCancelar.addActionListener(e -> dialog.dispose());
        JButton btnSalvar = new JButton("Salvar");
        UIConstants.stylePrimary(btnSalvar);
        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) {
                Toast.show(dialog, "O nome do restaurante é obrigatório.", Toast.Type.WARNING);
                return;
            }
            salvarRestaurante(idRestaurante, nome, txtCategoria.getText().trim(),
                    txtDescricao.getText().trim(), txtTempo.getText().trim(),
                    txtTaxa.getText().trim(), chkAtivo.isSelected());
            dialog.dispose();
            carregarDados(txtBusca.getText().trim());
        });
        btnPanel.add(btnCancelar); btnPanel.add(btnSalvar);
        gc.gridy = gridy++; gc.insets = new Insets(15, 0, 0, 0);
        panel.add(btnPanel, gc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void carregarDadosRestaurante(int id, JTextField nome, JTextField cat, JTextField desc,
                                          JTextField tempo, JTextField taxa, JCheckBox chkAtivo) {
        ConexaoBanco cb = new ConexaoBanco();
        try (Connection conn = cb.abrirConexao()) {
            if (conn == null) return;
            String sql = "SELECT * FROM tb_restaurantes WHERE ID_restaurante = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nome.setText(rs.getString("nome"));
                        cat.setText(rs.getString("categoria"));
                        desc.setText(rs.getString("descricao"));
                        tempo.setText(rs.getString("tempo_entrega"));
                        taxa.setText(rs.getString("taxa_entrega") != null ? rs.getString("taxa_entrega") : "");
                        chkAtivo.setSelected(rs.getBoolean("ativo"));
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao carregar restaurante: " + ex.getMessage());
        }
    }

    private void salvarRestaurante(Integer id, String nome, String categoria, String descricao,
                                   String tempo, String taxaStr, boolean ativo) {
        ConexaoBanco cb = new ConexaoBanco();
        try (Connection conn = cb.abrirConexao()) {
            if (conn == null) {
                Toast.show(this, "Erro de conexão.", Toast.Type.ERROR);
                return;
            }
            double taxa = 0.0;
            try { taxa = Double.parseDouble(taxaStr.replace(",", ".")); } catch (NumberFormatException ignored) {}

            if (id == null) {
                String sql = "INSERT INTO tb_restaurantes (nome, categoria, descricao, tempo_entrega, taxa_entrega, ativo, aberto) "
                           + "VALUES (?, ?, ?, ?, ?, ?, 1)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, nome); ps.setString(2, categoria); ps.setString(3, descricao);
                    ps.setString(4, tempo); ps.setDouble(5, taxa); ps.setBoolean(6, ativo);
                    ps.executeUpdate();
                }
                Toast.show(this, "Restaurante criado com sucesso!", Toast.Type.SUCCESS);
            } else {
                String sql = "UPDATE tb_restaurantes SET nome=?, categoria=?, descricao=?, tempo_entrega=?, taxa_entrega=?, ativo=? "
                           + "WHERE ID_restaurante=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, nome); ps.setString(2, categoria); ps.setString(3, descricao);
                    ps.setString(4, tempo); ps.setDouble(5, taxa); ps.setBoolean(6, ativo);
                    ps.setInt(7, id);
                    ps.executeUpdate();
                }
                Toast.show(this, "Restaurante atualizado!", Toast.Type.SUCCESS);
            }
        } catch (SQLException ex) {
            Toast.show(this, "Erro ao salvar: " + ex.getMessage(), Toast.Type.ERROR);
        }
    }

    private void alternarAtivo(int idRestaurante, boolean novoAtivo) {
        ConexaoBanco cb = new ConexaoBanco();
        try (Connection conn = cb.abrirConexao()) {
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE tb_restaurantes SET ativo=? WHERE ID_restaurante=?")) {
                ps.setBoolean(1, novoAtivo);
                ps.setInt(2, idRestaurante);
                ps.executeUpdate();
            }
            String msg = novoAtivo ? "Restaurante ativado na plataforma." : "Restaurante desativado da plataforma.";
            Toast.show(this, msg, novoAtivo ? Toast.Type.SUCCESS : Toast.Type.WARNING);
            carregarDados(txtBusca.getText().trim());
        } catch (SQLException ex) {
            Toast.show(this, "Erro: " + ex.getMessage(), Toast.Type.ERROR);
        }
    }

    private void entrarNoContexto(int idRestaurante, String nomeRestaurante) {
        SessionContext.getInstance().setRestauranteSelecionadoId(idRestaurante);
        Toast.show(this, "Contexto: " + nomeRestaurante, Toast.Type.INFO);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private JLabel criarLabelForm(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(UIConstants.ARIAL_12);
        l.setForeground(UIConstants.FG_MUTED);
        return l;
    }

    // -------------------------------------------------------------------------
    // Cell Renderers / Editors
    // -------------------------------------------------------------------------

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(UIConstants.ARIAL_12_B);
            if ("Ativo".equals(v) || "Aberto".equals(v)) {
                l.setForeground(UIConstants.SUCCESS_GREEN);
            } else {
                l.setForeground(UIConstants.DANGER_RED);
            }
            return l;
        }
    }

    private class AcoesRenderer extends DefaultTableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        private final JButton btnEditar   = new JButton("Editar");
        private final JButton btnToggle   = new JButton();
        private final JButton btnContexto = new JButton("Gerenciar");

        AcoesRenderer() {
            panel.setOpaque(true);
            UIConstants.styleSecondary(btnEditar);
            btnEditar.setFont(UIConstants.ARIAL_12);
            UIConstants.styleSecondary(btnToggle);
            btnToggle.setFont(UIConstants.ARIAL_12);
            UIConstants.stylePrimary(btnContexto);
            btnContexto.setFont(UIConstants.ARIAL_12);
            panel.add(btnEditar); panel.add(btnToggle); panel.add(btnContexto);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String ativo = (String) t.getValueAt(r, 4);
            btnToggle.setText("Ativo".equals(ativo) ? "Desativar" : "Ativar");
            panel.setBackground(sel ? UIConstants.SEL_BG : (r % 2 == 0 ? UIConstants.BG_DARK : UIConstants.ALT_ROW));
            return panel;
        }
    }

    private class AcoesEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel  = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        private final JButton btnEditar   = new JButton("Editar");
        private final JButton btnToggle   = new JButton();
        private final JButton btnContexto = new JButton("Gerenciar");
        private int currentRow;

        AcoesEditor() {
            panel.setOpaque(true);
            panel.setBackground(UIConstants.SEL_BG);

            UIConstants.styleSecondary(btnEditar);
            btnEditar.setFont(UIConstants.ARIAL_12);
            UIConstants.styleSecondary(btnToggle);
            btnToggle.setFont(UIConstants.ARIAL_12);
            UIConstants.stylePrimary(btnContexto);
            btnContexto.setFont(UIConstants.ARIAL_12);

            btnEditar.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) modeloTabela.getValueAt(currentRow, 0);
                abrirDialogRestaurante(id);
            });
            btnToggle.addActionListener(e -> {
                fireEditingStopped();
                int id       = (int) modeloTabela.getValueAt(currentRow, 0);
                String ativo = (String) modeloTabela.getValueAt(currentRow, 4);
                alternarAtivo(id, !"Ativo".equals(ativo));
            });
            btnContexto.addActionListener(e -> {
                fireEditingStopped();
                int id     = (int) modeloTabela.getValueAt(currentRow, 0);
                String nom = (String) modeloTabela.getValueAt(currentRow, 1);
                entrarNoContexto(id, nom);
            });

            panel.add(btnEditar); panel.add(btnToggle); panel.add(btnContexto);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            currentRow = r;
            String ativo = (String) modeloTabela.getValueAt(r, 4);
            btnToggle.setText("Ativo".equals(ativo) ? "Desativar" : "Ativar");
            return panel;
        }

        @Override public Object getCellEditorValue() { return "ACOES"; }
    }
}
