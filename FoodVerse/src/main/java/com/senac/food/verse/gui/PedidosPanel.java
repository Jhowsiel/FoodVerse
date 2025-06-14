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
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
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
            if (dao.haNovoPedido()) {
                dao.recarregarPedidos();
                criarMenuPedido();
                atualizarContadorPendentes();
                System.out.println("Novos pedidos! Interface atualizada.");
            }
        }).start();

        inputBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
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

    private JPanel criarPedidoCard(Pedidos pedido) {
        // --- Painel principal ---
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Antialiasing caso queira desenhar algo extra
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
            }
        };
        card.setLayout(new GroupLayout(card));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // --- Hover e clique ---
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
                mostrarDetalhesPedido(pedido);
            }
        });

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
                new Color(240, 230, 255), // fundo lilás claro
                new Color(180, 130, 255), // borda roxa suave
                16, 16, // arco de cantos
                6 // padding interno
        );

        // --- Layout ---
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(idLabel)
                                .addGap(6)
                                .addComponent(tagLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, Short.MAX_VALUE)
                                .addComponent(tempoLabel))
                        .addComponent(statusLabel)
                        .addComponent(modoEntrega, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
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
        BorderFactory.createLineBorder(corFundo.darker(), 1),
        BorderFactory.createEmptyBorder(10, 20, 10, 20)
    ));
    botao.setAlignmentX(Component.CENTER_ALIGNMENT);
    botao.setOpaque(true);
}

private void mostrarDetalhesPedido(Pedidos pedido) {
    TelaPedido.removeAll();

    JPanel detalhes = new JPanel();
    detalhes.setLayout(new BoxLayout(detalhes, BoxLayout.Y_AXIS));
    detalhes.setBackground(Color.decode("#F9FAFB")); // tom neutro e moderno
    detalhes.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JButton btnVoltar = new JButton("← Voltar");
    estilizarBotao(btnVoltar, new Color(108, 117, 125), 13);
    btnVoltar.setAlignmentX(Component.LEFT_ALIGNMENT);
    btnVoltar.addActionListener(e -> TelaPedido.setVisible(false));
    detalhes.add(btnVoltar);
    detalhes.add(Box.createVerticalStrut(20));

    // HEADER
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(Color.WHITE);
    header.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(230, 230, 230)),
        BorderFactory.createEmptyBorder(15, 20, 15, 20)
    ));

    // Header Left
    JPanel headerLeft = new JPanel();
    headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
    headerLeft.setBackground(Color.WHITE);

    JLabel clienteNome = new JLabel(pedido.getNomeCliente());
    clienteNome.setFont(new Font("Segoe UI", Font.BOLD, 20));
    clienteNome.setForeground(new Color(33, 37, 41));

    JLabel pedidoId = new JLabel("Pedido #" + pedido.getIdPedido());
    pedidoId.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    pedidoId.setForeground(new Color(108, 117, 125));

    JLabel localizador = new JLabel("Localizador: " + pedido.getCodigoLocalizador());
    localizador.setFont(new Font("Segoe UI", Font.ITALIC, 12));
    localizador.setForeground(new Color(173, 181, 189));

    headerLeft.add(clienteNome);
    headerLeft.add(Box.createVerticalStrut(4));
    headerLeft.add(pedidoId);
    headerLeft.add(Box.createVerticalStrut(4));
    headerLeft.add(localizador);

    // Header Right
    JPanel headerRight = new JPanel();
    headerRight.setLayout(new BoxLayout(headerRight, BoxLayout.Y_AXIS));
    headerRight.setBackground(Color.WHITE);
    headerRight.setAlignmentY(Component.TOP_ALIGNMENT);

    JLabel entregaPrevista = new JLabel("Entrega: " + pedido.getHoraEntrega());
    entregaPrevista.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    entregaPrevista.setForeground(new Color(52, 58, 64));

    JButton btnContato = new JButton("Entrar em contato");
    estilizarBotao(btnContato, new Color(0, 123, 255), 13);

    headerRight.add(entregaPrevista);
    headerRight.add(Box.createVerticalStrut(8));
    headerRight.add(btnContato);

    header.add(headerLeft, BorderLayout.CENTER);
    header.add(headerRight, BorderLayout.EAST);
    detalhes.add(header);
    detalhes.add(Box.createVerticalStrut(20));

    // Cards
    detalhes.add(criarCard("🛡 Entrega sem contato", "O cliente solicitou deixar o pedido na portaria."));
    detalhes.add(Box.createVerticalStrut(15));
    detalhes.add(criarCard("📍 Endereço", pedido.getEnderecoCompleto()));
    detalhes.add(Box.createVerticalStrut(15));

    // Entregador
    detalhes.add(criarEntregaHeader(pedido));
    detalhes.add(Box.createVerticalStrut(20));

    if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
        JLabel lblItens = new JLabel("Itens do Pedido:");
        lblItens.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblItens.setForeground(new Color(33, 37, 41));
        lblItens.setAlignmentX(Component.LEFT_ALIGNMENT);
        detalhes.add(lblItens);
        detalhes.add(Box.createVerticalStrut(10));

        for (ItemPedido item : pedido.getItens()) {
            detalhes.add(criarItemCard(item));
        }
    }

    JButton btnFinalizar = new JButton("✔ Avisar que o pedido está pronto");
    estilizarBotao(btnFinalizar, new Color(25, 135, 84), 15);
    btnFinalizar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
    detalhes.add(Box.createVerticalStrut(20));
    detalhes.add(btnFinalizar);

    JScrollPane scrollPane = new JScrollPane(detalhes);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    TelaPedido.setLayout(new BorderLayout());
    TelaPedido.add(scrollPane, BorderLayout.CENTER);
    TelaPedido.revalidate();
    TelaPedido.repaint();
    TelaPedido.setVisible(true);
}

