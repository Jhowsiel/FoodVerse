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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class PedidosPanel extends JPanel {

    private final PedidoDAO dao = new PedidoDAO();
    
    // Componentes de Interface Principal
    private JPanel panelLista;
    private JPanel panelDetalhes;
    private JPanel containerCards;
    private JTextField txtBuscar;
    private JComboBox<String> cbFiltro;
    private JLabel lblContador;
    private JScrollPane scrollLista;
    
    // Controlo de Estado e Cache Local
    private Pedidos pedidoSelecionado = null;
    private Timer timerAtualizacao;
    private boolean operacaoEmAndamento = false; 
    private List<Pedidos> cachePedidos = new ArrayList<>(); // Cache para buscas instantâneas
    
    public PedidosPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);
        
        initLeftPanel();
        initRightPanel();
        
        carregarDadosAsync(false);
        iniciarMonitoramentoBackground();
    }
    
    public void criarMenuPedido() { carregarDadosAsync(false); }

    // =========================================================================
    // 1. FILA OPERACIONAL (LISTA ESQUERDA)
    // =========================================================================
    private void initLeftPanel() {
        panelLista = new JPanel(new BorderLayout());
        panelLista.setBackground(UIConstants.BG_DARK_ALT);
        panelLista.setPreferredSize(new Dimension(420, 0));
        panelLista.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(45, 45, 45)));
        
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(UIConstants.BG_DARK_ALT);
        header.setBorder(new EmptyBorder(20, 20, 15, 20));
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1.0;
        
        // Linha 1: Busca e Refresh
        JPanel pnlBusca = new JPanel(new BorderLayout(10, 0));
        pnlBusca.setOpaque(false);
        
        txtBuscar = new JTextField();
        UIConstants.styleField(txtBuscar);
        txtBuscar.setPreferredSize(new Dimension(200, 40));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar Cliente ou ID...");
        txtBuscar.putClientProperty("JTextField.leadingIcon", IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SEARCH, 20, UIConstants.FG_MUTED));
        
        txtBuscar.addKeyListener(new KeyAdapter() { 
            public void keyReleased(KeyEvent e) { renderizarListaLocal(); } 
        });
        
        JButton btnRefresh = new JButton();
        UIConstants.styleSecondary(btnRefresh);
        btnRefresh.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.REFRESH, 20, UIConstants.FG_LIGHT));
        btnRefresh.setToolTipText("Forçar Atualização");
        btnRefresh.setPreferredSize(new Dimension(45, 40));
        btnRefresh.addActionListener(e -> carregarDadosAsync(false));
        
        pnlBusca.add(txtBuscar, BorderLayout.CENTER);
        pnlBusca.add(btnRefresh, BorderLayout.EAST);
        header.add(pnlBusca, gc);
        
        gc.gridy++;
        gc.insets = new Insets(10, 0, 0, 0);
        
        // Linha 2: Filtros
        JPanel filters = new JPanel(new BorderLayout(10, 0));
        filters.setOpaque(false);
        
        cbFiltro = new JComboBox<>(new String[] { "Ativos (Cozinha/Fila)", "Todos", "Pendente", "Em preparo", "Pronto", "Em rota", "Delivery", "Salão" });
        UIConstants.styleCombo(cbFiltro);
        cbFiltro.setPreferredSize(new Dimension(160, 35));
        cbFiltro.addActionListener(e -> renderizarListaLocal());
        
        lblContador = new JLabel("A carregar...");
        lblContador.setForeground(UIConstants.FG_MUTED);
        lblContador.setFont(UIConstants.ARIAL_12_B);
        
        filters.add(cbFiltro, BorderLayout.CENTER);
        filters.add(lblContador, BorderLayout.EAST);
        header.add(filters, gc);
        
        panelLista.add(header, BorderLayout.NORTH);
        
        containerCards = new JPanel();
        containerCards.setLayout(new BoxLayout(containerCards, BoxLayout.Y_AXIS));
        containerCards.setBackground(UIConstants.BG_DARK_ALT);
        containerCards.setBorder(new EmptyBorder(5, 15, 10, 15));
        
        scrollLista = new JScrollPane(containerCards);
        UIConstants.styleScrollPane(scrollLista);
        scrollLista.setBorder(null);
        scrollLista.getVerticalScrollBar().setUnitIncrement(20);
        scrollLista.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        panelLista.add(scrollLista, BorderLayout.CENTER);
        add(panelLista, BorderLayout.WEST);
    }
    
    // =========================================================================
    // 2. PAINEL VAZIO (ESTADO INICIAL)
    // =========================================================================
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
        
        JLabel msg = new JLabel("Selecione um pedido para iniciar a operação");
        msg.setForeground(UIConstants.FG_MUTED);
        msg.setFont(UIConstants.FONT_TITLE);
        msg.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ASSIGNMENT, 80, UIConstants.FG_MUTED));
        msg.setHorizontalTextPosition(SwingConstants.CENTER);
        msg.setVerticalTextPosition(SwingConstants.BOTTOM);
        msg.setIconTextGap(20);
        
        empty.add(msg);
        panelDetalhes.add(empty);
        panelDetalhes.revalidate();
        panelDetalhes.repaint();
    }

    // =========================================================================
    // 3. LÓGICA DE ATUALIZAÇÃO E CACHE
    // =========================================================================
    private void iniciarMonitoramentoBackground() {
        timerAtualizacao = new Timer(5000, e -> {
            if (operacaoEmAndamento) return; 
            
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return dao.haNovoPedido() || dao.houveAlteracoesPedidos();
                }
                @Override
                protected void done() {
                    try { if (get()) carregarDadosAsync(true); } 
                    catch (Exception ex) { }
                }
            }.execute();
        });
        timerAtualizacao.start();
    }
    
    private void carregarDadosAsync(boolean silencioso) {
        if(!silencioso) lblContador.setText("Atualizando...");
        
        new SwingWorker<List<Pedidos>, Void>() {
            @Override
            protected List<Pedidos> doInBackground() {
                try {
                    dao.recarregarPedidos();
                    return dao.buscarTodosPedidos();
                } catch (Exception e) {
                    return cachePedidos; 
                }
            }
            @Override
            protected void done() {
                try {
                    cachePedidos = get();
                    renderizarListaLocal();
                } catch (Exception e) {}
            }
        }.execute();
    }
    
    private void renderizarListaLocal() {
        int scrollPos = scrollLista.getVerticalScrollBar().getValue();
        String termo = txtBuscar.getText().trim().toLowerCase().replace("#", "");
        String filtro = (String) cbFiltro.getSelectedItem();
        
        containerCards.removeAll();
        int count = 0;
        
        for (Pedidos p : cachePedidos) {
            boolean match = true;
            String status = p.getStatusPedido().toLowerCase();
            String tipo = p.getModoEntrega().toLowerCase();
            
            if (filtro.equals("Ativos (Cozinha/Fila)")) {
                if (status.equals("concluido") || status.equals("cancelado")) match = false;
            } else if (filtro.equals("Delivery")) {
                if (!tipo.contains("delivery")) match = false;
            } else if (filtro.equals("Salão")) {
                if (!tipo.contains("salão") && !tipo.contains("mesa") && !tipo.contains("local")) match = false;
            } else if (!filtro.equals("Todos")) {
                if (!status.equalsIgnoreCase(filtro)) match = false;
            }
            
            if (match && (termo.isEmpty() || p.getNomeCliente().toLowerCase().contains(termo) || p.getIdPedido().contains(termo))) {
                containerCards.add(criarCardMiniatura(p));
                containerCards.add(Box.createVerticalStrut(10));
                count++;
            }
        }
        
        lblContador.setText(count + " na fila");
        
        if (pedidoSelecionado != null) {
            boolean aindaExiste = cachePedidos.stream().anyMatch(p -> p.getIdPedido().equals(pedidoSelecionado.getIdPedido()));
            if(aindaExiste) {
                pedidoSelecionado = cachePedidos.stream().filter(p -> p.getIdPedido().equals(pedidoSelecionado.getIdPedido())).findFirst().get();
                carregarDetalhesFull(pedidoSelecionado);
            } else {
                pedidoSelecionado = null;
                mostrarEstadoVazio();
            }
        }
        
        containerCards.revalidate();
        containerCards.repaint();
        SwingUtilities.invokeLater(() -> scrollLista.getVerticalScrollBar().setValue(scrollPos));
    }
    
    // =========================================================================
    // 4. CARDS DE PEDIDO
    // =========================================================================
    private JPanel criarCardMiniatura(Pedidos p) {
        boolean isSelected = (pedidoSelecionado != null && pedidoSelecionado.getIdPedido().equals(p.getIdPedido()));
        Color bgColor = isSelected ? new Color(70, 30, 30) : UIConstants.CARD_DARK;
        
        UIConstants.RoundedPanel card = new UIConstants.RoundedPanel(12, bgColor);
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setMaximumSize(new Dimension(500, 110));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel strip = new JPanel();
        strip.setPreferredSize(new Dimension(5, 0));
        strip.setBackground(getColorStatus(p.getStatusPedido()));
        card.add(strip, BorderLayout.WEST);
        
        JPanel center = new JPanel(new GridLayout(3, 1, 0, 2));
        center.setOpaque(false);
        
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        JLabel lId = new JLabel("#" + p.getIdPedido() + " ");
        lId.setForeground(UIConstants.PRIMARY_RED);
        lId.setFont(UIConstants.ARIAL_14_B);
        JLabel lNome = new JLabel(p.getNomeCliente());
        lNome.setForeground(Color.WHITE);
        lNome.setFont(UIConstants.ARIAL_14_B);
        row1.add(lId, BorderLayout.WEST);
        row1.add(lNome, BorderLayout.CENTER);
        
        boolean isDelivery = p.getModoEntrega().equalsIgnoreCase("Delivery");
        String tipoStr = isDelivery ? "Delivery" : "Salão (Mesa " + p.getMesa() + ")";
        JLabel lTipo = new JLabel(tipoStr);
        lTipo.setIcon(IconFontSwing.buildIcon(isDelivery ? GoogleMaterialDesignIcons.MOTORCYCLE : GoogleMaterialDesignIcons.RESTAURANT, 14, UIConstants.FG_LIGHT));
        lTipo.setForeground(UIConstants.FG_LIGHT);
        lTipo.setFont(UIConstants.ARIAL_12);
        lTipo.setIconTextGap(8); // Afasta o texto do ícone sem bugar o HTML
        
        JLabel lTempo = new JLabel(calcularSLA(p.getHoraPedido(), p.getStatusPedido()));
        lTempo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCESS_TIME, 14, UIConstants.FG_MUTED));
        lTempo.setFont(UIConstants.ARIAL_12_B);
        lTempo.setIconTextGap(8); // Afasta o texto do ícone
        
        center.add(row1);
        center.add(lTipo);
        center.add(lTempo);
        card.add(center, BorderLayout.CENTER);
        
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { 
                pedidoSelecionado = p;
                renderizarListaLocal(); 
                carregarDetalhesFull(p); 
            }
            public void mouseEntered(MouseEvent e) { if(!isSelected) card.setBackground(UIConstants.BG_DARK_ALT.brighter()); card.repaint(); }
            public void mouseExited(MouseEvent e) { if(!isSelected) card.setBackground(UIConstants.CARD_DARK); card.repaint(); }
        });
        
        return card;
    }
    
    private String calcularSLA(String horaPedido, String status) {
        if(status.equalsIgnoreCase("concluido") || status.equalsIgnoreCase("cancelado")) {
            return "<html><font color='#888888'>Fechado</font></html>";
        }
        try {
            LocalTime hp = LocalTime.parse(horaPedido, DateTimeFormatter.ofPattern("HH:mm"));
            long min = ChronoUnit.MINUTES.between(hp, LocalTime.now());
            if(min < 0) min = 0; 
            
            if(min > 30) return "<html><font color='#ff4444'>ATRASADO (" + min + " min)</font></html>";
            else if (min > 15) return "<html><font color='#ffaa00'>Em espera (" + min + " min)</font></html>";
            else return "<html><font color='#00cc66'>No prazo (" + min + " min)</font></html>";
        } catch (Exception e) {
            return "<html><font color='#aaaaaa'>Às " + horaPedido + "</font></html>";
        }
    }
    
    // =========================================================================
    // 5. PAINEL DE DETALHES (DASHBOARD DO PEDIDO)
    // =========================================================================
    private void carregarDetalhesFull(Pedidos p) {
        panelDetalhes.removeAll();
        
        JPanel containerPrincipal = new JPanel(new BorderLayout(0, 20));
        containerPrincipal.setBackground(UIConstants.BG_DARK);
        containerPrincipal.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        // --- CABEÇALHO ---
        JPanel headerInfo = new JPanel(new BorderLayout());
        headerInfo.setOpaque(false);
        
        JPanel pnlTitulos = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlTitulos.setOpaque(false);
        JLabel titulo = new JLabel("Pedido #" + p.getIdPedido());
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        
        JLabel badgeStatus = new JLabel("  " + p.getStatusPedido().toUpperCase() + "  ");
        badgeStatus.setOpaque(true);
        badgeStatus.setBackground(getColorStatus(p.getStatusPedido()));
        badgeStatus.setForeground(Color.WHITE);
        badgeStatus.setFont(UIConstants.ARIAL_12_B);
        badgeStatus.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        pnlTitulos.add(titulo); pnlTitulos.add(badgeStatus);
        
        JPanel pnlTimeline = criarTimelineInteligente(p.getStatusPedido(), p.getModoEntrega());
        
        headerInfo.add(pnlTitulos, BorderLayout.WEST);
        headerInfo.add(pnlTimeline, BorderLayout.EAST);
        
        containerPrincipal.add(headerInfo, BorderLayout.NORTH);
        
        // --- INFORMAÇÕES VITAIS ---
        JPanel gridInfo = new JPanel(new GridLayout(1, 2, 20, 0)); 
        gridInfo.setOpaque(false);
        
        JPanel col1 = new JPanel(new BorderLayout(0, 15));
        col1.setOpaque(false);
        col1.add(criarCardInformacao("CLIENTE / CONTATO", p.getNomeCliente(), p.getTelefoneEntregador(), GoogleMaterialDesignIcons.PERSON), BorderLayout.NORTH);
        
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        col1.add(criarCardInformacao("CAIXA / FINANCEIRO", "Total: " + nf.format(p.getSubtotal()), "Pagamento: " + p.getFormaPagamento(), GoogleMaterialDesignIcons.ACCOUNT_BALANCE_WALLET), BorderLayout.CENTER);
        
        JPanel col2 = new JPanel(new BorderLayout(0, 15));
        col2.setOpaque(false);
        
        if (p.getModoEntrega().equalsIgnoreCase("Delivery")) {
            String nomeEntregador = (p.getNomeEntregador() != null && !p.getNomeEntregador().isEmpty()) ? p.getNomeEntregador() : "A aguardar atribuição";
            col2.add(criarCardInformacao("ENDEREÇO DE DESPACHO", p.getEnderecoCompleto(), "Entregador: " + nomeEntregador, GoogleMaterialDesignIcons.LOCAL_SHIPPING), BorderLayout.NORTH);
        } else {
            col2.add(criarCardInformacao("ATENDIMENTO LOCAL", "Mesa Selecionada: " + p.getMesa(), "Consumo no Salão", GoogleMaterialDesignIcons.RESTAURANT), BorderLayout.NORTH);
        }
        
        if (p.getObservacoes() != null && !p.getObservacoes().trim().isEmpty()) {
            UIConstants.RoundedPanel pnlObs = new UIConstants.RoundedPanel(12, new Color(80, 40, 20)); 
            pnlObs.setLayout(new BorderLayout(10, 10));
            pnlObs.setBorder(new EmptyBorder(15, 15, 15, 15));
            JLabel icoObs = new JLabel(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.WARNING, 24, Color.ORANGE));
            JLabel txtObs = new JLabel("<html><b>ALERTA COZINHA:</b><br>" + p.getObservacoes() + "</html>");
            txtObs.setForeground(Color.WHITE);
            txtObs.setFont(UIConstants.ARIAL_14);
            pnlObs.add(icoObs, BorderLayout.WEST);
            pnlObs.add(txtObs, BorderLayout.CENTER);
            col2.add(pnlObs, BorderLayout.CENTER);
        }
        
        gridInfo.add(col1);
        gridInfo.add(col2);
        
        // --- ITENS DE PRODUÇÃO ---
        JPanel panelItens = new JPanel(new BorderLayout());
        panelItens.setOpaque(false);
        JLabel titItens = new JLabel("Lista de Produção (Itens)");
        titItens.setFont(UIConstants.ARIAL_16_B);
        titItens.setForeground(UIConstants.FG_LIGHT);
        titItens.setBorder(new EmptyBorder(20, 0, 10, 0));
        panelItens.add(titItens, BorderLayout.NORTH);
        
        JPanel listItens = new JPanel();
        listItens.setLayout(new BoxLayout(listItens, BoxLayout.Y_AXIS));
        listItens.setBackground(UIConstants.CARD_DARK);
        listItens.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        if(p.getItens() != null) {
            for(ItemPedido item : p.getItens()){
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(2000, 35)); 
                
                JLabel lQtd = new JLabel(item.getQuantidade() + "x  ");
                lQtd.setForeground(UIConstants.PRIMARY_RED);
                lQtd.setFont(UIConstants.ARIAL_14_B);
                
                JLabel lNome = new JLabel(item.getNomeProduto());
                lNome.setForeground(Color.WHITE);
                lNome.setFont(UIConstants.ARIAL_14);
                
                JLabel lPreco = new JLabel(nf.format(item.getPreco()));
                lPreco.setForeground(UIConstants.FG_MUTED);
                lPreco.setFont(UIConstants.ARIAL_14);
                
                JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                left.setOpaque(false);
                left.add(lQtd); left.add(lNome);
                
                row.add(left, BorderLayout.WEST);
                row.add(lPreco, BorderLayout.EAST);
                
                listItens.add(row);
                listItens.add(Box.createVerticalStrut(10));
            }
        }
        
        UIConstants.RoundedPanel baseItens = new UIConstants.RoundedPanel(12, UIConstants.CARD_DARK);
        baseItens.setLayout(new BorderLayout());
        baseItens.add(listItens, BorderLayout.CENTER);
        panelItens.add(baseItens, BorderLayout.CENTER);
        
        JPanel centerContent = new JPanel(new BorderLayout(0, 20));
        centerContent.setOpaque(false);
        centerContent.add(gridInfo, BorderLayout.NORTH);
        centerContent.add(panelItens, BorderLayout.CENTER);
        
        JScrollPane scrollConteudo = new JScrollPane(centerContent);
        scrollConteudo.setBorder(null);
        scrollConteudo.setOpaque(false);
        scrollConteudo.getViewport().setOpaque(false);
        scrollConteudo.getVerticalScrollBar().setUnitIncrement(16);
        
        containerPrincipal.add(scrollConteudo, BorderLayout.CENTER);
        panelDetalhes.add(containerPrincipal, BorderLayout.CENTER);
        
        // --- RODAPÉ: IMPRESSÃO E FLUXO ---
        JPanel bottomActions = new JPanel(new BorderLayout());
        bottomActions.setBackground(UIConstants.BG_DARK_ALT);
        bottomActions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.GRID_DARK));
        bottomActions.setPreferredSize(new Dimension(0, 70));
        
        JPanel pnlPrint = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pnlPrint.setOpaque(false);
        
        JButton btnPrintCozinha = new JButton("Via Cozinha");
        UIConstants.styleSecondary(btnPrintCozinha);
        btnPrintCozinha.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.RECEIPT, 18, UIConstants.FG_LIGHT));
        btnPrintCozinha.addActionListener(e -> mostrarVisualizadorImpressao(p, true));
        
        JButton btnPrintCliente = new JButton("Via Cliente");
        UIConstants.styleSecondary(btnPrintCliente);
        btnPrintCliente.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.PRINT, 18, UIConstants.FG_LIGHT));
        btnPrintCliente.addActionListener(e -> mostrarVisualizadorImpressao(p, false));
        
        pnlPrint.add(btnPrintCozinha);
        pnlPrint.add(btnPrintCliente);
        
        // --- FLUXO DE BOTÕES (USANDO UICONSTANTS) ---
        JPanel pnlFluxo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlFluxo.setOpaque(false);
        
        String statusAtual = p.getStatusPedido().toLowerCase();
        
        if ("pendente".equals(statusAtual)) {
            JButton btnCancel = new JButton("RECUSAR");
            UIConstants.styleDanger(btnCancel);
            btnCancel.addActionListener(e -> confirmarProcessarStatus(p, "cancelado", "Deseja realmente cancelar este pedido?"));
            
            JButton btnAccept = new JButton("ENVIAR PARA COZINHA");
            UIConstants.stylePrimary(btnAccept);
            btnAccept.setPreferredSize(new Dimension(280, 45));
            btnAccept.addActionListener(e -> processarStatusAsync(p, "em preparo"));
            
            pnlFluxo.add(btnCancel); pnlFluxo.add(btnAccept);
            
        } else if ("em preparo".equals(statusAtual)) {
            JButton btnReady = new JButton("MARCAR COMO PRONTO");
            UIConstants.styleSuccess(btnReady);
            btnReady.setPreferredSize(new Dimension(300, 45));
            btnReady.addActionListener(e -> processarStatusAsync(p, "pronto"));
            pnlFluxo.add(btnReady);
            
        } else if ("pronto".equals(statusAtual)) {
            if(p.getModoEntrega().equalsIgnoreCase("Delivery")) {
                JButton btnDespachar = new JButton("DESPACHAR (ENTREGAR AO MOTOBOY)");
                UIConstants.stylePrimary(btnDespachar);
                btnDespachar.setPreferredSize(new Dimension(320, 45));
                btnDespachar.addActionListener(e -> solicitarEntregadorEDespachar(p));
                pnlFluxo.add(btnDespachar);
            } else {
                JButton btnFinish = new JButton("ENTREGAR NA MESA (FINALIZAR)");
                UIConstants.stylePrimary(btnFinish);
                btnFinish.setPreferredSize(new Dimension(280, 45));
                btnFinish.addActionListener(e -> processarStatusAsync(p, "concluido"));
                pnlFluxo.add(btnFinish);
            }
        } else if ("em rota".equals(statusAtual)) {
            JButton btnFinish = new JButton("FINALIZAR ENTREGA (CONCLUÍDO)");
            UIConstants.styleSuccess(btnFinish);
            btnFinish.setPreferredSize(new Dimension(280, 45));
            btnFinish.addActionListener(e -> processarStatusAsync(p, "concluido"));
            pnlFluxo.add(btnFinish);
        }
        
        bottomActions.add(pnlPrint, BorderLayout.WEST);
        bottomActions.add(pnlFluxo, BorderLayout.EAST);
        
        panelDetalhes.add(bottomActions, BorderLayout.SOUTH);
        panelDetalhes.revalidate();
        panelDetalhes.repaint();
    }
    
    private JPanel criarTimelineInteligente(String statusAtual, String modoEntrega) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnl.setOpaque(false);
        
        String s = statusAtual.toLowerCase();
        int step = 0;
        if(s.equals("pendente")) step = 1;
        else if(s.equals("em preparo")) step = 2;
        else if(s.equals("pronto")) step = 3;
        else if(s.equals("em rota")) step = 4;
        else if(s.equals("concluido")) step = 5;
        
        boolean isDelivery = modoEntrega.equalsIgnoreCase("Delivery");
        
        pnl.add(criarBadgeTimeline("1. Pendente", step >= 1));
        pnl.add(criarSetaTimeline());
        pnl.add(criarBadgeTimeline("2. Cozinha", step >= 2));
        pnl.add(criarSetaTimeline());
        pnl.add(criarBadgeTimeline("3. Pronto", step >= 3));
        
        if (isDelivery) {
            pnl.add(criarSetaTimeline());
            pnl.add(criarBadgeTimeline("4. Em Rota", step >= 4));
            pnl.add(criarSetaTimeline());
            pnl.add(criarBadgeTimeline("5. Entregue", step >= 5));
        } else {
            pnl.add(criarSetaTimeline());
            pnl.add(criarBadgeTimeline("4. Concluído", step >= 4));
        }
        
        return pnl;
    }
    
    private JLabel criarBadgeTimeline(String txt, boolean ativo) {
        JLabel l = new JLabel(txt);
        l.setFont(UIConstants.ARIAL_12_B);
        l.setForeground(ativo ? Color.WHITE : UIConstants.FG_MUTED);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ativo ? UIConstants.PRIMARY_RED : UIConstants.GRID_DARK, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        l.setOpaque(true);
        l.setBackground(ativo ? UIConstants.PRIMARY_RED : UIConstants.BG_DARK);
        return l;
    }
    
    private JLabel criarSetaTimeline() {
        JLabel l = new JLabel(">");
        l.setForeground(UIConstants.GRID_DARK);
        l.setFont(UIConstants.ARIAL_14_B);
        return l;
    }

    private JPanel criarCardInformacao(String titulo, String linha1, String linha2, GoogleMaterialDesignIcons icon) {
        UIConstants.RoundedPanel card = new UIConstants.RoundedPanel(12, UIConstants.CARD_DARK);
        card.setLayout(new BorderLayout(15, 5));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel icone = new JLabel(IconFontSwing.buildIcon(icon, 32, UIConstants.FG_MUTED));
        card.add(icone, BorderLayout.WEST);
        
        JPanel textos = new JPanel(new GridLayout(3, 1, 0, 2));
        textos.setOpaque(false);
        
        JLabel lblTit = new JLabel(titulo.toUpperCase());
        lblTit.setFont(UIConstants.ARIAL_12_B);
        lblTit.setForeground(UIConstants.PRIMARY_RED);
        
        JLabel lblL1 = new JLabel(linha1);
        lblL1.setFont(UIConstants.ARIAL_14_B);
        lblL1.setForeground(Color.WHITE);
        
        JLabel lblL2 = new JLabel(linha2);
        lblL2.setFont(UIConstants.ARIAL_12);
        lblL2.setForeground(UIConstants.FG_MUTED);
        
        textos.add(lblTit); textos.add(lblL1); textos.add(lblL2);
        card.add(textos, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // 6. MODAIS (IMPRESSÃO E DESPACHO COM UICONSTANTS)
    // =========================================================================
    
    private void mostrarVisualizadorImpressao(Pedidos p, boolean viaCozinha) {
        operacaoEmAndamento = true; 
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Visualizador de Impressão", true);
        dialog.setSize(400, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(UIConstants.BG_DARK);
        
        JTextArea txtCupom = new JTextArea();
        txtCupom.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtCupom.setBackground(new Color(255, 255, 240)); 
        txtCupom.setForeground(Color.BLACK);
        txtCupom.setEditable(false);
        txtCupom.setMargin(new Insets(20, 20, 20, 20));
        
        StringBuilder sb = new StringBuilder();
        sb.append("      RESTAURANTE FOODVERSE      \n");
        sb.append("=================================\n");
        sb.append("PEDIDO #").append(p.getIdPedido()).append("\n");
        sb.append("DATA: ").append(p.getHoraPedido()).append("\n");
        sb.append("TIPO: ").append(p.getModoEntrega().toUpperCase()).append("\n");
        if(p.getModoEntrega().equalsIgnoreCase("Salão")) sb.append("MESA: ").append(p.getMesa()).append("\n");
        sb.append("=================================\n");
        
        if (!viaCozinha) {
            sb.append("CLIENTE: ").append(p.getNomeCliente()).append("\n");
            if(p.getModoEntrega().equalsIgnoreCase("Delivery")) {
                sb.append("END: ").append(p.getEnderecoCompleto()).append("\n");
                sb.append("TEL: ").append(p.getTelefoneEntregador()).append("\n");
            }
            sb.append("=================================\n");
        }
        
        sb.append("QTD   ITEM\n");
        sb.append("---------------------------------\n");
        if(p.getItens() != null) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            for (ItemPedido item : p.getItens()) {
                sb.append(String.format("%-5d %s\n", item.getQuantidade(), item.getNomeProduto()));
                if(!viaCozinha) {
                    sb.append(String.format("      Valor: %s\n", nf.format(item.getPreco())));
                }
            }
        }
        
        if (p.getObservacoes() != null && !p.getObservacoes().isEmpty()) {
            sb.append("---------------------------------\n");
            sb.append("OBS: ").append(p.getObservacoes()).append("\n");
        }
        
        if (!viaCozinha) {
            sb.append("=================================\n");
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            sb.append("TOTAL: ").append(nf.format(p.getSubtotal())).append("\n");
            sb.append("PAGAMENTO: ").append(p.getFormaPagamento().toUpperCase()).append("\n");
            sb.append("=================================\n");
            sb.append("   Obrigado pela preferência!    \n");
        } else {
            sb.append("=================================\n");
            sb.append("   *** VIA DA PRODUÇÃO *** \n");
        }
        
        txtCupom.setText(sb.toString());
        
        JScrollPane scroll = new JScrollPane(txtCupom);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        dialog.add(scroll, BorderLayout.CENTER);
        
        JPanel pnlBotao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBotao.setBackground(UIConstants.BG_DARK);
        pnlBotao.setBorder(new EmptyBorder(10, 10, 20, 10));
        
        JButton btnImprimir = new JButton("ENVIAR PARA IMPRESSORA");
        UIConstants.stylePrimary(btnImprimir);
        btnImprimir.setPreferredSize(new Dimension(300, 45));
        btnImprimir.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.PRINT, 20, Color.WHITE));
        btnImprimir.addActionListener(ev -> {
            UIConstants.showSuccess(this, "Documento enviado para a impressora.");
            dialog.dispose();
        });
        
        pnlBotao.add(btnImprimir);
        dialog.add(pnlBotao, BorderLayout.SOUTH);
        
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) { operacaoEmAndamento = false; }
        });
        
        dialog.setVisible(true);
    }
    
    // --- LÓGICA DRY: UTILIZAÇÃO DA UICONSTANTS ---
    private void solicitarEntregadorEDespachar(Pedidos p) {
        operacaoEmAndamento = true;
        UIConstants.showInputDialog(
            this,
            "Atribuir Motoboy", 
            "Introduza o nome do entregador para despacho:", 
            (nome) -> {
                operacaoEmAndamento = false;
                if(nome != null && !nome.trim().isEmpty()) {
                    atualizarStatusEDespachoAsync(p, "em rota", nome);
                } else {
                    UIConstants.showWarning(this, "O nome do entregador é obrigatório.");
                }
            }
        );
    }
    
    // =========================================================================
    // 7. CONTROLO DE ESTADOS E BASE DE DADOS SEGURO
    // =========================================================================
    private Color getColorStatus(String status) {
        String s = status.toLowerCase();
        if(s.equals("pendente")) return new Color(243, 156, 18); 
        if(s.equals("em preparo")) return UIConstants.PRIMARY_RED; 
        if(s.equals("pronto")) return UIConstants.SUCCESS_GREEN; 
        if(s.equals("em rota")) return new Color(52, 152, 219); 
        if(s.equals("cancelado")) return UIConstants.DANGER_RED; 
        return UIConstants.FG_MUTED; 
    }
    
    // --- LÓGICA DRY: UTILIZAÇÃO DA UICONSTANTS ---
    private void confirmarProcessarStatus(Pedidos p, String novoStatus, String mensagem) {
        operacaoEmAndamento = true;
        UIConstants.showConfirmDialog(
            this,
            "Atenção", 
            mensagem, 
            () -> {
                operacaoEmAndamento = false;
                processarStatusAsync(p, novoStatus);
            }
        );
    }
    
    private void processarStatusAsync(Pedidos p, String novoStatus) {
        atualizarStatusEDespachoAsync(p, novoStatus, null);
    }
    
    private void atualizarStatusEDespachoAsync(Pedidos p, String novoStatus, String nomeEntregador) {
        panelDetalhes.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        operacaoEmAndamento = true;
        
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                ConexaoBanco banco = new ConexaoBanco();
                try (Connection conn = banco.abrirConexao()) {
                    if (conn == null) return false;
                    
                    int statusId = 1; 
                    try (PreparedStatement ps = conn.prepareStatement("SELECT status_id FROM tb_status_pedido WHERE status_nome = ?")) {
                        ps.setString(1, novoStatus);
                        try (var rs = ps.executeQuery()) {
                            if(rs.next()) statusId = rs.getInt(1);
                        }
                    }
                    
                    String sql = (nomeEntregador != null) 
                        ? "UPDATE tb_pedidos SET status_id = ?, nome_entregador = ? WHERE ID_pedido = ?"
                        : "UPDATE tb_pedidos SET status_id = ? WHERE ID_pedido = ?";
                        
                    try (PreparedStatement up = conn.prepareStatement(sql)) {
                        up.setInt(1, statusId);
                        if (nomeEntregador != null) {
                            up.setString(2, nomeEntregador);
                            up.setString(3, p.getIdPedido());
                        } else {
                            up.setString(2, p.getIdPedido());
                        }
                        up.executeUpdate();
                    }
                    
                    dao.atualizarStatusPedido(p.getIdPedido(), novoStatus);
                    return true;
                } catch (Exception ex) { 
                    return false; 
                }
            }

            @Override
            protected void done() {
                panelDetalhes.setCursor(Cursor.getDefaultCursor());
                operacaoEmAndamento = false;
                try {
                    if (get()) {
                        UIConstants.showSuccess(PedidosPanel.this, "Sucesso: Pedido movido para " + novoStatus.toUpperCase());
                        carregarDadosAsync(true); 
                    } else {
                        UIConstants.showError(PedidosPanel.this, "Erro ao atualizar. Verifique a conexão.");
                    }
                } catch (Exception e) {}
            }
        }.execute();
    }
}