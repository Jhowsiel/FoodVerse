package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import com.senac.food.verse.SessionContext;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;

/**
 * Sprint 4 — Painel "Meu Restaurante".
 * Permite ao Gerente (e ao Admin no contexto do restaurante) editar os dados e abrir/fechar.
 * O Gerente NÃO pode alterar o campo {@code ativo} (pertence ao Admin).
 * O Admin pode alterar {@code ativo} (participação na plataforma) além de {@code aberto}.
 */
public class MeuRestaurantePanel extends JPanel {

    /** Diretório controlado para imagens de restaurantes. */
    static final String IMAGES_DIR = "media" + File.separator + "restaurantes";

    private JTextField txtNome, txtCategoria, txtDescricao, txtTempo, txtTaxa;
    private JTextField txtImagem, txtBanner;
    private JLabel lblStatusAberto;
    private JLabel lblStatusAtivo;
    private JButton btnToggleAberto;
    private JButton btnToggleAtivo;
    private JButton btnSalvar;

    private int idRestaurante = 0;
    private boolean estaAberto = true;
    private boolean estaAtivo  = true;

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

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        badgePanel.setOpaque(false);

        lblStatusAtivo  = criarBadgeStatus("Carregando...", UIConstants.CARD_DARK);
        lblStatusAberto = criarBadgeStatus("Carregando...", UIConstants.CARD_DARK);
        badgePanel.add(lblStatusAtivo);
        badgePanel.add(lblStatusAberto);
        p.add(badgePanel, BorderLayout.EAST);

