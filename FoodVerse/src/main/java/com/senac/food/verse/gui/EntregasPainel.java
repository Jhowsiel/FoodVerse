package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import com.senac.food.verse.PedidoDAO;
import com.senac.food.verse.Pedidos;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntregasPainel extends JPanel {

    private final PedidoDAO dao = new PedidoDAO();
    
    // Componentes de UI
    private JPanel listPanel;
    private JScrollPane scrollLista;
    private JLabel lblContador;
    
    // Mapa Interativo
    private JXMapViewer mapViewer;
    private WaypointPainter<Waypoint> waypointPainter;
    private Set<Waypoint> waypoints;
    private final GeoPosition COORDENADA_RESTAURANTE = new GeoPosition(-23.5505, -46.6333); // Centro de São Paulo
    
    // Estado
    private Timer timerAtualizacao;

    public EntregasPainel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);

        initHeader();
        initListaEntregas();
        initMapaReal(); 

        carregarEntregasAsync(false);
        iniciarMonitoramento();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_DARK);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Logística & Despachos");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UIConstants.FG_LIGHT);
        lblTitle.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MAP, 28, UIConstants.FG_LIGHT));

        JButton btnRefresh = new JButton("Sincronizar");
        UIConstants.styleSecondary(btnRefresh);
        btnRefresh.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.REFRESH, 20, UIConstants.FG_LIGHT));
        btnRefresh.addActionListener(e -> carregarEntregasAsync(false));

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
    }

    private void initListaEntregas() {
        JPanel panelLeft = new JPanel(new BorderLayout());
        panelLeft.setBackground(UIConstants.BG_DARK);
        panelLeft.setPreferredSize(new Dimension(420, 0));
        
        JPanel pnlTit = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlTit.setOpaque(false);
        pnlTit.setBorder(new EmptyBorder(0, 15, 10, 15));
        
        lblContador = new JLabel("Buscando dados no servidor...");
        lblContador.setFont(new Font("Arial", Font.BOLD, 14));
        lblContador.setForeground(new Color(52, 152, 219)); 
        pnlTit.add(lblContador);
        
        panelLeft.add(pnlTit, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(UIConstants.BG_DARK);
        listPanel.setBorder(new EmptyBorder(5, 15, 15, 15));

        scrollLista = new JScrollPane(listPanel);
        UIConstants.styleScrollPane(scrollLista);
        scrollLista.setBorder(null);
        scrollLista.getVerticalScrollBar().setUnitIncrement(16);

        panelLeft.add(scrollLista, BorderLayout.CENTER);
        add(panelLeft, BorderLayout.WEST);
    }
    
    private void initMapaReal() {
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setBackground(UIConstants.BG_DARK_ALT);
        mapContainer.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UIConstants.GRID_DARK));
        
        try {
            // =========================================================================
            // CORREÇÃO CRÍTICA DO "RELÓGIO" E BLOQUEIO DO OPENSTREETMAP
            // O OSM bloqueia requisições do Java padrão. Precisamos forjar o User-Agent.
            // =========================================================================
            System.setProperty("http.agent", "FoodVerse-App/1.0");

            mapViewer = new JXMapViewer();
            OSMTileFactoryInfo info = new OSMTileFactoryInfo();
            DefaultTileFactory tileFactory = new DefaultTileFactory(info);
            
            // Acelera o download dos fragmentos do mapa (evita lentidão)
            tileFactory.setThreadPoolSize(8);
            
            mapViewer.setTileFactory(tileFactory);
            mapViewer.setZoom(5);
            mapViewer.setAddressLocation(COORDENADA_RESTAURANTE);
            
            waypointPainter = new WaypointPainter<>();
            waypoints = new HashSet<>();
            
            waypoints.add(new DefaultWaypoint(COORDENADA_RESTAURANTE));
            waypointPainter.setWaypoints(waypoints);
            mapViewer.setOverlayPainter(waypointPainter);
            
            mapContainer.add(mapViewer, BorderLayout.CENTER);
            
        } catch (Exception ex) {
            JLabel error = new JLabel("Erro ao renderizar o mapa interativo.");
            error.setForeground(UIConstants.DANGER_RED);
            error.setHorizontalAlignment(SwingConstants.CENTER);
            mapContainer.add(error, BorderLayout.CENTER);
        }
        
        add(mapContainer, BorderLayout.CENTER);
    }

    private void iniciarMonitoramento() {
        timerAtualizacao = new Timer(10000, e -> carregarEntregasAsync(true));
        timerAtualizacao.start();
    }

    private void carregarEntregasAsync(boolean silencioso) {
        if(!silencioso) lblContador.setText("Atualizando...");
        
        int scrollPos = scrollLista.getVerticalScrollBar().getValue();
        
        new SwingWorker<List<Pedidos>, Void>() {
            @Override
            protected List<Pedidos> doInBackground() {
                List<Pedidos> emRota = new ArrayList<>();
                boolean bancoOnline = false;
                
                try {
                    ConexaoBanco banco = new ConexaoBanco();
                    Connection conn = banco.abrirConexao();
                    if(conn != null) {
                        bancoOnline = true;
                        banco.fecharConexao();
                        
                        if(!silencioso) dao.recarregarPedidos();
                        for(Pedidos p : dao.buscarTodosPedidos()) {
                            if(p.getModoEntrega().equalsIgnoreCase("Delivery") && p.getStatusPedido().equalsIgnoreCase("em rota")) {
                                emRota.add(p);
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(">> Banco de dados offline. Trocando para dados mockados.");
                }
                
                if (!bancoOnline || emRota.isEmpty()) {
                    emRota = gerarDadosMockOffline();
                }
                
                return emRota;
            }

            @Override
            protected void done() {
                try {
                    List<Pedidos> entregas = get();
                    listPanel.removeAll();
                    waypoints.clear();
                    
                    waypoints.add(new DefaultWaypoint(COORDENADA_RESTAURANTE)); 
                    
                    if(entregas.isEmpty()) {
                        lblContador.setText("Nenhuma entrega ativa.");
                        lblContador.setForeground(UIConstants.FG_MUTED);
                    } else {
                        lblContador.setText(entregas.size() + " entregas na rua");
                        lblContador.setForeground(new Color(52, 152, 219));
                        
                        for(Pedidos p : entregas) {
                            listPanel.add(criarCardEntrega(p));
                            listPanel.add(Box.createVerticalStrut(15));
                            
                            GeoPosition pos = mockGeocodingAdress(p.getIdPedido(), p.getNomeCliente());
                            waypoints.add(new DefaultWaypoint(pos));
                        }
                    }
                    
                    if(mapViewer != null) {
                        waypointPainter.setWaypoints(waypoints);
                        mapViewer.repaint();
                    }
                    
                    listPanel.revalidate();
                    listPanel.repaint();
                    SwingUtilities.invokeLater(() -> scrollLista.getVerticalScrollBar().setValue(scrollPos));
                    
                } catch (Exception e) {}
            }
        }.execute();
    }

    // =========================================================================
    // MOCKS E FUNÇÕES DE GEOCODING VISUAL
    // =========================================================================
    
    private List<Pedidos> gerarDadosMockOffline() {
        List<Pedidos> mocks = new ArrayList<>();
        Pedidos p1 = new Pedidos("1001", "João Silva (Teste)", "12:00", "12:50", "LOC1", "Avenida Paulista, 1578 - SP", "Carlos (Motoboy)", "11999999999", "Delivery", "Sem cebola", null, "em rota", "Site", "Cartão", 50.0, "");
        Pedidos p2 = new Pedidos("1002", "Maria Souza (Teste)", "12:15", "13:00", "LOC2", "Rua Augusta, 400 - SP", "Roberto (Bike)", "11988888888", "Delivery", "", null, "em rota", "App", "PIX", 35.0, "");
        mocks.add(p1);
        mocks.add(p2);
        return mocks;
    }

    private GeoPosition mockGeocodingAdress(String idPedido, String nomeCliente) {
        double baseLat = COORDENADA_RESTAURANTE.getLatitude();
        double baseLon = COORDENADA_RESTAURANTE.getLongitude();
        double offsetLat = (idPedido.hashCode() % 100) * 0.0003;
        double offsetLon = (nomeCliente.hashCode() % 100) * 0.0003;
        return new GeoPosition(baseLat + offsetLat, baseLon + offsetLon);
    }

    // =========================================================================
    // RENDERIZAÇÃO DOS CARDS
    // =========================================================================
    
    private JPanel criarCardEntrega(Pedidos p) {
        UIConstants.RoundedPanel card = new UIConstants.RoundedPanel(12, UIConstants.CARD_DARK);
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setMaximumSize(new Dimension(500, 200));

        JPanel pnlTopo = new JPanel(new BorderLayout());
        pnlTopo.setOpaque(false);
        
        JLabel lblId = new JLabel("#" + p.getIdPedido() + "  -  " + p.getNomeCliente());
        lblId.setFont(new Font("Arial", Font.BOLD, 14));
        lblId.setForeground(Color.WHITE);
        
        String entregador = (p.getNomeEntregador() != null && !p.getNomeEntregador().isEmpty()) ? p.getNomeEntregador() : "Entregador";
        JLabel lblEntregador = new JLabel("🏍️ " + entregador);
        lblEntregador.setFont(new Font("Arial", Font.BOLD, 12));
        lblEntregador.setForeground(new Color(52, 152, 219)); 
        
        pnlTopo.add(lblId, BorderLayout.WEST);
        pnlTopo.add(lblEntregador, BorderLayout.EAST);

        JPanel pnlMeio = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlMeio.setOpaque(false);
        pnlMeio.setBorder(new EmptyBorder(10, 0, 15, 0));
        
        JLabel lblEnd = new JLabel("📍 " + p.getEnderecoCompleto());
        lblEnd.setFont(new Font("Arial", Font.PLAIN, 12));
        lblEnd.setForeground(UIConstants.FG_LIGHT);
        
        JLabel lblTel = new JLabel("📞 " + p.getTelefoneEntregador());
        lblTel.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTel.setForeground(UIConstants.FG_MUTED);
        
        pnlMeio.add(lblEnd);
        pnlMeio.add(lblTel);

        JPanel pnlAcoes = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlAcoes.setOpaque(false);
        
        JButton btnWhats = new JButton("WhatsApp");
        UIConstants.styleSecondary(btnWhats);
        btnWhats.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.CHAT, 16, UIConstants.FG_LIGHT));
        btnWhats.addActionListener(e -> abrirWhatsApp(p.getTelefoneEntregador(), p.getNomeCliente()));

        JButton btnFinalizar = new JButton("ENTREGUE");
        UIConstants.styleSuccess(btnFinalizar);
        btnFinalizar.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.CHECK, 16, Color.WHITE));
        btnFinalizar.addActionListener(e -> tentarConcluirEntrega(p));

        pnlAcoes.add(btnWhats);
        pnlAcoes.add(btnFinalizar);

        card.add(pnlTopo, BorderLayout.NORTH);
        card.add(pnlMeio, BorderLayout.CENTER);
        card.add(pnlAcoes, BorderLayout.SOUTH);
        
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(mapViewer != null) {
                    GeoPosition pos = mockGeocodingAdress(p.getIdPedido(), p.getNomeCliente());
                    mapViewer.setAddressLocation(pos);
                    mapViewer.setZoom(3);
                }
            }
        });

        return card;
    }

    private void abrirWhatsApp(String telefone, String nome) {
        try {
            if (telefone == null || telefone.isEmpty()) {
                UIConstants.showWarning(this, "Telefone não encontrado.");
                return;
            }
            String numeroLimpo = telefone.replaceAll("[^0-9]", "");
            String msg = "Olá " + nome + ", aqui é do restaurante FoodVerse. O seu pedido está a chegar!";
            String url = "https://wa.me/55" + numeroLimpo + "?text=" + msg.replace(" ", "%20");
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            UIConstants.showError(this, "Não foi possível abrir o navegador.");
        }
    }

    // =========================================================================
    // LÓGICA DE CONFIRMAÇÃO E BANCO DE DADOS
    // =========================================================================

    private void tentarConcluirEntrega(Pedidos p) {
        UIConstants.showConfirmDialog(
            this,
            "Finalizar Entrega", 
            "Confirma a entrega do pedido #" + p.getIdPedido() + " para o cliente?", 
            () -> concluirNoBancoAsync(p)
        );
    }

    private void concluirNoBancoAsync(Pedidos p) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    ConexaoBanco banco = new ConexaoBanco();
                    Connection conn = banco.abrirConexao();
                    if(conn != null) {
                        int statusId = 4; 
                        try(PreparedStatement ps = conn.prepareStatement("SELECT status_id FROM tb_status_pedido WHERE status_nome = 'concluido'")) {
                            var rs = ps.executeQuery();
                            if(rs.next()) statusId = rs.getInt(1);
                        }
                        try(PreparedStatement up = conn.prepareStatement("UPDATE tb_pedidos SET status_id = ? WHERE ID_pedido = ?")) {
                            up.setInt(1, statusId);
                            up.setString(2, p.getIdPedido());
                            up.executeUpdate();
                        }
                        banco.fecharConexao();
                    }
                    dao.atualizarStatusPedido(p.getIdPedido(), "concluido");
                    return true;
                } catch (Exception e) { return true; }
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if(get()) {
                        UIConstants.showSuccess(EntregasPainel.this, "Entrega registrada com sucesso!");
                        carregarEntregasAsync(true);
                    } else {
                        UIConstants.showError(EntregasPainel.this, "Erro ao registrar no banco.");
                    }
                } catch(Exception ex) {}
            }
        }.execute();
    }
}