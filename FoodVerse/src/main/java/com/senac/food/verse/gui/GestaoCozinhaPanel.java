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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GestaoCozinhaPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(GestaoCozinhaPanel.class.getName());
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
        title.setFont(UIConstants.FONT_TITLE);
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
        containerPedidos = new JPanel(createPedidosLayout());
        containerPedidos.setBackground(UIConstants.BG_DARK);

        JScrollPane scroll = new JScrollPane(containerPedidos);
        UIConstants.styleScrollPane(scroll);
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
                dao.recarregarPedidos();
                
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
                        lblContador.setText("Cozinha em dia");
                        lblContador.setForeground(UIConstants.SUCCESS_GREEN);
                        renderizarEstadoVazio();
                    } else {
                        containerPedidos.setLayout(createPedidosLayout());
                        lblContador.setText(pedidos.size() + " pedidos na fila");
                        lblContador.setForeground(UIConstants.PRIMARY_RED);
                        
                        for (Pedidos p : pedidos) {
                            containerPedidos.add(criarCardPedidoReal(p));
                        }
                    }
                    containerPedidos.revalidate();
                    containerPedidos.repaint();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Falha ao sincronizar pedidos da cozinha.", e);
                    UIConstants.showError(GestaoCozinhaPanel.this, "Não foi possível sincronizar a cozinha.");
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
        
        UIConstants.RoundedPanel pedidoCard = new UIConstants.RoundedPanel(15, UIConstants.CARD_DARK);
        pedidoCard.setPreferredSize(new Dimension(300, 360));
        pedidoCard.setLayout(new BorderLayout());
        pedidoCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- TOPO DO CARD ---
        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setOpaque(false);
        
        JLabel lblId = new JLabel("#" + pedido.getIdPedido());
        lblId.setFont(UIConstants.ARIAL_18_B);
        lblId.setForeground(UIConstants.FG_LIGHT);
        
        JLabel lblTempo = new JLabel(minutosDecorridos + " min");
        lblTempo.setFont(UIConstants.ARIAL_14_B);
        lblTempo.setForeground(isAtrasado ? UIConstants.PRIMARY_RED_ALT : UIConstants.SUCCESS_GREEN);
        lblTempo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCESS_TIME, 16, isAtrasado ? UIConstants.PRIMARY_RED_ALT : UIConstants.SUCCESS_GREEN));

        headerCard.add(lblId, BorderLayout.WEST);
        headerCard.add(lblTempo, BorderLayout.EAST);

        boolean delivery = pedido.getModoEntrega().equalsIgnoreCase("Delivery");
        JLabel lblOrigem = new JLabel(buildKitchenOriginText(pedido.getModoEntrega(), pedido.getMesa()));
        lblOrigem.setFont(UIConstants.ARIAL_12_B);
        lblOrigem.setForeground(UIConstants.FG_MUTED);
        lblOrigem.setIcon(IconFontSwing.buildIcon(
                delivery ? GoogleMaterialDesignIcons.MOTORCYCLE : GoogleMaterialDesignIcons.RESTAURANT,
                14,
                UIConstants.FG_MUTED));
        lblOrigem.setIconTextGap(8);
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
                lblItem.setForeground(UIConstants.FG_LIGHT);
                lblItem.setFont(UIConstants.FONT_REGULAR_15);
                lblItem.setBorder(new EmptyBorder(0, 0, 8, 0));
                painelItens.add(lblItem);
            }
        }
        
        // --- OBSERVAÇÕES ---
        if(pedido.getObservacoes() != null && !pedido.getObservacoes().isEmpty()) {
            painelItens.add(Box.createVerticalStrut(10));
            JLabel lblObs = new JLabel(buildKitchenObservationMarkup(pedido.getObservacoes()));
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

        pedidoCard.add(topContainer, BorderLayout.NORTH);
        pedidoCard.add(scrollItens, BorderLayout.CENTER);
        pedidoCard.add(btnAction, BorderLayout.SOUTH);

        return pedidoCard;
    }

    private JButton createActionButton(String text, GoogleMaterialDesignIcons icon, Color bg) {
        JButton btn = new JButton(text);
        btn.setIcon(IconFontSwing.buildIcon(icon, 18, UIConstants.SEL_FG));
        btn.setFont(UIConstants.ARIAL_12_B);
        btn.setForeground(UIConstants.SEL_FG);
        btn.setBackground(bg);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        return btn;
    }

    private void renderizarEstadoVazio() {
        containerPedidos.setLayout(new GridBagLayout());

        JPanel empty = new JPanel();
        empty.setOpaque(false);
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel icon = new JLabel(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.KITCHEN, 64, UIConstants.FG_MUTED));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titulo = new JLabel("Nenhum pedido em preparo");
        titulo.setFont(UIConstants.FONT_SECTION);
        titulo.setForeground(UIConstants.FG_LIGHT);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descricao = new JLabel("Assim que novos pedidos entrarem na cozinha, eles aparecerão aqui.");
        descricao.setFont(UIConstants.FONT_REGULAR);
        descricao.setForeground(UIConstants.FG_MUTED);
        descricao.setAlignmentX(Component.CENTER_ALIGNMENT);

        empty.add(icon);
        empty.add(Box.createVerticalStrut(18));
        empty.add(titulo);
        empty.add(Box.createVerticalStrut(8));
        empty.add(descricao);

        containerPedidos.add(empty);
    }

    static String buildKitchenOriginText(String modoEntrega, String mesa) {
        if (modoEntrega != null && modoEntrega.equalsIgnoreCase("Delivery")) {
            return "Delivery";
        }
        if (mesa == null || mesa.isBlank()) {
            return "Salão";
        }
        return "Salão (" + mesa + ")";
    }

    static String buildKitchenObservationMarkup(String observacao) {
        return "<html><font color='" + UIConstants.toHex(UIConstants.WARNING_ORANGE) + "'><b>OBS:</b></font> "
                + observacao + "</html>";
    }

    private static FlowLayout createPedidosLayout() {
        return new FlowLayout(FlowLayout.LEFT, 20, 20);
    }
}
