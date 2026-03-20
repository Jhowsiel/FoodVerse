package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import com.senac.food.verse.PedidoDAO;
import com.senac.food.verse.Pedidos;
import com.senac.food.verse.SessionContext;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.sql.Connection;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EntregasPainel extends JPanel {

    private final PedidoDAO dao = new PedidoDAO();
    
    // Componentes de UI
    private JPanel panelLista;
    private JPanel panelDetalhes;
    private JPanel containerCards;
    private JLabel lblContador;
    private JTextField txtBuscar;
    private JScrollPane scrollLista;
    
    // Estado e Cache (Previne travamentos na busca e na atualização)
    private Timer timerAtualizacao;
    private Pedidos entregaSelecionada = null;
    private boolean operacaoEmAndamento = false;
    private List<Pedidos> cacheEntregas = new ArrayList<>(); // Memória rápida

    public EntregasPainel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);

        initLeftPanel();
        initRightPanel();

        carregarEntregasAsync(false);
        iniciarMonitoramento();
    }

    // =========================================================================
    // 1. PAINEL ESQUERDO: FILA DE DESPACHO E BUSCA
    // =========================================================================
    private void initLeftPanel() {
        panelLista = new JPanel(new BorderLayout());
        panelLista.setBackground(UIConstants.BG_DARK_ALT);
        panelLista.setPreferredSize(new Dimension(420, 0));
        panelLista.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.GRID_DARK));

        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(UIConstants.BG_DARK_ALT);
        header.setBorder(new EmptyBorder(20, 20, 15, 20));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1.0;

        // Título e Refresh
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("Em Rota (Frota)");
        lblTitle.setFont(UIConstants.FONT_SECTION);
        lblTitle.setForeground(UIConstants.FG_LIGHT);
        lblTitle.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MOTORCYCLE, 28, UIConstants.FG_LIGHT));

        JButton btnRefresh = new JButton();
        UIConstants.styleSecondary(btnRefresh);
        btnRefresh.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.REFRESH, 20, UIConstants.FG_LIGHT));
        btnRefresh.setPreferredSize(new Dimension(45, 40));
        btnRefresh.addActionListener(e -> carregarEntregasAsync(false));

        pnlTitle.add(lblTitle, BorderLayout.WEST);
        pnlTitle.add(btnRefresh, BorderLayout.EAST);
        header.add(pnlTitle, gc);

        // Barra de Busca Instantânea (Filtro Local)
        gc.gridy++;
        gc.insets = new Insets(15, 0, 10, 0);
        txtBuscar = new JTextField();
        UIConstants.styleField(txtBuscar);
        txtBuscar.setPreferredSize(new Dimension(0, 40));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar Cliente, ID ou Motoboy...");
        txtBuscar.putClientProperty("JTextField.leadingIcon", IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SEARCH, 20, UIConstants.FG_MUTED));
        txtBuscar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                renderizarListaLocal(); 
            }
        });
        header.add(txtBuscar, gc);

        // Contador
        gc.gridy++;
        gc.insets = new Insets(0, 0, 0, 0);
        lblContador = new JLabel("A carregar...");
        lblContador.setFont(UIConstants.ARIAL_12_B);
        lblContador.setForeground(UIConstants.INFO_BLUE); 
        header.add(lblContador, gc);

        panelLista.add(header, BorderLayout.NORTH);

        // Container de Cards
        containerCards = new JPanel();
        containerCards.setLayout(new BoxLayout(containerCards, BoxLayout.Y_AXIS));
        containerCards.setBackground(UIConstants.BG_DARK_ALT);
        containerCards.setBorder(new EmptyBorder(5, 15, 15, 15));

        scrollLista = new JScrollPane(containerCards);
        UIConstants.styleScrollPane(scrollLista);
        scrollLista.setBorder(null);
        scrollLista.getVerticalScrollBar().setUnitIncrement(20);

        panelLista.add(scrollLista, BorderLayout.CENTER);
        add(panelLista, BorderLayout.WEST);
    }

    // =========================================================================
    // 2. PAINEL DIREITO: DETALHES VAZIOS
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

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MAP, 80, UIConstants.FG_MUTED));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("Selecione uma entrega em andamento");
        msg.setForeground(UIConstants.FG_LIGHT);
        msg.setFont(UIConstants.FONT_TITLE);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Use o painel para copiar endereço, trocar motoboy ou concluir a entrega.");
        hint.setForeground(UIConstants.FG_MUTED);
        hint.setFont(UIConstants.FONT_REGULAR);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(icon);
        content.add(Box.createVerticalStrut(18));
        content.add(msg);
        content.add(Box.createVerticalStrut(8));
        content.add(hint);

        empty.add(content);
        panelDetalhes.add(empty);
        panelDetalhes.revalidate();
        panelDetalhes.repaint();
    }

    // =========================================================================
    // 3. LÓGICA DE DADOS (ASSÍNCRONA E PROTEGIDA)
    // =========================================================================
    private void iniciarMonitoramento() {
        timerAtualizacao = new Timer(10000, e -> {
            if (!operacaoEmAndamento) carregarEntregasAsync(true);
        });
        timerAtualizacao.start();
    }

    private void carregarEntregasAsync(boolean silencioso) {
        if(!silencioso) lblContador.setText("Sincronizando com a frota...");
        
        new SwingWorker<List<Pedidos>, Void>() {
            @Override
            protected List<Pedidos> doInBackground() {
                List<Pedidos> emRota = new ArrayList<>();
                try {
                    ConexaoBanco banco = new ConexaoBanco();
                    Connection conn = banco.abrirConexao();
                    if(conn != null) {
                        banco.fecharConexao();
                        dao.recarregarPedidos();
                        
                        // Filtra apenas os que estão em rota
                        for(Pedidos p : dao.buscarTodosPedidos()) {
                            if(p.getModoEntrega().equalsIgnoreCase("Delivery") && p.getStatusPedido().equalsIgnoreCase("em rota")) {
                                emRota.add(p);
                            }
                        }
                    } else {
                        throw new Exception("Offline");
                    }
                } catch (Exception ex) {
                    emRota = gerarDadosMockOffline();
                }
                return emRota;
            }

            @Override
            protected void done() {
                try {
                    cacheEntregas = get(); // Salva na memória rápida
                    renderizarListaLocal();
                } catch (Exception e) {}
            }
        }.execute();
    }

    // Método que desenha a lista sem travar a interface e preserva o scroll
    private void renderizarListaLocal() {
        int scrollPos = scrollLista.getVerticalScrollBar().getValue();
        String termo = txtBuscar.getText().trim().toLowerCase().replace("#", "");
        
        containerCards.removeAll();
        int count = 0;
        
        for(Pedidos p : cacheEntregas) {
            boolean match = termo.isEmpty() || 
                            p.getNomeCliente().toLowerCase().contains(termo) || 
                            p.getIdPedido().contains(termo) ||
                            (p.getNomeEntregador() != null && p.getNomeEntregador().toLowerCase().contains(termo));
            
            if (match) {
                containerCards.add(criarCardMiniatura(p));
                containerCards.add(Box.createVerticalStrut(10));
                count++;
            }
        }
        
        lblContador.setText(count + " entregas na rua");
        
        // Atualiza os dados do painel direito se o pedido sofrer alterações na BD
        if (entregaSelecionada != null) {
            boolean aindaExiste = cacheEntregas.stream().anyMatch(e -> e.getIdPedido().equals(entregaSelecionada.getIdPedido()));
            if (aindaExiste) {
                entregaSelecionada = cacheEntregas.stream().filter(e -> e.getIdPedido().equals(entregaSelecionada.getIdPedido())).findFirst().get();
                carregarDetalhesFull(entregaSelecionada);
            } else {
                entregaSelecionada = null;
                mostrarEstadoVazio();
            }
        }

        containerCards.revalidate();
        containerCards.repaint();
        SwingUtilities.invokeLater(() -> scrollLista.getVerticalScrollBar().setValue(scrollPos));
    }

    private List<Pedidos> gerarDadosMockOffline() {
        List<Pedidos> mocks = new ArrayList<>();
        mocks.add(new Pedidos("1001", "João Silva (Teste)", "19:00", "19:50", "LOC1", "Avenida Paulista, 1578 - SP", "Carlos (Motoboy)", "11999999999", "Delivery", "Trazer troco para R$ 100", null, "em rota", "Site", "Dinheiro", 50.0, ""));
        mocks.add(new Pedidos("1002", "Maria Souza (Teste)", "20:15", "21:00", "LOC2", "Rua Augusta, 400 - SP", "Roberto (Bike)", "11988888888", "Delivery", "", null, "em rota", "App", "PIX", 35.0, ""));
        return mocks;
    }

    // =========================================================================
    // 4. CARDS DE MINIATURA (COM ÍCONES CORRIGIDOS)
    // =========================================================================
    private JPanel criarCardMiniatura(Pedidos p) {
        boolean isSelected = (entregaSelecionada != null && entregaSelecionada.getIdPedido().equals(p.getIdPedido()));
        Color bgColor = isSelected ? UIConstants.SELECTED_INFO_CARD_BG : UIConstants.CARD_DARK;
        
        UIConstants.RoundedPanel card = new UIConstants.RoundedPanel(12, bgColor);
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setMaximumSize(new Dimension(500, 100));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel strip = new JPanel();
        strip.setPreferredSize(new Dimension(5, 0));
        strip.setBackground(UIConstants.INFO_BLUE);
        card.add(strip, BorderLayout.WEST);
        
        JPanel center = new JPanel(new GridLayout(3, 1, 0, 2));
        center.setOpaque(false);
        
        // Linha 1: ID e Nome
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        JLabel lId = new JLabel("#" + p.getIdPedido() + " ");
        lId.setForeground(UIConstants.PRIMARY_RED);
        lId.setFont(UIConstants.ARIAL_14_B);
        JLabel lNome = new JLabel(p.getNomeCliente());
        lNome.setForeground(UIConstants.FG_LIGHT);
        lNome.setFont(UIConstants.ARIAL_14_B);
        row1.add(lId, BorderLayout.WEST);
        row1.add(lNome, BorderLayout.CENTER);
        
        // Linha 2: Entregador (Sem Emoji - Usando Ícone)
        String entregador = (p.getNomeEntregador() != null && !p.getNomeEntregador().isEmpty()) ? p.getNomeEntregador() : "Sem Motoboy";
        JLabel lMotoboy = new JLabel("  " + entregador);
        lMotoboy.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MOTORCYCLE, 14, UIConstants.FG_LIGHT));
        lMotoboy.setForeground(UIConstants.FG_LIGHT);
        lMotoboy.setFont(UIConstants.ARIAL_12);

        // Linha 3: SLA de Tempo (Sem Emoji - Usando Ícone)
        JLabel lTempo = new JLabel(calcularTempoSLA(p.getHoraPedido()));
        lTempo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCESS_TIME, 14, UIConstants.FG_MUTED));
        lTempo.setFont(UIConstants.ARIAL_12_B);
        
        center.add(row1);
        center.add(lMotoboy);
        center.add(lTempo);
        card.add(center, BorderLayout.CENTER);
        
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { 
                entregaSelecionada = p;
                renderizarListaLocal(); // Usa a versão que não trava
                carregarDetalhesFull(p); 
            }
            public void mouseEntered(MouseEvent e) { if(!isSelected) card.setBackground(UIConstants.BG_DARK_ALT.brighter()); card.repaint(); }
            public void mouseExited(MouseEvent e) { if(!isSelected) card.setBackground(UIConstants.CARD_DARK); card.repaint(); }
        });
        
        return card;
    }

    static String calcularTempoSlaFormatado(String horaPedido) {
        if (horaPedido == null || horaPedido.isBlank()) {
            return "<html><font color='" + UIConstants.toHex(UIConstants.FG_MUTED) + "'>Horário indisponível</font></html>";
        }
        try {
            LocalTime hp = LocalTime.parse(horaPedido, DateTimeFormatter.ofPattern("HH:mm"));
            long min = ChronoUnit.MINUTES.between(hp, LocalTime.now());
            if(min < 0) min = 0;
            
            if(min > 45) return "<html><font color='" + UIConstants.toHex(UIConstants.DANGER_RED) + "'> ATRASADO (" + min + " min)</font></html>";
            if(min > 30) return "<html><font color='" + UIConstants.toHex(UIConstants.WARNING_ORANGE) + "'> Atenção (" + min + " min)</font></html>";
            return "<html><font color='" + UIConstants.toHex(UIConstants.SUCCESS_GREEN) + "'> No prazo (" + min + " min)</font></html>";
        } catch (Exception e) {
            return " Saiu às " + horaPedido;
        }
    }

    private String calcularTempoSLA(String horaPedido) {
        return calcularTempoSlaFormatado(horaPedido);
    }

    // =========================================================================
    // 5. PAINEL DE DETALHES "PRO" (LADO DIREITO)
    // =========================================================================
    private void carregarDetalhesFull(Pedidos p) {
        panelDetalhes.removeAll();
        
        JPanel containerPrincipal = new JPanel(new BorderLayout(0, 20));
        containerPrincipal.setBackground(UIConstants.BG_DARK);
        containerPrincipal.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        // --- CABEÇALHO DO PEDIDO ---
        JPanel headerInfo = new JPanel(new BorderLayout());
        headerInfo.setOpaque(false);
        
        JLabel titulo = new JLabel("Pedido #" + p.getIdPedido() + " - " + p.getNomeCliente());
        titulo.setFont(UIConstants.FONT_TITLE_LARGE);
        titulo.setForeground(UIConstants.FG_LIGHT);
        
        JLabel badgeStatus = new JLabel("  EM ROTA  ");
        badgeStatus.setOpaque(true);
        badgeStatus.setBackground(UIConstants.INFO_BLUE);
        badgeStatus.setForeground(UIConstants.SEL_FG);
        badgeStatus.setFont(UIConstants.ARIAL_12_B);
        badgeStatus.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JPanel pnlTitulos = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlTitulos.setOpaque(false);
        pnlTitulos.add(titulo); pnlTitulos.add(badgeStatus);
        
        headerInfo.add(pnlTitulos, BorderLayout.WEST);
        
        JLabel lblTopSLA = new JLabel(calcularTempoSLA(p.getHoraPedido()));
        lblTopSLA.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCESS_TIME, 18, UIConstants.FG_LIGHT));
        lblTopSLA.setFont(UIConstants.ARIAL_14_B);
        headerInfo.add(lblTopSLA, BorderLayout.EAST);

        containerPrincipal.add(headerInfo, BorderLayout.NORTH);
        
        // --- GRIDS DE INFORMAÇÃO OPERACIONAL ---
        JPanel gridInfo = new JPanel(new GridLayout(2, 2, 20, 20)); 
        gridInfo.setOpaque(false);
        
        // Bloco 1: Endereço e Copiar
        JPanel pnlEnd = criarCardInformacao("ENDEREÇO DE ENTREGA", p.getEnderecoCompleto(), "Use o botão para copiar", GoogleMaterialDesignIcons.LOCATION_ON);
        JButton btnCopiar = new JButton("Copiar");
        UIConstants.styleSecondary(btnCopiar);
        btnCopiar.setFont(UIConstants.ARIAL_12_B);
        btnCopiar.setMargin(new Insets(2,5,2,5));
        btnCopiar.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(p.getEnderecoCompleto()), null);
            UIConstants.showSuccess(this, "Endereço copiado!");
        });
        pnlEnd.add(btnCopiar, BorderLayout.EAST);
        gridInfo.add(pnlEnd);
        
        // Bloco 2: Contato
        gridInfo.add(criarCardInformacao("CONTATO DO CLIENTE", p.getTelefoneEntregador(), "Ligar em caso de dúvida", GoogleMaterialDesignIcons.PERSON));
        
        // Bloco 3: Motoboy e Reatribuição
        JPanel pnlMoto = criarCardInformacao("FROTA / MOTOBOY", (p.getNomeEntregador() != null ? p.getNomeEntregador() : "Não atribuído"), "Em trânsito", GoogleMaterialDesignIcons.MOTORCYCLE);
        JButton btnTrocar = new JButton("Trocar");
        UIConstants.styleSecondary(btnTrocar);
        btnTrocar.setFont(UIConstants.ARIAL_12_B);
        btnTrocar.addActionListener(e -> trocarMotoboy(p));
        pnlMoto.add(btnTrocar, BorderLayout.EAST);
        gridInfo.add(pnlMoto);
        
        // Bloco 4: Pagamento Visual (Alerta de Cobrança)
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String pgto = p.getFormaPagamento().toUpperCase();
        boolean isPago = pgto.contains("PIX") || pgto.contains("SITE") || pgto.contains("APP") || pgto.contains("ONLINE");
        
        JPanel pnlPgto = criarCardInformacao(
            isPago ? "PAGAMENTO ONLINE" : "ATENÇÃO: COBRAR NA ENTREGA", 
            "Forma: " + pgto, 
            p.getSubtotal() != null ? "Valor a receber: " + nf.format(p.getSubtotal()) : "Valor Total não informado", 
            GoogleMaterialDesignIcons.ACCOUNT_BALANCE_WALLET
        );
        if(!isPago) pnlPgto.setBorder(BorderFactory.createLineBorder(UIConstants.PRIMARY_RED, 2));
        gridInfo.add(pnlPgto);

        JPanel centerContent = new JPanel(new BorderLayout(0, 20));
        centerContent.setOpaque(false);
        centerContent.add(gridInfo, BorderLayout.NORTH);

        if (p.getObservacoes() != null && !p.getObservacoes().trim().isEmpty()) {
            UIConstants.RoundedPanel pnlObs = new UIConstants.RoundedPanel(12, UIConstants.WARNING_PANEL_BG); 
            pnlObs.setLayout(new BorderLayout(10, 10));
            pnlObs.setBorder(new EmptyBorder(15, 15, 15, 15));
            JLabel icoObs = new JLabel(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.WARNING, 30, UIConstants.WARNING_ORANGE));
            JLabel txtObs = new JLabel("<html><b>OBSERVAÇÕES (ATENÇÃO ENTREGADOR):</b><br>" + p.getObservacoes() + "</html>");
            txtObs.setForeground(UIConstants.FG_LIGHT);
            txtObs.setFont(UIConstants.ARIAL_14);
            pnlObs.add(icoObs, BorderLayout.WEST);
            pnlObs.add(txtObs, BorderLayout.CENTER);
            centerContent.add(pnlObs, BorderLayout.CENTER);
        }

        JScrollPane scrollInfo = new JScrollPane(centerContent);
        scrollInfo.setBorder(null);
        scrollInfo.setOpaque(false);
        scrollInfo.getViewport().setOpaque(false);
        containerPrincipal.add(scrollInfo, BorderLayout.CENTER);
        
        JPanel bottomActions = new JPanel(new BorderLayout());
        bottomActions.setBackground(UIConstants.BG_DARK_ALT);
        bottomActions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.GRID_DARK));
        bottomActions.setPreferredSize(new Dimension(0, 80));
        
        JPanel pnlEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 18));
        pnlEsquerda.setOpaque(false);
        JButton btnWhats = new JButton("Chamar no WhatsApp");
        UIConstants.styleSecondary(btnWhats);
        btnWhats.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.CHAT, 20, UIConstants.SUCCESS_GREEN));
        btnWhats.addActionListener(e -> abrirWhatsAppContextualizado(p));
        pnlEsquerda.add(btnWhats);

        JPanel pnlDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 18));
        pnlDireita.setOpaque(false);
        
        JButton btnProblema = new JButton("Retornou / Problema");
        UIConstants.styleDanger(btnProblema);
        btnProblema.addActionListener(e -> reportarProblemaEntrega(p));
        
        JButton btnFinalizar = new JButton("MARCAR COMO ENTREGUE");
        UIConstants.styleSuccess(btnFinalizar);
        btnFinalizar.setPreferredSize(new Dimension(260, 45));
        btnFinalizar.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.CHECK_CIRCLE, 20, Color.WHITE));
        btnFinalizar.addActionListener(e -> tentarConcluirEntrega(p));

        pnlDireita.add(btnProblema);
        pnlDireita.add(btnFinalizar);
        
        bottomActions.add(pnlEsquerda, BorderLayout.WEST);
        bottomActions.add(pnlDireita, BorderLayout.EAST);
        
        panelDetalhes.add(containerPrincipal, BorderLayout.CENTER);
        panelDetalhes.add(bottomActions, BorderLayout.SOUTH);
        
        panelDetalhes.revalidate();
        panelDetalhes.repaint();
    }

    private JPanel criarCardInformacao(String titulo, String linha1, String linha2, GoogleMaterialDesignIcons icon) {
        UIConstants.RoundedPanel card = new UIConstants.RoundedPanel(15, UIConstants.CARD_DARK);
        card.setLayout(new BorderLayout(15, 5));
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel icone = new JLabel(IconFontSwing.buildIcon(icon, 36, UIConstants.FG_MUTED));
        card.add(icone, BorderLayout.WEST);
        
        JPanel textos = new JPanel(new GridLayout(3, 1, 0, 2));
        textos.setOpaque(false);
        
        JLabel lblTit = new JLabel(titulo.toUpperCase());
        lblTit.setFont(UIConstants.ARIAL_12_B);
        lblTit.setForeground(UIConstants.INFO_BLUE); 
        
        JLabel lblL1 = new JLabel(linha1);
        lblL1.setFont(UIConstants.ARIAL_16_B);
        lblL1.setForeground(UIConstants.FG_LIGHT);
        
        JLabel lblL2 = new JLabel(linha2);
        lblL2.setFont(UIConstants.ARIAL_12);
        lblL2.setForeground(UIConstants.FG_LIGHT);
        
        textos.add(lblTit); textos.add(lblL1); textos.add(lblL2);
        card.add(textos, BorderLayout.CENTER);
        return card;
    }


    private void abrirWhatsAppContextualizado(Pedidos p) {
        try {
            String telefone = p.getTelefoneEntregador();
            if (telefone == null || telefone.isEmpty()) {
                UIConstants.showWarning(this, "Telefone não encontrado no cadastro deste pedido.");
                return;
            }
            String numeroLimpo = telefone.replaceAll("[^0-9]", "");
            String motoboy = (p.getNomeEntregador() != null && !p.getNomeEntregador().isBlank()) ? p.getNomeEntregador() : "o nosso entregador";
            
            String msg = "Olá *" + p.getNomeCliente() + "*, aqui é do *" + SessionContext.getInstance().getRestauranteLabel() + "*! 🛵\n\n"
                       + "O seu pedido *#" + p.getIdPedido() + "* acabou de sair para entrega com *" + motoboy + "*.\n"
                       + "Forma de pagamento registrada: *" + p.getFormaPagamento().toUpperCase() + "*.\n\n"
                       + "Qualquer dúvida, é só chamar aqui. Bom apetite!";
                       
            String url = "https://wa.me/55" + numeroLimpo + "?text=" + msg.replace(" ", "%20").replace("\n", "%0A");
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            UIConstants.showError(this, "Não foi possível abrir o navegador para o WhatsApp.");
        }
    }

    private void trocarMotoboy(Pedidos p) {
        operacaoEmAndamento = true;

        List<String> entregadoresAtivos = new ArrayList<>();
        for(Pedidos ped : cacheEntregas) {
            String nome = ped.getNomeEntregador();
            if(nome != null && !nome.isBlank() && !entregadoresAtivos.contains(nome)) {
                entregadoresAtivos.add(nome);
            }
        }
        entregadoresAtivos.add("👤 Adicionar Novo Entregador...");

        String[] opcoes = entregadoresAtivos.toArray(new String);
        
        String escolha = (String) JOptionPane.showInputDialog(
            this, 
            "Selecione o Entregador para assumir a rota do Pedido #" + p.getIdPedido() + ":",
            "Atribuir Motoboy", 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            opcoes, 
            opcoes
        );

        if (escolha != null) {
            String novoNome = escolha;
            if (escolha.equals("👤 Adicionar Novo Entregador...")) {
                novoNome = JOptionPane.showInputDialog(this, "Digite o nome do novo entregador:");
            }

            if (novoNome != null && !novoNome.trim().isEmpty()) {
                p.setNomeEntregador(novoNome); 
                executarUpdateBancoAsync(p, "em rota", novoNome, "Entregador alterado para " + novoNome + "!");
            }
        }
        operacaoEmAndamento = false;
    }

    private void reportarProblemaEntrega(Pedidos p) {
        operacaoEmAndamento = true;
        
        // Categorização de erros operacionais
        String[] motivos = {
            "Endereço não localizado",
            "Cliente ausente / Não atende",
            "Cliente recusou o pedido",
            "Acidente / Problema com o Motoboy",
            "Pedido danificado no trajeto"
        };

        String motivo = (String) JOptionPane.showInputDialog(
            this, 
            "Por que o pedido #" + p.getIdPedido() + " está retornando?\nEle voltará para a fila da Cozinha (Status: Pronto).",
            "Registrar Falha na Entrega", 
            JOptionPane.WARNING_MESSAGE, 
            IconFontSwing.buildIcon(GoogleMaterialDesignIcons.REPORT_PROBLEM, 32, UIConstants.DANGER_RED), 
            motivos, 
            motivos
        );

        if (motivo != null) {
            p.setNomeEntregador(null);
            executarUpdateBancoAsync(p, "pronto", null, "Retorno registrado por: " + motivo);
            entregaSelecionada = null;
        }
        operacaoEmAndamento = false;
    }

    private void tentarConcluirEntrega(Pedidos p) {
        UIConstants.showConfirmDialog(
            this,
            "Finalizar Entrega", 
            "Confirma que o motoboy entregou o pedido #" + p.getIdPedido() + " e o pagamento foi acertado?", 
            () -> {
                executarUpdateBancoAsync(p, "concluido", null, "Entrega finalizada com sucesso!");
                entregaSelecionada = null;
            }
        );
    }

    private void executarUpdateBancoAsync(Pedidos p, String novoStatus, String nomeEntregador, String msgSucesso) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        operacaoEmAndamento = true;
        
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    dao.atualizarStatusPedido(p.getIdPedido(), novoStatus);
                    return true;
                } catch (Exception e) { return false; }
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                operacaoEmAndamento = false;
                try {
                    if(get()) {
                        UIConstants.showSuccess(EntregasPainel.this, msgSucesso);
                        carregarEntregasAsync(true);
                    } else {
                        UIConstants.showError(EntregasPainel.this, "Erro de conexão ao BD.");
                    }
                } catch(Exception ex) {}
            }
        }.execute();
    }
}
