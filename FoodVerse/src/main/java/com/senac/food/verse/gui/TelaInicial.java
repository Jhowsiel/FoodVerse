package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import com.senac.food.verse.Funcionario;
import com.senac.food.verse.SessionContext;
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
import java.util.regex.Pattern;

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
    
    // Cargos
    private ButtonGroup grupoCargos;
    private JToggleButton tbAtendente, tbCozinheiro, tbEntregador;

    // Dashboard e Menu
    private JPanel dashboardPanel;
    private JPanel sidebarContainer;
    private JPanel panelBody;
    private JLabel lblNomeUsuario;
    private JLabel lblCargoUsuario;
    private JLabel lblTituloContexto;
    private JLabel lblDescricaoContexto;
    private JButton btnLimparContexto;
    
    private final List<JButton> botoesMenu = new ArrayList<>();

    public TelaInicial() {
        setTitle("FoodVerse - Manager System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 720)); 
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        UIConstants.applyDarkDefaults();
        
        try {
            IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
            
            // Define o ícone da janela e da barra de tarefas do Windows
            setIconImage(UIConstants.getAppIcon());
            
        } catch (Exception e) { e.printStackTrace(); }
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        initLoginScreen();
        initCadastroScreen();
        initDashboardScreen();

        add(mainContainer);
    }

    // =================================================================================
    // 1. TELA DE LOGIN
    // =================================================================================
    private void initLoginScreen() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(UIConstants.BG_DARK);

        UIConstants.RoundedPanel card = new UIConstants.RoundedPanel(30, UIConstants.BG_DARK_ALT);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 0, 10, 0);
        gc.weightx = 1.0;

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

        gc.gridy++; gc.insets = new Insets(0, 0, 20, 0);
        JButton btnEntrar = new JButton("ENTRAR");
        UIConstants.stylePrimary(btnEntrar);
        btnEntrar.setPreferredSize(new Dimension(300, 45));
        btnEntrar.addActionListener(e -> logar());
        card.add(btnEntrar, gc);

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
                lblLink.setForeground(UIConstants.PRIMARY_RED);
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
    // 2. TELA DE CADASTRO (Com Validações e Chips)
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

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("Novo Colaborador");
        lblTitulo.setFont(UIConstants.FONT_TITLE);
        lblTitulo.setForeground(UIConstants.FG_LIGHT);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblTitulo, gc);

        gc.gridwidth = 1; gc.gridy++;
        gc.gridx = 0; card.add(criarLabelSimples("Nome Completo *"), gc);
        gc.gridx = 1; card.add(criarLabelSimples("Usuário *"), gc);
        
        gc.gridy++;
        gc.gridx = 0; txtCadNome = criarInputTexto(); card.add(txtCadNome, gc);
        gc.gridx = 1; txtCadUser = criarInputTexto(); card.add(txtCadUser, gc);

        gc.gridy++;
        gc.gridx = 0; card.add(criarLabelSimples("E-mail *"), gc);
        gc.gridx = 1; card.add(criarLabelSimples("Telefone (Apenas Números)"), gc);

        gc.gridy++;
        gc.gridx = 0; txtCadEmail = criarInputTexto(); card.add(txtCadEmail, gc);
        gc.gridx = 1; txtCadTelefone = criarInputTexto(); card.add(txtCadTelefone, gc);

        // Painel de Cargos em formato Chip
        gc.gridy++; gc.gridx = 0; gc.gridwidth = 2;
        JPanel pCargo = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        pCargo.setOpaque(false);
        pCargo.setBorder(new EmptyBorder(15, 0, 15, 0));
        
        grupoCargos = new ButtonGroup();
        tbAtendente = criarChip("Atendente");
        tbCozinheiro = criarChip("Cozinheiro");
        tbEntregador = criarChip("Entregador");
        tbAtendente.setSelected(true);

        grupoCargos.add(tbAtendente); grupoCargos.add(tbCozinheiro); grupoCargos.add(tbEntregador);
        pCargo.add(tbAtendente); pCargo.add(tbCozinheiro); pCargo.add(tbEntregador);
        card.add(pCargo, gc);

        gc.gridy++; gc.gridwidth = 1;
        gc.gridx = 0; card.add(criarLabelSimples("Senha (mín 6 carac.) *"), gc);
        gc.gridx = 1; card.add(criarLabelSimples("Confirmar Senha *"), gc);

        gc.gridy++;
        gc.gridx = 0; txtCadSenha = criarInputSenha(); card.add(txtCadSenha, gc);
        gc.gridx = 1; txtCadConfirma = criarInputSenha(); card.add(txtCadConfirma, gc);

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

    private JToggleButton criarChip(String text) {
        JToggleButton tb = new JToggleButton(text);
        UIConstants.styleChipButton(tb);
        return tb;
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

        JPanel contextPanel = new JPanel(new GridLayout(2, 1));
        contextPanel.setOpaque(false);
        contextPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        lblTituloContexto = new JLabel("Painel FoodVerse");
        lblTituloContexto.setFont(UIConstants.FONT_BOLD);
        lblTituloContexto.setForeground(UIConstants.FG_LIGHT);
        contextPanel.add(lblTituloContexto);

        lblDescricaoContexto = new JLabel("Faça login para continuar");
        lblDescricaoContexto.setFont(UIConstants.ARIAL_12);
        lblDescricaoContexto.setForeground(UIConstants.FG_MUTED);
        contextPanel.add(lblDescricaoContexto);
        header.add(contextPanel, BorderLayout.WEST);

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

        btnLimparContexto = new JButton("Modo Global");
        UIConstants.styleSecondary(btnLimparContexto);
        btnLimparContexto.setVisible(false);
        btnLimparContexto.addActionListener(e -> limparContextoRestaurante());

        JLabel lblAvatar = new JLabel(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCOUNT_CIRCLE, 44, UIConstants.FG_MUTED));
        
        userPanel.add(btnLimparContexto);
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

        SessionContext ctx = SessionContext.getInstance();
        
        try {
            panelBody.add(new HomePanel(this::abrirModulo), "HOME");
            adicionarModulo("Início", GoogleMaterialDesignIcons.HOME, "HOME");
        } catch (Exception e) { e.printStackTrace(); }

        String role = cargo.toLowerCase();
        boolean isAdmin   = role.contains("admin");
        boolean isGerente = role.contains("gerente");
        boolean isCozinha = role.contains("cozinheiro") || role.contains("chef");
        boolean isAtend   = role.contains("atendente") || role.contains("garçom");
        boolean isEntreg  = role.contains("entregador");
        boolean adminComContexto = !isAdmin || ctx.adminTemContextoRestaurante();

        if (isAdmin) {
            // Admin Global: gerencia restaurantes + pode entrar no contexto de um deles
            addTituloSecao("ADMIN GLOBAL");
            adicionarPainelSeguro("Restaurantes", GoogleMaterialDesignIcons.STORE_MALL_DIRECTORY, "RESTAURANTES", new AdminRestaurantesPanel(this));
            adicionarPainelSeguro("Equipe", GoogleMaterialDesignIcons.SUPERVISOR_ACCOUNT, "USUARIOS", new AprovacaoCadastrosPanel());
        }

        if (adminComContexto && (isAdmin || isGerente)) {
            addTituloSecao("MEU RESTAURANTE");
            adicionarPainelSeguro("Perfil do Restaurante", GoogleMaterialDesignIcons.BUSINESS, "MEU_RESTAURANTE", new MeuRestaurantePanel());
            adicionarPainelSeguro("Cardápio", GoogleMaterialDesignIcons.RESTAURANT, "CARDAPIO", new CardapioPainel());
            adicionarPainelSeguro("Estoque", GoogleMaterialDesignIcons.STORE, "ESTOQUE", new EstoquePainel());
            if (!isAdmin) {
                adicionarPainelSeguro("Equipe", GoogleMaterialDesignIcons.SUPERVISOR_ACCOUNT, "USUARIOS", new AprovacaoCadastrosPanel());
            }
        }

        if ((adminComContexto && isAdmin) || isGerente || isAtend) {
            addTituloSecao("SALÃO & PEDIDOS");
            adicionarPainelSeguro("Mesas", GoogleMaterialDesignIcons.EVENT_SEAT, "MESAS", new GestaoMesasPanel());
            adicionarPainelSeguro("Novo Pedido", GoogleMaterialDesignIcons.ADD_SHOPPING_CART, "PEDIDOS", new PedidosPanel());
        }

        if ((adminComContexto && isAdmin) || isGerente || isCozinha) {
            addTituloSecao("COZINHA");
            adicionarPainelSeguro("KDS / Produção", GoogleMaterialDesignIcons.KITCHEN, "KDS", new GestaoCozinhaPanel());
            if (isCozinha) {
                adicionarPainelSeguro("Ver Estoque", GoogleMaterialDesignIcons.STORE, "ESTOQUE_COZ", new EstoquePainel());
            }
        }

        if ((adminComContexto && isAdmin) || isGerente || isEntreg || isAtend) {
            addTituloSecao("DELIVERY");
            adicionarPainelSeguro("Entregas", GoogleMaterialDesignIcons.MOTORCYCLE, "ENTREGAS", new EntregasPainel());
        }

        sidebarContainer.revalidate();
        sidebarContainer.repaint();
        
        CardLayout cl = (CardLayout) panelBody.getLayout();
        cl.show(panelBody, "HOME");
        resetarBotoesMenu("HOME");
    }

    private void abrirModulo(String cardName) {
        resetarBotoesMenu(cardName);
        CardLayout cl = (CardLayout) panelBody.getLayout();
        cl.show(panelBody, cardName);
    }
    
    private void adicionarPainelSeguro(String nome, GoogleMaterialDesignIcons icone, String cardName, JPanel painelInstancia) {
        try {
            if (painelInstancia != null) {
                panelBody.add(painelInstancia, cardName);
                adicionarModulo(nome, icone, cardName);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar módulo: " + nome);
            e.printStackTrace();
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
        btn.setActionCommand(cardName); 

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
            UIConstants.showWarning(this, "Preencha e-mail e senha!");
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Void, Void>() {
            String erroMsg = null;
            
            @Override
            protected Void doInBackground() {
                erroMsg = Funcionario.loginComContexto(email, senha);
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                if (erroMsg != null) {
                    Toast.show(TelaInicial.this, erroMsg, Toast.Type.ERROR);
                } else {
                    SessionContext ctx = SessionContext.getInstance();
                    loginSucesso(ctx.getNome(), ctx.getCargo());
                }
            }
        }.execute();
    }

    private void loginSucesso(String nome, String cargo) {
        lblNomeUsuario.setText(nome);
        lblCargoUsuario.setText(cargo);
        atualizarCabecalhoContexto();
        
        construirMenuPorCargo(cargo);
        
        txtLoginEmail.setText("");
        txtLoginSenha.setText("");
        cardLayout.show(mainContainer, "DashBoard");
        Toast.show(this, "Bem-vindo(a), " + nome + "!", Toast.Type.SUCCESS);
    }

    private void logout() {
        SessionContext.getInstance().limpar();
        panelBody.removeAll();
        lblNomeUsuario.setText("Usuário");
        lblCargoUsuario.setText("Cargo");
        atualizarCabecalhoContexto();
        cardLayout.show(mainContainer, "LOGIN");
    }

    public void atualizarContextoSessao() {
        SessionContext ctx = SessionContext.getInstance();
        lblCargoUsuario.setText(ctx.getCargo() != null ? ctx.getCargo() : "Cargo");
        lblNomeUsuario.setText(ctx.getNome() != null ? ctx.getNome() : "Usuário");
        atualizarCabecalhoContexto();
        if (ctx.getCargo() != null) {
            construirMenuPorCargo(ctx.getCargo());
        }
    }

    private void limparContextoRestaurante() {
        SessionContext ctx = SessionContext.getInstance();
        if (!ctx.adminTemContextoRestaurante()) {
            return;
        }
        ctx.setRestauranteSelecionadoId(0);
        atualizarContextoSessao();
        Toast.show(this, "Você voltou ao modo global.", Toast.Type.INFO);
    }

    private void atualizarCabecalhoContexto() {
        SessionContext ctx = SessionContext.getInstance();
        lblTituloContexto.setText(buildRestaurantContextText(ctx));
        lblDescricaoContexto.setText(buildOperationalModeText(ctx));
        btnLimparContexto.setVisible(ctx.adminTemContextoRestaurante());
    }

    static String buildRestaurantContextText(SessionContext ctx) {
        if (ctx.getCargo() == null) {
            return "Painel FoodVerse";
        }
        int restauranteId = ctx.getRestauranteEfetivo();
        if (restauranteId > 0) {
            return "Restaurante em contexto: #" + restauranteId;
        }
        if (ctx.isAdmin()) {
            return "Admin global sem restaurante selecionado";
        }
        return "Restaurante vinculado: #" + ctx.getRestauranteId();
    }

    static String buildOperationalModeText(SessionContext ctx) {
        if (ctx.getCargo() == null) {
            return "Faça login para continuar";
        }
        if (ctx.isAdmin() && !ctx.adminTemContextoRestaurante()) {
            return "Modo global ativo. Selecione um restaurante para operar.";
        }
        return "Operando como " + ctx.getCargo() + " no restaurante #" + ctx.getRestauranteEfetivo() + ".";
    }

    private void cadastrar() {
        String nome = txtCadNome.getText().trim();
        String user = txtCadUser.getText().trim();
        String email = txtCadEmail.getText().trim();
        String tel = txtCadTelefone.getText().trim();
        String senha = new String(txtCadSenha.getPassword());
        String conf = new String(txtCadConfirma.getPassword());
        
        // --- VALIDAÇÕES COM TOAST ---
        if(nome.isEmpty() || user.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            UIConstants.showWarning(this, "Preencha todos os campos obrigatórios (*)");
            return;
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailRegex, email)) {
            UIConstants.showWarning(this, "Por favor, insira um e-mail válido.");
            return;
        }

        if (!tel.isEmpty()) {
            if (!tel.matches("\\d+")) {
                UIConstants.showWarning(this, "O telefone deve conter apenas números.");
                return;
            }
            if (tel.length() < 10) {
                UIConstants.showWarning(this, "Insira um telefone válido com DDD.");
                return;
            }
        }

        if(senha.length() < 6) {
            UIConstants.showWarning(this, "A senha deve ter no mínimo 6 caracteres.");
            return;
        }

        if(!senha.equals(conf)) {
            UIConstants.showError(this, "As senhas não coincidem.");
            return;
        }

        String cargo = "Atendente";
        if(tbCozinheiro.isSelected()) cargo = "Cozinheiro";
        if(tbEntregador.isSelected()) cargo = "Entregador";
        final String finalCargo = cargo;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, Void>() {
            String erro = null;
            @Override
            protected Void doInBackground() {
                ConexaoBanco cb = new ConexaoBanco();
                try (Connection conn = cb.abrirConexao()) {
                    if(conn == null) throw new Exception("Sem conexão");
                    
                    String checkSql = "SELECT ID_funcionario FROM tb_funcionarios WHERE email = ? OR username = ?";
                    PreparedStatement checkPs = conn.prepareStatement(checkSql);
                    checkPs.setString(1, email);
                    checkPs.setString(2, user);
                    if(checkPs.executeQuery().next()) {
                        erro = "Usuário ou E-mail já cadastrado.";
                        return null;
                    }
                    
                    String sql = "INSERT INTO tb_funcionarios (nome, username, email, cargo, telefone, senha, data_cadastro, status) VALUES (?, ?, ?, ?, ?, ?, GETDATE(), 'pendente')";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, nome);
                    ps.setString(2, user);
                    ps.setString(3, email);
                    ps.setString(4, finalCargo);
                    ps.setString(5, tel);
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
                    UIConstants.showError(TelaInicial.this, erro);
                } else {
                    UIConstants.showSuccess(TelaInicial.this, "Solicitação validada e enviada!");
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
        tbAtendente.setSelected(true);
    }

    public static void main(String[] args) {
        // 1. REGISTAR A FONTE DOS ÍCONES ANTES DE TUDO
        IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());

        // 2. APLICAR O TEMA DARK
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception ex) {
            System.err.println("Não foi possível inicializar o FlatLaf Dark.");
        }
        
        // 3. ARRANCAR O SISTEMA
        SwingUtilities.invokeLater(() -> new TelaInicial().setVisible(true));
    }
}
