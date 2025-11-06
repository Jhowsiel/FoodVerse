package com.senac.food.verse;

import java.awt.*;
import javax.swing.*;

public class RoundedLabel extends JLabel {
    private Color backgroundColor;
    private Color borderColor;
    private int arcWidth;
    private int arcHeight;
    private int padding;


    public RoundedLabel(String text, Icon icon,
                        Color backgroundColor,
                        Color borderColor,
                        int arcWidth, int arcHeight,
                        int padding) {
        super(text, icon, SwingConstants.LEFT);
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.padding = padding;
        setOpaque(false);
        setFont(new Font("Arial", Font.PLAIN, 12));
        setForeground(new Color(75, 0, 130));
        setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        // fundo
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
        // borda
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1,
                         arcWidth, arcHeight);
        g2.dispose();
        super.paintComponent(g);
    }
}