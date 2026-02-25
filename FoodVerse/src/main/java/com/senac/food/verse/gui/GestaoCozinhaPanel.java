package com.senac.food.verse.gui;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class GestaoCozinhaPanel extends JPanel {

    public static class PedidoCozinhaDTO {
        int idPedido;
        String tipo; 
        String identificador; 
        LocalDateTime dataPedido;
        List<String> itens;
        String status;

        public PedidoCozinhaDTO(int id, String tipo, String ident, LocalDateTime data, List<String> itens, String status) {
            this.idPedido = id;
            this.tipo = tipo;
            this.identificador = ident;
            this.dataPedido = data;
            this.itens = itens;
            this.status = status;
        }
    }

    private JPanel containerPedidos;

    public GestaoCozinhaPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_DARK);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Fila de Preparo (Cozinha)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.FG_LIGHT);
        // Ícone corrigido: RESTAURANT em vez de SOUP_KITCHEN
        title.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.RESTAURANT, 28, UIConstants.FG_LIGHT));
        header.add(title, BorderLayout.WEST);

        JButton btnRefresh = createActionButton("Atualizar", GoogleMaterialDesignIcons.REFRESH, UIConstants.BG_DARK_ALT);
        btnRefresh.addActionListener(e -> carregarPedidos());
        header.add(btnRefresh, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        containerPedidos = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        containerPedidos.setBackground(UIConstants.BG_DARK);

        JScrollPane scroll = new JScrollPane(containerPedidos);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        
        add(scroll, BorderLayout.CENTER);

        carregarPedidos();
    }

    private void carregarPedidos() {
        containerPedidos.removeAll();

        List<PedidoCozinhaDTO> pedidos = new ArrayList<>();
        pedidos.add(new PedidoCozinhaDTO(101, "LOCAL", "Mesa 04", LocalDateTime.now().minusMinutes(45),
                List.of("2x Hambúrguer Artesanal", "1x Coca-Cola Zero"), "EM_PREPARO"));
        pedidos.add(new PedidoCozinhaDTO(102, "DELIVERY", "Jhowsiel (iFood)", LocalDateTime.now().minusMinutes(5),
                List.of("1x Pizza Calabresa", "1x Borda Recheada"), "PENDENTE"));
        pedidos.add(new PedidoCozinhaDTO(103, "LOCAL", "Mesa 12", LocalDateTime.now().minusMinutes(15),
                List.of("1x Salada Caesar", "1x Suco Laranja"), "PENDENTE"));

        for (PedidoCozinhaDTO p : pedidos) {
            containerPedidos.add(criarCardPedido(p));
        }

        containerPedidos.revalidate();
        containerPedidos.repaint();
    }

    private JPanel criarCardPedido(PedidoCozinhaDTO pedido) {
        long minutosDecorridos = ChronoUnit.MINUTES.between(pedido.dataPedido, LocalDateTime.now());
        boolean isAtrasado = minutosDecorridos > 30;
        
        RoundedPanel card = new RoundedPanel(15, UIConstants.CARD_DARK);
        card.setPreferredSize(new Dimension(280, 320));
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setOpaque(false);
        
        JLabel lblId = new JLabel("#" + pedido.idPedido);
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblId.setForeground(UIConstants.FG_LIGHT);
        
        JLabel lblTempo = new JLabel(minutosDecorridos + " min");
        lblTempo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTempo.setForeground(isAtrasado ? UIConstants.PRIMARY_RED : UIConstants.FG_MUTED);
        lblTempo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCESS_TIME, 16, isAtrasado ? UIConstants.PRIMARY_RED : UIConstants.FG_MUTED));

        headerCard.add(lblId, BorderLayout.WEST);
        headerCard.add(lblTempo, BorderLayout.EAST);

        JLabel lblOrigem = new JLabel(pedido.tipo + " • " + pedido.identificador);
        lblOrigem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblOrigem.setForeground(UIConstants.FG_MUTED);
        lblOrigem.setBorder(new EmptyBorder(5,0,10,0));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(headerCard, BorderLayout.NORTH);
        topContainer.add(lblOrigem, BorderLayout.CENTER);

        Box listaItens = Box.createVerticalBox();
        for (String item : pedido.itens) {
            JLabel lblItem = new JLabel("• " + item);
            lblItem.setForeground(UIConstants.FG_LIGHT);
            lblItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblItem.setAlignmentX(Component.LEFT_ALIGNMENT);
            listaItens.add(lblItem);
            listaItens.add(Box.createVerticalStrut(5));
        }
        
        JScrollPane scrollItens = new JScrollPane(listaItens);
        scrollItens.setOpaque(false);
        scrollItens.getViewport().setOpaque(false);
        scrollItens.setBorder(null);

        JButton btnAction = createActionButton("Concluir Prato", GoogleMaterialDesignIcons.CHECK, UIConstants.SUCCESS_GREEN);
        btnAction.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Pedido #" + pedido.idPedido + " marcado como PRONTO.");
        });

        card.add(topContainer, BorderLayout.NORTH);
        card.add(scrollItens, BorderLayout.CENTER);
        card.add(btnAction, BorderLayout.SOUTH);

        return card;
    }

    private JButton createActionButton(String text, GoogleMaterialDesignIcons icon, Color bg) {
        JButton btn = new JButton(text);
        btn.setIcon(IconFontSwing.buildIcon(icon, 18, Color.WHITE));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        return btn;
    }

    private static class RoundedPanel extends JPanel {
        private int radius; 
        private Color bgColor;
        public RoundedPanel(int radius, Color bgColor) { 
            this.radius = radius; 
            this.bgColor = bgColor; 
            setOpaque(false); 
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
        }
    }
}