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
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
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
    
private void mostrarDetalhesPedido(Pedidos pedido) {
    TelaPedido.removeAll();

    JPanel detalhes = new JPanel();
    detalhes.setLayout(new BoxLayout(detalhes, BoxLayout.Y_AXIS));
    detalhes.setBackground(Color.WHITE);
    detalhes.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

    // Cabeçalho com nome do cliente, ID e horário do pedido
    JLabel clienteInfo = new JLabel(pedido.getNomeCliente() + " - Pedido #" + pedido.getIdPedido());
    clienteInfo.setFont(new Font("Segoe UI", Font.BOLD, 20));
    clienteInfo.setForeground(new Color(33, 37, 41));  // cinza escuro
    clienteInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel horarioInfo = new JLabel("Feito às " + pedido.getHoraPedido());
    horarioInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    horarioInfo.setForeground(new Color(73, 80, 87));
    horarioInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel localizador = new JLabel("Localizador do pedido: " + pedido.getCodigoLocalizador());
    localizador.setFont(new Font("Segoe UI", Font.ITALIC, 12));
    localizador.setForeground(new Color(108, 117, 125));
    localizador.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel header = new JPanel(new GridLayout(3, 1));
    header.setBackground(Color.WHITE);
    header.add(clienteInfo);
    header.add(horarioInfo);
    header.add(localizador);

    // Entrega prevista e botão "Entrar em contato"
    JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    topRight.setBackground(Color.WHITE);

    JLabel entregaPrevista = new JLabel("Entrega prevista: " + pedido.getHoraEntrega());
    entregaPrevista.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    entregaPrevista.setForeground(new Color(73, 80, 87));
    topRight.add(entregaPrevista);

    JButton btnContato = new JButton("Entrar em contato");
    btnContato.setBackground(new Color(0, 123, 255));
    btnContato.setForeground(Color.WHITE);
    btnContato.setFocusPainted(false);
    btnContato.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btnContato.setCursor(new Cursor(Cursor.HAND_CURSOR));
    topRight.add(btnContato);

    JPanel headerContainer = new JPanel(new BorderLayout());
    headerContainer.setBackground(Color.WHITE);
    headerContainer.add(header, BorderLayout.CENTER);
    headerContainer.add(topRight, BorderLayout.EAST);

    detalhes.add(headerContainer);
    detalhes.add(Box.createVerticalStrut(20));

    // Modo de entrega e observações
    JLabel modoEntrega = new JLabel("Modo de entrega: " + pedido.getModoEntrega());
    modoEntrega.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    modoEntrega.setForeground(new Color(73, 80, 87));
    modoEntrega.setAlignmentX(Component.LEFT_ALIGNMENT);
    detalhes.add(modoEntrega);

    if (pedido.getObservacoes() != null && !pedido.getObservacoes().isEmpty()) {
        JLabel observacoes = new JLabel("<html><i>" + pedido.getObservacoes() + "</i></html>");
        observacoes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        observacoes.setForeground(new Color(220, 53, 69));  // vermelho suave
        observacoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        detalhes.add(Box.createVerticalStrut(5));
        detalhes.add(observacoes);
    }

    detalhes.add(Box.createVerticalStrut(20));

    // Endereço com ícone
    JLabel endereco = new JLabel("📍 " + pedido.getEnderecoCompleto());
    endereco.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    endereco.setForeground(new Color(73, 80, 87));
    endereco.setAlignmentX(Component.LEFT_ALIGNMENT);
    detalhes.add(endereco);

    detalhes.add(Box.createVerticalStrut(20));

    // Entregador
    JLabel entregador = new JLabel("Entregador a caminho: " + pedido.getNomeEntregador() + " - " + pedido.getTelefoneEntregador());
    entregador.setFont(new Font("Segoe UI", Font.BOLD, 15));
    entregador.setForeground(new Color(40, 167, 69));  // verde vibrante
    entregador.setAlignmentX(Component.LEFT_ALIGNMENT);
    detalhes.add(entregador);

    detalhes.add(Box.createVerticalStrut(25));

    // Itens do pedido (sem observações)
    if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
        JLabel tituloItens = new JLabel("Itens do pedido:");
        tituloItens.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tituloItens.setForeground(new Color(33, 37, 41));
        tituloItens.setAlignmentX(Component.LEFT_ALIGNMENT);
        detalhes.add(tituloItens);
        detalhes.add(Box.createVerticalStrut(10));

        for (ItemPedido item : pedido.getItens()) {
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBackground(new Color(245, 245, 245));
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)));

            JLabel nomeItem = new JLabel(item.getQuantidade() + "x " + item.getNomeProduto());
            nomeItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            nomeItem.setForeground(new Color(33, 37, 41));
            itemPanel.add(nomeItem, BorderLayout.WEST);

            JLabel preco = new JLabel("R$ " + String.format("%.2f", item.getPreco()));
            preco.setFont(new Font("Segoe UI", Font.BOLD, 14));
            preco.setForeground(new Color(33, 37, 41));
            itemPanel.add(preco, BorderLayout.EAST);

            detalhes.add(itemPanel);
            detalhes.add(Box.createVerticalStrut(10));
        }
    }

    // Botão Avisar pedido pronto
    JButton btnAvisarPronto = new JButton("Avisar pedido pronto");
    btnAvisarPronto.setBackground(new Color(40, 167, 69));
    btnAvisarPronto.setForeground(Color.WHITE);
    btnAvisarPronto.setFocusPainted(false);
    btnAvisarPronto.setFont(new Font("Segoe UI", Font.BOLD, 15));
    btnAvisarPronto.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnAvisarPronto.setAlignmentX(Component.CENTER_ALIGNMENT);

    detalhes.add(Box.createVerticalStrut(30));
    detalhes.add(btnAvisarPronto);

    TelaPedido.add(detalhes, BorderLayout.CENTER);
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
        jLabel3 = new javax.swing.JLabel();

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

        jLabel3.setText("monkey");
        TelaPedido.add(jLabel3, java.awt.BorderLayout.CENTER);

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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel quantidadePedidos;
    // End of variables declaration//GEN-END:variables
}
