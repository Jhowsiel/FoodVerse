package com.senac.food.verse.gui;

import com.senac.food.verse.EstoqueDAO;
import com.senac.food.verse.Formatador;
import com.senac.food.verse.Funcionario;
import com.senac.food.verse.ValidarCadastro;
import static com.senac.food.verse.NtpTime.pegarDataAtualNTP;

// IMPORTAÇÕES DO JICONFONT (MATERIAL DESIGN)
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class TelaInicial extends JFrame {

    // Cores
    private static final Color MENU_BG = new Color(30, 30, 35);
    private static final Color ACTIVE_BTN = new Color(45, 45, 50);
    private static final Color HOVER_BTN = new Color(55, 55, 60);
    private static final Color TEXT_COLOR = new Color(200, 200, 200);

    // Gerenciador de Telas
    private JPanel mainContainer;
    private CardLayout cardLayout;

    // Campos Login/Cadastro
    private JTextField txtLoginEmail;
    private JPasswordField txtLoginSenha;
    private JTextField txtCadNome, txtCadUser, txtCadEmail, txtCadTelefone;
    private JPasswordField txtCadSenha, txtCadConfirma;
    private JRadioButton rbCozinheiro, rbEntregador;
    private ButtonGroup grupoCargos;
    
    // Dashboard
    private JPanel dashboardPanel;
    private JPanel panelBody;
    private JLabel lblNomeUsuario;
    private JLabel lblTituloPagina;
    private List<ModernButton> botoesMenu = new ArrayList<>();
    private ModernButton btnMenuAdmin;

    // DAO
    private final EstoqueDAO estoqueDAO = new EstoqueDAO();

    public TelaInicial() {
        setTitle("FoodVerse - Manager System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 750));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        // 1. REGISTRAR A FONTE DE ÍCONES
        IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());

        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
        } catch (Exception ex) {
            System.err.println("FlatLaf não carregado.");
        }

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
        JPanel loginPanel = new GradientPanel(new Color(15, 15, 15), new Color(50, 10, 15));
        loginPanel.setLayout(new GridBagLayout());

        ShadowPanel card = new ShadowPanel(40, new Color(30, 30, 30, 245));
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(50, 60, 50, 60));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 0, 10, 0);

        // Logo
        JLabel lblLogo = new JLabel("FOODVERSE");
        Icon iconLogo = IconLoader.load("/imagens/logo.png", 100, 100);
        if(iconLogo == null) {
             iconLogo = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.RESTAURANT, 80, Color.WHITE);
        }
        lblLogo.setIcon(iconLogo);
        
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setHorizontalTextPosition(SwingConstants.CENTER);
        lblLogo.setVerticalTextPosition(SwingConstants.BOTTOM);
        card.add(lblLogo, gc);

        gc.gridy++;
        JLabel lblSub = new JLabel("Acesso Administrativo");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(150, 150, 150));
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblSub, gc);

        // Inputs
        gc.gridy++; gc.insets = new Insets(30, 0, 10, 0);
        txtLoginEmail = criarInputModerno("E-mail", GoogleMaterialDesignIcons.EMAIL, false);
        card.add(txtLoginEmail, gc);

        gc.gridy++; gc.insets = new Insets(10, 0, 30, 0);
        txtLoginSenha = (JPasswordField) criarInputModerno("Senha", GoogleMaterialDesignIcons.LOCK, true);
        card.add(txtLoginSenha, gc);

        // Botão Entrar
        gc.gridy++; gc.insets = new Insets(0, 0, 20, 0);
        ModernButton btnEntrar = new ModernButton("ENTRAR", UIConstants.PRIMARY_RED, Color.WHITE, true);
        btnEntrar.addActionListener(e -> logar());
        card.add(btnEntrar, gc);

        // Link
        gc.gridy++;
        ModernButton btnLink = new ModernButton("Criar nova conta", null, new Color(150, 150, 150), false);
        btnLink.setHoverColor(Color.WHITE);
        btnLink.addActionListener(e -> cardLayout.show(mainContainer, "CADASTRO"));
        card.add(btnLink, gc);

        loginPanel.add(card);
        mainContainer.add(loginPanel, "LOGIN");
    }

    // =================================================================================
    // 2. TELA DE CADASTRO
    // =================================================================================
    private void initCadastroScreen() {
        JPanel cadastroPanel = new GradientPanel(new Color(15, 15, 15), new Color(30, 30, 35));
        cadastroPanel.setLayout(new GridBagLayout());

        ShadowPanel card = new ShadowPanel(40, new Color(30, 30, 30));
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 10, 8, 10);

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("Novo Cadastro");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblTitulo, gc);

        // Inputs
        gc.gridwidth = 1; gc.gridy++;
        gc.gridx = 0; card.add(criarInputModerno("Nome", GoogleMaterialDesignIcons.PERSON, false), gc); 
        Component[] comps = card.getComponents(); txtCadNome = (JTextField) comps[comps.length-1];
        
        gc.gridx = 1; card.add(criarInputModerno("Username", GoogleMaterialDesignIcons.ACCOUNT_CIRCLE, false), gc);
        txtCadUser = (JTextField) card.getComponent(card.getComponentCount()-1);
        
        gc.gridy++;
        gc.gridx = 0; card.add(criarInputModerno("E-mail", GoogleMaterialDesignIcons.EMAIL, false), gc);
        txtCadEmail = (JTextField) card.getComponent(card.getComponentCount()-1);

        gc.gridx = 1; card.add(criarInputModerno("Telefone", GoogleMaterialDesignIcons.PHONE, false), gc);
        txtCadTelefone = (JTextField) card.getComponent(card.getComponentCount()-1);
        configurarFormatacaoTelefone(txtCadTelefone);

        // Cargo
        gc.gridy++; gc.gridx = 0; gc.gridwidth = 2;
        JPanel pCargo = new JPanel(new BorderLayout(10,5));
        pCargo.setOpaque(false);
        pCargo.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(15, new Color(60,60,60)), new EmptyBorder(10,15,10,15)));
        
        JLabel lCargo = new JLabel("Função:");
        lCargo.setForeground(Color.LIGHT_GRAY);
        lCargo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pCargo.add(lCargo, BorderLayout.NORTH);
        
        JPanel pRadios = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pRadios.setOpaque(false);
        grupoCargos = new ButtonGroup();
        rbCozinheiro = criarRadio("Cozinheiro");
        rbEntregador = criarRadio("Entregador");
        grupoCargos.add(rbCozinheiro); grupoCargos.add(rbEntregador);
        pRadios.add(rbCozinheiro); pRadios.add(rbEntregador);
        pCargo.add(pRadios, BorderLayout.CENTER);
        card.add(pCargo, gc);

        // Senhas
        gc.gridy++; gc.gridwidth = 1;
        gc.gridx = 0; card.add(criarInputModerno("Senha", GoogleMaterialDesignIcons.LOCK, true), gc);
        txtCadSenha = (JPasswordField) card.getComponent(card.getComponentCount()-1);

        gc.gridx = 1; card.add(criarInputModerno("Confirmar", GoogleMaterialDesignIcons.LOCK_OUTLINE, true), gc);
        txtCadConfirma = (JPasswordField) card.getComponent(card.getComponentCount()-1);

        // Botões
        gc.gridy++; gc.gridwidth = 2; gc.gridx = 0;
        gc.insets = new Insets(30, 10, 10, 10);
        ModernButton btnSalvar = new ModernButton("CADASTRAR", UIConstants.SUCCESS_GREEN, Color.WHITE, true);
        btnSalvar.addActionListener(e -> cadastrar());
        card.add(btnSalvar, gc);

        gc.gridy++;
        ModernButton btnVoltar = new ModernButton("Voltar ao Login", null, Color.GRAY, false);
        btnVoltar.setHoverColor(Color.WHITE);
        btnVoltar.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));
        card.add(btnVoltar, gc);

        cadastroPanel.add(card);
        mainContainer.add(cadastroPanel, "CADASTRO");
    }

    // =================================================================================
    // 3. DASHBOARD
    // =================================================================================
    private void initDashboardScreen() {
        dashboardPanel = new JPanel(new BorderLayout());
        
        // --- SIDEBAR ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(MENU_BG);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(45, 45, 45)));

        // Logo Topo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(30, 0, 30, 0));
        
        JLabel lblDashLogo = new JLabel("FOODVERSE");
        lblDashLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDashLogo.setForeground(UIConstants.PRIMARY_RED);
        lblDashLogo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.RESTAURANT_MENU, 28, UIConstants.PRIMARY_RED));
        lblDashLogo.setIconTextGap(10);
        
        logoPanel.add(lblDashLogo);
        sidebar.add(logoPanel);

        // --- MENU ITENS ---
        sidebar.add(criarTituloSecao("PRINCIPAL"));
        
        sidebar.add(criarBotaoMenu("Pedidos", GoogleMaterialDesignIcons.RECEIPT, "Pedidos", "Fila de Pedidos"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(criarBotaoMenu("Cardápio", GoogleMaterialDesignIcons.RESTAURANT, "Cardapio", "Gerenciar Itens"));
        sidebar.add(Box.createVerticalStrut(8));
        
        // CORREÇÃO DOS ÍCONES AQUI:
        sidebar.add(criarBotaoMenu("Entregas", GoogleMaterialDesignIcons.MOTORCYCLE, "Entregas", "Monitor de Entregas")); // Era TWO_WHEELER
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(criarBotaoMenu("Estoque", GoogleMaterialDesignIcons.STORAGE, "Estoque", "Controle de Estoque")); // Era INVENTORY_2
        
        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(criarTituloSecao("ADMINISTRAÇÃO"));
        
        btnMenuAdmin = criarBotaoMenu("Usuários", GoogleMaterialDesignIcons.SUPERVISOR_ACCOUNT, "AprovacaoCadastros", "Gestão de Acessos");
        sidebar.add(btnMenuAdmin);

        sidebar.add(Box.createVerticalGlue());

        // Botão Sair
        ModernButton btnSair = new ModernButton("Sair", null, new Color(200, 80, 80), false);
        btnSair.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.EXIT_TO_APP, 22, new Color(200, 80, 80)));
        btnSair.setHorizontalAlignment(SwingConstants.CENTER); 
        btnSair.setAlignmentX(Component.CENTER_ALIGNMENT);   
        btnSair.setMaximumSize(new Dimension(200, 45));
        btnSair.setHoverColor(UIConstants.PRIMARY_RED);
        btnSair.addActionListener(e -> logout());
        
        sidebar.add(btnSair);
        sidebar.add(Box.createVerticalStrut(30));

        dashboardPanel.add(sidebar, BorderLayout.WEST);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MENU_BG);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 45, 45)));

        lblTituloPagina = new JLabel("Visão Geral");
        lblTituloPagina.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTituloPagina.setForeground(Color.WHITE);
        lblTituloPagina.setBorder(new EmptyBorder(0, 40, 0, 0));
        header.add(lblTituloPagina, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        userPanel.setBorder(new EmptyBorder(0, 0, 0, 30));
        
        lblNomeUsuario = new JLabel("Usuário");
        lblNomeUsuario.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNomeUsuario.setForeground(Color.WHITE);
        lblNomeUsuario.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCOUNT_CIRCLE, 34, Color.WHITE));
        lblNomeUsuario.setIconTextGap(10);
        userPanel.add(lblNomeUsuario);
        header.add(userPanel, BorderLayout.EAST);

        dashboardPanel.add(header, BorderLayout.NORTH);

        // --- CORPO ---
        panelBody = new JPanel(new CardLayout());
        panelBody.setBackground(UIConstants.BG_DARK);

        panelBody.add(new CardapioPainel(), "Cardapio");
        panelBody.add(new EntregasPainel(), "Entregas");
        panelBody.add(new AprovacaoCadastrosPanel(), "AprovacaoCadastros");
        panelBody.add(new PedidosPanel(), "Pedidos");
        panelBody.add(criarPainelEstoque(), "Estoque");

        dashboardPanel.add(panelBody, BorderLayout.CENTER);
        mainContainer.add(dashboardPanel, "DashBoard");
    }

    // =================================================================================
    // 4. MÓDULO DE ESTOQUE
    // =================================================================================
    private JPanel criarPainelEstoque() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Topo
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        
        JTextField txtBusca = new JTextField();
        txtBusca.setPreferredSize(new Dimension(350, 45));
        UIConstants.styleField(txtBusca); 
        txtBusca.putClientProperty("JTextField.placeholderText", "Buscar item no estoque...");
        
        ModernButton btnNovo = new ModernButton("Novo Item", UIConstants.PRIMARY_RED, Color.WHITE, true);
        btnNovo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ADD, 18, Color.WHITE));
        btnNovo.setPreferredSize(new Dimension(150, 45));
        
        top.add(txtBusca);
        top.add(Box.createHorizontalStrut(15));
        top.add(btnNovo);
        p.add(top, BorderLayout.NORTH);

        // Tabela
        String[] colunas = {"ID", "Item", "Categoria", "Unid.", "Qtd. Atual", "Status"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        List<EstoqueDAO.ItemEstoque> itens = estoqueDAO.buscarTodos();
        for(EstoqueDAO.ItemEstoque i : itens) {
            model.addRow(new Object[]{i.getId(), i.getNome(), i.getCategoria(), i.getUnidadePadrao(), i.getQuantidadeAtual(), "Ativo"});
        }

        JTable table = new JTable(model);
        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        sp.getViewport().setBackground(new Color(40, 40, 40));
        p.add(sp, BorderLayout.CENTER);
        
        return p;
    }

    // =================================================================================
    // LÓGICA
    // =================================================================================
    private void navegarPara(String cardName, String tituloPagina) {
        CardLayout cl = (CardLayout) panelBody.getLayout();
        cl.show(panelBody, cardName);
        lblTituloPagina.setText(tituloPagina);
        for(ModernButton btn : botoesMenu) {
            btn.setActive(btn.getActionCommand().equals(cardName));
        }
    }

    private void logar() {
        String email = txtLoginEmail.getText().trim();
        String senha = new String(txtLoginSenha.getPassword());
        if (email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos."); return;
        }
        try {
            String role = Funcionario.loginFuncionario(email, senha);
            if (role != null) {
                String status = Funcionario.buscarNoBanco("email", email, "status");
                if ("pendente".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(this, "Aguarde aprovação do cadastro."); return;
                }
                String nome = Funcionario.buscarNoBanco("email", email, "name");
                if ("Simulação".equals(nome) || nome == null) nome = "Administrador";
                lblNomeUsuario.setText(nome + " (" + role + ")");
                btnMenuAdmin.setVisible(role.equalsIgnoreCase("admin"));
                cardLayout.show(mainContainer, "DashBoard");
                navegarPara("Pedidos", "Fila de Pedidos"); 
                txtLoginEmail.setText(""); txtLoginSenha.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Dados incorretos.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cadastrar() {
        JOptionPane.showMessageDialog(this, "Cadastro enviado! Aguarde aprovação.");
        cardLayout.show(mainContainer, "LOGIN");
        limparFormCadastro();
    }

    private void logout() { cardLayout.show(mainContainer, "LOGIN"); }
    
    private void limparFormCadastro() {
        txtCadNome.setText(""); txtCadUser.setText(""); txtCadEmail.setText("");
        txtCadTelefone.setText(""); txtCadSenha.setText(""); txtCadConfirma.setText("");
        grupoCargos.clearSelection();
    }
    
    private void configurarFormatacaoTelefone(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String t = field.getText().replaceAll("[^0-9]", "");
                if(t.length()>11) t = t.substring(0,11);
                field.setText(Formatador.formatarTelefone(t));
            }
        });
    }

    // =================================================================================
    // FACTORIES & COMPONENTES
    // =================================================================================

    private JTextField criarInputModerno(String placeholder, GoogleMaterialDesignIcons icon, boolean isPassword) {
        JTextField field = isPassword ? new JPasswordField() : new JTextField();
        field.setPreferredSize(new Dimension(420, 50));
        field.setMinimumSize(new Dimension(300, 50));
        field.setBackground(new Color(42, 42, 45));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        
        int paddingLeft = (icon != null) ? 15 : 15;
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, paddingLeft, 5, 10)
        ));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private JRadioButton criarRadio(String texto) {
        JRadioButton rb = new JRadioButton(texto);
        rb.setOpaque(false);
        rb.setForeground(Color.WHITE);
        rb.setFocusPainted(false);
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return rb;
    }
    
    private JLabel criarTituloSecao(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(100, 100, 100));
        l.setBorder(new EmptyBorder(10, 0, 5, 0));
        l.setAlignmentX(Component.CENTER_ALIGNMENT); 
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private ModernButton criarBotaoMenu(String texto, GoogleMaterialDesignIcons icon, String actionCommand, String tituloPagina) {
        ModernButton btn = new ModernButton(texto, MENU_BG, TEXT_COLOR, false);
        btn.setIcon(IconFontSwing.buildIcon(icon, 22, new Color(180,180,180)));
        btn.setActionCommand(actionCommand);
        btn.setIsMenuButton(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); 
        btn.addActionListener(e -> navegarPara(actionCommand, tituloPagina));
        botoesMenu.add(btn);
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(60, 60, 60));
        table.setBackground(new Color(45, 45, 48));
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(UIConstants.PRIMARY_RED);
        table.setSelectionForeground(Color.WHITE);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(35, 35, 40));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.PRIMARY_RED));
        
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    // --- CLASSES VISUAIS ---

    private static class ModernButton extends JButton {
        private Color normalColor, hoverColor, textColor;
        private boolean isRounded, isMenuButton = false, isActive = false;

        public ModernButton(String text, Color bg, Color fg, boolean rounded) {
            super(text);
            this.normalColor = bg != null ? bg : new Color(0,0,0,0);
            this.hoverColor = bg != null ? bg.brighter() : new Color(255,255,255,30);
            this.textColor = fg;
            this.isRounded = rounded;
            
            setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { if(!isActive) setBackground(hoverColor); }
                public void mouseExited(MouseEvent e) { if(!isActive) setBackground(normalColor); }
            });
        }
        
        public void setHoverColor(Color c) { this.hoverColor = c; }
        public void setIsMenuButton(boolean b) { 
            this.isMenuButton = b; 
            setHorizontalAlignment(SwingConstants.LEFT); 
            setBorder(new EmptyBorder(10, 30, 10, 10)); 
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
            setMaximumSize(new Dimension(230, 50)); 
            setPreferredSize(new Dimension(230, 50));
        }
        
        public void setActive(boolean active) {
            this.isActive = active;
            if(active) {
                setBackground(ACTIVE_BTN);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 15));
            } else {
                setBackground(normalColor);
                setForeground(textColor);
                setFont(new Font("Segoe UI", Font.PLAIN, 15));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isMenuButton && isActive) {
                g2.setColor(UIConstants.PRIMARY_RED);
                g2.fillRoundRect(0, 5, 4, getHeight()-10, 4, 4); 
                g2.setColor(getBackground());
                g2.fillRoundRect(4, 0, getWidth()-4, getHeight(), 10, 10);
            } else if (isRounded) {
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            } else {
                g2.setColor(getBackground());
                if(getBackground().getAlpha() > 0)
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
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
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillRoundRect(5, 8, getWidth()-10, getHeight()-10, radius, radius);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth()-5, getHeight()-5, radius, radius);
            super.paintComponent(g);
        }
    }
    
    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private int radius; private Color color;
        RoundedBorder(int radius, Color color) { this.radius = radius; this.color = color; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
            g2.dispose();
        }
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> new TelaInicial().setVisible(true));
    }
}