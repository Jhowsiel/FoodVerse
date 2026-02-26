package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EntregasPainel extends JPanel {

    // --- CORES & ESTILO (Dark Mode Moderno) ---
    private final Color BG_DARK = new Color(30, 30, 30);
    private final Color BG_CARD = new Color(45, 45, 48);
    private final Color COLOR_PRONTO = new Color(241, 196, 15); // Amarelo
    private final Color COLOR_EM_ROTA = new Color(52, 152, 219); // Azul
    private final Color COLOR_FINALIZADO = new Color(39, 174, 96); // Verde
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    // Paineis das Colunas
    private JPanel containerProntos;
    private JPanel containerEmRota;
    private JLabel lblStatusConexao;

    public EntregasPainel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        // 1. Header
        add(criarHeader(), BorderLayout.NORTH);

        // 2. Kanban Board (Colunas)
        JPanel board = new JPanel(new GridLayout(1, 2, 20, 0));
        board.setBackground(BG_DARK);
        board.setBorder(new EmptyBorder(10, 20, 20, 20));

        containerProntos = criarColuna("Aguardando Motoboy", COLOR_PRONTO, GoogleMaterialDesignIcons.STORE_MALL_DIRECTORY);
        containerEmRota = criarColuna("Em Trânsito", COLOR_EM_ROTA, GoogleMaterialDesignIcons.DIRECTIONS_BIKE);

        board.add(containerProntos);
        board.add(containerEmRota);
        add(board, BorderLayout.CENTER);

        // Timer de Atualização Automática (5s)
        new Timer(5000, e -> carregarDados()).start();
        
        // Carga Inicial
        carregarDados();
    }

    private JPanel criarHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Logística & Entregas");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        title.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MAP, 28, Color.WHITE));
        title.setIconTextGap(15);
        p.add(title, BorderLayout.WEST);

        lblStatusConexao = new JLabel("Verificando...");
        lblStatusConexao.setFont(FONT_SMALL);
        p.add(lblStatusConexao, BorderLayout.EAST);

        return p;
    }

    private JPanel criarColuna(String titulo, Color cor, GoogleMaterialDesignIcons icone) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Cabeçalho da Coluna
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT));
        head.setBackground(BG_DARK);
        head.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cor));
        
        JLabel l = new JLabel(titulo);
        l.setFont(FONT_BOLD);
        l.setForeground(cor);
        l.setIcon(IconFontSwing.buildIcon(icone, 18, cor));
        head.add(l);
        panel.add(head, BorderLayout.NORTH);

        // Conteúdo Scrollável
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_DARK);
        
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scroll, BorderLayout.CENTER);
        
        // Hack para acessar o painel interno depois
        scroll.putClientProperty("content", content);
        
        return panel;
    }

    // --- CARREGAMENTO DE DADOS ---
    private void carregarDados() {
        JPanel panelProntos = (JPanel) ((JScrollPane) containerProntos.getComponent(1)).getClientProperty("content");
        JPanel panelRota = (JPanel) ((JScrollPane) containerEmRota.getComponent(1)).getClientProperty("content");

        panelProntos.removeAll();
        panelRota.removeAll();

        List<PedidoDeliveryDTO> pedidos = buscarPedidos();

        // Modo Offline / Fallback se lista vazia
        if (pedidos.isEmpty()) {
            lblStatusConexao.setText("● Modo Offline (Simulação)");
            lblStatusConexao.setForeground(new Color(230, 126, 34));
            
            // Dados Mockados Profissionais
            pedidos.add(new PedidoDeliveryDTO(1045, "Fernanda Costa", "Av. Paulista, 1000 - Bela Vista", "Pizza Gigante", "pronto", "João (Moto 01)"));
            pedidos.add(new PedidoDeliveryDTO(1046, "Roberto Almeida", "Rua Augusta, 500 - Consolação", "Burguer Duplo", "em rota", "Carlos (Moto 03)"));
            pedidos.add(new PedidoDeliveryDTO(1047, "Empresa Tech", "Rua Funchal, 200 - Vila Olímpia", "Combo Sushi", "em rota", "Ana (Moto 02)"));
        } else {
            lblStatusConexao.setText("● Online");
            lblStatusConexao.setForeground(COLOR_FINALIZADO);
        }

        for (PedidoDeliveryDTO p : pedidos) {
            if ("em rota".equalsIgnoreCase(p.status)) {
                panelRota.add(criarCard(p, COLOR_EM_ROTA));
                panelRota.add(Box.createVerticalStrut(15));
            } else {
                panelProntos.add(criarCard(p, COLOR_PRONTO));
                panelProntos.add(Box.createVerticalStrut(15));
            }
        }

        panelProntos.revalidate(); panelProntos.repaint();
        panelRota.revalidate(); panelRota.repaint();
    }

    private List<PedidoDeliveryDTO> buscarPedidos() {
        List<PedidoDeliveryDTO> lista = new ArrayList<>();
        String sql = "SELECT p.ID_pedido, c.name, p.status_pedido, p.tipo_pedido " + // Ajustar campos conforme seu banco real
                     "FROM tb_pedidos p LEFT JOIN tb_clientes c ON p.ID_cliente = c.UserId " +
                     "WHERE p.tipo_pedido = 'Delivery' AND p.status_pedido IN ('pronto', 'em rota')";
        
        ConexaoBanco cb = new ConexaoBanco();
        try {
            if (cb.conn == null) cb.conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false", "sa", "123456");
            PreparedStatement ps = cb.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                lista.add(new PedidoDeliveryDTO(
                    rs.getInt("ID_pedido"),
                    rs.getString("name"),
                    "Endereço do Cliente (BD)", // Substituir por coluna real de endereço se tiver
                    "Ver Detalhes",
                    rs.getString("status_pedido"),
                    "Entregador Padrão"
                ));
            }
        } catch (Exception e) { /* Ignora para usar fallback */ }
        return lista;
    }

    // --- CRIAÇÃO DOS CARDS (UI) ---
    private JPanel criarCard(PedidoDeliveryDTO p, Color accent) {
        JPanel card = new RoundedPanel(15, BG_CARD);
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(400, 160)); // Tamanho fixo para alinhar
        card.setPreferredSize(new Dimension(300, 160));
        card.setBorder(new EmptyBorder(12, 15, 12, 15));

        // 1. Topo: ID e Tempo
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lblId = new JLabel("Pedido #" + p.id);
        lblId.setFont(FONT_BOLD);
        lblId.setForeground(accent);
        top.add(lblId, BorderLayout.WEST);
        
        // 2. Meio: Cliente e Endereço
        JPanel mid = new JPanel(new GridLayout(3, 1, 0, 2));
        mid.setOpaque(false);
        mid.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JLabel lblCliente = new JLabel(p.cliente);
        lblCliente.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCliente.setForeground(Color.WHITE);
        
        JLabel lblEnd = new JLabel("<html>" + p.endereco + "</html>");
        lblEnd.setFont(FONT_SMALL);
        lblEnd.setForeground(Color.LIGHT_GRAY);

        JLabel lblMotoboy = new JLabel("Entregador: " + p.motoboy);
        lblMotoboy.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMotoboy.setForeground(new Color(150,150,150));
        
        mid.add(lblCliente);
        mid.add(lblEnd);
        mid.add(lblMotoboy);

        // 3. Bottom: Botões de Ação
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        bot.setOpaque(false);

        if ("em rota".equalsIgnoreCase(p.status)) {
            JButton btnRastrear = criarBotaoIcone(GoogleMaterialDesignIcons.MAP, new Color(70, 70, 200));
            btnRastrear.setToolTipText("Abrir Rastreamento GPS");
            btnRastrear.addActionListener(e -> abrirRastreamento(p));
            
            JButton btnCheck = criarBotaoIcone(GoogleMaterialDesignIcons.CHECK, COLOR_FINALIZADO);
            btnCheck.setToolTipText("Finalizar Entrega");
            btnCheck.addActionListener(e -> finalizarEntrega(p));
            
            bot.add(btnRastrear);
            bot.add(btnCheck);
        } else {
            JButton btnDespachar = criarBotaoTexto("DESPACHAR", GoogleMaterialDesignIcons.SEND, accent);
            btnDespachar.addActionListener(e -> atualizarStatus(p.id, "em rota"));
            bot.add(btnDespachar);
        }

        card.add(top, BorderLayout.NORTH);
        card.add(mid, BorderLayout.CENTER);
        card.add(bot, BorderLayout.SOUTH);

        return card;
    }

    // --- AÇÕES DO SISTEMA ---
    private void atualizarStatus(int id, String status) {
        // Lógica de update no banco (simulada se offline)
        String sql = "UPDATE tb_pedidos SET status_pedido = ? WHERE ID_pedido = ?";
        ConexaoBanco cb = new ConexaoBanco();
        try {
             if (cb.conn == null) cb.conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false", "sa", "123456");
             PreparedStatement ps = cb.conn.prepareStatement(sql);
             ps.setString(1, status);
             ps.setInt(2, id);
             ps.executeUpdate();
        } catch(Exception e) {
             JOptionPane.showMessageDialog(this, "(Simulação) Status alterado para: " + status);
        }
        carregarDados();
    }

    private void finalizarEntrega(PedidoDeliveryDTO p) {
        int opt = JOptionPane.showConfirmDialog(this, "O entregador confirmou a entrega?", "Finalizar", JOptionPane.YES_NO_OPTION);
        if(opt == JOptionPane.YES_OPTION) {
            atualizarStatus(p.id, "finalizado");
        }
    }

    // --- TELA DE RASTREAMENTO (GPS) ---
    private void abrirRastreamento(PedidoDeliveryDTO p) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Rastreamento em Tempo Real", true);
        d.setSize(700, 500);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(BG_DARK);

        // Painel Superior (Info)
        JPanel top = new JPanel(new GridLayout(1, 3));
        top.setBackground(BG_CARD);
        top.setBorder(new EmptyBorder(15, 20, 15, 20));
        top.add(criarInfoLabel("Tempo Est.", "12 min", COLOR_EM_ROTA));
        top.add(criarInfoLabel("Distância", "3.4 km", Color.WHITE));
        top.add(criarInfoLabel("Entregador", p.motoboy, Color.LIGHT_GRAY));
        d.add(top, BorderLayout.NORTH);

        // O MAPA ANIMADO
        GPSPanel mapa = new GPSPanel();
        d.add(mapa, BorderLayout.CENTER);

        // Painel Inferior (Ações Reais)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBackground(BG_DARK);
        bottom.setBorder(new EmptyBorder(10,0,10,0));

        JButton btnGoogle = criarBotaoTexto("ABRIR NO GOOGLE MAPS", GoogleMaterialDesignIcons.OPEN_IN_BROWSER, new Color(219, 68, 55));
        btnGoogle.addActionListener(e -> {
            try {
                // Abre o navegador padrão com o endereço
                String enderecoFormatado = p.endereco.replace(" ", "+");
                Desktop.getDesktop().browse(new URI("https://www.google.com/maps/search/?api=1&query=" + enderecoFormatado));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Erro ao abrir navegador: " + ex.getMessage());
            }
        });
        
        bottom.add(btnGoogle);
        d.add(bottom, BorderLayout.SOUTH);

        d.setVisible(true);
    }
    
    private JLabel criarInfoLabel(String titulo, String valor, Color corValor) {
        JLabel l = new JLabel("<html><center><span style='font-size:10px;color:gray'>"+titulo+"</span><br><span style='font-size:14px'>"+valor+"</span></center></html>");
        l.setForeground(corValor);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    // --- COMPONENTES AUXILIARES ---
    private JButton criarBotaoIcone(GoogleMaterialDesignIcons icon, Color bg) {
        JButton btn = new JButton(IconFontSwing.buildIcon(icon, 18, Color.WHITE));
        btn.setBackground(bg);
        btn.setPreferredSize(new Dimension(40, 35));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(5,5,5,5));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton criarBotaoTexto(String txt, GoogleMaterialDesignIcons icon, Color bg) {
        JButton btn = new JButton(txt);
        btn.setIcon(IconFontSwing.buildIcon(icon, 18, Color.WHITE));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- CLASSES INTERNAS ---

    // DTO Simples
    static class PedidoDeliveryDTO {
        int id; String cliente, endereco, detalhes, status, motoboy;
        public PedidoDeliveryDTO(int id, String c, String e, String d, String s, String m) {
            this.id=id; this.cliente=c; this.endereco=e; this.detalhes=d; this.status=s; this.motoboy=m;
        }
    }

    // Painel Arredondado
    static class RoundedPanel extends JPanel {
        private int r; private Color c;
        public RoundedPanel(int r, Color c) { this.r=r; this.c=c; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),r,r);
            super.paintComponent(g);
        }
    }

    // --- O NOVO MAPA ESTILO GPS ---
    static class GPSPanel extends JPanel {
        private float progress = 0;
        
        public GPSPanel() {
            setBackground(new Color(25, 25, 25));
            new Timer(50, e -> {
                progress += 0.005;
                if(progress > 1) progress = 0;
                repaint();
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth(); int h = getHeight();
            
            // 1. Ruas (Grid Estilizado)
            g2.setColor(new Color(40, 40, 40));
            g2.setStroke(new BasicStroke(2));
            for(int i=0; i<w; i+=60) g2.drawLine(i, 0, i, h); // Verticais
            for(int i=0; i<h; i+=60) g2.drawLine(0, i, w, i); // Horizontais
            
            // 2. Rota (Linha Curva Suave)
            Path2D path = new Path2D.Float();
            path.moveTo(50, h/2);
            path.curveTo(w/3, h/2 - 100, w/1.5, h/2 + 100, w-50, h/2);
            
            // Desenhar a linha da rota
            g2.setColor(new Color(60, 60, 200)); // Azul Escuro (Fundo da rota)
            g2.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(path);
            
            g2.setColor(new Color(100, 100, 255)); // Azul Claro (Meio da rota)
            g2.setStroke(new BasicStroke(4));
            g2.draw(path);
            
            // 3. Pontos (Restaurante e Casa)
            drawMarker(g2, 50, h/2, new Color(241, 196, 15), "R"); // Restaurante
            drawMarker(g2, w-50, h/2, new Color(46, 204, 113), "C"); // Cliente
            
            // 4. Motoboy Animado
            Point2D p = getPointOnPath(path, progress);
            if(p != null) {
                // Sombra/Pulse do Motoboy
                g2.setColor(new Color(52, 152, 219, 100));
                int pulse = (int)(Math.sin(progress * 50) * 5 + 15);
                g2.fillOval((int)p.getX()-pulse, (int)p.getY()-pulse, pulse*2, pulse*2);
                
                // Ponto do Motoboy
                g2.setColor(Color.WHITE);
                g2.fillOval((int)p.getX()-8, (int)p.getY()-8, 16, 16);
                g2.setColor(new Color(52, 152, 219));
                g2.setStroke(new BasicStroke(3));
                g2.drawOval((int)p.getX()-8, (int)p.getY()-8, 16, 16);
            }
        }
        
        private void drawMarker(Graphics2D g2, int x, int y, Color c, String txt) {
            g2.setColor(c);
            g2.fillOval(x-15, y-15, 30, 30);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString(txt, x-5, y+5);
        }
        
        // Matemática para pegar ponto na curva (Simplificada)
        private Point2D getPointOnPath(Path2D path, float t) {
            // Aproximação linear para fins visuais (pegar ponto exato de Bezier é complexo math puro)
            // Aqui usamos interpolação simples entre inicio e fim para o exemplo visual
            PathIterator pi = path.getPathIterator(null);
            double[] coords = new double[6];
            // Simplificando: Movimento linear visual sobre o eixo X com senoide no Y para simular a curva
            double x = 50 + (getWidth()-100) * t;
            // Recria a curva matemática usada no draw
            // curveTo(w/3, h/2 - 100, w/1.5, h/2 + 100, w-50, h/2);
            double w = getWidth(); double h = getHeight();
            // Bezier Cubica Fórmula: B(t) = (1-t)^3 P0 + 3(1-t)^2 t P1 + 3(1-t)t^2 P2 + t^3 P3
            double p0x = 50, p0y = h/2;
            double p1x = w/3, p1y = h/2 - 100;
            double p2x = w/1.5, p2y = h/2 + 100;
            double p3x = w-50, p3y = h/2;
            
            double bx = Math.pow(1-t,3)*p0x + 3*Math.pow(1-t,2)*t*p1x + 3*(1-t)*Math.pow(t,2)*p2x + Math.pow(t,3)*p3x;
            double by = Math.pow(1-t,3)*p0y + 3*Math.pow(1-t,2)*t*p1y + 3*(1-t)*Math.pow(t,2)*p2y + Math.pow(t,3)*p3y;
            
            return new Point2D.Double(bx, by);
        }
    }
}