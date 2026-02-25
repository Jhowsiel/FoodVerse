package com.senac.food.verse.gui;

import com.senac.food.verse.Reserva;
import com.senac.food.verse.ReservaDAO;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GestaoMesasPanel extends JPanel {

    private final ReservaDAO dao = new ReservaDAO();
    private JPanel containerMesas;

    public GestaoMesasPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_DARK);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Mapa de Mesas & Reservas");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.FG_LIGHT);
        // EVENT_SEAT funcionou, então é seguro usar
        title.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.EVENT_SEAT, 28, UIConstants.FG_LIGHT));
        header.add(title, BorderLayout.WEST);

        JButton btnNova = createButton("Nova Reserva", GoogleMaterialDesignIcons.ADD, UIConstants.PRIMARY_RED);
        btnNova.addActionListener(e -> abrirModalNovaReserva());
        
        JButton btnRefresh = createButton("Atualizar", GoogleMaterialDesignIcons.REFRESH, UIConstants.BG_DARK_ALT);
        btnRefresh.addActionListener(e -> carregarMesas());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.setOpaque(false);
        botoes.add(btnNova);
        botoes.add(btnRefresh);
        header.add(botoes, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);

        // --- Área das Mesas ---
        containerMesas = new JPanel(new GridLayout(0, 5, 15, 15)); // Grid responsivo (5 colunas)
        containerMesas.setBackground(UIConstants.BG_DARK);
        containerMesas.setBorder(new EmptyBorder(10, 20, 20, 20));

        JScrollPane scroll = new JScrollPane(containerMesas);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // --- Legenda ---
        JPanel legenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        legenda.setBackground(UIConstants.BG_DARK_ALT);
        legenda.add(criarItemLegenda("Livre", UIConstants.SUCCESS_GREEN));
        legenda.add(criarItemLegenda("Reservada", Color.ORANGE));
        legenda.add(criarItemLegenda("Ocupada", UIConstants.PRIMARY_RED));
        add(legenda, BorderLayout.SOUTH);

        carregarMesas();
    }

    private void carregarMesas() {
        containerMesas.removeAll();
        
        List<String> todasMesas = dao.getListaMesas();
        List<Reserva> reservasHoje = dao.listarReservasDoDia();

        for (String nomeMesa : todasMesas) {
            // Verifica status
            Reserva reservaDaMesa = reservasHoje.stream()
                    .filter(r -> r.getMesa().equalsIgnoreCase(nomeMesa))
                    .findFirst().orElse(null);

            containerMesas.add(criarCardMesa(nomeMesa, reservaDaMesa));
        }
        
        containerMesas.revalidate();
        containerMesas.repaint();
    }

    private JPanel criarCardMesa(String nomeMesa, Reserva reserva) {
        boolean ocupada = (reserva != null);
        Color corStatus = ocupada ? Color.ORANGE : UIConstants.SUCCESS_GREEN;

        JPanel card = new RoundedPanel(15, UIConstants.CARD_DARK);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcon = new JLabel();
        // CORREÇÃO 1: Trocado TABLE_RESTAURANT por EVENT_SEAT (que sabemos que existe) ou LOCAL_DINING
        lblIcon.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.EVENT_SEAT, 40, corStatus));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblNome = new JLabel(nomeMesa);
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNome.setForeground(UIConstants.FG_LIGHT);
        lblNome.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblInfo = new JLabel(ocupada ? "Reservado (" + reserva.getDataReserva().format(DateTimeFormatter.ofPattern("HH:mm")) + ")" : "Livre");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(ocupada ? Color.ORANGE : UIConstants.FG_MUTED);
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(lblIcon, BorderLayout.CENTER);
        card.add(lblNome, BorderLayout.NORTH);
        card.add(lblInfo, BorderLayout.SOUTH);

        // Clique no card
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(ocupada) {
                    JOptionPane.showMessageDialog(GestaoMesasPanel.this, 
                        "Reserva de: " + reserva.getNomeCliente() + "\nPessoas: " + reserva.getNumPessoas());
                } else {
                    abrirModalNovaReserva(nomeMesa);
                }
            }
        });

        return card;
    }

    private void abrirModalNovaReserva() {
        abrirModalNovaReserva(null);
    }

    private void abrirModalNovaReserva(String mesaPreSelecionada) {
        JDialog modal = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nova Reserva", true);
        modal.setLayout(new BorderLayout());
        modal.setSize(400, 450);
        modal.setLocationRelativeTo(this);
        modal.getContentPane().setBackground(UIConstants.BG_DARK);

        JPanel form = new JPanel(new GridLayout(0, 1, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(UIConstants.BG_DARK);

        // Campos
        JComboBox<String> comboClientes = new JComboBox<>(dao.listarClientesSimples().toArray(new String[0]));
        JTextField txtData = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        JTextField txtPessoas = new JTextField("2");
        JComboBox<String> comboMesas = new JComboBox<>(dao.getListaMesas().toArray(new String[0]));
        if(mesaPreSelecionada != null) comboMesas.setSelectedItem(mesaPreSelecionada);

        UIConstants.styleField(txtData); 
        UIConstants.styleField(txtPessoas);

        form.add(label("Cliente:")); form.add(comboClientes);
        form.add(label("Data/Hora (aaaa-MM-dd HH:mm):")); form.add(txtData);
        form.add(label("Nº Pessoas:")); form.add(txtPessoas);
        form.add(label("Mesa:")); form.add(comboMesas);

        JButton btnSalvar = createButton("CONFIRMAR RESERVA", GoogleMaterialDesignIcons.CHECK, UIConstants.SUCCESS_GREEN);
        btnSalvar.addActionListener(e -> {
            try {
                Reserva r = new Reserva();
                String clienteStr = (String) comboClientes.getSelectedItem();
                if(clienteStr != null) r.setIdCliente(Integer.parseInt(clienteStr.split(" - ")[0]));
                
                r.setDataReserva(LocalDateTime.parse(txtData.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                r.setNumPessoas(Integer.parseInt(txtPessoas.getText()));
                r.setMesa((String) comboMesas.getSelectedItem());
                
                if(dao.criarReserva(r)) {
                    JOptionPane.showMessageDialog(modal, "Reserva criada com sucesso!");
                    modal.dispose();
                    carregarMesas();
                } else {
                    JOptionPane.showMessageDialog(modal, "Erro ao criar reserva no banco.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(modal, "Erro nos dados: " + ex.getMessage());
            }
        });

        modal.add(form, BorderLayout.CENTER);
        modal.add(btnSalvar, BorderLayout.SOUTH);
        modal.setVisible(true);
    }

    private JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(UIConstants.FG_LIGHT);
        return l;
    }

    private JLabel criarItemLegenda(String txt, Color cor) {
        JLabel l = new JLabel("  " + txt);
        l.setForeground(UIConstants.FG_LIGHT);
        // CORREÇÃO 2: Trocado CIRCLE por FIBER_MANUAL_RECORD (nome padrão do círculo no Material Design)
        l.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.FIBER_MANUAL_RECORD, 12, cor));
        return l;
    }

    private JButton createButton(String text, GoogleMaterialDesignIcons icon, Color bg) {
        JButton btn = new JButton(text);
        btn.setIcon(IconFontSwing.buildIcon(icon, 18, Color.WHITE));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }
    
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