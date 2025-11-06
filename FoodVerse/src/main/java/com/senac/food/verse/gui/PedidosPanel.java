package com.senac.food.verse.gui;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
import com.senac.food.verse.ConexaoBanco;
import com.senac.food.verse.ItemPedido;
import com.senac.food.verse.PedidoDAO;
import com.senac.food.verse.Pedidos;
import com.senac.food.verse.RoundedLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author josie
 */
public final class PedidosPanel extends javax.swing.JPanel {

    /**
     * Creates new form PedidosPanel
     */
    private final PedidoDAO dao = new PedidoDAO();

    public PedidosPanel() {
        initComponents();
        estilizarComboBoxSeta();
        dao.buscarTodosPedidos();
        criarMenuPedido();
        atualizarContadorPendentes();
        limparInput();

        TelaPedido.setVisible(false);

        new javax.swing.Timer(10_000, e -> {
            if (dao.haNovoPedido() || dao.houveAlteracoesPedidos()) {
                dao.recarregarPedidos();
                criarMenuPedido();
                atualizarContadorPendentes();
                System.out.println("Pedidos atualizados!");
            } else {
                System.out.println("Timer disparado, mas sem alterações!");
            }
        }).start();
        inputBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String textoDigitado = inputBuscar.getText().trim();
                if (!textoDigitado.equals("") && !textoDigitado.equalsIgnoreCase("Buscar")) {
                    buscarPedidos(textoDigitado);
                } else {
                    criarMenuPedido(); // Mostra todos se estiver vazio
                }
            }
        });
    }

    private void estilizarComboBoxSeta() {
        jComboBox1.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        int w = getWidth();
                        int h = getHeight();

                        g2.setColor(Color.BLACK);
                        int[] xPoints = {w / 2 - 5, w / 2 + 5, w / 2};
                        int[] yPoints = {h / 2 - 2, h / 2 - 2, h / 2 + 4};
                        g2.fillPolygon(xPoints, yPoints, 3);

                        g2.dispose();
                    }
                };

                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.setFocusPainted(false);
                button.setOpaque(true);
                return button;
            }
        });
    }

    public void PaineisPedidos(Pedidos pedido) {
        // Permite abrir detalhes para pendente e em preparo do delivery
        if (("pendente".equalsIgnoreCase(pedido.getStatusPedido())
                || "em preparo".equalsIgnoreCase(pedido.getStatusPedido()))
                && "Delivery".equalsIgnoreCase(pedido.getModoEntrega())) {
            mostrarPedidoLocal(pedido); // ou pode ser mostrarPedidoDetalhesDelivery
            return;
        }

        if (("pendente".equalsIgnoreCase(pedido.getStatusPedido())
                || "em preparo".equalsIgnoreCase(pedido.getStatusPedido())
                || "pronto".equalsIgnoreCase(pedido.getStatusPedido())
                || "entregue".equalsIgnoreCase(pedido.getStatusPedido()))
                && "No Local".equalsIgnoreCase(pedido.getModoEntrega())) {
            mostrarPedidoLocal(pedido);
            return;
        }
        // Se quiser permitir para outros status, adicione aqui:
        if ("pronto".equalsIgnoreCase(pedido.getStatusPedido())) {
            mostrarPedidoPronto(pedido);
            return;
        }
        if ("finalizado".equalsIgnoreCase(pedido.getStatusPedido())) {
            mostrarPedidoFinalizado(pedido);
            return;
        }
    }
    // Exemplo de campo statusLabel como variável de instância
    private JLabel statusLabelCardSelecionado = null;

    private JPanel criarPedidoCard(Pedidos pedido) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        };
        card.setLayout(new GroupLayout(card));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // --- Componentes internos ---
        GroupLayout layout = (GroupLayout) card.getLayout();
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel idLabel = new JLabel("#" + pedido.getIdPedido());
        idLabel.setFont(new Font("Arial", Font.BOLD, 13));
        idLabel.setForeground(new Color(60, 60, 60));

        JLabel tagLabel = new JLabel(pedido.getTipoPedido());
        tagLabel.setOpaque(true);
        tagLabel.setBackground(new Color(255, 245, 204));
        tagLabel.setForeground(new Color(120, 100, 20));
        tagLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        tagLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        tagLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel tempoLabel = new JLabel("50 min.");
        tempoLabel.setOpaque(true);
        tempoLabel.setBackground(new Color(220, 0, 0));
        tempoLabel.setForeground(Color.WHITE);
        tempoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        tempoLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        tempoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel statusLabel = new JLabel(pedido.getStatusPedido());
        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        statusLabel.setForeground(new Color(100, 100, 100));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // ícone de caminhão redimensionado
        ImageIcon rawIcon = new ImageIcon("caminhao.png");
        Image img = rawIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(img);

        RoundedLabel modoEntrega = new RoundedLabel(
                pedido.getModoEntrega(),
                icon,
                new Color(240, 230, 255),
                new Color(180, 130, 255),
                16, 16,
                6
        );

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(idLabel)
                                .addGap(6)
                                .addComponent(tagLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, Short.MAX_VALUE)
                                .addComponent(tempoLabel))
                        .addComponent(statusLabel)
                        .addComponent(modoEntrega, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(idLabel)
                                .addComponent(tagLabel)
                                .addComponent(tempoLabel))
                        .addGap(6)
                        .addComponent(statusLabel)
                        .addGap(6)
                        .addComponent(modoEntrega)
                        .addGap(6)
        );

        // Passa a referência do statusLabel para o campo de instância ao clicar
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(245, 245, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                statusLabelCardSelecionado = statusLabel; // Salva label do card selecionado
                PaineisPedidos(pedido);
            }
        });

        return card;
    }

    public void criarMenuPedido() {
        ArrayList<Pedidos> pedidos = dao.buscarTodosPedidos();

        jPanel3.removeAll(); // Limpa os pedidos anteriores
        jPanel3.setLayout(new BoxLayout(jPanel3, BoxLayout.Y_AXIS));

        for (Pedidos pedido : pedidos) {
            //ajudar a criar o cardMenu
            JPanel pedidoCard = criarPedidoCard(pedido);
            jPanel3.add(Box.createVerticalStrut(10)); // Espaço entre os cards
            jPanel3.add(pedidoCard);
        }

        jPanel3.revalidate();
        jPanel3.repaint();
    }

    private void estilizarBotao(JButton botao, Color corFundo, int tamanhoFonte) {
        botao.setFont(new Font("Segoe UI", Font.BOLD, tamanhoFonte));
        botao.setForeground(Color.WHITE);
        botao.setBackground(corFundo);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(corFundo.darker(), 1, true),
                new RoundedBorder(12)
        ));
        botao.setAlignmentX(Component.CENTER_ALIGNMENT);
        botao.setOpaque(true);
        botao.setFocusable(false);
        botao.setToolTipText(botao.getText());
    }

    private void mostrarPedidoLocal(Pedidos pedido) {
        TelaPedido.removeAll();

        JPanel detalhes = new JPanel();
        detalhes.setLayout(new BoxLayout(detalhes, BoxLayout.Y_AXIS));
        detalhes.setBackground(Color.WHITE);
        detalhes.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        detalhes.setPreferredSize(new Dimension(TelaPedido.getWidth(), TelaPedido.getHeight()));

        // Header do Pedido
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        JLabel lblPedido = new JLabel("Pedido #" + pedido.getIdPedido());
        lblPedido.setFont(new Font("Arial", Font.BOLD, 14));
        lblPedido.setForeground(new Color(33, 37, 41));
        JLabel lblFeito = new JLabel(" • Feito às ");
        lblFeito.setFont(new Font("Arial", Font.PLAIN, 13));
        lblFeito.setForeground(new Color(108, 117, 125));
        JLabel lblHora = new JLabel(pedido.getHoraPedido() != null ? pedido.getHoraPedido() : "--:--");
        lblHora.setFont(new Font("Arial", Font.BOLD, 13));
        header.add(lblPedido);
        header.add(lblFeito);
        header.add(lblHora);
        detalhes.add(header);

        detalhes.add(Box.createVerticalStrut(8));

        // Info de mesa ou balcão
        JLabel lblMesa = new JLabel("Mesa/Balcão: " + (pedido.getMesa() != null ? pedido.getMesa() : "N/A"));
        lblMesa.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMesa.setForeground(new Color(108, 117, 125));
        lblMesa.setAlignmentX(Component.CENTER_ALIGNMENT);
        detalhes.add(lblMesa);

        detalhes.add(Box.createVerticalStrut(10));

        // Painel de itens do pedido
        JPanel painelPedido = new JPanel();
        painelPedido.setLayout(new BoxLayout(painelPedido, BoxLayout.Y_AXIS));
        painelPedido.setBackground(Color.WHITE);
        painelPedido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230, 150)),
                new EmptyBorder(4, 8, 4, 8)
        ));
        painelPedido.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel statusBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusBanner.setBackground(new Color(235, 245, 255));
        statusBanner.setBorder(new EmptyBorder(14, 20, 14, 20));
        statusBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel lblStatus = new JLabel(
                pedido.getStatusPedido().equalsIgnoreCase("pendente") ? "Pendente"
                : pedido.getStatusPedido().equalsIgnoreCase("em preparo") ? "Em Preparo"
                : pedido.getStatusPedido().equalsIgnoreCase("pronto") ? "Pronto"
                : pedido.getStatusPedido().equalsIgnoreCase("entregue") ? "Entregue"
                : pedido.getStatusPedido()
        );
        lblStatus.setFont(new Font("Arial", Font.BOLD, 13));
        lblStatus.setForeground(new Color(25, 135, 84));
        statusBanner.add(lblStatus);
        painelPedido.add(statusBanner);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(new EmptyBorder(2, 8, 2, 8));

        NumberFormat formatBR = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        for (ItemPedido item : pedido.getItens()) {
            JPanel linhaItem = new JPanel(new BorderLayout());
            linhaItem.setBackground(Color.WHITE);
            JLabel lblQtdItem = new JLabel(item.getQuantidade() + "x " + item.getNomeProduto());
            lblQtdItem.setFont(new Font("Arial", Font.PLAIN, 15));
            JLabel lblPrecoItem = new JLabel(formatBR.format(item.getPreco()));
            lblPrecoItem.setFont(new Font("Arial", Font.PLAIN, 13));
            linhaItem.add(lblQtdItem, BorderLayout.WEST);
            linhaItem.add(lblPrecoItem, BorderLayout.EAST);
            itemsPanel.add(linhaItem);
            itemsPanel.add(Box.createVerticalStrut(2));
        }

        itemsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        itemsPanel.add(Box.createVerticalStrut(4));

        JPanel linhaSubtotal = new JPanel(new BorderLayout());
        linhaSubtotal.setBackground(Color.WHITE);
        linhaSubtotal.setBorder(new EmptyBorder(2, 0, 0, 0));
        JLabel lblSubtotal = new JLabel("Subtotal");
        lblSubtotal.setFont(new Font("Arial", Font.PLAIN, 15));
        JLabel lblPrecoSubtotal = new JLabel(formatBR.format(pedido.getSubtotal())); // troque por seu cálculo real
        lblPrecoSubtotal.setFont(new Font("Arial", Font.BOLD, 13));
        linhaSubtotal.add(lblSubtotal, BorderLayout.WEST);
        linhaSubtotal.add(lblPrecoSubtotal, BorderLayout.EAST);
        itemsPanel.add(linhaSubtotal);

        painelPedido.add(itemsPanel);
        painelPedido.add(new JSeparator());

        // Pagamento
        JPanel pagamentoBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pagamentoBanner.setBackground(new Color(245, 245, 245));
        pagamentoBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230, 150)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        pagamentoBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        JLabel iconePagamento = new JLabel("\u0024");
        iconePagamento.setFont(new Font("Arial", Font.BOLD, 18));
        iconePagamento.setForeground(new Color(120, 120, 120));
        pagamentoBanner.add(iconePagamento);
        pagamentoBanner.add(Box.createHorizontalStrut(10));

        JPanel textoPagamento = new JPanel();
        textoPagamento.setOpaque(false);
        textoPagamento.setLayout(new BoxLayout(textoPagamento, BoxLayout.Y_AXIS));
        JLabel lblFormaPagamento = new JLabel("Forma de pagamento: " + pedido.getFormaPagamento());
        lblFormaPagamento.setFont(new Font("Arial", Font.BOLD, 12));
        lblFormaPagamento.setForeground(new Color(120, 120, 120));
        textoPagamento.add(lblFormaPagamento);

        pagamentoBanner.add(textoPagamento);
        painelPedido.add(pagamentoBanner);

        detalhes.add(painelPedido);
        detalhes.add(Box.createVerticalStrut(12));

        // Painel de Botões (rodapé)
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBotoes.setOpaque(false);

        // Exemplo: botões de ação para pedido local
        if ("pendente".equalsIgnoreCase(pedido.getStatusPedido())) {
            JButton btnCancelar = new JButton("Cancelar");
            estilizarBotao(btnCancelar, Color.WHITE, 13);
            btnCancelar.setForeground(new Color(220, 53, 69));
            btnCancelar.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 1));
            btnCancelar.setPreferredSize(new Dimension(110, 30));
            panelBotoes.add(btnCancelar);
            panelBotoes.add(Box.createHorizontalStrut(8));

            JButton btnConfirmar = new JButton("Confirmar pedido");
            estilizarBotao(btnConfirmar, new Color(220, 53, 69), 13);
            btnConfirmar.setPreferredSize(new Dimension(140, 34));
            panelBotoes.add(btnConfirmar);

            btnConfirmar.addActionListener((ActionEvent e) -> {
                mudarStatusPedidoBanco(pedido.getIdPedido(), "em preparo");
                dao.recarregarPedidos();
                Pedidos pedidoAtualizado = dao.buscarPedidoPorId(pedido.getIdPedido());
                mostrarPedidoLocal(pedidoAtualizado);
                criarMenuPedido();
                atualizarContadorPendentes();
            });
        } else if ("em preparo".equalsIgnoreCase(pedido.getStatusPedido())) {
            JButton btnFinalizar = new JButton("Finalizar");
            estilizarBotao(btnFinalizar, new Color(25, 135, 84), 13);
            btnFinalizar.setPreferredSize(new Dimension(130, 34));
            panelBotoes.add(btnFinalizar);

            btnFinalizar.addActionListener(ev -> {
                mudarStatusPedidoBanco(pedido.getIdPedido(), "pronto");
                dao.recarregarPedidos();
                Pedidos pedidoAtualizado = dao.buscarPedidoPorId(pedido.getIdPedido());
                mostrarPedidoLocal(pedidoAtualizado);
                criarMenuPedido();
                JOptionPane.showMessageDialog(TelaPedido, "Pedido finalizado com sucesso!", "Finalizado", JOptionPane.INFORMATION_MESSAGE);
            });
        } else {
            JButton btnFechar = new JButton("Fechar");
            estilizarBotao(btnFechar, new Color(33, 37, 41), 13);
            btnFechar.setPreferredSize(new Dimension(110, 30));
            btnFechar.addActionListener(e -> TelaPedido.setVisible(false));
            panelBotoes.add(btnFechar);
        }

        detalhes.add(panelBotoes);

        JScrollPane scrollPane = new JScrollPane(detalhes);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        TelaPedido.setLayout(new BorderLayout());
        TelaPedido.add(scrollPane, BorderLayout.CENTER);
        TelaPedido.revalidate();
        TelaPedido.repaint();
        TelaPedido.setVisible(true);
    }

    private void mostrarPedidoPronto(Pedidos pedido) {
        TelaPedido.removeAll();

        JPanel detalhes = new JPanel();
        detalhes.setLayout(new BoxLayout(detalhes, BoxLayout.Y_AXIS));
        detalhes.setBackground(Color.WHITE);
        detalhes.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        detalhes.setPreferredSize(new Dimension(TelaPedido.getWidth(), TelaPedido.getHeight()));

        // Header do Pedido
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        JLabel lblPedido = new JLabel("Pedido #" + pedido.getIdPedido());
        lblPedido.setFont(new Font("Arial", Font.PLAIN, 13));
        lblPedido.setForeground(new Color(33, 37, 41));
        JLabel lblFeito = new JLabel(" • Feito às ");
        lblFeito.setFont(new Font("Arial", Font.PLAIN, 13));
        lblFeito.setForeground(new Color(108, 117, 125));
        JLabel lblHora = new JLabel(pedido.getHoraPedido() != null ? pedido.getHoraPedido() : "--:--");
        lblHora.setFont(new Font("Arial", Font.BOLD, 13));
        header.add(lblPedido);
        header.add(lblFeito);
        header.add(lblHora);
        detalhes.add(header);

        detalhes.add(Box.createVerticalStrut(6));

        // Linha de info de entrega
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);

        JLabel iconeRelogio = new JLabel("\u23F0");
        iconeRelogio.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        iconeRelogio.setForeground(new Color(120, 120, 120));
        row.add(iconeRelogio);
        row.add(Box.createHorizontalStrut(4));

        JLabel lblEntrega = new JLabel("Entrega prevista: " + (pedido.getHoraEntrega() != null ? pedido.getHoraEntrega() : "--:--"));
        lblEntrega.setFont(new Font("Arial", Font.PLAIN, 12));
        row.add(lblEntrega);
        row.add(Box.createHorizontalStrut(8));

        JPanel pedidosBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pedidosBox.setBackground(new Color(240, 240, 240));
        pedidosBox.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220, 150)));
        JLabel iconeEstrela = new JLabel("\u2605");
        iconeEstrela.setForeground(new Color(108, 117, 125));
        iconeEstrela.setFont(new Font("Arial", Font.PLAIN, 11));
        JLabel lblQtdPedidos = new JLabel(pedido.getQtdPedidos() + " pedidos");
        lblQtdPedidos.setFont(new Font("Arial", Font.PLAIN, 11));
        lblQtdPedidos.setForeground(new Color(108, 117, 125));
        pedidosBox.add(iconeEstrela);
        pedidosBox.add(lblQtdPedidos);
        row.add(pedidosBox);
        detalhes.add(row);
        detalhes.add(Box.createVerticalStrut(10));

        // Endereço de entrega (sempre mostra no pronto)
        adicionarEnderecoAcimaDeEntregaParceira(detalhes, null, pedido);

        // Tag "Entrega Parceira"
        JLabel tagEntregaParceira = new JLabel("Entrega Parceira");
        tagEntregaParceira.setFont(new Font("Arial", Font.PLAIN, 10));
        tagEntregaParceira.setOpaque(true);
        tagEntregaParceira.setBackground(new Color(248, 249, 250));
        tagEntregaParceira.setForeground(new Color(220, 53, 69));
        tagEntregaParceira.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 53, 69)),
                new EmptyBorder(2, 8, 2, 8)
        ));
        tagEntregaParceira.setAlignmentX(Component.CENTER_ALIGNMENT);
        detalhes.add(tagEntregaParceira);
        detalhes.add(Box.createVerticalStrut(12));

        // Painel de itens do pedido
        JPanel painelPedido = new JPanel();
        painelPedido.setLayout(new BoxLayout(painelPedido, BoxLayout.Y_AXIS));
        painelPedido.setBackground(Color.WHITE);
        painelPedido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230, 150)),
                new EmptyBorder(4, 8, 4, 8)
        ));
        painelPedido.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel prontoBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        prontoBanner.setBackground(new Color(220, 255, 220));
        prontoBanner.setBorder(new EmptyBorder(14, 20, 14, 20));
        prontoBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel lblPronto = new JLabel("Pronto");
        lblPronto.setFont(new Font("Arial", Font.BOLD, 13));
        lblPronto.setForeground(new Color(25, 135, 84));
        prontoBanner.add(lblPronto);
        painelPedido.add(prontoBanner);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(new EmptyBorder(2, 8, 2, 8));

        NumberFormat formatBR = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        for (ItemPedido item : pedido.getItens()) {
            JPanel linhaItem = new JPanel(new BorderLayout());
            linhaItem.setBackground(Color.WHITE);
            JLabel lblQtdItem = new JLabel(item.getQuantidade() + "x " + item.getNomeProduto());
            lblQtdItem.setFont(new Font("Arial", Font.PLAIN, 15));
            JLabel lblPrecoItem = new JLabel(formatBR.format(item.getPreco()));
            lblPrecoItem.setFont(new Font("Arial", Font.PLAIN, 13));
            linhaItem.add(lblQtdItem, BorderLayout.WEST);
            linhaItem.add(lblPrecoItem, BorderLayout.EAST);
            itemsPanel.add(linhaItem);
            itemsPanel.add(Box.createVerticalStrut(2));
        }

        itemsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        itemsPanel.add(Box.createVerticalStrut(4));

        JPanel linhaSubtotal = new JPanel(new BorderLayout());
        linhaSubtotal.setBackground(Color.WHITE);
        linhaSubtotal.setBorder(new EmptyBorder(2, 0, 0, 0));
        JLabel lblSubtotal = new JLabel("Subtotal");
        lblSubtotal.setFont(new Font("Arial", Font.PLAIN, 15));
        JLabel lblPrecoSubtotal = new JLabel(formatBR.format(pedido.getSubtotal())); // troque por cálculo real
        lblPrecoSubtotal.setFont(new Font("Arial", Font.BOLD, 13));
        linhaSubtotal.add(lblSubtotal, BorderLayout.WEST);
        linhaSubtotal.add(lblPrecoSubtotal, BorderLayout.EAST);
        itemsPanel.add(linhaSubtotal);

        painelPedido.add(itemsPanel);
        painelPedido.add(new JSeparator());

        // Pagamento
        JPanel pagamentoBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pagamentoBanner.setBackground(new Color(245, 245, 245));
        pagamentoBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230, 150)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        pagamentoBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        JLabel iconePagamento = new JLabel("\u0024");
        iconePagamento.setFont(new Font("Arial", Font.BOLD, 18));
        iconePagamento.setForeground(new Color(120, 120, 120));
        pagamentoBanner.add(iconePagamento);
        pagamentoBanner.add(Box.createHorizontalStrut(10));

        JPanel textoPagamento = new JPanel();
        textoPagamento.setOpaque(false);
        textoPagamento.setLayout(new BoxLayout(textoPagamento, BoxLayout.Y_AXIS));
        JLabel lblFormaPagamento = new JLabel("Forma de pagamento: " + pedido.getFormaPagamento());
        lblFormaPagamento.setFont(new Font("Arial", Font.BOLD, 12));
        lblFormaPagamento.setForeground(new Color(120, 120, 120));
        textoPagamento.add(lblFormaPagamento);

        pagamentoBanner.add(textoPagamento);
        painelPedido.add(pagamentoBanner);

        detalhes.add(painelPedido);
        detalhes.add(Box.createVerticalStrut(12));

        // Painel de Botões (rodapé) - apenas botão fechar/voltar
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBotoes.setOpaque(false);

        JButton btnFechar = new JButton("Fechar");
        estilizarBotao(btnFechar, new Color(33, 37, 41), 13);
        btnFechar.setPreferredSize(new Dimension(110, 30));
        btnFechar.addActionListener(e -> TelaPedido.setVisible(false));
        panelBotoes.add(btnFechar);

        detalhes.add(panelBotoes);

        JScrollPane scrollPane = new JScrollPane(detalhes);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        TelaPedido.setLayout(new BorderLayout());
        TelaPedido.add(scrollPane, BorderLayout.CENTER);
        TelaPedido.revalidate();
        TelaPedido.repaint();
        TelaPedido.setVisible(true);
    }

    // Método para mostrar pedido pendente com endereço centralizado após confirmação
    private void mostrarPedidoPendenteDelivery(Pedidos pedido) {
        TelaPedido.removeAll();

        JPanel detalhes = new JPanel();
        detalhes.setLayout(new BoxLayout(detalhes, BoxLayout.Y_AXIS));
        detalhes.setBackground(Color.WHITE);
        detalhes.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        detalhes.setPreferredSize(new Dimension(TelaPedido.getWidth(), TelaPedido.getHeight()));

        // Banner Cliente
        boolean mostrarEndereco = "em preparo".equalsIgnoreCase(pedido.getStatusPedido())
                || "pronto".equalsIgnoreCase(pedido.getStatusPedido())
                || "entregue".equalsIgnoreCase(pedido.getStatusPedido());
        if (!mostrarEndereco) {
            JPanel bannerCliente = new JPanel();
            bannerCliente.setBackground(new Color(248, 249, 250));
            bannerCliente.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(240, 240, 240, 150)),
                    BorderFactory.createEmptyBorder(12, 20, 12, 20)
            ));
            bannerCliente.setLayout(new BoxLayout(bannerCliente, BoxLayout.Y_AXIS));
            JLabel lblConfirme = new JLabel("Confirme o pedido para ver os dados do cliente");
            lblConfirme.setForeground(new Color(220, 53, 69));
            lblConfirme.setFont(new Font("Arial", Font.BOLD, 13));
            lblConfirme.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel lblObs = new JLabel("As informações do cliente estão ocultas até que o pedido seja confirmado.");
            lblObs.setForeground(new Color(120, 120, 120));
            lblObs.setFont(new Font("Arial", Font.PLAIN, 11));
            lblObs.setAlignmentX(Component.CENTER_ALIGNMENT);
            bannerCliente.add(lblConfirme);
            bannerCliente.add(lblObs);
            detalhes.add(bannerCliente);
            detalhes.add(Box.createVerticalStrut(8));
        }

        // Header do Pedido
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        JLabel lblPedido = new JLabel("Pedido #" + pedido.getIdPedido());
        lblPedido.setFont(new Font("Arial", Font.PLAIN, 13));
        lblPedido.setForeground(new Color(33, 37, 41));
        JLabel lblFeito = new JLabel(" • Feito às ");
        lblFeito.setFont(new Font("Arial", Font.PLAIN, 13));
        lblFeito.setForeground(new Color(108, 117, 125));
        JLabel lblHora = new JLabel(pedido.getHoraPedido() != null ? pedido.getHoraPedido() : "--:--");
        lblHora.setFont(new Font("Arial", Font.BOLD, 13));
        header.add(lblPedido);
        header.add(lblFeito);
        header.add(lblHora);
        detalhes.add(header);

        detalhes.add(Box.createVerticalStrut(6));

        // Linha de info de entrega
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);

        JLabel iconeRelogio = new JLabel("\u23F0");
        iconeRelogio.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        iconeRelogio.setForeground(new Color(120, 120, 120));
        row.add(iconeRelogio);
        row.add(Box.createHorizontalStrut(4));

        JLabel lblEntrega = new JLabel("Entrega prevista: " + (pedido.getHoraEntrega() != null ? pedido.getHoraEntrega() : "--:--"));
        lblEntrega.setFont(new Font("Arial", Font.PLAIN, 12));
        row.add(lblEntrega);
        row.add(Box.createHorizontalStrut(8));

        JPanel pedidosBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pedidosBox.setBackground(new Color(240, 240, 240));
        pedidosBox.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220, 150)));
        JLabel iconeEstrela = new JLabel("\u2605");
        iconeEstrela.setForeground(new Color(108, 117, 125));
        iconeEstrela.setFont(new Font("Arial", Font.PLAIN, 11));
        JLabel lblQtdPedidos = new JLabel(pedido.getQtdPedidos() + " pedidos");
        lblQtdPedidos.setFont(new Font("Arial", Font.PLAIN, 11));
        lblQtdPedidos.setForeground(new Color(108, 117, 125));
        pedidosBox.add(iconeEstrela);
        pedidosBox.add(lblQtdPedidos);
        row.add(pedidosBox);
        detalhes.add(row);
        detalhes.add(Box.createVerticalStrut(10));

        // Tag "Entrega Parceira"
        JLabel tagEntregaParceira = new JLabel("Entrega Parceira");
        tagEntregaParceira.setFont(new Font("Arial", Font.PLAIN, 10));
        tagEntregaParceira.setOpaque(true);
        tagEntregaParceira.setBackground(new Color(248, 249, 250));
        tagEntregaParceira.setForeground(new Color(220, 53, 69));
        tagEntregaParceira.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 53, 69)),
                new EmptyBorder(2, 8, 2, 8)
        ));
        tagEntregaParceira.setAlignmentX(Component.CENTER_ALIGNMENT);
        detalhes.add(tagEntregaParceira);
        detalhes.add(Box.createVerticalStrut(12));

        // Adiciona endereço somente se já tiver confirmado
        if (mostrarEndereco) {
            adicionarEnderecoAcimaDeEntregaParceira(detalhes, tagEntregaParceira, pedido);
        }

        // Painel de itens do pedido
        JPanel painelPedido = new JPanel();
        painelPedido.setLayout(new BoxLayout(painelPedido, BoxLayout.Y_AXIS));
        painelPedido.setBackground(Color.WHITE);
        painelPedido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230, 150)),
                new EmptyBorder(4, 8, 4, 8)
        ));
        painelPedido.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel pendenteBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pendenteBanner.setBackground(new Color(255, 235, 236));
        pendenteBanner.setBorder(new EmptyBorder(14, 20, 14, 20));
        pendenteBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel lblPendente = new JLabel(
                pedido.getStatusPedido().equalsIgnoreCase("pendente") ? "Pendente"
                : pedido.getStatusPedido().equalsIgnoreCase("em preparo") ? "Em Preparo"
                : pedido.getStatusPedido().equalsIgnoreCase("pronto") ? "Pronto"
                : pedido.getStatusPedido().equalsIgnoreCase("entregue") ? "Entregue" : pedido.getStatusPedido()
        );
        lblPendente.setFont(new Font("Arial", Font.BOLD, 13));
        lblPendente.setForeground(new Color(220, 53, 69));
        JLabel lblTempo = new JLabel("   5 minutos para confirmar o pedido");
        lblTempo.setFont(new Font("Arial", Font.PLAIN, 11));
        lblTempo.setForeground(new Color(220, 53, 69));
        pendenteBanner.add(lblPendente);
        if (pedido.getStatusPedido().equalsIgnoreCase("pendente")) {
            pendenteBanner.add(lblTempo);
        }
        painelPedido.add(pendenteBanner);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(new EmptyBorder(2, 8, 2, 8));

        NumberFormat formatBR = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        for (ItemPedido item : pedido.getItens()) {
            JPanel linhaItem = new JPanel(new BorderLayout());
            linhaItem.setBackground(Color.WHITE);
            JLabel lblQtdItem = new JLabel(item.getQuantidade() + "x " + item.getNomeProduto());
            lblQtdItem.setFont(new Font("Arial", Font.PLAIN, 15));
            JLabel lblPrecoItem = new JLabel(formatBR.format(item.getPreco()));
            lblPrecoItem.setFont(new Font("Arial", Font.PLAIN, 13));
            linhaItem.add(lblQtdItem, BorderLayout.WEST);
            linhaItem.add(lblPrecoItem, BorderLayout.EAST);
            itemsPanel.add(linhaItem);
            itemsPanel.add(Box.createVerticalStrut(2));
        }

        itemsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        itemsPanel.add(Box.createVerticalStrut(4));

        JPanel linhaSubtotal = new JPanel(new BorderLayout());
        linhaSubtotal.setBackground(Color.WHITE);
        linhaSubtotal.setBorder(new EmptyBorder(2, 0, 0, 0));
        JLabel lblSubtotal = new JLabel("Subtotal");
        lblSubtotal.setFont(new Font("Arial", Font.PLAIN, 15));
        JLabel lblPrecoSubtotal = new JLabel(formatBR.format(39.99)); // Troque por seu cálculo real
        lblPrecoSubtotal.setFont(new Font("Arial", Font.BOLD, 13));
        linhaSubtotal.add(lblSubtotal, BorderLayout.WEST);
        linhaSubtotal.add(lblPrecoSubtotal, BorderLayout.EAST);
        itemsPanel.add(linhaSubtotal);

        painelPedido.add(itemsPanel);
        painelPedido.add(new JSeparator());

        // Pagamento
        JPanel pagamentoBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pagamentoBanner.setBackground(new Color(245, 245, 245));
        pagamentoBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230, 150)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        pagamentoBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel iconePagamento = new JLabel("\u0024");
        iconePagamento.setFont(new Font("Arial", Font.BOLD, 18));
        iconePagamento.setForeground(new Color(120, 120, 120));
        pagamentoBanner.add(iconePagamento);
        pagamentoBanner.add(Box.createHorizontalStrut(10));

        JPanel textoPagamento = new JPanel();
        textoPagamento.setOpaque(false);
        textoPagamento.setLayout(new BoxLayout(textoPagamento, BoxLayout.Y_AXIS));
        JLabel lblConfirmePagamento = new JLabel("Confirme o pedido para ver a forma de pagamento");
        lblConfirmePagamento.setFont(new Font("Arial", Font.BOLD, 12));
        lblConfirmePagamento.setForeground(new Color(120, 120, 120));
        JLabel lblObsPagamento = new JLabel("As informações de pagamento estão ocultas até que o pedido seja confirmado.");
        lblObsPagamento.setFont(new Font("Arial", Font.PLAIN, 10));
        lblObsPagamento.setForeground(new Color(120, 120, 120));
        textoPagamento.add(lblConfirmePagamento);
        textoPagamento.add(lblObsPagamento);

        pagamentoBanner.add(textoPagamento);
        painelPedido.add(pagamentoBanner);

        detalhes.add(painelPedido);
        detalhes.add(Box.createVerticalStrut(12));

        // Painel de Botões (rodapé)
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBotoes.setOpaque(false);

        // Só mostra o cancelar se for pendente!
        if ("pendente".equalsIgnoreCase(pedido.getStatusPedido())) {
            JButton btnCancelar = new JButton("Cancelar");
            estilizarBotao(btnCancelar, Color.WHITE, 13);
            btnCancelar.setForeground(new Color(220, 53, 69));
            btnCancelar.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 1));
            btnCancelar.setPreferredSize(new Dimension(110, 30));
            panelBotoes.add(btnCancelar);
            panelBotoes.add(Box.createHorizontalStrut(8));

            JButton btnConfirmar = new JButton("Confirmar pedido");
            estilizarBotao(btnConfirmar, new Color(220, 53, 69), 13);
            btnConfirmar.setPreferredSize(new Dimension(140, 34));
            panelBotoes.add(btnConfirmar);

            btnConfirmar.addActionListener((ActionEvent e) -> {
                mudarStatusPedidoBanco(pedido.getIdPedido(), "em preparo");
                dao.recarregarPedidos(); // <-- ATUALIZA O CACHE AGORA!
                Pedidos pedidoAtualizado = dao.buscarPedidoPorId(pedido.getIdPedido());
                mostrarPedidoPendenteDelivery(pedidoAtualizado);
                criarMenuPedido(); // <-- RECONSTRÓI CARDS NA TELA!
                atualizarContadorPendentes();
            });
        } else if ("em preparo".equalsIgnoreCase(pedido.getStatusPedido())) {
            JButton btnFinalizar = new JButton("Finalizar");
            estilizarBotao(btnFinalizar, new Color(25, 135, 84), 13);
            btnFinalizar.setPreferredSize(new Dimension(130, 34));
            panelBotoes.add(btnFinalizar);

            btnFinalizar.addActionListener(ev -> {
                mudarStatusPedidoBanco(pedido.getIdPedido(), "pronto"); // ou "finalizado" se for o último status
                dao.recarregarPedidos();
                Pedidos pedidoAtualizado = dao.buscarPedidoPorId(pedido.getIdPedido());
                mostrarPedidoPendenteDelivery(pedidoAtualizado);
                criarMenuPedido();
                JOptionPane.showMessageDialog(TelaPedido, "Pedido finalizado com sucesso!", "Finalizado", JOptionPane.INFORMATION_MESSAGE);
            });
        }
        // Adiciona o painel de botões no final
        detalhes.add(panelBotoes);

        JScrollPane scrollPane = new JScrollPane(detalhes);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        TelaPedido.setLayout(new BorderLayout());
        TelaPedido.add(scrollPane, BorderLayout.CENTER);
        TelaPedido.revalidate();
        TelaPedido.repaint();
        TelaPedido.setVisible(true);
    }

    private void mudarStatusPedidoBanco(String idPedido, String statusNovo) {
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = banco.abrirConexao();
            // 1. Buscar o status_id
            String queryStatus = "SELECT status_id FROM tb_status_pedido WHERE status_nome = ?";
            stmt = conn.prepareStatement(queryStatus);
            stmt.setString(1, statusNovo);
            rs = stmt.executeQuery();
            int statusId = -1;
            if (rs.next()) {
                statusId = rs.getInt("status_id");
            }
            rs.close();
            stmt.close();

            if (statusId != -1) {
                // 2. Atualizar o pedido clicado
                String queryUpdate = "UPDATE tb_pedidos SET status_id = ? WHERE ID_pedido = ?";
                stmt = conn.prepareStatement(queryUpdate);
                stmt.setInt(1, statusId);
                stmt.setString(2, idPedido);
                int linhasAfetadas = stmt.executeUpdate();

                if (linhasAfetadas > 0) {
                    System.out.println("Status do pedido atualizado com sucesso!");
                } else {
                    System.out.println("Nenhum pedido encontrado com o ID informado.");
                }
            } else {
                System.out.println("Status não encontrado!");
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao atualizar valor: " + ex.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    banco.fecharConexao();
                }
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }
// Função utilitária para adicionar o endereço centralizado acima do tagEntregaParceira

    private void adicionarEnderecoAcimaDeEntregaParceira(JPanel container, Component tagEntregaParceira, Pedidos pedido) {
        JPanel enderecoPanel = new JPanel();
        enderecoPanel.setLayout(new BoxLayout(enderecoPanel, BoxLayout.Y_AXIS));
        enderecoPanel.setBackground(Color.WHITE);
        enderecoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230, 180), 1),
                new EmptyBorder(18, 36, 18, 36)
        ));
        enderecoPanel.setMaximumSize(new Dimension(400, 400));
        enderecoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTituloEndereco = new JLabel("Endereço de Entrega");
        lblTituloEndereco.setFont(new Font("Arial", Font.BOLD, 15));
        lblTituloEndereco.setForeground(new Color(33, 37, 41));
        lblTituloEndereco.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEndereco = new JLabel("<html><div style='text-align: center;'>" + pedido.getEnderecoCompleto() + "</div></html>");
        lblEndereco.setFont(new Font("Arial", Font.PLAIN, 13));
        lblEndereco.setForeground(new Color(80, 80, 80));
        lblEndereco.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblEndereco.setBorder(new EmptyBorder(8, 0, 0, 0));

        enderecoPanel.add(lblTituloEndereco);
        enderecoPanel.add(lblEndereco);

        // Descobre o índice do tagEntregaParceira e insere o endereço antes dele
        int idx = -1;
        for (int i = 0; i < container.getComponentCount(); i++) {
            if (container.getComponent(i) == tagEntregaParceira) {
                idx = i;
                break;
            }
        }
        if (idx != -1) {
            container.add(Box.createVerticalStrut(12), idx);
            container.add(enderecoPanel, idx + 1);
            container.add(Box.createVerticalStrut(10), idx + 2);
        } else {
            // Se não achou, adiciona ao final como fallback
            container.add(Box.createVerticalStrut(12));
            container.add(enderecoPanel);
            container.add(Box.createVerticalStrut(10));
        }
    }

    class RoundedBorder extends AbstractBorder {

        private final int radius;
        private final int padding;

        public RoundedBorder(int radius) {
            this(radius, 8);
        }

        public RoundedBorder(int radius, int padding) {
            this.radius = radius;
            this.padding = padding;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(230, 230, 230));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(padding, padding, padding, padding);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = padding;
            return insets;
        }
    }

    private void mostrarPedidoFinalizado(Pedidos pedido) {
        TelaPedido.removeAll();

        JPanel detalhes = new JPanel();
        detalhes.setLayout(new BoxLayout(detalhes, BoxLayout.Y_AXIS));
        detalhes.setBackground(Color.WHITE);
        detalhes.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // Header do Pedido
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        JLabel lblPedido = new JLabel("Pedido FINALIZADO #" + pedido.getIdPedido());
        lblPedido.setFont(new Font("Arial", Font.BOLD, 15));
        lblPedido.setForeground(new Color(25, 135, 84));
        JLabel lblFeito = new JLabel(" • Feito às ");
        lblFeito.setFont(new Font("Arial", Font.PLAIN, 13));
        lblFeito.setForeground(new Color(108, 117, 125));
        JLabel lblHora = new JLabel(pedido.getHoraPedido() != null ? pedido.getHoraPedido() : "--:--");
        lblHora.setFont(new Font("Arial", Font.BOLD, 13));
        header.add(lblPedido);
        header.add(lblFeito);
        header.add(lblHora);
        detalhes.add(header);

        detalhes.add(Box.createVerticalStrut(8));

        // Info de cliente/mesa/local
        JLabel lblCliente = new JLabel("Cliente: " + (pedido.getNomeCliente() != null ? pedido.getNomeCliente() : "N/A"));
        lblCliente.setFont(new Font("Arial", Font.PLAIN, 13));
        lblCliente.setForeground(new Color(108, 117, 125));
        lblCliente.setAlignmentX(Component.CENTER_ALIGNMENT);
        detalhes.add(lblCliente);

        detalhes.add(Box.createVerticalStrut(6));

        JLabel lblMesa = new JLabel("Mesa/Balcão: " + (pedido.getMesa() != null ? pedido.getMesa() : "N/A"));
        lblMesa.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMesa.setForeground(new Color(108, 117, 125));
        lblMesa.setAlignmentX(Component.CENTER_ALIGNMENT);
        detalhes.add(lblMesa);

        detalhes.add(Box.createVerticalStrut(10));

        // Painel de itens do pedido
        JPanel painelPedido = new JPanel();
        painelPedido.setLayout(new BoxLayout(painelPedido, BoxLayout.Y_AXIS));
        painelPedido.setBackground(Color.WHITE);
        painelPedido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230, 150)),
                new EmptyBorder(4, 8, 4, 8)
        ));
        painelPedido.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel statusBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusBanner.setBackground(new Color(235, 245, 255));
        statusBanner.setBorder(new EmptyBorder(14, 20, 14, 20));
        statusBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel lblStatus = new JLabel("Finalizado");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 15));
        lblStatus.setForeground(new Color(25, 135, 84));
        statusBanner.add(lblStatus);
        painelPedido.add(statusBanner);

        // Lista de Itens
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(new EmptyBorder(2, 8, 2, 8));

        NumberFormat formatBR = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        for (ItemPedido item : pedido.getItens()) {
            JPanel linhaItem = new JPanel(new BorderLayout());
            linhaItem.setBackground(Color.WHITE);
            JLabel lblQtdItem = new JLabel(item.getQuantidade() + "x " + item.getNomeProduto());
            lblQtdItem.setFont(new Font("Arial", Font.PLAIN, 15));
            JLabel lblPrecoItem = new JLabel(formatBR.format(item.getPreco()));
            lblPrecoItem.setFont(new Font("Arial", Font.PLAIN, 13));
            linhaItem.add(lblQtdItem, BorderLayout.WEST);
            linhaItem.add(lblPrecoItem, BorderLayout.EAST);
            itemsPanel.add(linhaItem);
            itemsPanel.add(Box.createVerticalStrut(2));
        }

        itemsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        itemsPanel.add(Box.createVerticalStrut(4));

        JPanel linhaSubtotal = new JPanel(new BorderLayout());
        linhaSubtotal.setBackground(Color.WHITE);
        linhaSubtotal.setBorder(new EmptyBorder(2, 0, 0, 0));
        JLabel lblSubtotal = new JLabel("Subtotal");
        lblSubtotal.setFont(new Font("Arial", Font.PLAIN, 15));
        JLabel lblPrecoSubtotal = new JLabel(formatBR.format(pedido.getSubtotal()));
        lblPrecoSubtotal.setFont(new Font("Arial", Font.BOLD, 13));
        linhaSubtotal.add(lblSubtotal, BorderLayout.WEST);
        linhaSubtotal.add(lblPrecoSubtotal, BorderLayout.EAST);
        itemsPanel.add(linhaSubtotal);

        painelPedido.add(itemsPanel);
        painelPedido.add(new JSeparator());

        // Pagamento
        JPanel pagamentoBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pagamentoBanner.setBackground(new Color(245, 245, 245));
        pagamentoBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230, 150)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        pagamentoBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        JLabel iconePagamento = new JLabel("\u0024");
        iconePagamento.setFont(new Font("Arial", Font.BOLD, 18));
        iconePagamento.setForeground(new Color(120, 120, 120));
        pagamentoBanner.add(iconePagamento);
        pagamentoBanner.add(Box.createHorizontalStrut(10));

        JPanel textoPagamento = new JPanel();
        textoPagamento.setOpaque(false);
        textoPagamento.setLayout(new BoxLayout(textoPagamento, BoxLayout.Y_AXIS));
        JLabel lblFormaPagamento = new JLabel("Forma de pagamento: " + pedido.getFormaPagamento());
        lblFormaPagamento.setFont(new Font("Arial", Font.BOLD, 12));
        lblFormaPagamento.setForeground(new Color(120, 120, 120));
        JLabel lblObsPagamento = new JLabel("Confira todos os itens e valores para análise gerencial.");
        lblObsPagamento.setFont(new Font("Arial", Font.PLAIN, 10));
        lblObsPagamento.setForeground(new Color(120, 120, 120));
        textoPagamento.add(lblFormaPagamento);
        textoPagamento.add(lblObsPagamento);
        pagamentoBanner.add(textoPagamento);
        painelPedido.add(pagamentoBanner);

        // Observações do pedido, se houver
        if (pedido.getObservacoes() != null && !pedido.getObservacoes().trim().isEmpty()) {
            JLabel lblObs = new JLabel("Observações: " + pedido.getObservacoes());
            lblObs.setFont(new Font("Arial", Font.ITALIC, 12));
            lblObs.setForeground(new Color(33, 37, 41));
            detalhes.add(lblObs);
            detalhes.add(Box.createVerticalStrut(6));
        }

        detalhes.add(painelPedido);
        detalhes.add(Box.createVerticalStrut(12));

        // Painel de Botões (rodapé)
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBotoes.setOpaque(false);

        JButton btnFechar = new JButton("Fechar");
        estilizarBotao(btnFechar, new Color(33, 37, 41), 13);
        btnFechar.setPreferredSize(new Dimension(110, 30));
        btnFechar.addActionListener(e -> TelaPedido.setVisible(false));
        panelBotoes.add(btnFechar);

        detalhes.add(panelBotoes);

        JScrollPane scrollPane = new JScrollPane(detalhes);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        TelaPedido.setLayout(new BorderLayout());
        TelaPedido.add(scrollPane, BorderLayout.CENTER);
        TelaPedido.revalidate();
        TelaPedido.repaint();
        TelaPedido.setVisible(true);
    }
    
    private void buscarPedidos(String pesquisa) {
        ArrayList<Pedidos> pedidos = dao.buscarTodosPedidos();

        jPanel3.removeAll();

        String pesquisaFormatada = pesquisa.toLowerCase().replace("#", "").trim();
        for (Pedidos pedido : pedidos) {

            if (pedido.getIdPedido().trim().toLowerCase().contains(pesquisaFormatada)
                    || pedido.getStatusPedido().trim().toLowerCase().contains(pesquisaFormatada)
                    || pedido.getTipoPedido().trim().toLowerCase().contains(pesquisaFormatada)) {

                JPanel pedidoCard = criarPedidoCard(pedido);
                jPanel3.add(Box.createVerticalStrut(10));
                jPanel3.add(pedidoCard);
            }
        }

        jPanel3.revalidate();
        jPanel3.repaint();
    }

    private void atualizarContadorPendentes() {
        int qtd = dao.quantidadePedidosPendentes();
        quantidadePedidos.setText(String.valueOf(qtd));
    }

    public void limparInput() {
        inputBuscar.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Limpa o texto quando o campo recebe o foco
                if (inputBuscar.getText().equals("Buscar")) {
                    inputBuscar.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Caso o campo perca o foco e esteja vazio, restaura o texto original
                if (inputBuscar.getText().isEmpty()) {
                    inputBuscar.setText("Buscar");
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        MenuPedido = new javax.swing.JPanel();
        inputBuscar = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        quantidadePedidos = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        JScrollPanel = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        TelaPedido = new javax.swing.JPanel();

        jLabel2.setText("jLabel2");

        setLayout(new java.awt.BorderLayout());

        MenuPedido.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        inputBuscar.setText("Buscar");
        inputBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputBuscarActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Pendente", "Pronto", "Cancelado", "Finalizado" }));
        jComboBox1.setToolTipText("");
        jComboBox1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Pendentes");

        quantidadePedidos.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 392, Short.MAX_VALUE)
        );

        JScrollPanel.setViewportView(jPanel3);

        javax.swing.GroupLayout MenuPedidoLayout = new javax.swing.GroupLayout(MenuPedido);
        MenuPedido.setLayout(MenuPedidoLayout);
        MenuPedidoLayout.setHorizontalGroup(
            MenuPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(MenuPedidoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(MenuPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(MenuPedidoLayout.createSequentialGroup()
                        .addComponent(JScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(MenuPedidoLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(quantidadePedidos)
                        .addContainerGap())
                    .addGroup(MenuPedidoLayout.createSequentialGroup()
                        .addComponent(inputBuscar, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))))
        );
        MenuPedidoLayout.setVerticalGroup(
            MenuPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MenuPedidoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(MenuPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inputBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(MenuPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(quantidadePedidos))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(MenuPedido, java.awt.BorderLayout.LINE_START);

        TelaPedido.setLayout(new java.awt.BorderLayout());
        add(TelaPedido, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        String filtroSelecionado = jComboBox1.getSelectedItem().toString().toLowerCase();
        System.out.println(filtroSelecionado);

        if (filtroSelecionado.equalsIgnoreCase("Todos")) {
            criarMenuPedido(); // mostra todos
        } else {
            ArrayList<Pedidos> pedidos = dao.buscarTodosPedidos();

            jPanel3.removeAll();
            for (Pedidos pedido : pedidos) {
                if (pedido.getStatusPedido().equalsIgnoreCase(filtroSelecionado)) {
                    JPanel pedidoCard = criarPedidoCard(pedido);
                    jPanel3.add(Box.createVerticalStrut(10));
                    jPanel3.add(pedidoCard);
                }
            }

            jPanel3.revalidate();
            jPanel3.repaint();
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void inputBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputBuscarActionPerformed

        String textoDigitado = inputBuscar.getText().trim();
        System.out.println(textoDigitado);

        if (!textoDigitado.equals("") && !textoDigitado.equalsIgnoreCase("Buscar")) {
            buscarPedidos(textoDigitado);
        } else {
            criarMenuPedido(); // Mostra todos se o campo estiver vazio
        }
    }//GEN-LAST:event_inputBuscarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane JScrollPanel;
    private javax.swing.JPanel MenuPedido;
    private javax.swing.JPanel TelaPedido;
    private javax.swing.JTextField inputBuscar;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel quantidadePedidos;
    // End of variables declaration//GEN-END:variables
}
