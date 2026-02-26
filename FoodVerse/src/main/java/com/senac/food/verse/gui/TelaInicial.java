package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TelaInicial extends JFrame {

    // --- VARIÁVEIS GLOBAIS ---
    private JPanel mainContainer;
    private CardLayout cardLayout;

    // Campos Login
    private JTextField txtLoginEmail;
    private JPasswordField txtLoginSenha;

    // Campos Cadastro
    private JTextField txtCadNome, txtCadUser, txtCadEmail, txtCadTelefone;
    private JPasswordField txtCadSenha, txtCadConfirma;
    private ButtonGroup grupoCargos;
    private JRadioButton rbCozinheiro, rbEntregador, rbAtendente;

    // Dashboard e Menu
    private JPanel dashboardPanel;
    private JPanel sidebarContainer;
    private JPanel panelBody;
    private JLabel lblNomeUsuario;
    private JLabel lblCargoUsuario;
    
    // Lista para gerenciar botões do menu
    private final List<JButton> botoesMenu = new ArrayList<>();

    public TelaInicial() {
        setTitle("FoodVerse - Manager System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 720)); 
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        // Aplica o tema escuro
        UIConstants.applyDarkDefaults();
        // Registra ícones
        try {
            IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
        } catch (Exception e) { e.printStackTrace(); }

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        initLoginScreen();
        initCadastroScreen();
        initDashboardScreen();

        add(mainContainer);
    }

    // =================================================================================
    // 1. TELA DE LOGIN (CORRIGIDA)
    // =================================================================================
    private void initLoginScreen() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(UIConstants.BG_DARK);

        // Card Central Arredondado
        UIConstants.RoundedPanel card = new UIConstants.RoundedPanel(30, UIConstants.BG_DARK_ALT);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 0, 10, 0);
        gc.weightx = 1.0;

        // Logo
        JLabel lblLogo = new JLabel("FOODVERSE");
        lblLogo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.RESTAURANT_MENU, 50, UIConstants.PRIMARY_RED));
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblLogo.setForeground(UIConstants.FG_LIGHT);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setVerticalTextPosition(SwingConstants.BOTTOM);
        lblLogo.setHorizontalTextPosition(SwingConstants.CENTER);
        card.add(lblLogo, gc);

        gc.gridy++;
        JLabel lblSub = new JLabel("Sistema de Gestão");
        lblSub.setFont(UIConstants.FONT_REGULAR);
        lblSub.setForeground(UIConstants.FG_MUTED);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblSub, gc);

        // Inputs
        gc.gridy++; gc.insets = new Insets(30, 0, 5, 0);
        card.add(criarLabelSimples("E-mail ou Usuário"), gc);
        
        gc.gridy++; gc.insets = new Insets(5, 0, 15, 0);
        txtLoginEmail = criarInputTexto();
        card.add(txtLoginEmail, gc);

        gc.gridy++; gc.insets = new Insets(0, 0, 5, 0);
        card.add(criarLabelSimples("Senha"), gc);
        
        gc.gridy++; gc.insets = new Insets(5, 0, 30, 0);
        txtLoginSenha = criarInputSenha();
        txtLoginSenha.addActionListener(e -> logar());
        card.add(txtLoginSenha, gc);

        // Botão Entrar
        gc.gridy++; gc.insets = new Insets(0, 0, 20, 0);
        JButton btnEntrar = new JButton("ENTRAR");
        UIConstants.stylePrimary(btnEntrar);
        btnEntrar.setPreferredSize(new Dimension(300, 45));
        btnEntrar.addActionListener(e -> logar());
        card.add(btnEntrar, gc);

        // --- CORREÇÃO DO LINK DE CADASTRO ---
        // Usar JLabel em vez de JButton evita o problema de borda/fundo "estranho"
        gc.gridy++;
        JLabel lblLink = new JLabel("Não tem conta? Solicite Acesso");
        lblLink.setFont(UIConstants.ARIAL_12);
        lblLink.setForeground(UIConstants.FG_MUTED);
        lblLink.setHorizontalAlignment(SwingConstants.CENTER);
        lblLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        lblLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                limparFormCadastro();
                cardLayout.show(mainContainer, "CADASTRO");
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                lblLink.setForeground(UIConstants.PRIMARY_RED); // Destaque ao passar mouse
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblLink.setForeground(UIConstants.FG_MUTED);
            }
        });
        card.add(lblLink, gc);

        loginPanel.add(card);
        mainContainer.add(loginPanel, "LOGIN");
    }

    // =================================================================================
    // 2. TELA DE CADASTRO
    // =================================================================================
    private void initCadastroScreen() {
        JPanel cadastroPanel = new JPanel(new GridBagLayout());
        cadastroPanel.setBackground(UIConstants.BG_DARK);

        UIConstants.RoundedPanel card = new UIConstants.RoundedPanel(30, UIConstants.BG_DARK_ALT);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 10, 5, 10);
        gc.weightx = 1.0;

        // Título
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("Novo Colaborador");
        lblTitulo.setFont(UIConstants.FONT_TITLE);
        lblTitulo.setForeground(UIConstants.FG_LIGHT);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblTitulo, gc);

        // Linha 1
        gc.gridwidth = 1; gc.gridy++;
        gc.gridx = 0; card.add(criarLabelSimples("Nome Completo *"), gc);
        gc.gridx = 1; card.add(criarLabelSimples("Usuário *"), gc);
        
        gc.gridy++;
        gc.gridx = 0; txtCadNome = criarInputTexto(); card.add(txtCadNome, gc);
        gc.gridx = 1; txtCadUser = criarInputTexto(); card.add(txtCadUser, gc);

        // Linha 2
        gc.gridy++;
        gc.gridx = 0; card.add(criarLabelSimples("E-mail *"), gc);
        gc.gridx = 1; card.add(criarLabelSimples("Telefone"), gc);

        gc.gridy++;
        gc.gridx = 0; txtCadEmail = criarInputTexto(); card.add(txtCadEmail, gc);
        gc.gridx = 1; txtCadTelefone = criarInputTexto(); card.add(txtCadTelefone, gc);

        // Linha 3: Cargo
        gc.gridy++; gc.gridx = 0; gc.gridwidth = 2;
        JPanel pCargo = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pCargo.setOpaque(false);
        pCargo.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        grupoCargos = new ButtonGroup();
        rbAtendente = criarRadio("Atendente");
        rbCozinheiro = criarRadio("Cozinheiro");
        rbEntregador = criarRadio("Entregador");
        rbAtendente.setSelected(true);

        grupoCargos.add(rbAtendente); grupoCargos.add(rbCozinheiro); grupoCargos.add(rbEntregador);
        pCargo.add(rbAtendente); pCargo.add(rbCozinheiro); pCargo.add(rbEntregador);
        card.add(pCargo, gc);

        // Linha 4
        gc.gridy++; gc.gridwidth = 1;
        gc.gridx = 0; card.add(criarLabelSimples("Senha *"), gc);
        gc.gridx = 1; card.add(criarLabelSimples("Confirmar Senha *"), gc);

        gc.gridy++;
        gc.gridx = 0; txtCadSenha = criarInputSenha(); card.add(txtCadSenha, gc);
        gc.gridx = 1; txtCadConfirma = criarInputSenha(); card.add(txtCadConfirma, gc);

        // Botões
        gc.gridy++; gc.gridwidth = 2; gc.gridx = 0; gc.insets = new Insets(25, 10, 10, 10);
        JButton btnSalvar = new JButton("ENVIAR SOLICITAÇÃO");
        UIConstants.styleSuccess(btnSalvar);
        btnSalvar.setPreferredSize(new Dimension(0, 45));
        btnSalvar.addActionListener(e -> cadastrar());
        card.add(btnSalvar, gc);

        gc.gridy++; gc.insets = new Insets(5, 10, 5, 10);
        JButton btnVoltar = new JButton("Voltar ao Login");
        UIConstants.styleSecondary(btnVoltar);
        btnVoltar.setPreferredSize(new Dimension(0, 35));
        btnVoltar.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));
        card.add(btnVoltar, gc);

        cadastroPanel.add(card);
        mainContainer.add(cadastroPanel, "CADASTRO");
    }

    // --- Helpers Visuais ---
    private JTextField criarInputTexto() {
        JTextField field = new JTextField(20);
        UIConstants.styleField(field); 
        return field;
    }

    private JPasswordField criarInputSenha() {
        JPasswordField field = new JPasswordField(20);
        UIConstants.styleField(field); 
        return field;
    }

    private JLabel criarLabelSimples(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(UIConstants.FG_MUTED);
        l.setFont(UIConstants.ARIAL_12_B);
        return l;
    }

    private JRadioButton criarRadio(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setOpaque(false);
        rb.setForeground(UIConstants.FG_LIGHT);
        rb.setFocusPainted(false);
        rb.setFont(UIConstants.ARIAL_14);
        return rb;
    }

    // =================================================================================
    // 3. DASHBOARD
    // =================================================================================
    private void initDashboardScreen() {
        dashboardPanel = new JPanel(new BorderLayout());
        
        // --- SIDEBAR ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UIConstants.BG_DARK_ALT);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.GRID_DARK));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(30, 0, 30, 0));
        JLabel lblDashLogo = new JLabel("FoodVerse");
        lblDashLogo.setFont(UIConstants.FONT_TITLE);
        lblDashLogo.setForeground(UIConstants.FG_LIGHT);
        lblDashLogo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.RESTAURANT_MENU, 24, UIConstants.PRIMARY_RED));
        logoPanel.add(lblDashLogo);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        sidebarContainer = new JPanel();
        sidebarContainer.setLayout(new BoxLayout(sidebarContainer, BoxLayout.Y_AXIS));
        sidebarContainer.setBackground(UIConstants.BG_DARK_ALT);
        sidebarContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollMenu = new JScrollPane(sidebarContainer);
        UIConstants.styleScrollPane(scrollMenu);
        scrollMenu.setBorder(null);
        sidebar.add(scrollMenu, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(UIConstants.BG_DARK_ALT);
        footerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JButton btnSair = new JButton("Sair");
        UIConstants.styleDanger(btnSair);
        btnSair.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.EXIT_TO_APP, 18, Color.WHITE));
        btnSair.addActionListener(e -> logout());
        footerPanel.add(btnSair, BorderLayout.CENTER);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        dashboardPanel.add(sidebar, BorderLayout.WEST);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_DARK);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.GRID_DARK));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        userPanel.setOpaque(false);
        
        lblNomeUsuario = new JLabel("Usuário");
        lblNomeUsuario.setFont(UIConstants.FONT_BOLD);
        lblNomeUsuario.setForeground(UIConstants.FG_LIGHT);
        
        lblCargoUsuario = new JLabel("Cargo");
        lblCargoUsuario.setFont(UIConstants.ARIAL_12);
        lblCargoUsuario.setForeground(UIConstants.FG_MUTED);
        
        JPanel textInfo = new JPanel(new GridLayout(2, 1));
        textInfo.setOpaque(false);
        textInfo.add(lblNomeUsuario);
        textInfo.add(lblCargoUsuario);

        JLabel lblAvatar = new JLabel(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCOUNT_CIRCLE, 44, UIConstants.FG_MUTED));
        
        userPanel.add(textInfo);
        userPanel.add(lblAvatar);
        header.add(userPanel, BorderLayout.EAST);

        dashboardPanel.add(header, BorderLayout.NORTH);

        // --- CORPO (ONDE AS TELAS CARREGAM) ---
        panelBody = new JPanel(new CardLayout());
        panelBody.setBackground(UIConstants.BG_DARK);
        dashboardPanel.add(panelBody, BorderLayout.CENTER);
        
        mainContainer.add(dashboardPanel, "DashBoard");
    }

    // =================================================================================
    // 4. LÓGICA DE NAVEGAÇÃO
    // =================================================================================
    
    private void construirMenuPorCargo(String cargo) {
        sidebarContainer.removeAll();
        botoesMenu.clear();
        panelBody.removeAll();
        
        // --- INSTANCIAÇÃO DAS TELAS ---
        // Aqui usamos try-catch para cada painel. Se uma tela (ex: EstoquePainel) tiver erro no código dela,
        // o menu continua carregando e colocamos um painel de erro no lugar.
        
        // 1. Home (Dashboard)
        try {
            panelBody.add(new HomePanel(), "HOME");
            adicionarModulo("Início", GoogleMaterialDesignIcons.HOME, "HOME");
        } catch (Exception e) { e.printStackTrace(); }

        String role = cargo.toLowerCase();
        boolean isAdmin = role.contains("admin") || role.contains("gerente");
        boolean isCozinha = role.contains("cozinheiro") || role.contains("chef");
        boolean isAtend = role.contains("atendente") || role.contains("garçom");
        boolean isEntreg = role.contains("entregador");

        // --- SEÇÃO GESTÃO ---
        if (isAdmin) {
            addTituloSecao("ADMINISTRAÇÃO");
            adicionarPainelSeguro("Equipe", GoogleMaterialDesignIcons.SUPERVISOR_ACCOUNT, "USUARIOS", new AprovacaoCadastrosPanel());
            adicionarPainelSeguro("Cardápio", GoogleMaterialDesignIcons.RESTAURANT, "CARDAPIO", new CardapioPainel());
            adicionarPainelSeguro("Estoque", GoogleMaterialDesignIcons.STORE, "ESTOQUE", new EstoquePainel());
        }

        // --- SEÇÃO OPERAÇÃO ---
        if (isAdmin || isAtend) {
            addTituloSecao("SALÃO & PEDIDOS");
            adicionarPainelSeguro("Mesas", GoogleMaterialDesignIcons.EVENT_SEAT, "MESAS", new GestaoMesasPanel());
            adicionarPainelSeguro("Novo Pedido", GoogleMaterialDesignIcons.ADD_SHOPPING_CART, "PEDIDOS", new PedidosPanel());
        }

        // --- SEÇÃO PRODUÇÃO ---
        if (isAdmin || isCozinha) {
            addTituloSecao("COZINHA");
            adicionarPainelSeguro("KDS / Produção", GoogleMaterialDesignIcons.KITCHEN, "KDS", new GestaoCozinhaPanel());
            if(isCozinha) { 
                // Cozinheiro vê estoque mas talvez use outra tela ou a mesma em modo leitura
                adicionarPainelSeguro("Ver Estoque", GoogleMaterialDesignIcons.STORE, "ESTOQUE", new EstoquePainel());
            }
        }

        // --- SEÇÃO DELIVERY ---
        if (isAdmin || isEntreg || isAtend) {
            addTituloSecao("DELIVERY");
            adicionarPainelSeguro("Entregas", GoogleMaterialDesignIcons.MOTORCYCLE, "ENTREGAS", new EntregasPainel());
        }

        sidebarContainer.revalidate();
        sidebarContainer.repaint();
        
        // Força ir para Home ao logar
        CardLayout cl = (CardLayout) panelBody.getLayout();
        cl.show(panelBody, "HOME");
        resetarBotoesMenu("HOME");
    }
    
    // Método auxiliar para adicionar painel com segurança (evita crash se a classe do painel tiver erro)
    private void adicionarPainelSeguro(String nome, GoogleMaterialDesignIcons icone, String cardName, JPanel painelInstancia) {
        try {
            if (painelInstancia != null) {
                panelBody.add(painelInstancia, cardName);
                adicionarModulo(nome, icone, cardName);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar módulo: " + nome);
            e.printStackTrace();
            // Opcional: Adicionar um painel de erro visual
            JPanel erroPanel = new JPanel(new BorderLayout());
            erroPanel.setBackground(UIConstants.BG_DARK);
            erroPanel.add(new JLabel("Erro ao carregar módulo " + nome, SwingConstants.CENTER));
            panelBody.add(erroPanel, cardName);
            adicionarModulo(nome, icone, cardName);
        }
    }

    private void addTituloSecao(String titulo) {
        JLabel l = new JLabel(titulo);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(UIConstants.FG_MUTED);
        l.setBorder(new EmptyBorder(20, 10, 5, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarContainer.add(l);
    }

    private void adicionarModulo(String nome, GoogleMaterialDesignIcons icone, String cardName) {
        JButton btn = new JButton(nome);
        btn.setIcon(IconFontSwing.buildIcon(icone, 20, UIConstants.FG_MUTED));
        btn.setFont(UIConstants.FONT_REGULAR);
        btn.setForeground(UIConstants.FG_LIGHT);
        btn.setBackground(UIConstants.BG_DARK_ALT);
        btn.setBorder(new EmptyBorder(12, 15, 12, 15));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(280, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setActionCommand(cardName); // Usamos action command para identificar qual é

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if(btn.getBackground() != UIConstants.BG_DARK)
                    btn.setBackground(new Color(50, 50, 50));
            }
            public void mouseExited(MouseEvent e) {
                if(btn.getBackground() != UIConstants.BG_DARK)
                    btn.setBackground(UIConstants.BG_DARK_ALT);
            }
        });

        btn.addActionListener(e -> {
            resetarBotoesMenu(cardName);
            CardLayout cl = (CardLayout) panelBody.getLayout();
            cl.show(panelBody, cardName);
        });

        botoesMenu.add(btn);
        sidebarContainer.add(btn);
        sidebarContainer.add(Box.createVerticalStrut(2));
    }
    
    private void resetarBotoesMenu(String cardNameAtivo) {
        for(JButton b : botoesMenu) {
            if (b.getActionCommand().equals(cardNameAtivo)) {
                b.setBackground(UIConstants.BG_DARK);
                b.setForeground(UIConstants.PRIMARY_RED);
                // Atualizar ícone para vermelho seria ideal, mas exige recriar o ícone
            } else {
                b.setBackground(UIConstants.BG_DARK_ALT);
                b.setForeground(UIConstants.FG_LIGHT);
            }
        }
    }

    // =================================================================================
    // 5. LÓGICA DE LOGIN/LOGOUT
    // =================================================================================

    private void logar() {
        String email = txtLoginEmail.getText().trim();
        String senha = new String(txtLoginSenha.getPassword());

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.show(this, "Preencha e-mail e senha!", Toast.Type.WARNING);
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Void, Void>() {
            String dbNome = null;
            String dbCargo = null;
            String erroMsg = null;
            
            @Override
            protected Void doInBackground() {
                ConexaoBanco cb = new ConexaoBanco();
                try (Connection conn = cb.abrirConexao()) {
                    if (conn == null) {
                        // BACKDOOR PARA TESTES (Remover em produção)
                        if(email.equals("admin") && senha.equals("admin")) {
                            dbNome = "Administrador";
                            dbCargo = "Gerente";
                            return null;
                        }
                        erroMsg = "Erro de conexão com o banco.";
                        return null;
                    }

                    String sql = "SELECT name, role, status FROM tb_funcionarios WHERE (email = ? OR userName = ?) AND password = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, email);
                    ps.setString(2, email);
                    ps.setString(3, senha);
                    
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        String status = rs.getString("status");
                        if ("bloqueado".equalsIgnoreCase(status)) {
                            erroMsg = "Usuário bloqueado.";
                        } else if ("pendente".equalsIgnoreCase(status)) {
                            erroMsg = "Seu cadastro ainda está em análise.";
                        } else {
                            dbNome = rs.getString("name");
                            dbCargo = rs.getString("role");
                        }
                    } else {
                        erroMsg = "Credenciais inválidas.";
                    }
                } catch (Exception ex) {
                    erroMsg = "Erro: " + ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                if(erroMsg != null) {
                    Toast.show(TelaInicial.this, erroMsg, Toast.Type.ERROR);
                } else if (dbNome != null) {
                    loginSucesso(dbNome, dbCargo);
                }
            }
        }.execute();
    }

    private void loginSucesso(String nome, String cargo) {
        lblNomeUsuario.setText(nome);
        lblCargoUsuario.setText(cargo);
        
        construirMenuPorCargo(cargo);
        
        txtLoginEmail.setText("");
        txtLoginSenha.setText("");
        cardLayout.show(mainContainer, "DashBoard");
        Toast.show(this, "Bem-vindo(a), " + nome + "!", Toast.Type.SUCCESS);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente sair?", "Sair", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            panelBody.removeAll();
            cardLayout.show(mainContainer, "LOGIN");
        }
    }

    private void cadastrar() {
        String nome = txtCadNome.getText().trim();
        String user = txtCadUser.getText().trim();
        String email = txtCadEmail.getText().trim();
        String senha = new String(txtCadSenha.getPassword());
        String conf = new String(txtCadConfirma.getPassword());
        
        if(nome.isEmpty() || user.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.show(this, "Preencha todos os campos obrigatórios (*)", Toast.Type.WARNING);
            return;
        }
        if(!senha.equals(conf)) {
            Toast.show(this, "As senhas não coincidem.", Toast.Type.ERROR);
            return;
        }

        String cargo = "Atendente";
        if(rbCozinheiro.isSelected()) cargo = "Cozinheiro";
        if(rbEntregador.isSelected()) cargo = "Entregador";
        final String finalCargo = cargo;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, Void>() {
            String erro = null;
            @Override
            protected Void doInBackground() {
                ConexaoBanco cb = new ConexaoBanco();
                try (Connection conn = cb.abrirConexao()) {
                    if(conn == null) throw new Exception("Sem conexão");
                    
                    String sql = "INSERT INTO tb_funcionarios (name, userName, email, role, phone, password, registrationDate, status) VALUES (?, ?, ?, ?, ?, ?, GETDATE(), 'pendente')";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, nome);
                    ps.setString(2, user);
                    ps.setString(3, email);
                    ps.setString(4, finalCargo);
                    ps.setString(5, txtCadTelefone.getText());
                    ps.setString(6, senha);
                    ps.executeUpdate();
                    
                } catch (Exception ex) {
                    erro = "Erro: " + ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                if(erro != null) {
                    Toast.show(TelaInicial.this, erro, Toast.Type.ERROR);
                } else {
                    Toast.show(TelaInicial.this, "Solicitação enviada!", Toast.Type.SUCCESS);
                    limparFormCadastro();
                    cardLayout.show(mainContainer, "LOGIN");
                }
            }
        }.execute();
    }

    private void limparFormCadastro() {
        txtCadNome.setText("");
        txtCadUser.setText("");
        txtCadEmail.setText("");
        txtCadTelefone.setText("");
        txtCadSenha.setText("");
        txtCadConfirma.setText("");
        rbAtendente.setSelected(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaInicial().setVisible(true));
    }
}