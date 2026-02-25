package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import com.senac.food.verse.PedidoDAO;
import com.senac.food.verse.Pedidos; // Certifique-se que esta classe existe ou use o DTO interno
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntregasPainel extends JPanel {

    // Cores e Estilos
    private final Color BG_DARK = new Color(30, 30, 30);
    private final Color COLOR_PRONTO = new Color(241, 196, 15); // Amarelo
    private final Color COLOR_ROTA = new Color(52, 152, 219);   // Azul
    private final Color COLOR_FINALIZADO = new Color(46, 204, 113); // Verde
    
    // Componentes
    private JPanel colunaProntos;
    private JPanel colunaEmRota;
    private JLabel lblStatusConexao;

    public EntregasPainel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        // Header
        add(criarHeader(), BorderLayout.NORTH);

        // Corpo (Kanban com 2 colunas)
        JPanel board = new JPanel(new GridLayout(1, 2, 20, 0));
        board.setBackground(BG_DARK);
        board.setBorder(new EmptyBorder(10, 20, 20, 20));

        colunaProntos = criarColuna("Aguardando Entregador", COLOR_PRONTO, GoogleMaterialDesignIcons.STORE);
        colunaEmRota = criarColuna("Em Rota de Entrega", COLOR_ROTA, GoogleMaterialDesignIcons.STORE);

        board.add(colunaProntos);
        board.add(colunaEmRota);
        add(board, BorderLayout.CENTER);

        // Timer para auto-atualização (Simula tempo real)
        new Timer(5000, e -> carregarDados()).start();
        
        // Carga inicial
        carregarDados();
    }

    private JPanel criarHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Central de Logística");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MAP, 32, Color.WHITE));
        p.add(title, BorderLayout.WEST);

        lblStatusConexao = new JLabel("Verificando conexão...");
        lblStatusConexao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatusConexao.setForeground(Color.GRAY);
        p.add(lblStatusConexao, BorderLayout.EAST);

        return p;
    }

    private JPanel criarColuna(String titulo, Color corTopo, GoogleMaterialDesignIcons icone) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new LineBorder(new Color(50,50,50), 1, true));

        // Cabeçalho da Coluna
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT));
        head.setBackground(new Color(40,40,40));
        head.setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, corTopo));
        
        JLabel l = new JLabel(titulo);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(Color.WHITE);
        l.setIcon(IconFontSwing.buildIcon(icone, 18, corTopo));
        head.add(l);
        p.add(head, BorderLayout.NORTH);

        // Área de Cards (Scroll)
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(35,35,35));
        
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(35,35,35));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        p.add(scroll, BorderLayout.CENTER);
        
        // Guardar referência do painel de conteúdo no JScrollPane para acesso posterior
        scroll.putClientProperty("contentPanel", content);
        
        return p;
    }

    // --- CARREGAMENTO DE DADOS (COM FALLBACK OFFLINE) ---
    private void carregarDados() {
        JPanel panelProntos = (JPanel) ((JScrollPane) colunaProntos.getComponent(1)).getClientProperty("contentPanel");
        JPanel panelRota = (JPanel) ((JScrollPane) colunaEmRota.getComponent(1)).getClientProperty("contentPanel");

        panelProntos.removeAll();
        panelRota.removeAll();

        List<PedidoDeliveryDTO> pedidos = buscarPedidosDoBanco();

        // Se banco falhar ou estiver vazio, carrega MOCK DATA (Modo Offline)
        if (pedidos.isEmpty()) {
            lblStatusConexao.setText("● Modo Offline (Simulação)");
            lblStatusConexao.setForeground(new Color(230, 126, 34)); // Laranja
            
            // Gerar dados falsos para teste de UI
            pedidos.add(new PedidoDeliveryDTO(101, "Ana Silva", "Rua das Flores, 123", "Pizza G", "pronto"));
            pedidos.add(new PedidoDeliveryDTO(102, "Carlos Souza", "Av. Paulista, 900", "Hambúrguer", "pronto"));
            pedidos.add(new PedidoDeliveryDTO(103, "Marcos Dev", "Rua Java, 8", "Sushi Combo", "em rota"));
        } else {
            lblStatusConexao.setText("● Conectado");
            lblStatusConexao.setForeground(new Color(46, 204, 113)); // Verde
        }

        for (PedidoDeliveryDTO p : pedidos) {
            if ("pronto".equalsIgnoreCase(p.status) || "pendente".equalsIgnoreCase(p.status)) {
                panelProntos.add(criarCard(p, COLOR_PRONTO));
                panelProntos.add(Box.createVerticalStrut(10));
            } else if ("em rota".equalsIgnoreCase(p.status)) {
                panelRota.add(criarCard(p, COLOR_ROTA));
                panelRota.add(Box.createVerticalStrut(10));
            }
        }

        panelProntos.revalidate(); panelProntos.repaint();
        panelRota.revalidate(); panelRota.repaint();
    }

    private List<PedidoDeliveryDTO> buscarPedidosDoBanco() {
        List<PedidoDeliveryDTO> lista = new ArrayList<>();
        // SQL Adaptado para sua estrutura
        String sql = "SELECT p.ID_pedido, c.name, p.status_pedido " +
                     "FROM tb_pedidos p " +
                     "LEFT JOIN tb_clientes c ON p.ID_cliente = c.UserId " +
                     "WHERE p.tipo_pedido = 'Delivery' AND p.status_pedido IN ('pronto', 'em rota')";
        
        ConexaoBanco cb = new ConexaoBanco();
        try {
             if (cb.conn == null) cb.conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false", "sa", "123456");
             PreparedStatement ps = cb.conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             while(rs.next()){
                 // Endereço mockado pois não temos na tabela pedidos ainda, ajuste conforme necessidade
                 lista.add(new PedidoDeliveryDTO(
                     rs.getInt("ID_pedido"),
                     rs.getString("name"),
                     "Endereço Cadastrado", 
                     "Ver Detalhes",
                     rs.getString("status_pedido")
                 ));
             }
        } catch (Exception e) {
            // Silencioso para ativar o modo offline no método carregarDados
        }
        return lista;
    }

    // --- CRIAÇÃO DOS CARDS ---
    private JPanel criarCard(PedidoDeliveryDTO p, Color accentColor) {
        JPanel card = new RoundedPanel(15, new Color(50, 50, 55));
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(350, 140));
        card.setPreferredSize(new Dimension(300, 140));
        card.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Header do Card
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblId = new JLabel("#" + p.id);
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblId.setForeground(accentColor);
        
        JLabel lblTempo = new JLabel("~25 min");
        lblTempo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTempo.setForeground(Color.GRAY);
        
        header.add(lblId, BorderLayout.WEST);
        header.add(lblTempo, BorderLayout.EAST);
        
        // Corpo do Card
        JPanel body = new JPanel(new GridLayout(2, 1));
        body.setOpaque(false);
        JLabel lblNome = new JLabel(p.cliente);
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNome.setForeground(Color.WHITE);
        
        JLabel lblEnd = new JLabel("<html>"+p.endereco+"</html>");
        lblEnd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEnd.setForeground(Color.LIGHT_GRAY);
        
        body.add(lblNome);
        body.add(lblEnd);

        // Footer (Botões)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        footer.setOpaque(false);

        if ("pronto".equalsIgnoreCase(p.status) || "pendente".equalsIgnoreCase(p.status)) {
            JButton btnEnviar = criarBotaoPequeno(GoogleMaterialDesignIcons.LOCATION_ON, COLOR_ROTA);
            btnEnviar.setToolTipText("Iniciar Entrega");
            btnEnviar.addActionListener(e -> atualizarStatusPedido(p.id, "em rota"));
            footer.add(btnEnviar);
        } else {
            // Botão Mapa
            JButton btnMapa = criarBotaoPequeno(GoogleMaterialDesignIcons.MAP, new Color(100, 100, 255));
            btnMapa.setToolTipText("Ver Rota no Mapa");
            btnMapa.addActionListener(e -> abrirSimuladorMapa(p));
            
            // Botão Finalizar
            JButton btnFim = criarBotaoPequeno(GoogleMaterialDesignIcons.CHECK, COLOR_FINALIZADO);
            btnFim.setToolTipText("Confirmar Entrega");
            btnFim.addActionListener(e -> {
                if(JOptionPane.showConfirmDialog(this, "Confirmar entrega?", "Fim", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    atualizarStatusPedido(p.id, "finalizado");
                }
            });
            
            footer.add(btnMapa);
            footer.add(btnFim);
        }

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        
        return card;
    }

    private JButton criarBotaoPequeno(GoogleMaterialDesignIcons icon, Color bg) {
        JButton btn = new JButton(IconFontSwing.buildIcon(icon, 16, Color.WHITE));
        btn.setBackground(bg);
        btn.setPreferredSize(new Dimension(35, 30));
        btn.setFocusPainted(false);
        btn.setBorder(null);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- AÇÕES ---
    private void atualizarStatusPedido(int id, String novoStatus) {
        // Tenta atualizar no banco, se falhar (modo offline), atualiza na memória visualmente
        String sql = "UPDATE tb_pedidos SET status_pedido = ? WHERE ID_pedido = ?";
        ConexaoBanco cb = new ConexaoBanco();
        try {
            if (cb.conn == null) cb.conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false", "sa", "123456");
            PreparedStatement ps = cb.conn.prepareStatement(sql);
            ps.setString(1, novoStatus);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            if(rows == 0) throw new Exception("Offline");
        } catch (Exception e) {
            // Simulação Visual
            JOptionPane.showMessageDialog(this, "(Simulação) Status do pedido #" + id + " alterado para: " + novoStatus);
        }
        carregarDados(); // Recarrega interface
    }

    // --- O GRANDE DIFERENCIAL: SIMULADOR DE MAPA ---
    private void abrirSimuladorMapa(PedidoDeliveryDTO p) {
        JDialog mapDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Rastreamento em Tempo Real - Pedido #" + p.id, true);
        mapDialog.setSize(600, 450);
        mapDialog.setLocationRelativeTo(this);
        
        // Painel do Mapa Customizado
        JPanel mapPanel = new MapPanel(p.cliente);
        mapDialog.add(mapPanel);
        
        mapDialog.setVisible(true);
    }

    /**
     * Classe interna que desenha um "Mapa" vetorial usando Java 2D.
     * Desenha ruas, o restaurante, o cliente e a rota do motoboy.
     */
    private static class MapPanel extends JPanel {
        private String clienteNome;
        private Timer animationTimer;
        private float progress = 0.0f; // 0.0 a 1.0 (Progresso do motoboy)

        public MapPanel(String clienteNome) {
            this.clienteNome = clienteNome;
            setBackground(new Color(30, 30, 30));
            // Animação do motoboy
            animationTimer = new Timer(50, e -> {
                progress += 0.01f;
                if (progress > 1.0f) progress = 0.0f; // Loop
                repaint();
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Desenhar Grid (Ruas)
            g2.setColor(new Color(50, 50, 50));
            for (int i = 0; i < w; i += 40) g2.drawLine(i, 0, i, h);
            for (int i = 0; i < h; i += 40) g2.drawLine(0, i, w, i);

            // 2. Definir Pontos
            int xRest = 50, yRest = 50;
            int xCli = w - 80, yCli = h - 80;

            // 3. Desenhar Rota (Linha Tracejada)
            g2.setColor(new Color(100, 100, 255));
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, new float[]{10}, 0));
            // Caminho simples estilo "L" (Manhattan distance)
            Path2D path = new Path2D.Float();
            path.moveTo(xRest, yRest);
            path.lineTo(xCli, yRest); // Vai reto horizontal
            path.lineTo(xCli, yCli);  // Desce vertical
            g2.draw(path);

            // 4. Desenhar Ícones (Simulados com Formas)
            
            // Restaurante
            g2.setColor(new Color(230, 126, 34)); // Laranja
            g2.fill(new RoundRectangle2D.Double(xRest-15, yRest-15, 30, 30, 10, 10));
            g2.setColor(Color.WHITE);
            g2.drawString("R", xRest-4, yRest+5);
            g2.drawString("Restaurante", xRest-20, yRest-20);

            // Cliente
            g2.setColor(new Color(46, 204, 113)); // Verde
            g2.fill(new Ellipse2D.Double(xCli-15, yCli-15, 30, 30));
            g2.setColor(Color.WHITE);
            g2.drawString("C", xCli-4, yCli+5);
            g2.drawString(clienteNome, xCli-30, yCli+25);

            // 5. Motoboy Animado
            // Calcular posição atual baseada no progresso ao longo do "L"
            double currentX, currentY;
            if (progress < 0.5) {
                // Primeira perna (Horizontal)
                currentX = xRest + ((xCli - xRest) * (progress * 2));
                currentY = yRest;
            } else {
                // Segunda perna (Vertical)
                currentX = xCli;
                currentY = yRest + ((yCli - yRest) * ((progress - 0.5) * 2));
            }

            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Double(currentX-8, currentY-8, 16, 16));
            g2.setColor(new Color(52, 152, 219)); // Azul aura
            g2.setStroke(new BasicStroke(2));
            g2.draw(new Ellipse2D.Double(currentX-8, currentY-8, 16, 16));
            
            // Info HUD
            g2.setColor(new Color(0,0,0,150));
            g2.fillRoundRect(10, h-60, 200, 50, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString("Status: Em Rota", 20, h-40);
            g2.drawString("Velocidade: 45 km/h", 20, h-20);
        }
    }

    // --- DTO INTERNO (Data Transfer Object) ---
    private static class PedidoDeliveryDTO {
        int id;
        String cliente;
        String endereco;
        String detalhes;
        String status;

        public PedidoDeliveryDTO(int id, String cliente, String endereco, String detalhes, String status) {
            this.id = id;
            this.cliente = cliente;
            this.endereco = endereco;
            this.detalhes = detalhes;
            this.status = status;
        }
    }

    // --- COMPONENTE VISUAL ARREDONDADO ---
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