package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import com.senac.food.verse.SessionContext;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

/**
 * Sprint 4 — Painel "Meu Restaurante".
 * Permite ao Gerente (e ao Admin no contexto do restaurante) editar os dados e abrir/fechar.
 * O Gerente NÃO pode alterar o campo `ativo` (pertence ao Admin).
 */
public class MeuRestaurantePanel extends JPanel {

    private JTextField txtNome, txtCategoria, txtDescricao, txtTempo, txtTaxa, txtImagem;
    private JLabel lblStatusAberto;
    private JButton btnToggleAberto;
    private JButton btnSalvar;

    private int idRestaurante = 0;
    private boolean estaAberto = true;

    public MeuRestaurantePanel() {
        setLayout(new BorderLayout());
        UIConstants.stylePanel(this);

        add(criarHeader(), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(0, 20));
        corpo.setBackground(UIConstants.BG_DARK);
        corpo.setBorder(new EmptyBorder(15, 30, 30, 30));
        corpo.add(criarFormulario(), BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);

        carregarDados();
    }

    // -------------------------------------------------------------------------
    // Header
    // -------------------------------------------------------------------------
    private JPanel criarHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(15, 10, 15, 10));

        JLabel titulo = new JLabel("Meu Restaurante");
        titulo.setFont(UIConstants.FONT_TITLE);
        titulo.setForeground(UIConstants.FG_LIGHT);
        titulo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.BUSINESS, 32, UIConstants.PRIMARY_RED));
        titulo.setIconTextGap(15);
        p.add(titulo, BorderLayout.WEST);

        // Badge de status operacional
        lblStatusAberto = criarBadgeStatus();
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bp.setOpaque(false);
        bp.add(lblStatusAberto);
        p.add(bp, BorderLayout.EAST);

        return p;
    }

    private JLabel criarBadgeStatus() {
        JLabel l = new JLabel("Carregando...") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        l.setFont(UIConstants.FONT_BOLD);
        l.setForeground(Color.WHITE);
        l.setOpaque(false);
        l.setBorder(new EmptyBorder(6, 14, 6, 14));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    // -------------------------------------------------------------------------
    // Formulário
    // -------------------------------------------------------------------------
    private JPanel criarFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_DARK);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 10, 6, 10);
        gc.weightx = 1.0;

        txtNome      = new JTextField(); UIConstants.styleField(txtNome);
        txtCategoria = new JTextField(); UIConstants.styleField(txtCategoria);
        txtDescricao = new JTextField(); UIConstants.styleField(txtDescricao);
        txtTempo     = new JTextField(); UIConstants.styleField(txtTempo);
        txtTaxa      = new JTextField(); UIConstants.styleField(txtTaxa);
        txtImagem    = new JTextField(); UIConstants.styleField(txtImagem);

        int gridy = 0;

        // Linha 1: Nome + Categoria
        gc.gridwidth = 1; gc.gridx = 0; gc.gridy = gridy; form.add(criarLabel("Nome *"), gc);
        gc.gridx = 1; form.add(criarLabel("Categoria"), gc);
        gridy++;
        gc.gridx = 0; gc.gridy = gridy; form.add(txtNome, gc);
        gc.gridx = 1; form.add(txtCategoria, gc);
        gridy++;

        // Linha 2: Descrição (span 2)
        gc.gridx = 0; gc.gridy = gridy; gc.gridwidth = 2; form.add(criarLabel("Descrição"), gc);
        gridy++;
        gc.gridx = 0; gc.gridy = gridy; form.add(txtDescricao, gc);
        gridy++;

        // Linha 3: Tempo + Taxa
        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = gridy; form.add(criarLabel("Tempo de Entrega"), gc);
        gc.gridx = 1; form.add(criarLabel("Taxa de Entrega (R$)"), gc);
        gridy++;
        gc.gridx = 0; gc.gridy = gridy; form.add(txtTempo, gc);
        gc.gridx = 1; form.add(txtTaxa, gc);
        gridy++;

        // Linha 4: Imagem (span 2)
        gc.gridx = 0; gc.gridy = gridy; gc.gridwidth = 2; form.add(criarLabel("Imagem (URL ou caminho)"), gc);
        gridy++;
        gc.gridx = 0; gc.gridy = gridy; form.add(txtImagem, gc);
        gridy++;

        // Separador
        gc.gridx = 0; gc.gridy = gridy; gc.insets = new Insets(20, 10, 10, 10);
        JSeparator sep = new JSeparator();
        sep.setForeground(UIConstants.GRID_DARK);
        form.add(sep, gc);
        gridy++;

        // Botão Abrir/Fechar + Salvar
        gc.insets = new Insets(10, 10, 6, 10);
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        acoes.setOpaque(false);

        btnToggleAberto = new JButton("Fechar Restaurante");
        UIConstants.styleDanger(btnToggleAberto);
        btnToggleAberto.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.LOCK, 18, Color.WHITE));
        btnToggleAberto.addActionListener(e -> toggleAberto());
        acoes.add(btnToggleAberto);

        btnSalvar = new JButton("Salvar Alterações");
        UIConstants.stylePrimary(btnSalvar);
        btnSalvar.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SAVE, 18, Color.WHITE));
        btnSalvar.addActionListener(e -> salvarDados());
        acoes.add(btnSalvar);

        gc.gridx = 0; gc.gridy = gridy; gc.gridwidth = 2;
        form.add(acoes, gc);

        return form;
    }

    private JLabel criarLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(UIConstants.ARIAL_12);
        l.setForeground(UIConstants.FG_MUTED);
        return l;
    }

    // -------------------------------------------------------------------------
    // Carregar dados do restaurante
    // -------------------------------------------------------------------------
    private void carregarDados() {
        SessionContext ctx = SessionContext.getInstance();
        idRestaurante = ctx.getRestauranteEfetivo();

        if (idRestaurante == 0) {
            Toast.show(this, "Nenhum restaurante selecionado.", Toast.Type.WARNING);
            return;
        }

        new SwingWorker<Void, Void>() {
            String nome, cat, desc, tempo, taxa, img;
            boolean aberto;

            @Override
            protected Void doInBackground() {
                ConexaoBanco cb = new ConexaoBanco();
                try (Connection conn = cb.abrirConexao()) {
                    if (conn == null) return null;
                    String sql = "SELECT nome, categoria, descricao, tempo_entrega, taxa_entrega, imagem, aberto "
                               + "FROM tb_restaurantes WHERE ID_restaurante = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, idRestaurante);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                nome   = rs.getString("nome");
                                cat    = rs.getString("categoria");
                                desc   = rs.getString("descricao");
                                tempo  = rs.getString("tempo_entrega");
                                taxa   = rs.getString("taxa_entrega");
                                img    = rs.getString("imagem");
                                aberto = rs.getBoolean("aberto");
                            }
                        }
                    }
                } catch (SQLException ex) {
                    System.err.println("Erro ao carregar restaurante: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                if (nome != null) {
                    txtNome.setText(nome);
                    txtCategoria.setText(cat != null ? cat : "");
                    txtDescricao.setText(desc != null ? desc : "");
                    txtTempo.setText(tempo != null ? tempo : "");
                    txtTaxa.setText(taxa != null ? taxa : "");
                    txtImagem.setText(img != null ? img : "");
                    estaAberto = aberto;
                    atualizarBadgeAberto();
                }
            }
        }.execute();
    }

    private void atualizarBadgeAberto() {
        if (estaAberto) {
            lblStatusAberto.setText("● ABERTO");
            lblStatusAberto.setBackground(UIConstants.SUCCESS_GREEN);
            btnToggleAberto.setText("Fechar Restaurante");
            btnToggleAberto.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.LOCK, 18, Color.WHITE));
        } else {
            lblStatusAberto.setText("● FECHADO");
            lblStatusAberto.setBackground(UIConstants.DANGER_RED);
            btnToggleAberto.setText("Abrir Restaurante");
            btnToggleAberto.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.LOCK_OPEN, 18, Color.WHITE));
        }
    }

    // -------------------------------------------------------------------------
    // Salvar dados
    // -------------------------------------------------------------------------
    private void salvarDados() {
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            Toast.show(this, "O nome do restaurante é obrigatório.", Toast.Type.WARNING);
            return;
        }
        if (idRestaurante == 0) {
            Toast.show(this, "Nenhum restaurante selecionado.", Toast.Type.WARNING);
            return;
        }

        double taxa = 0.0;
        try { taxa = Double.parseDouble(txtTaxa.getText().trim().replace(",", ".")); } catch (NumberFormatException ignored) {}
        final double taxaFinal = taxa;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                ConexaoBanco cb = new ConexaoBanco();
                try (Connection conn = cb.abrirConexao()) {
                    if (conn == null) return false;
                    String sql = "UPDATE tb_restaurantes SET nome=?, categoria=?, descricao=?, "
                               + "tempo_entrega=?, taxa_entrega=?, imagem=? "
                               + "WHERE ID_restaurante=?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, txtNome.getText().trim());
                        ps.setString(2, txtCategoria.getText().trim());
                        ps.setString(3, txtDescricao.getText().trim());
                        ps.setString(4, txtTempo.getText().trim());
                        ps.setDouble(5, taxaFinal);
                        ps.setString(6, txtImagem.getText().trim());
                        ps.setInt(7, idRestaurante);
                        return ps.executeUpdate() > 0;
                    }
                } catch (SQLException ex) {
                    System.err.println("Erro ao salvar: " + ex.getMessage());
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        Toast.show(MeuRestaurantePanel.this, "Dados atualizados com sucesso!", Toast.Type.SUCCESS);
                    } else {
                        Toast.show(MeuRestaurantePanel.this, "Erro ao salvar dados.", Toast.Type.ERROR);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // -------------------------------------------------------------------------
    // Abrir / Fechar restaurante
    // -------------------------------------------------------------------------
    private void toggleAberto() {
        if (idRestaurante == 0) {
            Toast.show(this, "Nenhum restaurante selecionado.", Toast.Type.WARNING);
            return;
        }
        boolean novoEstado = !estaAberto;
        String acao = novoEstado ? "abrir" : "fechar temporariamente";
        UIConstants.showConfirmDialog(this,
                "Confirmar",
                "Deseja " + acao + " o restaurante para pedidos?",
                () -> executarToggleAberto(novoEstado));
    }

    private void executarToggleAberto(boolean novoEstado) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                ConexaoBanco cb = new ConexaoBanco();
                try (Connection conn = cb.abrirConexao()) {
                    if (conn == null) return false;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE tb_restaurantes SET aberto=? WHERE ID_restaurante=?")) {
                        ps.setBoolean(1, novoEstado);
                        ps.setInt(2, idRestaurante);
                        return ps.executeUpdate() > 0;
                    }
                } catch (SQLException ex) {
                    System.err.println("Erro ao atualizar aberto: " + ex.getMessage());
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        estaAberto = novoEstado;
                        atualizarBadgeAberto();
                        String msg = novoEstado ? "Restaurante aberto para pedidos!" : "Restaurante fechado temporariamente.";
                        Toast.show(MeuRestaurantePanel.this, msg,
                                novoEstado ? Toast.Type.SUCCESS : Toast.Type.WARNING);
                    } else {
                        Toast.show(MeuRestaurantePanel.this, "Erro ao atualizar status.", Toast.Type.ERROR);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}
