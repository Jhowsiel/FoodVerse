package com.senac.food.verse.gui;

import com.senac.food.verse.ItemPedido;
import com.senac.food.verse.PedidoDAO;
import com.senac.food.verse.Pedidos;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class GestaoCozinhaPanel extends JPanel {

    private final PedidoDAO dao = new PedidoDAO();
    private JPanel containerPedidos;
    private JLabel lblContador;
    private Timer timerAtualizacao;

    public GestaoCozinhaPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_DARK);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlTitle.setOpaque(false);
        JLabel title = new JLabel("Produção (KDS)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.FG_LIGHT);
        title.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.RESTAURANT, 28, UIConstants.FG_LIGHT));
        
        lblContador = new JLabel("0 pedidos na fila");
        lblContador.setFont(UIConstants.ARIAL_14_B);
        lblContador.setForeground(UIConstants.PRIMARY_RED);
        
        pnlTitle.add(title);
        pnlTitle.add(lblContador);
        header.add(pnlTitle, BorderLayout.WEST);

        JButton btnRefresh = createActionButton("Sincronizar", GoogleMaterialDesignIcons.SYNC, UIConstants.BG_DARK_ALT);
        btnRefresh.addActionListener(e -> carregarPedidosAsync(false));
        header.add(btnRefresh, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- GRID DE PEDIDOS ---
        containerPedidos = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        containerPedidos.setBackground(UIConstants.BG_DARK);

        JScrollPane scroll = new JScrollPane(containerPedidos);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        
        add(scroll, BorderLayout.CENTER);

        carregarPedidosAsync(false);
        iniciarRadarCozinha();
    }

    private void iniciarRadarCozinha() {
        // Atualiza a tela a cada 10 segundos silenciosamente
        timerAtualizacao = new Timer(10000, e -> carregarPedidosAsync(true));
        timerAtualizacao.start();
    }

    private void carregarPedidosAsync(boolean silencioso) {
        if(!silencioso) lblContador.setText("Atualizando...");
        
        new SwingWorker<List<Pedidos>, Void>() {
            @Override
            protected List<Pedidos> doInBackground() {
                // Força atualização
                if(!silencioso) dao.recarregarPedidos(); 
                
                ArrayList<Pedidos> todos = dao.buscarTodosPedidos();
                List<Pedidos> filaCozinha = new ArrayList<>();
                
                // Filtra apenas os pedidos que a cozinha precisa fazer AGORA
                for(Pedidos p : todos) {
                    if(p.getStatusPedido().equalsIgnoreCase("em preparo")) {
                        filaCozinha.add(p);
                    }
                }
                return filaCozinha;
            }

            @Override
            protected void done() {
                try {
                    List<Pedidos> pedidos = get();
                    containerPedidos.removeAll();
                    
                    if(pedidos.isEmpty()) {
                        lblContador.setText("Cozinha Limpa! (Nenhum pedido)");
                        lblContador.setForeground(UIConstants.SUCCESS_GREEN);
                    } else {
                        lblContador.setText(pedidos.size() + " pedidos na fila");
                        lblContador.setForeground(UIConstants.PRIMARY_RED);
                        
                        for (Pedidos p : pedidos) {
                            containerPedidos.add(criarCardPedidoReal(p));
                        }
                    }
                    containerPedidos.revalidate();
                    containerPedidos.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private JPanel criarCardPedidoReal(Pedidos pedido) {
        // Calcula atraso real com base na hora do pedido
        long minutosDecorridos = 0;
        try {
            LocalTime hp = LocalTime.parse(pedido.getHoraPedido(), DateTimeFormatter.ofPattern("HH:mm"));
            minutosDecorridos = ChronoUnit.MINUTES.between(hp, LocalTime.now());
            if(minutosDecorridos < 0) minutosDecorridos = 0;
        } catch (Exception e) {}
        
        boolean isAtrasado = minutosDecorridos > 25; // SLA da cozinha (25 min)
        
        RoundedPanel card = new RoundedPanel(15, UIConstants.CARD_DARK);
        card.setPreferredSize(new Dimension(300, 360));
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- TOPO DO CARD ---
        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setOpaque(false);
        
        JLabel lblId = new JLabel("#" + pedido.getIdPedido());
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblId.setForeground(UIConstants.FG_LIGHT);
        
        JLabel lblTempo = new JLabel(minutosDecorridos + " min");
        lblTempo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTempo.setForeground(isAtrasado ? UIConstants.PRIMARY_RED_ALT : UIConstants.SUCCESS_GREEN);
        lblTempo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCESS_TIME, 16, isAtrasado ? UIConstants.PRIMARY_RED_ALT : UIConstants.SUCCESS_GREEN));

        headerCard.add(lblId, BorderLayout.WEST);
        headerCard.add(lblTempo, BorderLayout.EAST);

        String local = pedido.getModoEntrega().equalsIgnoreCase("Delivery") ? "🛵 DELIVERY" : "🍽️ SALÃO (" + pedido.getMesa() + ")";
        JLabel lblOrigem = new JLabel(local);
        lblOrigem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblOrigem.setForeground(UIConstants.FG_MUTED);
        lblOrigem.setBorder(new EmptyBorder(5,0,15,0));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(headerCard, BorderLayout.NORTH);
        topContainer.add(lblOrigem, BorderLayout.CENTER);

        // --- LISTA DE ITENS ---
        JPanel painelItens = new JPanel();
        painelItens.setLayout(new BoxLayout(painelItens, BoxLayout.Y_AXIS));
        painelItens.setOpaque(false);
        
        if(pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                JLabel lblItem = new JLabel("<html><b>" + item.getQuantidade() + "x</b> " + item.getNomeProduto() + "</html>");
                lblItem.setForeground(Color.WHITE);
                lblItem.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                lblItem.setBorder(new EmptyBorder(0, 0, 8, 0));
                painelItens.add(lblItem);
            }
        }
        
        // --- OBSERVAÇÕES ---
        if(pedido.getObservacoes() != null && !pedido.getObservacoes().isEmpty()) {
            painelItens.add(Box.createVerticalStrut(10));
            JLabel lblObs = new JLabel("<html><font color='#FFA500'>⚠️ OBS:</font> " + pedido.getObservacoes() + "</html>");
            lblObs.setFont(UIConstants.ARIAL_12);
            painelItens.add(lblObs);
        }
        
        JScrollPane scrollItens = new JScrollPane(painelItens);
        scrollItens.setOpaque(false);
        scrollItens.getViewport().setOpaque(false);
        scrollItens.setBorder(null);

        // --- BOTÃO DE AÇÃO ---
        JButton btnAction = createActionButton("MARCAR COMO PRONTO", GoogleMaterialDesignIcons.CHECK, UIConstants.SUCCESS_GREEN);
        btnAction.setPreferredSize(new Dimension(0, 45));
        btnAction.addActionListener(e -> {
            btnAction.setEnabled(false);
            btnAction.setText("Processando...");
            dao.atualizarStatusPedido(pedido.getIdPedido(), "pronto");
            carregarPedidosAsync(true);
            UIConstants.showSuccess(this, "Pedido #" + pedido.getIdPedido() + " finalizado!");
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
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
        }
    }
}