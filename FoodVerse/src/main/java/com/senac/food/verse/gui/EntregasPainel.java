package com.senac.food.verse.gui;

import com.senac.food.verse.PedidoDAO;
import com.senac.food.verse.Pedidos;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

public class EntregasPainel extends JPanel {

    private final PedidoDAO dao = new PedidoDAO();
    private JPanel containerCards;
    private JLabel lblTotal;
    private String filtroAtual = "pronto"; // ou "em rota"

    public EntregasPainel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);

        initHeader();
        initList();
        
        // Atualiza a cada 2 segundos para apanhar mudanças do outro painel
        new Timer(2000, e -> carregarEntregas()).start();
    }
    
    // Compatibilidade
    public void setPedidosPanel(PedidosPanel pp) { }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_DARK);
        header.setBorder(new EmptyBorder(30, 40, 10, 40));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        JLabel icon = new JLabel(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MOTORCYCLE, 36, UIConstants.FG_LIGHT));
        JLabel title = new JLabel("Gestão de Entregas");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UIConstants.FG_LIGHT);
        top.add(icon); top.add(title);
        
        // Filtros (Abas Visuais)
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tabs.setOpaque(false);
        tabs.add(criarBotaoFiltro("Aguardando Motoboy", "pronto", new Color(200, 150, 0)));
        tabs.add(criarBotaoFiltro("Em Rota", "em rota", new Color(50, 100, 200)));
        
        lblTotal = new JLabel("0 pedidos");
        lblTotal.setForeground(Color.GRAY);
        
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(top, BorderLayout.NORTH);
        p.add(tabs, BorderLayout.CENTER);
        p.add(lblTotal, BorderLayout.EAST);
        
        header.add(p, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
    }
    
    private JButton criarBotaoFiltro(String texto, String filtroKey, Color corAtiva) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(UIConstants.BG_DARK_ALT);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60,60,60)),
            new EmptyBorder(10, 20, 10, 20)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            this.filtroAtual = filtroKey;
            // Visual simples de ativo
            ((JButton)e.getSource()).setBackground(corAtiva);
            carregarEntregas();
        });
        
        return btn;
    }

    private void initList() {
        containerCards = new JPanel();
        containerCards.setLayout(new BoxLayout(containerCards, BoxLayout.Y_AXIS));
        containerCards.setBackground(UIConstants.BG_DARK);
        containerCards.setBorder(new EmptyBorder(20, 40, 40, 40));

        JScrollPane scroll = new JScrollPane(containerCards);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);
    }

    public void carregarEntregas() {
        containerCards.removeAll();
        // Força recarregamento do DAO
        ArrayList<Pedidos> pedidos = dao.buscarTodosPedidos(); 
        int count = 0;

        for (Pedidos p : pedidos) {
            // Verifica status exato para a aba atual
            if ("Delivery".equalsIgnoreCase(p.getModoEntrega()) && 
                p.getStatusPedido().equalsIgnoreCase(filtroAtual)) {
                
                containerCards.add(criarCard(p));
                containerCards.add(Box.createVerticalStrut(15));
                count++;
            }
        }
        
        lblTotal.setText(count + " na lista");
        
        if (count == 0) {
            JLabel empty = new JLabel("Nenhuma entrega nesta etapa.");
            empty.setForeground(UIConstants.FG_MUTED);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            containerCards.add(Box.createVerticalStrut(50));
            containerCards.add(empty);
        }

        containerCards.revalidate();
        containerCards.repaint();
    }

    private JPanel criarCard(Pedidos p) {
        RoundedPanel card = new RoundedPanel(15, UIConstants.CARD_DARK);
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(15, 25, 15, 25));
        card.setMaximumSize(new Dimension(2000, 110)); // Estica a largura
        card.setPreferredSize(new Dimension(1000, 110));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- ESQUERDA: Identificação ---
        JPanel pLeft = new JPanel(new GridLayout(2, 1, 0, 5));
        pLeft.setOpaque(false);
        pLeft.setPreferredSize(new Dimension(200, 0));
        
        JLabel lId = new JLabel("#" + p.getIdPedido());
        lId.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lId.setForeground(UIConstants.PRIMARY_RED);
        
        JLabel lClient = new JLabel(p.getNomeCliente());
        lClient.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lClient.setForeground(UIConstants.FG_LIGHT);
        lClient.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.PERSON, 18, UIConstants.FG_MUTED));
        
        pLeft.add(lId); pLeft.add(lClient);
        
        // --- CENTRO: Endereço Grande ---
        JPanel pCenter = new JPanel(new BorderLayout());
        pCenter.setOpaque(false);
        
        JLabel lAddress = new JLabel(p.getEnderecoCompleto());
        lAddress.setFont(new Font("Segoe UI", Font.PLAIN, 18)); // Fonte maior
        lAddress.setForeground(Color.WHITE);
        lAddress.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.PLACE, 28, new Color(200, 80, 80)));
        lAddress.setIconTextGap(15);
        
        pCenter.add(lAddress, BorderLayout.CENTER);

        // --- DIREITA: Ação + Mapa ---
        JPanel pRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pRight.setOpaque(false);
        
        JButton btnMap = new JButton("Ver Mapa");
        btnMap.setBackground(new Color(60, 60, 60));
        btnMap.setForeground(Color.WHITE);
        btnMap.setFocusPainted(false);
        btnMap.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btnMap.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MAP, 18, Color.WHITE));
        
        JButton btnAction = new JButton();
        btnAction.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAction.setForeground(Color.WHITE);
        btnAction.setFocusPainted(false);
        btnAction.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        if(filtroAtual.equals("pronto")) {
            btnAction.setText("Enviar Moto");
            btnAction.setBackground(new Color(20, 100, 150));
            btnAction.addActionListener(e -> atribuirMotoboy(p));
        } else {
            btnAction.setText("Confirmar Entrega");
            btnAction.setBackground(UIConstants.SUCCESS_GREEN);
            btnAction.addActionListener(e -> finalizarEntrega(p));
        }
        
        pRight.add(btnMap);
        pRight.add(btnAction);

        card.add(pLeft, BorderLayout.WEST);
        card.add(pCenter, BorderLayout.CENTER);
        card.add(pRight, BorderLayout.EAST);

        return card;
    }
    
    private void atribuirMotoboy(Pedidos p) {
        String motoboy = JOptionPane.showInputDialog(this, "Atribuir ao Entregador:", "Expedição", JOptionPane.QUESTION_MESSAGE);
        if(motoboy != null && !motoboy.isEmpty()) {
            dao.atualizarStatusPedido(p.getIdPedido(), "em rota"); // Atualiza status
            JOptionPane.showMessageDialog(this, "Pedido #" + p.getIdPedido() + " em rota com " + motoboy);
            carregarEntregas();
        }
    }

    private void finalizarEntrega(Pedidos p) {
        int op = JOptionPane.showConfirmDialog(this, "Confirmar entrega realizada?", "Finalizar", JOptionPane.YES_NO_OPTION);
        if(op == JOptionPane.YES_OPTION) {
            dao.atualizarStatusPedido(p.getIdPedido(), "finalizado");
            carregarEntregas();
        }
    }
    
    private static class RoundedPanel extends JPanel {
        private int radius; private Color bgColor;
        public RoundedPanel(int radius, Color bgColor) { this.radius = radius; this.bgColor = bgColor; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
        }
    }
}