private JPanel criarCard(String titulo, String descricao) {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(230, 230, 230)),
        BorderFactory.createEmptyBorder(12, 20, 12, 20)
    ));
    card.setBackground(Color.WHITE);
    card.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));

    JLabel lblDesc = new JLabel("<html>" + descricao + "</html>");
    lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    lblDesc.setForeground(new Color(90, 90, 90));

    card.add(lblTitulo);
    card.add(Box.createVerticalStrut(5));
    card.add(lblDesc);

    return card;
}

private JPanel criarEntregaHeader(Pedidos pedido) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(new Color(209, 250, 229));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

    JLabel lblEsquerda = new JLabel("Entregador a caminho");
    lblEsquerda.setFont(new Font("Segoe UI", Font.BOLD, 14));
    lblEsquerda.setForeground(new Color(25, 135, 84));

    JLabel lblDireita = new JLabel("<html>" + pedido.getNomeEntregador() + "<br/>" + pedido.getTelefoneEntregador() + " • Moto</html>");
    lblDireita.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lblDireita.setForeground(new Color(33, 37, 41));

    panel.add(lblEsquerda, BorderLayout.WEST);
    panel.add(lblDireita, BorderLayout.EAST);

    return panel;
}

private JPanel criarItemCard(ItemPedido item) {
    JPanel itemPanel = new JPanel(new BorderLayout());
    itemPanel.setBackground(Color.WHITE);
    itemPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(230, 230, 230)),
        BorderFactory.createEmptyBorder(10, 15, 10, 15)
    ));

    JLabel nome = new JLabel(item.getQuantidade() + "x " + item.getNomeProduto());
    nome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    nome.setForeground(new Color(33, 37, 41));

    JLabel preco = new JLabel("R$ " + String.format("%.2f", item.getPreco()));
    preco.setFont(new Font("Segoe UI", Font.BOLD, 14));
    preco.setForeground(new Color(33, 37, 41));

    itemPanel.add(nome, BorderLayout.WEST);
    itemPanel.add(preco, BorderLayout.EAST);

    JPanel container = new JPanel();
    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
    container.setBackground(Color.WHITE);
    container.add(itemPanel);

    if (item.getObservacao() != null && !item.getObservacao().isEmpty()) {
        JLabel obs = new JLabel("📝 " + item.getObservacao());
        obs.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        obs.setForeground(new Color(108, 117, 125));
        obs.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        container.add(obs);
    }

    container.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
    return container;
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

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Pendente", "Pronto", "Entregue", "Cancelado", "Finalizado" }));
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
