package com.senac.food.verse.gui;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toast extends JWindow {

    private final int RADIUS = 15;
    private float opacity = 0.0f;
    private Timer fadeInTimer;
    private Timer fadeOutTimer;
    private final int DISPLAY_TIME = 2500; // Tempo visível em ms

    public enum Type {
        SUCCESS(UIConstants.SUCCESS_GREEN, GoogleMaterialDesignIcons.CHECK_CIRCLE),
        ERROR(UIConstants.DANGER_RED, GoogleMaterialDesignIcons.ERROR_OUTLINE),
        WARNING(new Color(230, 126, 34), GoogleMaterialDesignIcons.WARNING),
        INFO(new Color(52, 152, 219), GoogleMaterialDesignIcons.INFO);

        final Color color;
        final GoogleMaterialDesignIcons icon;

        Type(Color color, GoogleMaterialDesignIcons icon) {
            this.color = color;
            this.icon = icon;
        }
    }

    // Método estático para chamar facilmente: Toast.show(painel, "Msg", Type.SUCCESS)
    public static void show(Component owner, String message, Type type) {
        Window window = SwingUtilities.getWindowAncestor(owner);
        if (window != null) {
            new Toast(window, message, type).animate();
        }
    }

    private Toast(Window owner, String message, Type type) {
        super(owner);
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0)); // Transparente para suportar bordas arredondadas
        setSize(350, 50);

        // Painel interno com desenho arredondado
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fundo escuro semi-transparente
                g2.setColor(new Color(40, 40, 40, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);

                // Barra lateral colorida
                g2.setColor(type.color);
                g2.fillRoundRect(0, 0, 10, getHeight(), RADIUS, RADIUS);
                g2.fillRect(5, 0, 10, getHeight()); // Retângulo para cobrir a curva interna da barra
                
                g2.dispose();
            }
        };
        content.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 12));
        content.setOpaque(false);

        // Ícone
        JLabel iconLabel = new JLabel(IconFontSwing.buildIcon(type.icon, 24, type.color));
        content.add(iconLabel);

        // Texto
        JLabel textLabel = new JLabel(message);
        textLabel.setFont(UIConstants.FONT_BOLD);
        textLabel.setForeground(Color.WHITE);
        content.add(textLabel);

        add(content);

        // Posicionamento (Canto inferior direito da janela pai, ou centro topo)
        // Vamos colocar no Topo Central, estilo notificação moderna
        Point loc = owner.getLocationOnScreen();
        int x = loc.x + (owner.getWidth() - getWidth()) / 2;
        int y = loc.y + 80; // 80px do topo
        setLocation(x, y);
        
        setOpacity(0f);
    }

    private void animate() {
        setVisible(true);

        // Fade In
        fadeInTimer = new Timer(20, e -> {
            opacity += 0.1f;
            if (opacity >= 0.95f) {
                opacity = 1f;
                setOpacity(opacity);
                fadeInTimer.stop();
                startWaitTimer();
            } else {
                setOpacity(opacity);
            }
        });
        fadeInTimer.start();
    }

    private void startWaitTimer() {
        Timer waitTimer = new Timer(DISPLAY_TIME, e -> startFadeOut());
        waitTimer.setRepeats(false);
        waitTimer.start();
    }

    private void startFadeOut() {
        fadeOutTimer = new Timer(20, e -> {
            opacity -= 0.05f;
            if (opacity <= 0.0f) {
                opacity = 0f;
                setOpacity(opacity);
                fadeOutTimer.stop();
                dispose();
            } else {
                setOpacity(opacity);
            }
        });
        fadeOutTimer.start();
    }
}