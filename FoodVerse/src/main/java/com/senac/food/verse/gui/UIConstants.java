package com.senac.food.verse.gui;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public final class UIConstants {

    private UIConstants() {}

    // =================================================================================
    // 1. PALETA DE CORES
    // =================================================================================
    public static final Color BG_DARK         = new Color(0x30, 0x30, 0x30);
    public static final Color BG_DARK_ALT     = new Color(0x38, 0x38, 0x38);
    public static final Color CARD_DARK       = new Color(0x3F, 0x3F, 0x3F);
    public static final Color HEADER_DARK     = new Color(0x2B, 0x2B, 0x2B);
    public static final Color FG_LIGHT        = new Color(0xE4, 0xE4, 0xE4);
    public static final Color FG_MUTED        = new Color(0xB0, 0xB0, 0xB0);
    
    // Cores de Ação
    public static final Color PRIMARY_RED     = new Color(0xBC, 0x10, 0x15);
    public static final Color PRIMARY_RED_ALT = new Color(0xEB, 0x00, 0x29);
    public static final Color SUCCESS_GREEN   = new Color(0x19, 0x87, 0x54);
    public static final Color DANGER_RED      = new Color(220, 53, 69);
    public static final Color WARNING_ORANGE  = new Color(230, 126, 34);
    public static final Color INFO_BLUE       = new Color(52, 152, 219);
    
    // Tabelas e Grades
    public static final Color GRID_DARK       = new Color(70, 70, 70);
    public static final Color ALT_ROW         = new Color(0x35, 0x35, 0x35);
    public static final Color SEL_BG          = PRIMARY_RED;
    public static final Color SEL_FG          = Color.WHITE;
    public static final Color STATUS_INACTIVE_BG = CARD_DARK;

    // =================================================================================
    // 2. FONTES
    // =================================================================================
    public static final Font ARIAL_12   = new Font("Arial", Font.PLAIN, 12);
    public static final Font ARIAL_12_B = new Font("Arial", Font.BOLD, 12);
    public static final Font ARIAL_14   = new Font("Arial", Font.PLAIN, 14);
    public static final Font ARIAL_14_B = new Font("Arial", Font.BOLD, 14);
    public static final Font ARIAL_16_B = new Font("Arial", Font.BOLD, 16);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_TITLE_LARGE = FONT_TITLE.deriveFont(28f);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);

    // =================================================================================
    // 3. CONFIGURAÇÃO GLOBAL (FLATLAF)
    // =================================================================================
    public static void applyDarkDefaults() {
        put("Component.focusWidth", 0);
        put("Component.innerFocusWidth", 0);
        put("TabbedPane.focusColor", new Color(0,0,0,0));
        put("Button.focusWidth", 0);

        put("Panel.background", BG_DARK);
        put("Label.foreground", FG_LIGHT);
        put("Label.background", BG_DARK);
        put("Separator.foreground", GRID_DARK);
        put("Separator.background", BG_DARK);

        put("Component.arc", 12);
        put("TextComponent.arc", 12);
        put("Button.arc", 12);

        put("Component.focusColor", PRIMARY_RED);
        put("TextComponent.selectionBackground", PRIMARY_RED);
        put("TextComponent.selectionForeground", Color.WHITE);
        
        put("TextComponent.background", CARD_DARK);
        put("TextComponent.foreground", FG_LIGHT);
        put("TextComponent.caretForeground", FG_LIGHT);
        put("TextComponent.border", BorderFactory.createEmptyBorder(6, 8, 6, 8));

        put("ComboBox.background", CARD_DARK);
        put("ComboBox.foreground", FG_LIGHT);
        put("ComboBox.selectionBackground", PRIMARY_RED);
        put("ComboBox.selectionForeground", Color.WHITE);
        put("ComboBox.buttonBackground", CARD_DARK);
        put("ComboBox.buttonHoverBackground", BG_DARK_ALT);
        put("ComboBox.popupBackground", CARD_DARK);

        put("Table.background", BG_DARK);
        put("Table.foreground", FG_LIGHT);
        put("Table.selectionBackground", PRIMARY_RED);
        put("Table.selectionForeground", Color.WHITE);
        put("Table.gridColor", GRID_DARK);
        put("TableHeader.background", HEADER_DARK);
        put("TableHeader.foreground", FG_LIGHT);
        put("Table.alternateRowColor", ALT_ROW);

        put("ScrollPane.background", BG_DARK);
        put("ScrollPane.border", BorderFactory.createLineBorder(GRID_DARK, 1));
        put("ScrollBar.track", CARD_DARK);
        put("ScrollBar.thumb", HEADER_DARK);
        put("ScrollBar.background", BG_DARK);
        put("ScrollBar.thumbArc", 999); 

        put("Menu.background", CARD_DARK);
        put("Menu.foreground", FG_LIGHT);
        put("PopupMenu.background", CARD_DARK);
        put("PopupMenu.foreground", FG_LIGHT);
        put("MenuItem.background", CARD_DARK);
        put("MenuItem.foreground", FG_LIGHT);
        
        put("OptionPane.background", BG_DARK);
        put("OptionPane.messageForeground", FG_LIGHT);
        put("Dialog.background", BG_DARK);
        
        put("TitledBorder.titleColor", FG_LIGHT);
        put("ToolTip.background", CARD_DARK);
        put("ToolTip.foreground", FG_LIGHT);
    }

    private static void put(String k, Object v){ UIManager.put(k,v); }

    // =================================================================================
    // 4. ESTILIZADORES DE COMPONENTES
    // =================================================================================
    
    public static void styleLabel(JLabel l){
        l.setFont(ARIAL_12_B);
        l.setForeground(FG_LIGHT);
        l.setBackground(BG_DARK);
        l.setOpaque(false);
    }

    public static void styleChipButton(JToggleButton tb) {
        tb.setFont(ARIAL_12_B);
        tb.setBackground(CARD_DARK);
        tb.setForeground(FG_MUTED);
        tb.setFocusPainted(false);
        tb.setBorderPainted(false);
        tb.setOpaque(true);
        tb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID_DARK, 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        tb.addItemListener(e -> {
            if (tb.isSelected()) {
                tb.setBackground(PRIMARY_RED);
                tb.setForeground(Color.WHITE);
                tb.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_RED.darker(), 1),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)
                ));
            } else {
                tb.setBackground(CARD_DARK);
                tb.setForeground(FG_MUTED);
                tb.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GRID_DARK, 1),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)
                ));
            }
        });
    }

    public static void stylePrimary(JButton b){
        b.setFont(ARIAL_12_B);
        b.setBackground(PRIMARY_RED);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true); 
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_RED.darker(), 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
    }

    public static void styleSecondary(JButton b){
        b.setFont(ARIAL_12_B);
        b.setBackground(CARD_DARK);
        b.setForeground(FG_LIGHT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID_DARK, 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
    }

    public static void styleSuccess(JButton b){
        b.setFont(ARIAL_12_B);
        b.setBackground(SUCCESS_GREEN);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true); 
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SUCCESS_GREEN.darker(), 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
    }
    
    public static void styleDanger(JButton b){
        b.setFont(ARIAL_12_B);
        b.setBackground(DANGER_RED);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true); 
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DANGER_RED.darker(), 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
    }

    public static void styleField(JTextField tf) {
        tf.setBackground(CARD_DARK); 
        tf.setForeground(Color.WHITE); 
        tf.setCaretColor(Color.WHITE); 
        tf.setFont(ARIAL_14);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID_DARK, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10) 
        ));
        tf.setOpaque(true); 
    }

    public static void styleSpinner(JSpinner sp){
        sp.setBackground(CARD_DARK);
        sp.setForeground(FG_LIGHT);
        if (sp.getEditor() instanceof JSpinner.DefaultEditor de){
            JTextField tf = de.getTextField();
            tf.setBackground(CARD_DARK);
            tf.setForeground(FG_LIGHT);
            tf.setCaretColor(FG_LIGHT);
            tf.setFont(ARIAL_14);
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRID_DARK, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
        }
    }

    public static <T> void styleCombo(JComboBox<T> cb){
        cb.setBackground(CARD_DARK);
        cb.setForeground(FG_LIGHT);
        cb.setFont(ARIAL_12);
        cb.setBorder(BorderFactory.createLineBorder(GRID_DARK, 1));
        cb.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean isSelected,boolean cellHasFocus){
                super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                setOpaque(true);
                setBackground(isSelected?SEL_BG:CARD_DARK);
                setForeground(FG_LIGHT);
                setFont(ARIAL_12);
                return this;
            }
        });
    }

    public static void stylePanel(JPanel p){
        p.setBackground(BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    // =================================================================================
    // MÉTODOS DE JANELAS CUSTOMIZADAS (MODAIS)
    // =================================================================================

    public static void showConfirmDialog(Component parent, String titulo, String mensagem, Runnable onConfirm) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        if (window == null && parent instanceof Window) {
            window = (Window) parent;
        }
        
        JDialog dialog = new JDialog(window, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setUndecorated(true); 
        
        RoundedPanel panel = new RoundedPanel(20, BG_DARK_ALT);
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        JLabel lblIcon = new JLabel(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.HELP_OUTLINE, 40, PRIMARY_RED));
        JLabel lblMsg = new JLabel("<html><center>" + mensagem + "</center></html>");
        lblMsg.setFont(ARIAL_16_B);
        lblMsg.setForeground(FG_LIGHT);
        lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel pnlCenter = new JPanel(new BorderLayout(10, 10));
        pnlCenter.setOpaque(false);
        pnlCenter.add(lblIcon, BorderLayout.NORTH);
        pnlCenter.add(lblMsg, BorderLayout.CENTER);
        
        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        pnlBotoes.setOpaque(false);
        
        JButton btnCancelar = new JButton("CANCELAR");
        styleSecondary(btnCancelar);
        btnCancelar.setPreferredSize(new Dimension(130, 40));
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        JButton btnConfirmar = new JButton("CONFIRMAR");
        stylePrimary(btnConfirmar);
        btnConfirmar.setPreferredSize(new Dimension(130, 40));
        btnConfirmar.addActionListener(e -> {
            dialog.dispose();
            if (onConfirm != null) onConfirm.run();
        });
        
        pnlBotoes.add(btnCancelar);
        pnlBotoes.add(btnConfirmar);
        
        panel.add(pnlCenter, BorderLayout.CENTER);
        panel.add(pnlBotoes, BorderLayout.SOUTH);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setBackground(new Color(0, 0, 0, 0)); 
        dialog.setVisible(true);
    }

    public static void showInputDialog(Component parent, String titulo, String mensagem, Consumer<String> onConfirm) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        if (window == null && parent instanceof Window) {
            window = (Window) parent;
        }

        JDialog dialog = new JDialog(window, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setUndecorated(true); 

        RoundedPanel panel = new RoundedPanel(20, BG_DARK_ALT);
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel lblIcon = new JLabel(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.EDIT, 40, INFO_BLUE));
        JLabel lblMsg = new JLabel("<html><center>" + mensagem + "</center></html>");
        lblMsg.setFont(ARIAL_16_B);
        lblMsg.setForeground(FG_LIGHT);
        lblMsg.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel pnlCenter = new JPanel(new BorderLayout(10, 10));
        pnlCenter.setOpaque(false);
        pnlCenter.add(lblIcon, BorderLayout.NORTH);
        pnlCenter.add(lblMsg, BorderLayout.CENTER);

        JTextField txtInput = new JTextField();
        styleField(txtInput);
        txtInput.setPreferredSize(new Dimension(250, 45));
        pnlCenter.add(txtInput, BorderLayout.SOUTH);

        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        pnlBotoes.setOpaque(false);

        JButton btnCancelar = new JButton("CANCELAR");
        styleSecondary(btnCancelar);
        btnCancelar.setPreferredSize(new Dimension(130, 40));
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnConfirmar = new JButton("CONFIRMAR");
        stylePrimary(btnConfirmar);
        btnConfirmar.setPreferredSize(new Dimension(130, 40));
        btnConfirmar.addActionListener(e -> {
            dialog.dispose();
            if (onConfirm != null) onConfirm.accept(txtInput.getText());
        });

        pnlBotoes.add(btnCancelar);
        pnlBotoes.add(btnConfirmar);

        panel.add(pnlCenter, BorderLayout.CENTER);
        panel.add(pnlBotoes, BorderLayout.SOUTH);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setBackground(new Color(0, 0, 0, 0)); 
        dialog.setVisible(true);
        
        // Foca automaticamente no campo de texto ao abrir
        SwingUtilities.invokeLater(txtInput::requestFocusInWindow);
    }

    // =================================================================================
    // MÉTODOS DE TABELAS E SCROLL
    // =================================================================================
    
    public static void styleScrollPane(JScrollPane sp){
        sp.setBackground(BG_DARK);
        sp.setBorder(BorderFactory.createLineBorder(GRID_DARK, 1));
        sp.getViewport().setBackground(BG_DARK);
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(GRID_DARK);
        table.setBackground(BG_DARK_ALT);
        table.setForeground(FG_LIGHT);
        table.setSelectionBackground(SEL_BG);
        table.setSelectionForeground(SEL_FG);
        table.setFont(ARIAL_14);
        table.setFocusable(false); 

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_DARK);
        header.setForeground(FG_MUTED);
        header.setFont(ARIAL_12_B);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, GRID_DARK));
        header.setReorderingAllowed(false); 

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // Sistema de Mensagens (Toast)
    public static void showSuccess(Component parent, String mensagem) {
        Toast.show(parent, mensagem, Toast.Type.SUCCESS);
    }

    public static void showError(Component parent, String mensagem) {
        Toast.show(parent, mensagem, Toast.Type.ERROR);
    }
    
    public static void showWarning(Component parent, String mensagem) {
        Toast.show(parent, mensagem, Toast.Type.WARNING);
    }

    // Classe Interna para Painéis Arredondados
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
        }
    }

    // =================================================================================
    // 5. ÍCONE DO SISTEMA (TASKBAR E JANELA)
    // =================================================================================
    public static Image getAppIcon() {
        Icon icon = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.RESTAURANT_MENU, 64, PRIMARY_RED);
        
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
            icon.getIconWidth(), 
            icon.getIconHeight(), 
            java.awt.image.BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        icon.paintIcon(null, g2d, 0, 0);
        g2d.dispose();
        
        return image;
    }
}
