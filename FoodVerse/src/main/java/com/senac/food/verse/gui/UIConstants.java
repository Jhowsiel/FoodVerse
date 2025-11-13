package com.senac.food.verse.gui;

import javax.swing.*;
import java.awt.*;

public final class UIConstants {

    private UIConstants() {}

    // Cores
    public static final Color BG_DARK        = new Color(0x30,0x30,0x30);
    public static final Color BG_DARK_ALT    = new Color(0x38,0x38,0x38);
    public static final Color CARD_DARK      = new Color(0x3F,0x3F,0x3F);
    public static final Color HEADER_DARK    = new Color(0x2B,0x2B,0x2B);
    public static final Color FG_LIGHT       = new Color(0xE4,0xE4,0xE4);
    public static final Color FG_MUTED       = new Color(0xB0,0xB0,0xB0);
    public static final Color PRIMARY_RED    = new Color(0xBC,0x10,0x15);
    public static final Color PRIMARY_RED_ALT= new Color(0xEB,0x00,0x29);
    public static final Color SUCCESS_GREEN  = new Color(0x19,0x87,0x54);
    public static final Color GRID_DARK      = new Color(70,70,70);
    public static final Color ALT_ROW        = new Color(0x35,0x35,0x35);
    public static final Color SEL_BG         = PRIMARY_RED;
    public static final Color SEL_FG         = Color.WHITE;
    public static final Color STATUS_INACTIVE_BG = CARD_DARK;

    // Fonts
    public static final Font ARIAL_12   = new Font("Arial", Font.PLAIN, 12);
    public static final Font ARIAL_12_B = new Font("Arial", Font.BOLD, 12);
    public static final Font ARIAL_14   = new Font("Arial", Font.PLAIN, 14);
    public static final Font ARIAL_14_B = new Font("Arial", Font.BOLD, 14);
    public static final Font ARIAL_16_B = new Font("Arial", Font.BOLD, 16);

    public static void applyDarkDefaults() {
        // General
        put("Panel.background", BG_DARK);
        put("Label.foreground", FG_LIGHT);
        put("Label.background", BG_DARK); // Ensure labels don't show white
        put("Separator.foreground", GRID_DARK);
        put("Separator.background", BG_DARK);

        // Buttons
        put("Button.background", PRIMARY_RED);
        put("Button.foreground", Color.WHITE);
        put("Button.focusColor", PRIMARY_RED_ALT);
        put("Button.hoverBackground", PRIMARY_RED_ALT);
        put("Button.border", BorderFactory.createEmptyBorder());

        // Text Components
        put("TextComponent.background", CARD_DARK);
        put("TextComponent.foreground", FG_LIGHT);
        put("TextComponent.caretForeground", FG_LIGHT);
        put("TextComponent.selectionBackground", SEL_BG);
        put("TextComponent.selectionForeground", SEL_FG);
        put("TextComponent.border", BorderFactory.createEmptyBorder(6, 8, 6, 8));

        // ComboBox
        put("ComboBox.background", CARD_DARK);
        put("ComboBox.foreground", FG_LIGHT);
        put("ComboBox.selectionBackground", SEL_BG);
        put("ComboBox.selectionForeground", SEL_FG);
        put("ComboBox.buttonBackground", CARD_DARK);
        put("ComboBox.buttonHoverBackground", BG_DARK_ALT);
        put("ComboBox.popupBackground", CARD_DARK);

        // Table
        put("Table.background", BG_DARK);
        put("Table.foreground", FG_LIGHT);
        put("Table.selectionBackground", SEL_BG);
        put("Table.selectionForeground", SEL_FG);
        put("Table.gridColor", GRID_DARK);
        put("TableHeader.background", HEADER_DARK);
        put("TableHeader.foreground", FG_LIGHT);
        put("Table.alternateRowColor", ALT_ROW);

        // ScrollPane
        put("ScrollPane.background", BG_DARK);
        put("ScrollBar.track", CARD_DARK);
        put("ScrollBar.thumb", HEADER_DARK);
        put("ScrollBar.background", BG_DARK);

        // Menus
        put("Menu.background", CARD_DARK);
        put("Menu.foreground", FG_LIGHT);
        put("PopupMenu.background", CARD_DARK);
        put("PopupMenu.foreground", FG_LIGHT);
        put("MenuItem.background", CARD_DARK);
        put("MenuItem.foreground", FG_LIGHT);

        // TabbedPane
        put("TabbedPane.background", BG_DARK);
        put("TabbedPane.foreground", FG_LIGHT);
        put("TabbedPane.contentAreaColor", BG_DARK);
        put("TabbedPane.selectedBackground", CARD_DARK);
        put("TabbedPane.tabAreaBackground", BG_DARK);

        // Dialogs and Windows
        put("OptionPane.background", BG_DARK);
        put("OptionPane.foreground", FG_LIGHT);
        put("Dialog.background", BG_DARK);
        put("Dialog.foreground", FG_LIGHT);

        // Borders and Miscellaneous
        put("TitledBorder.titleColor", FG_LIGHT);
        put("ToolTip.background", CARD_DARK);
        put("ToolTip.foreground", FG_LIGHT);
    }

    private static void put(String k, Object v){ UIManager.put(k,v); }

    // Helper methods for styling components
    public static void styleLabel(JLabel l){
        l.setFont(ARIAL_12_B);
        l.setForeground(FG_LIGHT);
        l.setBackground(BG_DARK);
        l.setOpaque(false); // Unless needed
    }
    public static void stylePrimary(JButton b){
        b.setFont(ARIAL_12_B);
        b.setBackground(PRIMARY_RED);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SUCCESS_GREEN.darker(), 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
    }
    public static void styleField(JTextField tf){
        tf.setBackground(CARD_DARK);
        tf.setForeground(FG_LIGHT);
        tf.setCaretColor(FG_LIGHT);
        tf.setFont(ARIAL_14);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID_DARK, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
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
    public static void styleScrollPane(JScrollPane sp){
        sp.setBackground(BG_DARK);
        sp.setBorder(BorderFactory.createLineBorder(GRID_DARK, 1));
        sp.getViewport().setBackground(BG_DARK);
    }
}