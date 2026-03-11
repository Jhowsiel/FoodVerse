package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import com.senac.food.verse.SessionContext;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AprovacaoCadastrosPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(AprovacaoCadastrosPanel.class.getName());

    private static final String STATUS_ATIVO = "ativo";
    private static final String STATUS_PENDENTE = "pendente";
    private static final String STATUS_BLOQUEADO = "bloqueado";
    private static final String STATUS_DESLIGADO = "desligado";
    private static final String STATUS_OFFLINE = "offline";
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final SessionContext sessionContext;
    private final boolean modoGerentesGlobal;

    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JTextField txtBusca;
    private JLabel lblTotalPendentes;
    private JLabel lblTotalAtivos;

    private final Color STATUS_PENDING = new Color(230, 126, 34);

    public AprovacaoCadastrosPanel() {
        this.sessionContext = SessionContext.getInstance();
        this.modoGerentesGlobal = isGlobalManagerMode(sessionContext);

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

    static boolean isGlobalManagerMode(SessionContext ctx) {
        return ctx != null && ctx.isAdmin() && !ctx.adminTemContextoRestaurante();
    }

    static String buildPanelTitle(SessionContext ctx) {
        return isGlobalManagerMode(ctx)
                ? "Gestão Global de Gerentes"
                : "Gestão de Equipe do Restaurante";
    }

    static String buildPrimaryActionLabel(SessionContext ctx) {
        return isGlobalManagerMode(ctx) ? "Novo Gerente" : "Novo Colaborador";
    }

    static String buildSearchPlaceholder(SessionContext ctx) {
        return isGlobalManagerMode(ctx)
                ? "Buscar gerente por nome, e-mail ou restaurante..."
                : "Buscar nome, e-mail, usuário ou cargo...";
    }

    static List<String> buildAllowedRoles(SessionContext ctx) {
        if (isGlobalManagerMode(ctx)) {
            return List.of("Gerente");
        }
        if (ctx != null && ctx.isAdmin()) {
            return List.of("Gerente", "Atendente", "Cozinheiro", "Entregador");
        }
        return List.of("Atendente", "Cozinheiro", "Entregador");
    }

    private JPanel criarHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(15, 10, 15, 10));

        JLabel titulo = new JLabel(buildPanelTitle(sessionContext));
        titulo.setFont(UIConstants.FONT_TITLE);
        titulo.setForeground(UIConstants.FG_LIGHT);
        titulo.setIcon(IconFontSwing.buildIcon(
                GoogleMaterialDesignIcons.SUPERVISOR_ACCOUNT,
                32,
                UIConstants.FG_LIGHT));
        titulo.setIconTextGap(15);
        p.add(titulo, BorderLayout.WEST);

        JPanel cards = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        cards.setOpaque(false);
        lblTotalPendentes = criarBadgeInfo("Pendentes", STATUS_PENDING);
        lblTotalAtivos = criarBadgeInfo("Ativos", UIConstants.SUCCESS_GREEN);
        cards.add(lblTotalAtivos);
        cards.add(lblTotalPendentes);
        p.add(cards, BorderLayout.EAST);

        return p;
    }

    private JLabel criarBadgeInfo(String texto, Color cor) {
        JLabel l = new JLabel("0 " + texto) {
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
        l.setBackground(cor);
        l.setOpaque(false);
        l.setBorder(new EmptyBorder(5, 12, 5, 12));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private JPanel criarToolbar() {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setOpaque(false);

        txtBusca = new JTextField();
        UIConstants.styleField(txtBusca);
        txtBusca.setPreferredSize(new Dimension(320, 40));
        txtBusca.putClientProperty("JTextField.placeholderText", buildSearchPlaceholder(sessionContext));
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnNovo = new JButton(buildPrimaryActionLabel(sessionContext));
        btnNovo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.PERSON_ADD, 18, Color.WHITE));
        UIConstants.stylePrimary(btnNovo);
        btnNovo.setPreferredSize(new Dimension(170, 40));
        btnNovo.addActionListener(e -> abrirModalEdicao(null));

        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.REFRESH, 18, UIConstants.FG_LIGHT));
        UIConstants.styleSecondary(btnAtualizar);
        btnAtualizar.setPreferredSize(new Dimension(130, 40));
        btnAtualizar.addActionListener(e -> carregarDados(txtBusca.getText().trim()));

        btnPanel.add(btnNovo);
        btnPanel.add(btnAtualizar);

        p.add(txtBusca, BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.EAST);
        return p;
    }

    private void filtrar() {
        carregarDados(txtBusca.getText().trim());
    }

    private JScrollPane criarTabela() {
        String[] colunas = {"ID", "Nome", "Usuário", "E-mail", "Cargo", "Status", "Restaurante", "Ações"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        tabela = new JTable(modeloTabela);
        UIConstants.styleTable(tabela);
        tabela.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        tabela.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());
        tabela.getColumnModel().getColumn(7).setCellEditor(new ActionEditor());

        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        tabela.getColumnModel().getColumn(4).setMaxWidth(130);
        tabela.getColumnModel().getColumn(5).setMinWidth(130);
        tabela.getColumnModel().getColumn(5).setMaxWidth(140);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(7).setMinWidth(140);
        tabela.getColumnModel().getColumn(7).setMaxWidth(160);
        tabela.setRowHeight(46);

        if (!modoGerentesGlobal) {
            tabela.getColumnModel().getColumn(6).setMinWidth(0);
            tabela.getColumnModel().getColumn(6).setMaxWidth(0);
            tabela.getColumnModel().getColumn(6).setWidth(0);
        }

        JScrollPane scroll = new JScrollPane(tabela);
        UIConstants.styleScrollPane(scroll);
        return scroll;
    }

    private void carregarDados(String filtro) {
        modeloTabela.setRowCount(0);
        lblTotalPendentes.setText("0 Pendentes");
        lblTotalAtivos.setText("0 Ativos");

        int restauranteEfetivo = sessionContext.getRestauranteEfetivo();
        if (!modoGerentesGlobal && restauranteEfetivo <= 0) {
            return;
        }

        int pendentes = 0;
        int ativos = 0;

        StringBuilder sql = new StringBuilder(
                "SELECT f.ID_funcionario, f.nome, f.username, f.email, f.cargo, f.status, "
                        + "ISNULL(r.nome, '-') AS nome_restaurante "
                        + "FROM tb_funcionarios f "
                        + "LEFT JOIN tb_restaurantes r ON r.ID_restaurante = f.ID_restaurante ");

        List<Object> parametros = new ArrayList<>();
        if (modoGerentesGlobal) {
            sql.append("WHERE f.cargo = ? ");
            parametros.add("Gerente");
        } else {
            sql.append("WHERE f.ID_restaurante = ? AND (f.cargo IS NULL OR f.cargo <> ?) ");
            parametros.add(restauranteEfetivo);
            parametros.add("Admin");
        }

        if (!filtro.isEmpty()) {
            sql.append("AND (f.nome LIKE ? OR f.email LIKE ? OR f.cargo LIKE ? OR f.username LIKE ? OR ISNULL(r.nome, '-') LIKE ?) ");
            String like = "%" + filtro + "%";
            parametros.add(like);
            parametros.add(like);
            parametros.add(like);
            parametros.add(like);
            parametros.add(like);
        }

        sql.append("ORDER BY CASE WHEN f.status = 'pendente' THEN 0 ELSE 1 END, f.nome ASC");

        ConexaoBanco cb = new ConexaoBanco();
        try (Connection conn = cb.abrirConexao()) {
            if (conn == null) {
                throw new SQLException("Modo Offline");
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    Object parametro = parametros.get(i);
                    if (parametro instanceof Integer valorInt) {
                        ps.setInt(i + 1, valorInt);
                    } else if (parametro instanceof String texto) {
                        ps.setString(i + 1, texto);
                    } else {
                        throw new IllegalArgumentException("Tipo de parâmetro não suportado: " + parametro.getClass().getName());
                    }
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String status = normalizarStatus(rs.getString("status"));
                        if (STATUS_PENDENTE.equals(status)) pendentes++;
                        if (STATUS_ATIVO.equals(status)) ativos++;

                        modeloTabela.addRow(new Object[]{
                                rs.getInt("ID_funcionario"),
                                rs.getString("nome"),
                                rs.getString("username"),
                                rs.getString("email"),
                                rs.getString("cargo"),
                                status,
                                rs.getString("nome_restaurante"),
                                ""
                        });
                    }
                }
            }

            lblTotalPendentes.setText(pendentes + " Pendentes");
            lblTotalPendentes.setBackground(pendentes > 0 ? STATUS_PENDING : UIConstants.CARD_DARK);
            lblTotalAtivos.setText(ativos + " Ativos");
            lblTotalAtivos.setBackground(ativos > 0 ? UIConstants.SUCCESS_GREEN : UIConstants.CARD_DARK);

        } catch (Exception e) {
            modeloTabela.addRow(new Object[]{0, "Sistema Offline", "-", "-", modoGerentesGlobal ? "Gerente" : "Equipe", STATUS_OFFLINE, "-", ""});
        }
    }

    private void abrirModalEdicao(Integer idFuncionario) {
        FuncionarioFormData dadosAtuais = idFuncionario != null ? buscarFuncionarioPorId(idFuncionario) : null;
        if (idFuncionario != null && dadosAtuais == null) {
            UIConstants.showError(this, "Não foi possível carregar os dados do usuário.");
            return;
        }

        JDialog modal = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                idFuncionario == null ? buildPrimaryActionLabel(sessionContext) : "Editar cadastro", true);
        modal.setSize(470, modoGerentesGlobal ? 620 : 590);
        modal.setLocationRelativeTo(this);
        modal.getContentPane().setBackground(UIConstants.BG_DARK);
        modal.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_DARK);
        form.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 12, 0);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;

        JLabel lblTitulo = new JLabel(idFuncionario == null ? buildPrimaryActionLabel(sessionContext) : "Editar cadastro");
        lblTitulo.setFont(UIConstants.FONT_TITLE);
        lblTitulo.setForeground(UIConstants.FG_LIGHT);
        form.add(lblTitulo, gc);

        JTextField tNome = new JTextField();
        JTextField tUser = new JTextField();
        JTextField tEmail = new JTextField();
        JTextField tTelefone = new JTextField();
        JPasswordField tSenha = new JPasswordField();
        JComboBox<String> cRole = new JComboBox<>(buildAllowedRoles(sessionContext).toArray(new String[0]));
        JComboBox<RestauranteOption> cRestaurante = new JComboBox<>();
        JComboBox<String> cStatus = new JComboBox<>(new String[]{STATUS_ATIVO, STATUS_PENDENTE, STATUS_BLOQUEADO, STATUS_DESLIGADO});

        UIConstants.styleField(tNome);
        UIConstants.styleField(tUser);
        UIConstants.styleField(tEmail);
        UIConstants.styleField(tTelefone);
        UIConstants.styleField(tSenha);
        UIConstants.styleCombo(cRole);
        UIConstants.styleCombo(cRestaurante);
        UIConstants.styleCombo(cStatus);

        List<RestauranteOption> restaurantes = carregarRestaurantesParaSelecao();
        for (RestauranteOption option : restaurantes) {
            cRestaurante.addItem(option);
        }
        cRestaurante.setEnabled(modoGerentesGlobal);

        gc.gridy++;
        form.add(criarLabel("Nome Completo *"), gc);
        gc.gridy++;
        form.add(tNome, gc);

        gc.gridy++;
        form.add(criarLabel("Usuário (Login) *"), gc);
        gc.gridy++;
        form.add(tUser, gc);

        gc.gridy++;
        form.add(criarLabel("E-mail *"), gc);
        gc.gridy++;
        form.add(tEmail, gc);

        gc.gridy++;
        form.add(criarLabel("Telefone"), gc);
        gc.gridy++;
        form.add(tTelefone, gc);

        gc.gridy++;
        form.add(criarLabel("Cargo"), gc);
        gc.gridy++;
        form.add(cRole, gc);

        gc.gridy++;
        form.add(criarLabel(modoGerentesGlobal ? "Restaurante Vinculado *" : "Restaurante em Contexto"), gc);
        gc.gridy++;
        form.add(cRestaurante, gc);

        gc.gridy++;
        form.add(criarLabel("Status"), gc);
        gc.gridy++;
        form.add(cStatus, gc);

        gc.gridy++;
        form.add(criarLabel(idFuncionario == null ? "Senha *" : "Nova Senha (deixe vazio para manter)"), gc);
        gc.gridy++;
        form.add(tSenha, gc);

        if (cRole.getItemCount() == 1) {
            cRole.setSelectedIndex(0);
            cRole.setEnabled(false);
        }

        if (dadosAtuais != null) {
            tNome.setText(dadosAtuais.nome);
            tUser.setText(dadosAtuais.username);
            tEmail.setText(dadosAtuais.email);
            tTelefone.setText(dadosAtuais.telefone);
            cRole.setSelectedItem(dadosAtuais.cargo);
            cStatus.setSelectedItem(normalizarStatus(dadosAtuais.status));
            selecionarRestaurante(cRestaurante, dadosAtuais.restauranteId);
        } else {
            cStatus.setSelectedItem(STATUS_ATIVO);
            if (!restaurantes.isEmpty()) {
                cRestaurante.setSelectedIndex(0);
            }
        }

        modal.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(UIConstants.BG_DARK);
        btnPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton btnCancelar = new JButton("Cancelar");
        UIConstants.styleSecondary(btnCancelar);
        btnCancelar.addActionListener(e -> modal.dispose());

        JButton btnSalvar = new JButton("Salvar");
        UIConstants.styleSuccess(btnSalvar);
        btnSalvar.addActionListener(e -> {
            RestauranteOption restauranteSelecionado = (RestauranteOption) cRestaurante.getSelectedItem();
            boolean salvo = salvarFuncionario(
                    idFuncionario,
                    tNome.getText().trim(),
                    tUser.getText().trim(),
                    tEmail.getText().trim(),
                    tTelefone.getText().trim(),
                    Objects.toString(cRole.getSelectedItem(), ""),
                    restauranteSelecionado,
                    Objects.toString(cStatus.getSelectedItem(), STATUS_ATIVO),
                    new String(tSenha.getPassword()),
                    modal);
            if (salvo) {
                modal.dispose();
                carregarDados(txtBusca.getText().trim());
            }
        });

        btnPanel.add(btnCancelar);
        btnPanel.add(btnSalvar);
        modal.add(btnPanel, BorderLayout.SOUTH);
        modal.setVisible(true);
    }

    private boolean salvarFuncionario(Integer idFuncionario,
                                      String nome,
                                      String username,
                                      String email,
                                      String telefone,
                                      String cargo,
                                      RestauranteOption restauranteSelecionado,
                                      String status,
                                      String senha,
                                      Component parent) {
        if (nome.isEmpty() || username.isEmpty() || email.isEmpty()) {
            UIConstants.showWarning(parent, "Preencha nome, usuário e e-mail.");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            UIConstants.showWarning(parent, "Informe um e-mail válido.");
            return false;
        }
        String telefoneNormalizado = normalizarTelefone(telefone);
        if (!telefoneNormalizado.isEmpty() && !telefoneNormalizado.matches("\\d{10,11}")) {
            UIConstants.showWarning(parent, "Informe um telefone com 10 ou 11 dígitos. Formatação é aceita.");
            return false;
        }
        if (idFuncionario == null && senha.length() < 6) {
            UIConstants.showWarning(parent, "A senha deve ter no mínimo 6 caracteres.");
            return false;
        }
        if (idFuncionario != null && !senha.isBlank() && senha.length() < 6) {
            UIConstants.showWarning(parent, "A nova senha deve ter no mínimo 6 caracteres.");
            return false;
        }
        if (modoGerentesGlobal && !"Gerente".equalsIgnoreCase(cargo)) {
            UIConstants.showWarning(parent, "O painel global pode cadastrar apenas gerentes.");
            return false;
        }
        if (restauranteSelecionado == null || restauranteSelecionado.id <= 0) {
            UIConstants.showWarning(parent, "Selecione um restaurante válido.");
            return false;
        }

        String statusNormalizado = normalizarStatus(status);
        ConexaoBanco cb = new ConexaoBanco();
        try (Connection conn = cb.abrirConexao()) {
            if (conn == null) {
                UIConstants.showError(parent, "Banco de dados indisponível.");
                return false;
            }
            if (existeUsuarioDuplicado(conn, idFuncionario, username, email)) {
                UIConstants.showWarning(parent, "Já existe um usuário com este login ou e-mail.");
                return false;
            }

            if (idFuncionario == null) {
                String sql = "INSERT INTO tb_funcionarios (ID_restaurante, nome, username, email, cargo, telefone, senha, data_cadastro, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, restauranteSelecionado.id);
                    ps.setString(2, nome);
                    ps.setString(3, username);
                    ps.setString(4, email);
                    ps.setString(5, cargo);
                    ps.setString(6, telefoneNormalizado);
                    ps.setString(7, senha);
                    ps.setString(8, statusNormalizado);
                    ps.executeUpdate();
                }
                UIConstants.showSuccess(parent, modoGerentesGlobal ? "Gerente cadastrado com sucesso!" : "Colaborador cadastrado com sucesso!");
            } else {
                String sql = !senha.isBlank()
                        ? "UPDATE tb_funcionarios SET ID_restaurante=?, nome=?, username=?, email=?, cargo=?, telefone=?, status=?, senha=? WHERE ID_funcionario=?"
                        : "UPDATE tb_funcionarios SET ID_restaurante=?, nome=?, username=?, email=?, cargo=?, telefone=?, status=? WHERE ID_funcionario=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, restauranteSelecionado.id);
                    ps.setString(2, nome);
                    ps.setString(3, username);
                    ps.setString(4, email);
                    ps.setString(5, cargo);
                    ps.setString(6, telefoneNormalizado);
                    ps.setString(7, statusNormalizado);
                    if (!senha.isBlank()) {
                        ps.setString(8, senha);
                        ps.setInt(9, idFuncionario);
                    } else {
                        ps.setInt(8, idFuncionario);
                    }
                    ps.executeUpdate();
                }
                UIConstants.showSuccess(parent, "Cadastro atualizado com sucesso!");
            }
            return true;
        } catch (SQLException ex) {
            UIConstants.showError(parent, "Erro ao salvar cadastro: " + ex.getMessage());
            return false;
        }
    }

    private boolean existeUsuarioDuplicado(Connection conn, Integer idFuncionario, String username, String email) throws SQLException {
        String sql = idFuncionario == null
                ? "SELECT COUNT(*) FROM tb_funcionarios WHERE username = ? OR email = ?"
                : "SELECT COUNT(*) FROM tb_funcionarios WHERE (username = ? OR email = ?) AND ID_funcionario <> ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            if (idFuncionario != null) {
                ps.setInt(3, idFuncionario);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private FuncionarioFormData buscarFuncionarioPorId(int idFuncionario) {
        String sql = "SELECT ID_funcionario, ID_restaurante, nome, username, email, telefone, cargo, status "
                + "FROM tb_funcionarios WHERE ID_funcionario = ?";
        ConexaoBanco cb = new ConexaoBanco();
        try (Connection conn = cb.abrirConexao()) {
            if (conn == null) {
                return null;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idFuncionario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        FuncionarioFormData data = new FuncionarioFormData();
                        data.id = rs.getInt("ID_funcionario");
                        data.restauranteId = rs.getInt("ID_restaurante");
                        data.nome = rs.getString("nome");
                        data.username = rs.getString("username");
                        data.email = rs.getString("email");
                        data.telefone = rs.getString("telefone");
                        data.cargo = rs.getString("cargo");
                        data.status = rs.getString("status");
                        return data;
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Erro ao carregar funcionário", ex);
        }
        return null;
    }

    private List<RestauranteOption> carregarRestaurantesParaSelecao() {
        List<RestauranteOption> restaurantes = new ArrayList<>();
        ConexaoBanco cb = new ConexaoBanco();
        try (Connection conn = cb.abrirConexao()) {
            if (conn == null) {
                return restaurantes;
            }

            if (modoGerentesGlobal) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT ID_restaurante, nome FROM tb_restaurantes ORDER BY nome")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            restaurantes.add(new RestauranteOption(rs.getInt("ID_restaurante"), rs.getString("nome")));
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT ID_restaurante, nome FROM tb_restaurantes WHERE ID_restaurante = ?")) {
                    ps.setInt(1, sessionContext.getRestauranteEfetivo());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            restaurantes.add(new RestauranteOption(rs.getInt("ID_restaurante"), rs.getString("nome")));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Erro ao carregar restaurantes", ex);
        }

        if (restaurantes.isEmpty() && sessionContext.getRestauranteEfetivo() > 0) {
            restaurantes.add(new RestauranteOption(sessionContext.getRestauranteEfetivo(), buildFallbackRestaurantName(sessionContext.getRestauranteEfetivo())));
        }
        return restaurantes;
    }

    private static String buildFallbackRestaurantName(int restauranteId) {
        return "Restaurante #" + restauranteId;
    }

    private void selecionarRestaurante(JComboBox<RestauranteOption> combo, int restauranteId) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            RestauranteOption option = combo.getItemAt(i);
            if (option.id == restauranteId) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void atualizarStatus(int id, String novoStatus) {
        String sql = "UPDATE tb_funcionarios SET status = ? WHERE ID_funcionario = ?";
        ConexaoBanco cb = new ConexaoBanco();
        try (Connection conn = cb.abrirConexao()) {
            if (conn == null) {
                UIConstants.showError(this, "Banco de dados indisponível.");
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, normalizarStatus(novoStatus));
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            UIConstants.showSuccess(this, "Status atualizado para: " + normalizarStatus(novoStatus));
            carregarDados(txtBusca.getText().trim());
        } catch (SQLException e) {
            UIConstants.showError(this, "Erro ao atualizar status: " + e.getMessage());
        }
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) {
            return STATUS_PENDENTE;
        }
        if ("erro".equalsIgnoreCase(status) || STATUS_OFFLINE.equalsIgnoreCase(status)) {
            return STATUS_OFFLINE;
        }
        if ("aprovado".equalsIgnoreCase(status)) {
            return STATUS_ATIVO;
        }
        return status.toLowerCase();
    }

    private String normalizarTelefone(String telefone) {
        return telefone == null ? "" : telefone.replaceAll("\\D", "");
    }

    private JLabel criarLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(UIConstants.FG_MUTED);
        l.setFont(UIConstants.ARIAL_12_B);
        return l;
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = normalizarStatus((String) value);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(UIConstants.ARIAL_12_B);

            switch (status) {
                case STATUS_ATIVO -> {
                    l.setForeground(UIConstants.SUCCESS_GREEN);
                    l.setText("● ATIVO");
                }
                case STATUS_PENDENTE -> {
                    l.setForeground(STATUS_PENDING);
                    l.setText("● PENDENTE");
                }
                case STATUS_DESLIGADO -> {
                    l.setForeground(UIConstants.FG_MUTED);
                    l.setText("● DESLIGADO");
                }
                case STATUS_OFFLINE -> {
                    l.setForeground(UIConstants.FG_MUTED);
                    l.setText("● OFFLINE");
                }
                default -> {
                    l.setForeground(UIConstants.DANGER_RED);
                    l.setText("● BLOQUEADO");
                }
            }
            return l;
        }
    }

    class ActionPanel extends JPanel {
        JButton btnAtivar = new JButton();
        JButton btnEditar = new JButton();
        JButton btnBloquear = new JButton();

        ActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
            setOpaque(false);
            configBtn(btnAtivar, GoogleMaterialDesignIcons.CHECK, UIConstants.SUCCESS_GREEN);
            configBtn(btnEditar, GoogleMaterialDesignIcons.EDIT, UIConstants.FG_MUTED);
            configBtn(btnBloquear, GoogleMaterialDesignIcons.BLOCK, UIConstants.DANGER_RED);
            add(btnAtivar);
            add(btnEditar);
            add(btnBloquear);
        }

        private void configBtn(JButton b, GoogleMaterialDesignIcons icon, Color c) {
            b.setIcon(IconFontSwing.buildIcon(icon, 16, c));
            b.setPreferredSize(new Dimension(32, 32));
            b.setBackground(new Color(0, 0, 0, 0));
            b.setOpaque(false);
            b.setBorder(BorderFactory.createLineBorder(c, 1));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setContentAreaFilled(false);
        }
    }

    private void configurarPainelAcoes(ActionPanel panel, int row, boolean selected) {
        String status = normalizarStatus((String) tabela.getValueAt(row, 5));
        panel.setBackground(selected ? tabela.getSelectionBackground() : tabela.getBackground());
        panel.btnAtivar.setVisible(!STATUS_ATIVO.equals(status));
        panel.btnAtivar.setToolTipText("Ativar usuário");
        panel.btnEditar.setToolTipText("Editar cadastro");
        boolean podeEscalarBloqueio = !STATUS_DESLIGADO.equals(status);
        panel.btnBloquear.setVisible(podeEscalarBloqueio);
        panel.btnBloquear.setToolTipText(STATUS_BLOQUEADO.equals(status) ? "Desligar usuário" : "Bloquear usuário");
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        private final ActionPanel panel = new ActionPanel();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            configurarPainelAcoes(panel, row, isSelected);
            return panel;
        }
    }

    class ActionEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final ActionPanel panel = new ActionPanel();
        private int currentRow;

        ActionEditor() {
            panel.btnAtivar.addActionListener(e -> {
                int id = (int) tabela.getValueAt(currentRow, 0);
                fireEditingStopped();
                atualizarStatus(id, STATUS_ATIVO);
            });
            panel.btnEditar.addActionListener(e -> {
                int id = (int) tabela.getValueAt(currentRow, 0);
                fireEditingStopped();
                abrirModalEdicao(id);
            });
            panel.btnBloquear.addActionListener(e -> {
                int id = (int) tabela.getValueAt(currentRow, 0);
                String statusAtual = normalizarStatus((String) tabela.getValueAt(currentRow, 5));
                String proximoStatus = STATUS_BLOQUEADO.equals(statusAtual) ? STATUS_DESLIGADO : STATUS_BLOQUEADO;
                String acao = STATUS_DESLIGADO.equals(proximoStatus) ? "desligar" : "bloquear";
                fireEditingStopped();
                UIConstants.showConfirmDialog(AprovacaoCadastrosPanel.this,
                        "Confirmar ação",
                        "Deseja " + acao + " este cadastro?",
                        () -> atualizarStatus(id, proximoStatus));
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            configurarPainelAcoes(panel, row, true);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private static final class RestauranteOption {
        private final int id;
        private final String nome;

        private RestauranteOption(int id, String nome) {
            this.id = id;
            this.nome = nome == null || nome.isBlank() ? buildFallbackRestaurantName(id) : nome;
        }

        @Override
        public String toString() {
            return nome;
        }
    }

    private static final class FuncionarioFormData {
        private int id;
        private int restauranteId;
        private String nome;
        private String username;
        private String email;
        private String telefone;
        private String cargo;
        private String status;
    }
}
