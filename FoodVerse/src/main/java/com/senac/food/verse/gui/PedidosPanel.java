package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import com.senac.food.verse.ItemPedido;
import com.senac.food.verse.PedidoDAO;
import com.senac.food.verse.Pedidos;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PedidosPanel extends JPanel {

    private final PedidoDAO dao = new PedidoDAO();
    
    // UI Components
    private JPanel panelLista;
    private JPanel panelDetalhes;
    private JPanel containerCards;
    private JTextField txtBuscar;
    private JComboBox<String> cbFiltro;
    private JLabel lblContador;
    
    public PedidosPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);
        
        initLeftPanel();
        initRightPanel();
        
        recarregarListaPedidos();
        
        // Timer de atualização
        new Timer(5000, e -> {
            if (dao.haNovoPedido() || dao.houveAlteracoesPedidos()) {
                recarregarListaPedidos();
            }
        }).start();
    }
    
    public void criarMenuPedido() { recarregarListaPedidos(); }

    // --- PAINEL ESQUERDO (LISTA) ---
    private void initLeftPanel() {
        panelLista = new JPanel(new BorderLayout());
        panelLista.setBackground(UIConstants.BG_DARK_ALT);
        panelLista.setPreferredSize(new Dimension(380, 0));
        panelLista.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(45, 45, 45)));
        
        // Header Lista
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(UIConstants.BG_DARK_ALT);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1.0;
        
        txtBuscar = new JTextField();
        UIConstants.styleField(txtBuscar);
        txtBuscar.setPreferredSize(new Dimension(200, 45));
        txtBuscar.putClientProperty("JTextField.placeholderText", "🔍 Buscar pedido...");
        txtBuscar.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { filtrarPedidos(); } });
        header.add(txtBuscar, gc);
        
        gc.gridy++;
        gc.insets = new Insets(10, 0, 0, 0);
        
        JPanel filters = new JPanel(new BorderLayout(10, 0));
        filters.setOpaque(false);
        
        cbFiltro = new JComboBox<>(new String[] { "Todos", "Pendente", "Em preparo", "Pronto", "Delivery", "Salão" });
        UIConstants.styleCombo(cbFiltro);
        cbFiltro.setPreferredSize(new Dimension(150, 40));
        cbFiltro.addActionListener(e -> filtrarPedidos());
        
        lblContador = new JLabel("0");
        lblContador.setForeground(UIConstants.FG_MUTED);
        lblContador.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        filters.add(cbFiltro, BorderLayout.CENTER);
        filters.add(lblContador, BorderLayout.EAST);
        header.add(filters, gc);
        
        panelLista.add(header, BorderLayout.NORTH);
        
        // Lista Cards
        containerCards = new JPanel();
        containerCards.setLayout(new BoxLayout(containerCards, BoxLayout.Y_AXIS));
        containerCards.setBackground(UIConstants.BG_DARK_ALT);
        containerCards.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JScrollPane scroll = new JScrollPane(containerCards);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getViewport().setBackground(UIConstants.BG_DARK_ALT);
        
        panelLista.add(scroll, BorderLayout.CENTER);
        add(panelLista, BorderLayout.WEST);
    }
    
    // --- PAINEL DIREITO (DETALHES - EXPANDIDO) ---
    private void initRightPanel() {
        panelDetalhes = new JPanel(new BorderLayout());
        panelDetalhes.setBackground(UIConstants.BG_DARK);
        mostrarEstadoVazio();
        add(panelDetalhes, BorderLayout.CENTER);
    }
    
    private void mostrarEstadoVazio() {
        panelDetalhes.removeAll();
        JPanel empty = new JPanel(new GridBagLayout());
        empty.setBackground(UIConstants.BG_DARK);
        
        JLabel msg = new JLabel("Selecione um pedido para gerenciar");
        msg.setForeground(new Color(60, 60, 60));
        msg.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        // --- CORREÇÃO DO ÍCONE AQUI: Mudado para TOUCH_APP ---
        msg.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.TOUCH_APP, 60, new Color(60,60,60)));
        
        msg.setHorizontalTextPosition(SwingConstants.CENTER);
        msg.setVerticalTextPosition(SwingConstants.BOTTOM);
        
        empty.add(msg);
        panelDetalhes.add(empty);
        panelDetalhes.revalidate();
        panelDetalhes.repaint();
    }
    
    // --- LÓGICA ---
    private void recarregarListaPedidos() { filtrarPedidos(); }
    
    private void filtrarPedidos() {
        String termo = txtBuscar.getText().trim().toLowerCase().replace("#", "");
        String status = (String) cbFiltro.getSelectedItem();
        
        ArrayList<Pedidos> todos = dao.buscarTodosPedidos();
        containerCards.removeAll();
        
        int count = 0;
        for (Pedidos p : todos) {
            boolean match = true;
            if (status.equals("Delivery")) {
                if (!p.getModoEntrega().equalsIgnoreCase("Delivery")) match = false;
            } else if (status.equals("Salão")) {
                if (!p.getModoEntrega().equalsIgnoreCase("Salão") && !p.getModoEntrega().equalsIgnoreCase("No Local")) match = false;
            } else if (!status.equals("Todos")) {
                if (!p.getStatusPedido().equalsIgnoreCase(status)) match = false;
            }
            
            if (match && (termo.isEmpty() || p.getNomeCliente().toLowerCase().contains(termo) || p.getIdPedido().contains(termo))) {
                containerCards.add(criarCardPedido(p));
                containerCards.add(Box.createVerticalStrut(10));
                count++;
            }
        }
        lblContador.setText(count + " pedidos");
        containerCards.revalidate();
        containerCards.repaint();
    }
    
    private JPanel criarCardPedido(Pedidos p) {
        RoundedPanel card = new RoundedPanel(15, UIConstants.CARD_DARK);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setMaximumSize(new Dimension(400, 100));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Status Strip
        JPanel strip = new JPanel();
        strip.setPreferredSize(new Dimension(5, 0));
        strip.setBackground(getColorStatus(p.getStatusPedido()));
        card.add(strip, BorderLayout.WEST);
        
        // Centro
        JPanel center = new JPanel(new GridLayout(2, 1));
        center.setOpaque(false);
        
        JLabel lNome = new JLabel(p.getNomeCliente());
        lNome.setForeground(Color.WHITE);
        lNome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row2.setOpaque(false);
        JLabel lId = new JLabel("#" + p.getIdPedido() + "  •  ");
        lId.setForeground(UIConstants.PRIMARY_RED);
        JLabel lTime = new JLabel(p.getHoraPedido());
        lTime.setForeground(Color.GRAY);
        row2.add(lId); row2.add(lTime);
        
        center.add(lNome);
        center.add(row2);
        card.add(center, BorderLayout.CENTER);
        
        // Icon
        GoogleMaterialDesignIcons ic = p.getModoEntrega().equalsIgnoreCase("Delivery") ? 
            GoogleMaterialDesignIcons.MOTORCYCLE : GoogleMaterialDesignIcons.STORE;
        JLabel icon = new JLabel(IconFontSwing.buildIcon(ic, 24, new Color(80, 80, 80)));
        card.add(icon, BorderLayout.EAST);
        
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { carregarDetalhesFull(p); }
            public void mouseEntered(MouseEvent e) { card.bgColor = new Color(65,65,65); card.repaint(); }
            public void mouseExited(MouseEvent e) { card.bgColor = UIConstants.CARD_DARK; card.repaint(); }
        });
        
        return card;
    }
    
    // --- DETALHES (LAYOUT EXPANDIDO) ---
    private void carregarDetalhesFull(Pedidos p) {
        panelDetalhes.removeAll();
        
        // 1. TOPO (Header Info)
        JPanel topInfo = new JPanel(new GridLayout(1, 3, 20, 0));
        topInfo.setBackground(UIConstants.BG_DARK);
        topInfo.setBorder(new EmptyBorder(30, 40, 20, 40));
        
        topInfo.add(criarBigInfoBlock("PEDIDO", "#" + p.getIdPedido(), GoogleMaterialDesignIcons.RECEIPT, getColorStatus(p.getStatusPedido())));
        topInfo.add(criarBigInfoBlock("CLIENTE", p.getNomeCliente(), GoogleMaterialDesignIcons.PERSON, Color.WHITE));
        
        String tipo = p.getModoEntrega();
        if(tipo.equalsIgnoreCase("Delivery")) tipo = "Delivery"; 
        topInfo.add(criarBigInfoBlock("TIPO", tipo, 
                tipo.equals("Delivery") ? GoogleMaterialDesignIcons.MOTORCYCLE : GoogleMaterialDesignIcons.STORE, Color.WHITE));
        
        panelDetalhes.add(topInfo, BorderLayout.NORTH);
        
        // 2. CENTRO (Lista de Itens + Endereço)
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setBackground(UIConstants.BG_DARK);
        centerPanel.setBorder(new EmptyBorder(0, 40, 20, 40));
        
        // Endereço (se delivery)
        if(p.getModoEntrega().equalsIgnoreCase("Delivery")) {
            JLabel lblEnd = new JLabel("📍  " + p.getEnderecoCompleto());
            lblEnd.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblEnd.setForeground(Color.LIGHT_GRAY);
            lblEnd.setBorder(new EmptyBorder(0, 0, 15, 0));
            centerPanel.add(lblEnd, BorderLayout.NORTH);
        }
        
        // Tabela de Itens (Manual com Panels para design)
        JPanel itemsList = new JPanel();
        itemsList.setLayout(new BoxLayout(itemsList, BoxLayout.Y_AXIS));
        itemsList.setBackground(UIConstants.CARD_DARK);
        itemsList.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        
        if(p.getItens() != null) {
            for(ItemPedido item : p.getItens()){
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(2000, 40)); 
                
                JLabel lQtd = new JLabel(item.getQuantidade() + "x   ");
                lQtd.setForeground(UIConstants.PRIMARY_RED);
                lQtd.setFont(new Font("Segoe UI", Font.BOLD, 16));
                
                JLabel lNome = new JLabel(item.getNomeProduto());
                lNome.setForeground(Color.WHITE);
                lNome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                
                JLabel lPreco = new JLabel(nf.format(item.getPreco()));
                lPreco.setForeground(Color.LIGHT_GRAY);
                lPreco.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                
                JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                left.setOpaque(false);
                left.add(lQtd); left.add(lNome);
                
                row.add(left, BorderLayout.WEST);
                row.add(lPreco, BorderLayout.EAST);
                
                itemsList.add(row);
                
                JPanel sep = new JPanel(); sep.setBackground(new Color(80,80,80));
                sep.setMaximumSize(new Dimension(2000, 1));
                itemsList.add(Box.createVerticalStrut(10));
                itemsList.add(sep);
                itemsList.add(Box.createVerticalStrut(10));
            }
        }
        
        // Área de Total e Pagamento
        JPanel footerInfo = new JPanel(new BorderLayout());
        footerInfo.setOpaque(false);
        
        JLabel lPag = new JLabel("Pagamento: " + p.getFormaPagamento());
        lPag.setForeground(Color.GRAY);
        lPag.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        
        JLabel lTotal = new JLabel("TOTAL: " + nf.format(p.getSubtotal()));
        lTotal.setForeground(UIConstants.SUCCESS_GREEN);
        lTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        footerInfo.add(lPag, BorderLayout.WEST);
        footerInfo.add(lTotal, BorderLayout.EAST);
        
        itemsList.add(Box.createVerticalStrut(20));
        itemsList.add(footerInfo);
        
        JScrollPane scrollItens = new JScrollPane(itemsList);
        scrollItens.setBorder(BorderFactory.createLineBorder(new Color(60,60,60)));
        scrollItens.getVerticalScrollBar().setUnitIncrement(16);
        centerPanel.add(scrollItens, BorderLayout.CENTER);
        
        panelDetalhes.add(centerPanel, BorderLayout.CENTER);
        
        // 3. BASE (Ações Grandes)
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottomActions.setBackground(UIConstants.BG_DARK_ALT);
        bottomActions.setPreferredSize(new Dimension(0, 80));
        bottomActions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60,60,60)));
        
        JButton btnPrint = criarBotaoAcao("Imprimir", new Color(50, 50, 50));
        btnPrint.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.PRINT, 20, Color.WHITE));
        
        bottomActions.add(btnPrint);
        
        if ("pendente".equalsIgnoreCase(p.getStatusPedido())) {
            JButton btnCancel = criarBotaoAcao("Recusar", new Color(150, 50, 50));
            btnCancel.addActionListener(e -> atualizarStatus(p, "cancelado"));
            
            JButton btnAccept = criarBotaoAcao("ACEITAR PEDIDO", UIConstants.PRIMARY_RED);
            btnAccept.setPreferredSize(new Dimension(200, 50));
            btnAccept.addActionListener(e -> atualizarStatus(p, "em preparo"));
            
            bottomActions.add(btnCancel);
            bottomActions.add(btnAccept);
        } else if ("em preparo".equalsIgnoreCase(p.getStatusPedido())) {
            JButton btnReady = criarBotaoAcao("MARCAR PRONTO", UIConstants.SUCCESS_GREEN);
            btnReady.setPreferredSize(new Dimension(200, 50));
            btnReady.addActionListener(e -> atualizarStatus(p, "pronto"));
            bottomActions.add(btnReady);
        }
        
        panelDetalhes.add(bottomActions, BorderLayout.SOUTH);
        
        panelDetalhes.revalidate();
        panelDetalhes.repaint();
    }
    
    private JPanel criarBigInfoBlock(String titulo, String valor, GoogleMaterialDesignIcons icon, Color accent) {
        RoundedPanel p = new RoundedPanel(15, UIConstants.CARD_DARK);
        p.setLayout(new BorderLayout(15, 0));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel lIcon = new JLabel(IconFontSwing.buildIcon(icon, 36, accent));
        
        JPanel txts = new JPanel(new GridLayout(2, 1));
        txts.setOpaque(false);
        JLabel lTit = new JLabel(titulo);
        lTit.setForeground(Color.GRAY);
        lTit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel lVal = new JLabel(valor);
        lVal.setForeground(Color.WHITE);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        txts.add(lTit); txts.add(lVal);
        
        p.add(lIcon, BorderLayout.WEST);
        p.add(txts, BorderLayout.CENTER);
        return p;
    }
    
    private JButton criarBotaoAcao(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private Color getColorStatus(String status) {
        if(status.equalsIgnoreCase("pendente")) return new Color(220, 160, 0);
        if(status.equalsIgnoreCase("em preparo")) return UIConstants.PRIMARY_RED;
        if(status.equalsIgnoreCase("pronto")) return UIConstants.SUCCESS_GREEN;
        return Color.GRAY;
    }
    
    private void atualizarStatus(Pedidos p, String novo) {
        // Atualiza banco real se houver
        ConexaoBanco banco = new ConexaoBanco();
        try {
            Connection conn = banco.abrirConexao();
            if (conn != null) {
                int statusId = 1; 
                PreparedStatement ps = conn.prepareStatement("SELECT status_id FROM tb_status_pedido WHERE status_nome = ?");
                ps.setString(1, novo);
                var rs = ps.executeQuery();
                if(rs.next()) statusId = rs.getInt(1);
                PreparedStatement up = conn.prepareStatement("UPDATE tb_pedidos SET status_id = ? WHERE ID_pedido = ?");
                up.setInt(1, statusId); up.setString(2, p.getIdPedido()); up.executeUpdate();
            }
        } catch(Exception e) {} finally { banco.fecharConexao(); }
        
        // Atualiza Cache
        dao.atualizarStatusPedido(p.getIdPedido(), novo);
        
        JOptionPane.showMessageDialog(this, "Status alterado para: " + novo.toUpperCase());
        recarregarListaPedidos();
        carregarDetalhesFull(dao.buscarPedidoPorId(p.getIdPedido()));
    }

    private static class RoundedPanel extends JPanel {
        private int radius; public Color bgColor;
        public RoundedPanel(int radius, Color bgColor) { this.radius = radius; this.bgColor = bgColor; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
        }
    }
}