        return p;
    }

    private JLabel criarBadgeStatus(String texto, Color cor) {
        JLabel l = new JLabel(texto) {
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
        l.setForeground(UIConstants.SEL_FG);
        l.setBackground(cor);
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
        txtBanner    = new JTextField(); UIConstants.styleField(txtBanner);

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

        // Linha 4: Imagem (span 2, com botão de seleção de arquivo)
        gc.gridx = 0; gc.gridy = gridy; gc.gridwidth = 2; form.add(criarLabel("Imagem do Restaurante"), gc);
        gridy++;
        gc.gridx = 0; gc.gridy = gridy; form.add(criarCampoArquivo(txtImagem, "Escolher Imagem"), gc);
        gridy++;

        // Linha 5: Banner (span 2, com botão de seleção de arquivo)
        gc.gridx = 0; gc.gridy = gridy; gc.gridwidth = 2; form.add(criarLabel("Banner do Restaurante"), gc);
        gridy++;
        gc.gridx = 0; gc.gridy = gridy; form.add(criarCampoArquivo(txtBanner, "Escolher Banner"), gc);
        gridy++;

        // Linha 6: Status na Plataforma (ativo) — visível para todos; botão de toggle apenas para Admin
        SessionContext ctx = SessionContext.getInstance();
        if (ctx.isAdmin()) {
            gc.gridwidth = 2;
            gc.insets = new Insets(14, 10, 4, 10);
            gc.gridx = 0; gc.gridy = gridy;
            form.add(criarLabel("Status na Plataforma (Admin)"), gc);
            gridy++;
            gc.insets = new Insets(4, 10, 6, 10);
            gc.gridx = 0; gc.gridy = gridy;
            JPanel ativoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            ativoPanel.setOpaque(false);
            btnToggleAtivo = new JButton("Desativar da Plataforma");
            UIConstants.styleSecondary(btnToggleAtivo);
            btnToggleAtivo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.BLOCK, 16, UIConstants.FG_LIGHT));
            btnToggleAtivo.addActionListener(e -> toggleAtivo());
            ativoPanel.add(btnToggleAtivo);
            form.add(ativoPanel, gc);
            gridy++;
        }

        // Separador
        gc.gridwidth = 2;
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
        btnToggleAberto.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.LOCK, 18, UIConstants.SEL_FG));
        btnToggleAberto.addActionListener(e -> toggleAberto());
        acoes.add(btnToggleAberto);

        btnSalvar = new JButton("Salvar Alterações");
        UIConstants.stylePrimary(btnSalvar);
        btnSalvar.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SAVE, 18, UIConstants.SEL_FG));
        btnSalvar.addActionListener(e -> salvarDados());
        acoes.add(btnSalvar);

        gc.gridx = 0; gc.gridy = gridy; gc.gridwidth = 2;
        form.add(acoes, gc);

        return form;
    }

    /**
     * Cria um painel com campo de texto + botão "Escolher..." para seleção de arquivo de imagem.
     */
    private JPanel criarCampoArquivo(JTextField campo, String labelBotao) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.add(campo, BorderLayout.CENTER);

        JButton btnEscolher = new JButton(labelBotao);
        UIConstants.styleSecondary(btnEscolher);
        btnEscolher.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.FOLDER_OPEN, 16, UIConstants.FG_LIGHT));
        btnEscolher.addActionListener(e -> selecionarArquivoImagem(campo));
        p.add(btnEscolher, BorderLayout.EAST);
        return p;
    }

    private JLabel criarLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(UIConstants.ARIAL_12);
        l.setForeground(UIConstants.FG_MUTED);
        return l;
    }

    // -------------------------------------------------------------------------
    // Seleção de arquivo de imagem
    // -------------------------------------------------------------------------
    private void selecionarArquivoImagem(JTextField campo) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar imagem");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imagens (JPG, PNG, GIF, WEBP)", "jpg", "jpeg", "png", "gif", "webp"));
        chooser.setAcceptAllFileFilterUsed(false);

        int resultado = chooser.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) return;

        File arquivoOrigem = chooser.getSelectedFile();
        try {
            Path dirDestino = Path.of(IMAGES_DIR);
            Files.createDirectories(dirDestino);
            Path arquivoDestino = dirDestino.resolve(arquivoOrigem.getName());
            Files.copy(arquivoOrigem.toPath(), arquivoDestino, StandardCopyOption.REPLACE_EXISTING);
            campo.setText(IMAGES_DIR + File.separator + arquivoOrigem.getName());
        } catch (IOException ex) {
            Toast.show(this, "Erro ao copiar imagem: " + ex.getMessage(), Toast.Type.ERROR);
        }
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
            String nome, cat, desc, tempo, taxa, img, banner;
            boolean aberto, ativo;

            @Override
            protected Void doInBackground() {
                ConexaoBanco cb = new ConexaoBanco();
                try (Connection conn = cb.abrirConexao()) {
                    if (conn == null) return null;
                    String sql = "SELECT nome, categoria, descricao, tempo_entrega, taxa_entrega, "
                               + "imagem, banner, aberto, ativo "
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
                                banner = rs.getString("banner");
                                aberto = rs.getBoolean("aberto");
                                ativo  = rs.getBoolean("ativo");
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
                    txtBanner.setText(banner != null ? banner : "");
                    estaAberto = aberto;
                    estaAtivo  = ativo;
                    atualizarBadgeAberto();
                    atualizarBadgeAtivo();
                }
            }
        }.execute();
    }

    private void atualizarBadgeAberto() {
        if (estaAberto) {
            lblStatusAberto.setText("● ABERTO");
            lblStatusAberto.setBackground(UIConstants.SUCCESS_GREEN);
            btnToggleAberto.setText("Fechar Restaurante");
            btnToggleAberto.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.LOCK, 18, UIConstants.SEL_FG));
            UIConstants.styleDanger(btnToggleAberto);
        } else {
            lblStatusAberto.setText("● FECHADO");
            lblStatusAberto.setBackground(UIConstants.DANGER_RED);
            btnToggleAberto.setText("Abrir Restaurante");
            btnToggleAberto.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.LOCK_OPEN, 18, UIConstants.SEL_FG));
            UIConstants.styleSuccess(btnToggleAberto);
        }
    }

    private void atualizarBadgeAtivo() {
        if (estaAtivo) {
            lblStatusAtivo.setText("● Plataforma: ATIVO");
            lblStatusAtivo.setBackground(UIConstants.SUCCESS_GREEN);
        } else {
            lblStatusAtivo.setText("● Plataforma: INATIVO");
            lblStatusAtivo.setBackground(UIConstants.DANGER_RED);
        }
        if (btnToggleAtivo != null) {
            if (estaAtivo) {
                btnToggleAtivo.setText("Desativar da Plataforma");
                btnToggleAtivo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.BLOCK, 16, UIConstants.FG_LIGHT));
                UIConstants.styleSecondary(btnToggleAtivo);
            } else {
                btnToggleAtivo.setText("Ativar na Plataforma");
                btnToggleAtivo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.CHECK_CIRCLE, 16, UIConstants.SEL_FG));
                UIConstants.styleSuccess(btnToggleAtivo);
            }
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
                               + "tempo_entrega=?, taxa_entrega=?, imagem=?, banner=? "
                               + "WHERE ID_restaurante=?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, txtNome.getText().trim());
                        ps.setString(2, txtCategoria.getText().trim());
                        ps.setString(3, txtDescricao.getText().trim());
                        ps.setString(4, txtTempo.getText().trim());
                        ps.setDouble(5, taxaFinal);
                        ps.setString(6, txtImagem.getText().trim());
                        ps.setString(7, txtBanner.getText().trim());
                        ps.setInt(8, idRestaurante);
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
    // Abrir / Fechar restaurante (campo aberto — Gerente e Admin)
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

    // -------------------------------------------------------------------------
    // Ativar / Desativar restaurante na plataforma (campo ativo — Admin apenas)
    // -------------------------------------------------------------------------
    private void toggleAtivo() {
        if (idRestaurante == 0) {
            Toast.show(this, "Nenhum restaurante selecionado.", Toast.Type.WARNING);
            return;
        }
        boolean novoEstado = !estaAtivo;
        String acao = novoEstado ? "ativar" : "desativar permanentemente";
        UIConstants.showConfirmDialog(this,
                "Confirmar",
                "Deseja " + acao + " este restaurante na plataforma?",
                () -> executarToggleAtivo(novoEstado));
    }

    private void executarToggleAtivo(boolean novoEstado) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                ConexaoBanco cb = new ConexaoBanco();
                try (Connection conn = cb.abrirConexao()) {
                    if (conn == null) return false;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE tb_restaurantes SET ativo=? WHERE ID_restaurante=?")) {
                        ps.setBoolean(1, novoEstado);
                        ps.setInt(2, idRestaurante);
                        return ps.executeUpdate() > 0;
                    }
                } catch (SQLException ex) {
                    System.err.println("Erro ao atualizar ativo: " + ex.getMessage());
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        estaAtivo = novoEstado;
                        atualizarBadgeAtivo();
                        String msg = novoEstado ? "Restaurante ativado na plataforma!" : "Restaurante desativado da plataforma.";
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
