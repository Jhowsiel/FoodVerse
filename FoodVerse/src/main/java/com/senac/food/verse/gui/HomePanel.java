package com.senac.food.verse.gui;

import com.senac.food.verse.SessionContext;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;
import java.util.function.Consumer;

public class HomePanel extends JPanel {

    private final Consumer<String> onNavigate;

    public HomePanel(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);

        SessionContext ctx = SessionContext.getInstance();
        
        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_DARK);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitulo = new JLabel("Visão Geral");
        lblTitulo.setFont(UIConstants.FONT_TITLE);
        lblTitulo.setForeground(UIConstants.FG_LIGHT);
        header.add(lblTitulo, BorderLayout.WEST);
        
        JLabel lblData = new JLabel("Hoje, " + java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"))));
        lblData.setFont(UIConstants.ARIAL_14);
        lblData.setForeground(UIConstants.FG_MUTED);
        header.add(lblData, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);

        // --- CONTEÚDO SCROLLÁVEL ---
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIConstants.BG_DARK);
        content.setBorder(new EmptyBorder(0, 30, 30, 30));

        content.add(criarPainelBoasVindas(ctx));
        content.add(Box.createVerticalStrut(20));

        // 1. Cards de Indicadores (KPIs)
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        kpiPanel.setBackground(UIConstants.BG_DARK);
        kpiPanel.setMaximumSize(new Dimension(2000, 140)); // Altura fixa para os cards
        
        kpiPanel.add(new InfoCard("Usuário", valorOuPadrao(ctx.getNome(), "Não identificado"), GoogleMaterialDesignIcons.ACCOUNT_CIRCLE, UIConstants.PRIMARY_RED));
        kpiPanel.add(new InfoCard("Perfil", valorOuPadrao(ctx.getCargo(), "Sem cargo"), GoogleMaterialDesignIcons.PERSON, UIConstants.SUCCESS_GREEN));
        kpiPanel.add(new InfoCard("Escopo", buildScopeLabel(ctx), GoogleMaterialDesignIcons.TRACK_CHANGES, new Color(230, 126, 34)));
        kpiPanel.add(new InfoCard("Restaurante", buildRestaurantSummary(ctx), GoogleMaterialDesignIcons.STORE, new Color(52, 152, 219)));

        content.add(kpiPanel);
        content.add(Box.createVerticalStrut(30)); // Espaçamento

        // 2. Seção Inferior (Resumo + Atalhos)
        JPanel bottomSection = new JPanel(new GridBagLayout());
        bottomSection.setBackground(UIConstants.BG_DARK);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 0, 0, 20);
        
        // Resumo operacional (Esquerda - 70%)
        gc.gridx = 0; gc.weightx = 0.7; gc.weighty = 1.0;
        
        JPanel pTable = new JPanel(new BorderLayout());
        pTable.setBackground(UIConstants.BG_DARK_ALT);
        pTable.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.GRID_DARK, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblUltimos = new JLabel("Contexto Operacional");
        lblUltimos.setFont(UIConstants.FONT_BOLD);
        lblUltimos.setForeground(UIConstants.FG_LIGHT);
        lblUltimos.setBorder(new EmptyBorder(0, 0, 15, 0));
        pTable.add(lblUltimos, BorderLayout.NORTH);
        pTable.add(criarResumoOperacional(ctx), BorderLayout.CENTER);
        
        bottomSection.add(pTable, gc);

        // Atalhos Rápidos (Direita - 30%)
        gc.gridx = 1; gc.weightx = 0.4; gc.insets = new Insets(0, 0, 0, 0);
        
        JPanel actionsWrapper = new JPanel(new BorderLayout(0, 15));
        actionsWrapper.setOpaque(false);
        actionsWrapper.setPreferredSize(new Dimension(320, 0));

        JLabel lblAtalhos = new JLabel("Atalhos Rápidos");
        lblAtalhos.setFont(UIConstants.FONT_BOLD);
        lblAtalhos.setForeground(UIConstants.FG_LIGHT);
        actionsWrapper.add(lblAtalhos, BorderLayout.NORTH);

        JPanel pActions = new JPanel();
        pActions.setOpaque(false);
        pActions.setLayout(new BoxLayout(pActions, BoxLayout.Y_AXIS));
        
        adicionarAtalhosRapidos(pActions, ctx);
        
        actionsWrapper.add(pActions, BorderLayout.CENTER);
        bottomSection.add(actionsWrapper, gc);
        
        content.add(bottomSection);

        JScrollPane scrollPane = new JScrollPane(content);
        UIConstants.styleScrollPane(scrollPane);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    // Construtor auxiliar para os botões de atalho da Home
    private JButton criarBotaoAtalho(String texto, GoogleMaterialDesignIcons icon, String cardName, boolean enabled, String tooltip) {
        JButton btn = new JButton(texto);
        UIConstants.styleSecondary(btn);
        btn.setIcon(IconFontSwing.buildIcon(icon, 20, UIConstants.FG_LIGHT));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(15);
        btn.setEnabled(enabled);
        btn.setToolTipText(tooltip);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btn.setPreferredSize(new Dimension(280, 52));
        if (enabled) {
            btn.addActionListener(e -> onNavigate.accept(cardName));
        }
        return btn;
    }

    private JPanel criarPainelBoasVindas(SessionContext ctx) {
        UIConstants.RoundedPanel panel = new UIConstants.RoundedPanel(20, UIConstants.BG_DARK_ALT);
        panel.setLayout(new BorderLayout(20, 0));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel icon = new JLabel(IconFontSwing.buildIcon(
                ctx.isAdmin() ? GoogleMaterialDesignIcons.SUPERVISOR_ACCOUNT : GoogleMaterialDesignIcons.RESTAURANT_MENU,
                42,
                UIConstants.PRIMARY_RED));
        panel.add(icon, BorderLayout.WEST);

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 8));
        textos.setOpaque(false);

        JLabel titulo = new JLabel("Bem-vindo(a), " + valorOuPadrao(ctx.getNome(), "usuário") + "!");
        titulo.setFont(UIConstants.FONT_TITLE);
        titulo.setForeground(UIConstants.FG_LIGHT);
        textos.add(titulo);

        JLabel descricao = new JLabel(buildHomeSummaryText(ctx));
        descricao.setFont(UIConstants.FONT_REGULAR);
        descricao.setForeground(UIConstants.FG_MUTED);
        textos.add(descricao);

        panel.add(textos, BorderLayout.CENTER);
        return panel;
    }

    private JPanel criarResumoOperacional(SessionContext ctx) {
        JPanel resumo = new JPanel();
        resumo.setOpaque(false);
        resumo.setLayout(new BoxLayout(resumo, BoxLayout.Y_AXIS));

        for (String linha : buildOperationalLines(ctx)) {
            JLabel label = new JLabel("• " + linha);
            label.setFont(UIConstants.FONT_REGULAR);
            label.setForeground(UIConstants.FG_MUTED);
            label.setBorder(new EmptyBorder(0, 0, 12, 0));
            resumo.add(label);
        }

        if (ctx.isAdmin() && !ctx.adminTemContextoRestaurante()) {
            JButton selecionar = new JButton("Selecionar restaurante para operar");
            UIConstants.stylePrimary(selecionar);
            selecionar.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.STORE_MALL_DIRECTORY, 18, Color.WHITE));
            selecionar.setAlignmentX(Component.LEFT_ALIGNMENT);
            selecionar.addActionListener(e -> onNavigate.accept("RESTAURANTES"));
            resumo.add(Box.createVerticalStrut(8));
            resumo.add(selecionar);
        }

        return resumo;
    }

    private void adicionarAtalhosRapidos(JPanel pActions, SessionContext ctx) {
        boolean adminSemContexto = ctx.isAdmin() && !ctx.adminTemContextoRestaurante();
        boolean gerenteOuAdminContexto = (!ctx.isAdmin() && hasRole(ctx, "gerente")) || ctx.adminTemContextoRestaurante();
        boolean atendente = hasRole(ctx, "atendente") || hasRole(ctx, "garçom");
        boolean cozinha = hasRole(ctx, "cozinheiro") || hasRole(ctx, "chef");
        boolean entrega = hasRole(ctx, "entregador");

        pActions.add(criarBotaoAtalho(
                adminSemContexto ? "Selecionar Restaurante" : "Perfil do Restaurante",
                adminSemContexto ? GoogleMaterialDesignIcons.STORE_MALL_DIRECTORY : GoogleMaterialDesignIcons.BUSINESS,
                adminSemContexto ? "RESTAURANTES" : "MEU_RESTAURANTE",
                true,
                adminSemContexto ? "Escolha um restaurante antes de operar módulos internos." : "Abrir dados do restaurante em contexto."));
        pActions.add(Box.createVerticalStrut(15));

        pActions.add(criarBotaoAtalho(
                "Pedidos",
                GoogleMaterialDesignIcons.ADD_SHOPPING_CART,
                "PEDIDOS",
                gerenteOuAdminContexto || atendente,
                gerenteOuAdminContexto || atendente ? "Abrir fluxo de pedidos." : "Disponível apenas com restaurante em contexto."));
        pActions.add(Box.createVerticalStrut(15));

        pActions.add(criarBotaoAtalho(
                "Cozinha",
                GoogleMaterialDesignIcons.KITCHEN,
                "KDS",
                gerenteOuAdminContexto || cozinha,
                gerenteOuAdminContexto || cozinha ? "Abrir gestão da cozinha." : "Disponível apenas com restaurante em contexto."));
        pActions.add(Box.createVerticalStrut(15));

        pActions.add(criarBotaoAtalho(
                adminSemContexto ? "Gerenciar Equipe" : "Entregas",
                adminSemContexto ? GoogleMaterialDesignIcons.SUPERVISOR_ACCOUNT : GoogleMaterialDesignIcons.MOTORCYCLE,
                adminSemContexto ? "USUARIOS" : "ENTREGAS",
                adminSemContexto || gerenteOuAdminContexto || atendente || entrega,
                adminSemContexto ? "Abrir aprovações e equipe global." : "Abrir painel de entregas."));
    }

    private boolean hasRole(SessionContext ctx, String role) {
        return ctx.getCargo() != null && ctx.getCargo().toLowerCase().contains(role);
    }

    static String buildHomeSummaryText(SessionContext ctx) {
        if (ctx.getCargo() == null) {
            return "Sua sessão ainda não foi carregada.";
        }
        if (ctx.isAdmin() && !ctx.adminTemContextoRestaurante()) {
            return "Você está no modo global. Selecione um restaurante para liberar os módulos operacionais.";
        }
        return "Seu painel inicial já está filtrado para o restaurante #" + ctx.getRestauranteEfetivo() + ".";
    }

    private static String[] buildOperationalLines(SessionContext ctx) {
        if (ctx.isAdmin() && !ctx.adminTemContextoRestaurante()) {
            return new String[]{
                    "Você está vendo apenas os módulos globais de administração da plataforma.",
                    "Os painéis operacionais do restaurante ficam disponíveis depois que um contexto é selecionado.",
                    "Use a gestão de restaurantes para entrar no contexto correto antes de editar cardápio, estoque ou pedidos."
            };
        }
        return new String[]{
                "As permissões e os dados exibidos respeitam o cargo do usuário logado.",
                "O escopo atual impede que informações de outros restaurantes sejam exibidas por engano.",
                "Use os atalhos rápidos ao lado para continuar o fluxo operacional do restaurante atual."
        };
    }

    private static String buildScopeLabel(SessionContext ctx) {
        if (ctx.isAdmin() && !ctx.adminTemContextoRestaurante()) {
            return "Global";
        }
        return "Restaurante";
    }

    private static String buildRestaurantSummary(SessionContext ctx) {
        if (ctx.isAdmin() && !ctx.adminTemContextoRestaurante()) {
            return "Selecione";
        }
        int restauranteId = ctx.getRestauranteEfetivo();
        return restauranteId > 0 ? "#" + restauranteId : "Não definido";
    }

    private static String valorOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor;
    }

    // --- Componente Interno: Card de Informação Visual ---
    private class InfoCard extends UIConstants.RoundedPanel {
        public InfoCard(String titulo, String valor, GoogleMaterialDesignIcons icon, Color corIcone) {
            super(20, UIConstants.BG_DARK_ALT); // Painel com bordas arredondadas
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));
            
            // Ícone grande
            JLabel lblIcon = new JLabel(IconFontSwing.buildIcon(icon, 40, corIcone));
            add(lblIcon, BorderLayout.EAST);
            
            // Textos
            JPanel pText = new JPanel(new GridLayout(2, 1));
            pText.setOpaque(false);
            
            JLabel lblTit = new JLabel(titulo);
            lblTit.setFont(UIConstants.ARIAL_14);
            lblTit.setForeground(UIConstants.FG_MUTED);
            
            JLabel lblVal = new JLabel(valor);
            lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22)); 
            lblVal.setForeground(UIConstants.FG_LIGHT);
            
            pText.add(lblTit);
            pText.add(lblVal);
            
            add(pText, BorderLayout.CENTER);
        }
    }
}
