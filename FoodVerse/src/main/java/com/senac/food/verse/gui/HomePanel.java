package com.senac.food.verse.gui;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HomePanel extends JPanel {

    public HomePanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);
        
        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_DARK);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitulo = new JLabel("Visão Geral");
        lblTitulo.setFont(UIConstants.FONT_TITLE);
        lblTitulo.setForeground(UIConstants.FG_LIGHT);
        header.add(lblTitulo, BorderLayout.WEST);
        
        JLabel lblData = new JLabel("Hoje, " + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));
        lblData.setFont(UIConstants.ARIAL_14);
        lblData.setForeground(UIConstants.FG_MUTED);
        header.add(lblData, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);

        // --- CONTEÚDO SCROLLÁVEL ---
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIConstants.BG_DARK);
        content.setBorder(new EmptyBorder(0, 30, 30, 30));

        // 1. Cards de Indicadores (KPIs)
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        kpiPanel.setBackground(UIConstants.BG_DARK);
        kpiPanel.setMaximumSize(new Dimension(2000, 140)); // Altura fixa para os cards
        
        kpiPanel.add(new InfoCard("Vendas Hoje", "R$ 1.250,00", GoogleMaterialDesignIcons.MONETIZATION_ON, UIConstants.SUCCESS_GREEN));
        kpiPanel.add(new InfoCard("Pedidos", "24", GoogleMaterialDesignIcons.RECEIPT, UIConstants.PRIMARY_RED));
        kpiPanel.add(new InfoCard("Em Produção", "5", GoogleMaterialDesignIcons.KITCHEN, new Color(230, 126, 34))); // Laranja
        kpiPanel.add(new InfoCard("Entregas", "3", GoogleMaterialDesignIcons.MOTORCYCLE, new Color(52, 152, 219))); // Azul

        content.add(kpiPanel);
        content.add(Box.createVerticalStrut(30)); // Espaçamento

        // 2. Seção Inferior (Tabela de Atividades + Atalhos)
        JPanel bottomSection = new JPanel(new GridBagLayout());
        bottomSection.setBackground(UIConstants.BG_DARK);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 0, 0, 20);
        
        // Tabela de Últimos Pedidos (Esquerda - 70%)
        gc.gridx = 0; gc.weightx = 0.7; gc.weighty = 1.0;
        
        JPanel pTable = new JPanel(new BorderLayout());
        pTable.setBackground(UIConstants.BG_DARK_ALT);
        // Borda elegante ao redor de todo o bloco da tabela
        pTable.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.GRID_DARK, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblUltimos = new JLabel("Atividades Recentes");
        lblUltimos.setFont(UIConstants.FONT_BOLD);
        lblUltimos.setForeground(UIConstants.FG_LIGHT);
        lblUltimos.setBorder(new EmptyBorder(0, 0, 15, 0));
        pTable.add(lblUltimos, BorderLayout.NORTH);
        
        // Dados provisórios
        String[] colunas = {"ID", "Cliente", "Status", "Valor", "Hora"};
        Object[][] dados = {
            {"#1024", "João Silva", "Entregue", "R$ 45,00", "19:30"},
            {"#1025", "Mesa 04", "Na Cozinha", "R$ 120,00", "20:05"},
            {"#1026", "Ana Maria", "Pendente", "R$ 32,50", "20:10"},
            {"#1027", "Carlos D.", "Enviado", "R$ 88,90", "20:12"},
            {"#1028", "Mesa 02", "Novo", "R$ 15,00", "20:15"},
        };
        JTable table = new JTable(dados, colunas);
        UIConstants.styleTable(table); // APLICA O ESTILO CORRIGIDO
        
        JScrollPane scroll = new JScrollPane(table);
        UIConstants.styleScrollPane(scroll); 
        scroll.setBorder(null); // Remove a borda extra do Scroll para ficar flat
        pTable.add(scroll, BorderLayout.CENTER);
        
        bottomSection.add(pTable, gc);

        // Atalhos Rápidos (Direita - 30%)
        gc.gridx = 1; gc.weightx = 0.3; gc.insets = new Insets(0, 0, 0, 0);
        
        JPanel pActions = new JPanel(new GridLayout(4, 1, 0, 15));
        pActions.setOpaque(false);
        
        pActions.add(criarBotaoAtalho("Novo Pedido", GoogleMaterialDesignIcons.ADD_SHOPPING_CART));
        pActions.add(criarBotaoAtalho("Reservar Mesa", GoogleMaterialDesignIcons.EVENT_SEAT));
        pActions.add(criarBotaoAtalho("Novo Cliente", GoogleMaterialDesignIcons.PERSON_ADD));
        pActions.add(criarBotaoAtalho("Fechar Caixa", GoogleMaterialDesignIcons.ATTACH_MONEY));
        
        bottomSection.add(pActions, gc);
        
        content.add(bottomSection);

        add(content, BorderLayout.CENTER);
    }
    
    // Construtor auxiliar para os botões de atalho da Home
    private JButton criarBotaoAtalho(String texto, GoogleMaterialDesignIcons icon) {
        JButton btn = new JButton(texto);
        UIConstants.styleSecondary(btn);
        btn.setIcon(IconFontSwing.buildIcon(icon, 20, UIConstants.FG_LIGHT));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(15);
        return btn;
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