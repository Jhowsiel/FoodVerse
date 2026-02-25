package com.senac.food.verse.gui;

import com.senac.food.verse.EstoqueDAO;
import com.senac.food.verse.Formatador;
import com.senac.food.verse.Funcionario;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
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
    private JRadioButton rbCozinheiro, rbEntregador, rbAtendente; // Adicionei Atendente

    // Dashboard e Menu
    private JPanel dashboardPanel;
    private JPanel sidebarContainer; // Container para itens do menu
    private JPanel panelBody;
    private JLabel lblNomeUsuario;
    private JLabel lblTituloPagina;
    private JLabel lblCargoUsuario;
    
    // Lista para gerenciar estado dos botões do menu
    private final List<ModernButton> botoesMenu = new ArrayList<>();
    
    // DAO
    private final EstoqueDAO estoqueDAO = new EstoqueDAO();

    public TelaInicial() {
        setTitle("FoodVerse - Manager System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 800));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        setupAmbiente();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        initLoginScreen();
        initCadastroScreen();
        initDashboardScreen(); // Agora cria o dashboard vazio, o menu será preenchido no login

        add(mainContainer);
    }

    private void setupAmbiente() {
        IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("Button.arc", 12);
            // Forçar cores globais para evitar texto sumindo
            UIManager.put("TextField.foreground", Color.WHITE);
            UIManager.put("TextField.caretForeground", Color.WHITE);
            UIManager.put("PasswordField.foreground", Color.WHITE);
            UIManager.put("PasswordField.caretForeground", Color.WHITE);
        } catch (Exception ex) {
            System.err.println("Erro ao carregar FlatLaf: " + ex.getMessage());
        }
    }

    // =================================================================================
    // 1. TELA DE LOGIN
    // =================================================================================
    private void initLoginScreen() {
        JPanel loginPanel = new GradientPanel(new Color(10, 10, 12), new Color(35, 10, 15));
        loginPanel.setLayout(new GridBagLayout());

        ShadowPanel card = new ShadowPanel(30, new Color(30, 30, 30, 250));
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 0, 10, 0);

        JLabel lblLogo = new JLabel("FOODVERSE");
        lblLogo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.RESTAURANT_MENU, 60, UIConstants.PRIMARY_RED));
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setVerticalTextPosition(SwingConstants.BOTTOM);
        lblLogo.setHorizontalTextPosition(SwingConstants.CENTER);
        card.add(lblLogo, gc);

        gc.gridy++;
        JLabel lblSub = new JLabel("Sistema de Gestão Integrada");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(UIConstants.FG_MUTED);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblSub, gc);

        // Inputs
        gc.gridy++; gc.insets = new Insets(30, 0, 10, 0);
        txtLoginEmail = criarInputModerno("E-mail ou Usuário", GoogleMaterialDesignIcons.PERSON, false);
        card.add(txtLoginEmail, gc);

        gc.gridy++; gc.insets = new Insets(10, 0, 30, 0);
        txtLoginSenha = (JPasswordField) criarInputModerno("Senha", GoogleMaterialDesignIcons.LOCK, true);
        txtLoginSenha.addActionListener(e -> logar());
        card.add(txtLoginSenha, gc);

        // Botão Entrar
        gc.gridy++; gc.insets = new Insets(0, 0, 15, 0);
        ModernButton btnEntrar = new ModernButton("ACESSAR SISTEMA", UIConstants.PRIMARY_RED, Color.WHITE, true);
        btnEntrar.addActionListener(e -> logar());
        card.add(btnEntrar, gc);

        // Link Cadastro
        gc.gridy++;
        ModernButton btnLink = new ModernButton("Solicitar novo acesso", null, UIConstants.FG_MUTED, false);
        btnLink.setHoverColor(Color.WHITE);
        btnLink.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLink.addActionListener(e -> {
            limparFormCadastro();
            cardLayout.show(mainContainer, "CADASTRO");
        });
        card.add(btnLink, gc);

        loginPanel.add(card);
        mainContainer.add(loginPanel, "LOGIN");
    }

    // =================================================================================
    // 2. TELA DE CADASTRO
    // =================================================================================
    private void initCadastroScreen() {
        JPanel cadastroPanel = new GradientPanel(new Color(15, 15, 20), new Color(20, 20, 25));
        cadastroPanel.setLayout(new GridBagLayout());

        ShadowPanel card = new ShadowPanel(30, UIConstants.CARD_DARK);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 10, 5, 10);

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("Novo Colaborador");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(UIConstants.FG_LIGHT);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 20, 0));
        card.add(lblTitulo, gc);

        // Linha 1
        gc.gridwidth = 1; gc.gridy++;
        gc.gridx = 0; card.add(criarInputModerno("Nome Completo", GoogleMaterialDesignIcons.CARD_MEMBERSHIP, false), gc); 
        txtCadNome = (JTextField) card.getComponent(card.getComponentCount()-1);
        
        gc.gridx = 1; card.add(criarInputModerno("Nome de Usuário", GoogleMaterialDesignIcons.ACCOUNT_CIRCLE, false), gc);
        txtCadUser = (JTextField) card.getComponent(card.getComponentCount()-1);
        
        // Linha 2
        gc.gridy++;
        gc.gridx = 0; card.add(criarInputModerno("E-mail Corporativo", GoogleMaterialDesignIcons.EMAIL, false), gc);
        txtCadEmail = (JTextField) card.getComponent(card.getComponentCount()-1);

        gc.gridx = 1; card.add(criarInputModerno("Telefone / Celular", GoogleMaterialDesignIcons.PHONE, false), gc);
        txtCadTelefone = (JTextField) card.getComponent(card.getComponentCount()-1);
        configurarFormatacaoTelefone(txtCadTelefone);

        // Linha 3 (Radio Buttons)
        gc.gridy++; gc.gridx = 0; gc.gridwidth = 2;
        JPanel pCargo = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pCargo.setOpaque(false);
        pCargo.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIConstants.BG_DARK_ALT), "Função", 0, 0, new Font("Segoe UI", Font.PLAIN, 12), UIConstants.FG_MUTED));
        
        grupoCargos = new ButtonGroup();
        rbAtendente = criarRadio("Atendente");
        rbCozinheiro = criarRadio("Cozinheiro");
        rbEntregador = criarRadio("Entregador");
        grupoCargos.add(rbAtendente); grupoCargos.add(rbCozinheiro); grupoCargos.add(rbEntregador);
        
        pCargo.add(rbAtendente); pCargo.add(rbCozinheiro); pCargo.add(rbEntregador);
        card.add(pCargo, gc);

        // Linha 4 (Senhas)
        gc.gridy++; gc.gridwidth = 1;
        gc.gridx = 0; card.add(criarInputModerno("Senha", GoogleMaterialDesignIcons.LOCK, true), gc);
        txtCadSenha = (JPasswordField) card.getComponent(card.getComponentCount()-1);

        gc.gridx = 1; card.add(criarInputModerno("Confirmar Senha", GoogleMaterialDesignIcons.LOCK_OPEN, true), gc);
        txtCadConfirma = (JPasswordField) card.getComponent(card.getComponentCount()-1);

        // Botões
        gc.gridy++; gc.gridwidth = 2; gc.gridx = 0;
        gc.insets = new Insets(25, 10, 10, 10);
        ModernButton btnSalvar = new ModernButton("ENVIAR CADASTRO", UIConstants.SUCCESS_GREEN, Color.WHITE, true);
        btnSalvar.addActionListener(e -> cadastrar());
        card.add(btnSalvar, gc);

        gc.gridy++; gc.insets = new Insets(0, 10, 0, 10);
        ModernButton btnVoltar = new ModernButton("Voltar", null, UIConstants.FG_MUTED, false);
        btnVoltar.setHoverColor(Color.WHITE);
        btnVoltar.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));
        card.add(btnVoltar, gc);

        cadastroPanel.add(card);
        mainContainer.add(cadastroPanel, "CADASTRO");
    }

    // =================================================================================
    // 3. DASHBOARD E NAVEGAÇÃO
    // =================================================================================
    private void initDashboardScreen() {
        dashboardPanel = new JPanel(new BorderLayout());
        
        // --- SIDEBAR ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(UIConstants.BG_DARK_ALT);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(50, 50, 50)));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(30, 0, 30, 0));
        JLabel lblDashLogo = new JLabel("FoodVerse");
        lblDashLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDashLogo.setForeground(UIConstants.FG_LIGHT);
        lblDashLogo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.RESTAURANT_MENU, 24, UIConstants.PRIMARY_RED));
        logoPanel.add(lblDashLogo);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Container dos Botões (Dinâmico)
        sidebarContainer = new JPanel();
        sidebarContainer.setLayout(new BoxLayout(sidebarContainer, BoxLayout.Y_AXIS));
        sidebarContainer.setBackground(UIConstants.BG_DARK_ALT);
        sidebar.add(sidebarContainer, BorderLayout.CENTER);

        // Botão Sair (Fixo no fundo)
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(UIConstants.BG_DARK_ALT);
        footerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        ModernButton btnSair = new ModernButton("Sair do Sistema", null, new Color(200, 80, 80), false);
        btnSair.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.EXIT_TO_APP, 20, new Color(200, 80, 80)));
        btnSair.setHorizontalAlignment(SwingConstants.CENTER);   
        btnSair.setMaximumSize(new Dimension(200, 40));
        btnSair.setHoverColor(new Color(200, 80, 80));
        btnSair.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnSair.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btnSair.setForeground(new Color(200, 80, 80)); }
        });
        btnSair.addActionListener(e -> logout());
        footerPanel.add(btnSair);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        dashboardPanel.add(sidebar, BorderLayout.WEST);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_DARK);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));

        lblTituloPagina = new JLabel("Visão Geral");
        lblTituloPagina.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTituloPagina.setForeground(UIConstants.FG_LIGHT);
        lblTituloPagina.setBorder(new EmptyBorder(0, 30, 0, 0));
        header.add(lblTituloPagina, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        userPanel.setOpaque(false);
        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);
        lblNomeUsuario = new JLabel("Usuário");
        lblNomeUsuario.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNomeUsuario.setForeground(UIConstants.FG_LIGHT);
        lblNomeUsuario.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCargoUsuario = new JLabel("Cargo");
        lblCargoUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCargoUsuario.setForeground(UIConstants.FG_MUTED);
        lblCargoUsuario.setHorizontalAlignment(SwingConstants.RIGHT);
        userInfo.add(lblNomeUsuario);
        userInfo.add(lblCargoUsuario);
        JLabel lblAvatar = new JLabel(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCOUNT_CIRCLE, 40, UIConstants.FG_MUTED));
        userPanel.add(userInfo);
        userPanel.add(lblAvatar);
        header.add(userPanel, BorderLayout.EAST);

        dashboardPanel.add(header, BorderLayout.NORTH);

        // --- CORPO ---
        panelBody = new JPanel(new CardLayout());
        panelBody.setBackground(UIConstants.BG_DARK);
        dashboardPanel.add(panelBody, BorderLayout.CENTER);
        mainContainer.add(dashboardPanel, "DashBoard");
    }

    /**
     * LÓGICA CORE: Constrói o menu baseado no nível de acesso.
     * Admin/Gerente: Tudo
     * Cozinheiro: Apenas Cozinha e Cardapio (Ler)
     * Atendente: Mesas e Pedidos
     * Entregador: Entregas
     */
    private void construirMenuPorCargo(String cargo) {
        sidebarContainer.removeAll();
        botoesMenu.clear();
        panelBody.removeAll(); // Remove paineis antigos para economizar memória

        String role = cargo.toLowerCase();
        boolean isAdmin = role.contains("admin") || role.contains("gerente");

        // --- 1. MÓDULO OPERACIONAL (ATENDIMENTO) ---
        if (isAdmin || role.contains("atendente") || role.contains("garçom")) {
            sidebarContainer.add(criarTituloSecao("ATENDIMENTO"));
            adicionarModulo("Mesas & Reservas", GoogleMaterialDesignIcons.EVENT_SEAT, "Mesas", "Mapa do Restaurante", new GestaoMesasPanel());
            adicionarModulo("Novo Pedido", GoogleMaterialDesignIcons.RECEIPT, "Pedidos", "Lançamento de Pedidos", new PedidosPanel());
            sidebarContainer.add(Box.createVerticalStrut(10));
        }

        // --- 2. MÓDULO PRODUÇÃO (COZINHA) ---
        // Cozinheiros veem a fila de produção. Admins também.
        if (isAdmin || role.contains("cozinheiro") || role.contains("chef")) {
            if(!isAdmin && sidebarContainer.getComponentCount() == 0) sidebarContainer.add(criarTituloSecao("PRODUÇÃO")); // Titulo se for só cozinheiro
            adicionarModulo("KDS / Cozinha", GoogleMaterialDesignIcons.KITCHEN, "Cozinha", "Fila de Preparo", new GestaoCozinhaPanel());
        }

        // --- 3. MÓDULO LOGÍSTICA (DELIVERY) ---
        if (isAdmin || role.contains("entregador") || role.contains("delivery")) {
             if(!isAdmin && sidebarContainer.getComponentCount() == 0) sidebarContainer.add(criarTituloSecao("LOGÍSTICA"));
            adicionarModulo("Entregas", GoogleMaterialDesignIcons.MOTORCYCLE, "Entregas", "Monitor de Delivery", new EntregasPainel());
        }
        
        sidebarContainer.add(Box.createVerticalStrut(10));

        // --- 4. MÓDULO GESTÃO (GERENTE/ADMIN) ---
        // Todos podem ver cardápio, mas admin gerencia. 
        // Vamos deixar cardapio visivel para todos consultarem preços, mas edição bloqueada na tela (futuro)
        sidebarContainer.add(criarTituloSecao("CONSULTA & GESTÃO"));
        adicionarModulo("Cardápio", GoogleMaterialDesignIcons.IMPORT_CONTACTS, "Cardapio", "Catálogo de Produtos", new CardapioPainel());

        if (isAdmin || role.contains("estoque")) {
            adicionarModulo("Estoque", GoogleMaterialDesignIcons.STORAGE, "Estoque", "Controle de Insumos", criarPainelEstoque());
        }

        if (isAdmin) {
            sidebarContainer.add(Box.createVerticalStrut(10));
            sidebarContainer.add(criarTituloSecao("ADMINISTRAÇÃO"));
            adicionarModulo("Usuários", GoogleMaterialDesignIcons.SETTINGS_APPLICATIONS, "AprovacaoCadastros", "Controle de Acessos", new AprovacaoCadastrosPanel());
        }

        sidebarContainer.add(Box.createVerticalGlue());
        sidebarContainer.revalidate();
        sidebarContainer.repaint();
    }

    private void adicionarModulo(String nome, GoogleMaterialDesignIcons icone, String cardName, String titulo, JPanel painel) {
        ModernButton btn = criarBotaoMenu(nome, icone, cardName, titulo);
        sidebarContainer.add(btn);
        sidebarContainer.add(Box.createVerticalStrut(5));
        panelBody.add(painel, cardName);
    }

    private void navegarPara(String cardName, String tituloPagina) {
        CardLayout cl = (CardLayout) panelBody.getLayout();
        cl.show(panelBody, cardName);
        lblTituloPagina.setText(tituloPagina);
        
        // Lógica de seleção limpa
        for(ModernButton btn : botoesMenu) {
            btn.setActive(btn.getActionCommand().equals(cardName));
        }
    }

    // =================================================================================
    // 4. LÓGICA DE LOGIN & SISTEMA
    // =================================================================================
    private void logar() {
        String email = txtLoginEmail.getText().trim();
        String senha = new String(txtLoginSenha.getPassword());

        if (email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try {
                    return Funcionario.loginFuncionario(email, senha);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "ERRO_CONEXAO";
                }
            }

            @Override
            protected void done() {
                try {
                    String role = get();
                    if ("ERRO_CONEXAO".equals(role)) {
                        JOptionPane.showMessageDialog(TelaInicial.this, "Erro ao conectar ao banco de dados.", "Erro Crítico", JOptionPane.ERROR_MESSAGE);
                    } else if (role != null) {
                        String status = Funcionario.buscarNoBanco("email", email, "status");
                        if ("pendente".equalsIgnoreCase(status)) {
                            JOptionPane.showMessageDialog(TelaInicial.this, "Seu cadastro ainda está em análise pelo gerente.", "Aguarde Aprovação", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            // SUCESSO
                            String nome = Funcionario.buscarNoBanco("email", email, "name");
                            lblNomeUsuario.setText(nome != null ? nome : "Usuário");
                            lblCargoUsuario.setText(role.toUpperCase());

                            // Constroi menu baseado no cargo
                            construirMenuPorCargo(role);

                            cardLayout.show(mainContainer, "DashBoard");
                            
                            // Navega para a primeira tela disponível
                            if(!botoesMenu.isEmpty()) {
                                botoesMenu.get(0).doClick();
                            }
                            
                            txtLoginSenha.setText("");
                        }
                    } else {
                        JOptionPane.showMessageDialog(TelaInicial.this, "E-mail ou senha inválidos.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void cadastrar() {
        if(txtCadNome.getText().isEmpty() || txtCadUser.getText().isEmpty() || new String(txtCadSenha.getPassword()).isEmpty()) {
             JOptionPane.showMessageDialog(this, "Preencha os campos obrigatórios.", "Erro", JOptionPane.WARNING_MESSAGE);
             return;
        }
        
        String s1 = new String(txtCadSenha.getPassword());
        String s2 = new String(txtCadConfirma.getPassword());
        
        if(!s1.equals(s2)) {
            JOptionPane.showMessageDialog(this, "Senhas não conferem.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Capturar cargo
        String cargoSelecionado = "Atendente"; // Default
        if(rbCozinheiro.isSelected()) cargoSelecionado = "Cozinheiro";
        if(rbEntregador.isSelected()) cargoSelecionado = "Entregador";
        
        // Aqui chamaria Funcionario.cadastrar(...) - Simulando sucesso
        JOptionPane.showMessageDialog(this, "Cadastro de " + cargoSelecionado + " enviado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        limparFormCadastro();
        cardLayout.show(mainContainer, "LOGIN");
    }

    private void logout() {
        int opt = JOptionPane.showConfirmDialog(this, "Deseja realmente sair?", "Sair", JOptionPane.YES_NO_OPTION);
        if(opt == JOptionPane.YES_OPTION) {
            cardLayout.show(mainContainer, "LOGIN");
            txtLoginEmail.setText("");
            txtLoginSenha.setText("");
            panelBody.removeAll(); // Limpa a memória das telas
        }
    }

    // =================================================================================
    // 5. COMPONENTES VISUAIS (CORRIGIDOS)
    // =================================================================================
    
    // CORREÇÃO: Campos de texto agora forçam a cor branca
    private JTextField criarInputModerno(String placeholder, GoogleMaterialDesignIcons icon, boolean isPassword) {
        JTextField field = isPassword ? new JPasswordField() : new JTextField();
        field.setPreferredSize(new Dimension(350, 45));
        field.setBackground(new Color(45, 45, 48)); // Fundo cinza escuro
        field.setForeground(Color.WHITE);           // Texto branco
        field.setCaretColor(Color.WHITE);           // Cursor branco
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Placeholder do FlatLaf
        field.putClientProperty("JTextField.placeholderText", placeholder);
        
        int pad = (icon != null) ? 10 : 10;
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, pad, 5, 10)
        ));
        return field;
    }

    private ModernButton criarBotaoMenu(String texto, GoogleMaterialDesignIcons icon, String actionCommand, String tituloPagina) {
        ModernButton btn = new ModernButton(texto, UIConstants.BG_DARK_ALT, UIConstants.FG_MUTED, false);
        btn.setIcon(IconFontSwing.buildIcon(icon, 20, UIConstants.FG_MUTED));
        btn.setActionCommand(actionCommand);
        btn.setIsMenuButton(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> navegarPara(actionCommand, tituloPagina));
        botoesMenu.add(btn);
        return btn;
    }

    private JLabel criarTituloSecao(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(100, 100, 100));
        l.setBorder(new EmptyBorder(15, 20, 5, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JRadioButton criarRadio(String texto) {
        JRadioButton rb = new JRadioButton(texto);
        rb.setOpaque(false);
        rb.setForeground(UIConstants.FG_LIGHT);
        rb.setFocusPainted(false);
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return rb;
    }

    private void limparFormCadastro() {
        txtCadNome.setText(""); txtCadUser.setText(""); txtCadEmail.setText("");
        txtCadTelefone.setText(""); txtCadSenha.setText(""); txtCadConfirma.setText("");
        grupoCargos.clearSelection();
    }

    private void configurarFormatacaoTelefone(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String t = field.getText().replaceAll("[^0-9]", "");
                if(t.length() > 11) t = t.substring(0, 11);
                try { field.setText(Formatador.formatarTelefone(t)); } catch (Exception ex) { field.setText(t); }
            }
        });
    }

    // =================================================================================
    // 6. PAINEL ESTOQUE (MANTIDO)
    // =================================================================================
    private JPanel criarPainelEstoque() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(20, 30, 30, 30));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        
        JTextField txtBusca = new JTextField();
        txtBusca.setPreferredSize(new Dimension(400, 40));
        UIConstants.styleField(txtBusca); 
        
        ModernButton btnNovo = new ModernButton("Novo Item", UIConstants.PRIMARY_RED, Color.WHITE, true);
        btnNovo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ADD, 18, Color.WHITE));
        btnNovo.setPreferredSize(new Dimension(140, 40));
        
        top.add(txtBusca);
        top.add(Box.createHorizontalStrut(15));
        top.add(btnNovo);
        p.add(top, BorderLayout.NORTH);

        String[] colunas = {"ID", "Item", "Categoria", "Unid.", "Qtd. Atual", "Status"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        try {
            List<EstoqueDAO.ItemEstoque> itens = estoqueDAO.buscarTodos();
            for(EstoqueDAO.ItemEstoque i : itens) model.addRow(new Object[]{i.getId(), i.getNome(), i.getCategoria(), i.getUnidadePadrao(), i.getQuantidadeAtual(), "Ativo"});
        } catch (Exception e) { model.addRow(new Object[]{"-", "Erro Conexão", "-", "-", "-", "OFFLINE"}); }

        JTable table = new JTable(model);
        styleTable(table);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }
    
    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(50, 50, 50));
        table.setBackground(new Color(40, 40, 43));
        table.setForeground(UIConstants.FG_LIGHT);
        table.setSelectionBackground(UIConstants.PRIMARY_RED);
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(30, 30, 35));
        header.setForeground(UIConstants.FG_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(60, 60, 60)));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    // =================================================================================
    // 7. CLASSES UI CUSTOMIZADAS (CORRIGIDAS)
    // =================================================================================

    // CORREÇÃO: Lógica de Hover e Active separadas para não travar
    private static class ModernButton extends JButton {
        private Color normalColor, hoverColor, textColor;
        private boolean isRounded, isMenuButton = false, isActive = false, isHover = false;

        public ModernButton(String text, Color bg, Color fg, boolean rounded) {
            super(text);
            this.normalColor = (bg != null) ? bg : new Color(0,0,0,0);
            this.hoverColor = (bg != null) ? bg.brighter() : new Color(255,255,255,20);
            this.textColor = (fg != null) ? fg : Color.WHITE;
            this.isRounded = rounded;
            
            setForeground(textColor);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { 
                    isHover = true; 
                    repaint(); 
                }
                public void mouseExited(MouseEvent e) { 
                    isHover = false; 
                    repaint(); 
                }
            });
        }
        
        public void setHoverColor(Color c) { this.hoverColor = c; }
        
        public void setIsMenuButton(boolean b) { 
            this.isMenuButton = b; 
            setHorizontalAlignment(SwingConstants.LEFT); 
            setBorder(new EmptyBorder(10, 25, 10, 10)); 
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setMaximumSize(new Dimension(250, 45)); 
        }
        
        public void setActive(boolean active) {
            this.isActive = active;
            if(active) {
                setIcon(IconFontSwing.buildIcon((GoogleMaterialDesignIcons)null, 20, UIConstants.PRIMARY_RED));
                setFont(new Font("Segoe UI", Font.BOLD, 14));
            } else {
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isMenuButton) {
                if (isActive) {
                    g2.setColor(UIConstants.PRIMARY_RED);
                    g2.fillRoundRect(0, 10, 4, getHeight()-20, 4, 4); // Indicador lateral
                    g2.setColor(new Color(60, 60, 60)); // Fundo Active
                } else if (isHover) {
                    g2.setColor(new Color(50, 50, 50)); // Fundo Hover
                } else {
                    g2.setColor(normalColor); // Fundo Normal
                }
                
                if (isActive || isHover) g2.fillRoundRect(4, 2, getWidth()-6, getHeight()-4, 8, 8);
                else if (normalColor.getAlpha() > 0) g2.fillRect(0, 0, getWidth(), getHeight());

                // Cor do texto
                if(isActive) setForeground(UIConstants.PRIMARY_RED);
                else if(isHover) setForeground(Color.WHITE);
                else setForeground(textColor);

            } else {
                // Botão Normal (Login/Cadastro)
                if (isHover) g2.setColor(hoverColor);
                else g2.setColor(normalColor);
                
                if (isRounded) g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                else if (normalColor.getAlpha() > 0) g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class GradientPanel extends JPanel {
        private Color c1, c2;
        public GradientPanel(Color c1, Color c2) { this.c1 = c1; this.c2 = c2; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class ShadowPanel extends JPanel {
        private int radius;
        private Color bgColor;
        public ShadowPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRoundRect(6, 6, getWidth()-12, getHeight()-12, radius, radius);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth()-6, getHeight()-6, radius, radius);
            super.paintComponent(g);
        }
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> new TelaInicial().setVisible(true));
    }
}