package com.senac.food.verse.gui;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
import com.senac.food.verse.ConexaoBanco;
import com.senac.food.verse.PedidoDAO;
import com.senac.food.verse.Pedidos;
import com.senac.food.verse.RoundedLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
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
public class PedidosPanel extends javax.swing.JPanel {

    /**
     * Creates new form PedidosPanel
     */
    public PedidosPanel() {
        initComponents();
        estilizarComboBoxSeta();
        criarMenuPedido();
        quantidadePedidosPendentes();

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
                // aqui você dispara a lógica para mostrar detalhes no main
                System.out.println("Clicou no pedido #" + pedido.getIdPedido());
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
        PedidoDAO dao = new PedidoDAO();
        ArrayList<Pedidos> pedidos = dao.buscarTodosPedidos();

        jPanel3.removeAll(); // Limpa os pedidos anteriores
        jPanel3.setLayout(new BoxLayout(jPanel3, BoxLayout.Y_AXIS));

        for (Pedidos pedido : pedidos) {
            JPanel pedidoCard = criarPedidoCard(pedido);
            jPanel3.add(Box.createVerticalStrut(10)); // Espaço entre os cards
            jPanel3.add(pedidoCard);
        }

        jPanel3.revalidate();
        jPanel3.repaint();
    }

    public void quantidadePedidosPendentes() {
        PedidoDAO dao = new PedidoDAO();
        int quantidade = dao.quantidadePedidos(); 
        
        System.out.println(quantidade);
        quantidadePedidos.setText(String.valueOf(quantidade)); 
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        quantidadePedidos = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jTextField1.setText("Buscar");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos" }));
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
            .addGap(0, 307, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 372, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(quantidadePedidos)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))))
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(quantidadePedidos))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.LINE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed

    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel quantidadePedidos;
    // End of variables declaration//GEN-END:variables
}
