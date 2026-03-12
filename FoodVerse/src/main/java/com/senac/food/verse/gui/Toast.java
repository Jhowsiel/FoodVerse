package com.senac.food.verse.gui;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

public class Toast extends JWindow {

    private static final Logger LOGGER = Logger.getLogger(Toast.class.getName());
    private static final int RADIUS = 15;
    private static final int MIN_WIDTH = 360;
    private static final int MAX_WIDTH = 520;
    private static final int MIN_HEIGHT = 56;
    private static final int HORIZONTAL_PADDING = 18;
    private static final int VERTICAL_PADDING = 12;
    private float opacity = 0.0f;
    private Timer fadeInTimer;
    private Timer fadeOutTimer;
    private final int displayTime;

    public enum Type {
        SUCCESS(UIConstants.SUCCESS_GREEN, GoogleMaterialDesignIcons.CHECK_CIRCLE),
        ERROR(UIConstants.DANGER_RED, GoogleMaterialDesignIcons.ERROR_OUTLINE),
        WARNING(UIConstants.WARNING_ORANGE, GoogleMaterialDesignIcons.WARNING),
        INFO(UIConstants.INFO_BLUE, GoogleMaterialDesignIcons.INFO);

        final Color color;
        final GoogleMaterialDesignIcons icon;

        Type(Color color, GoogleMaterialDesignIcons icon) {
            this.color = color;
            this.icon = icon;
        }
    }

    public static void show(Component owner, String message, Type type) {
        // Verifica se o owner JÁ É uma Window (como JFrame/TelaInicial)
        Window window = (owner instanceof Window) ? (Window) owner : SwingUtilities.getWindowAncestor(owner);
        
        if (window != null) {
            new Toast(window, message, type).animate();
        } else {
            LOGGER.warning("Nenhuma janela encontrada para exibir a notificação.");
        }
    }

    private Toast(Window owner, String message, Type type) {
        super(owner);
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(40, 40, 40, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);

                g2.setColor(type.color);
                g2.fillRoundRect(0, 0, 10, getHeight(), RADIUS, RADIUS);
                g2.fillRect(5, 0, 10, getHeight());
                
                g2.dispose();
            }
        };
        content.setLayout(new BorderLayout(12, 0));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(VERTICAL_PADDING, HORIZONTAL_PADDING, VERTICAL_PADDING, HORIZONTAL_PADDING));

        JLabel iconLabel = new JLabel(IconFontSwing.buildIcon(type.icon, 24, type.color));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        content.add(iconLabel, BorderLayout.WEST);

        JLabel textLabel = new JLabel(toHtmlMessage(message));
        textLabel.setFont(UIConstants.FONT_BOLD);
        textLabel.setForeground(UIConstants.FG_LIGHT);
        textLabel.setVerticalAlignment(SwingConstants.CENTER);
        content.add(textLabel, BorderLayout.CENTER);

        add(content);

        Dimension toastSize = calculateToastSize(message);
        setSize(toastSize);

        Point loc = owner.getLocationOnScreen();
        int x = loc.x + (owner.getWidth() - getWidth()) / 2;
        int y = loc.y + 80; 
        setLocation(x, y);
        
        setOpacity(0f);
        displayTime = resolveDisplayTime(message);
    }

    private void animate() {
        setVisible(true);
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
        Timer waitTimer = new Timer(displayTime, e -> startFadeOut());
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

    static Dimension calculateToastSize(String message) {
        JLabel probe = new JLabel(toHtmlMessage(message));
        probe.setFont(UIConstants.FONT_BOLD);
        Dimension preferred = probe.getPreferredSize();

        int width = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, preferred.width + HORIZONTAL_PADDING * 2 + 40));
        int height = Math.max(MIN_HEIGHT, preferred.height + VERTICAL_PADDING * 2 + 8);
        return new Dimension(width, height);
    }

    static String toHtmlMessage(String message) {
        String safeMessage = message == null || message.isBlank() ? "Operação concluída." : message.trim();
        int wrapWidth = Math.max(180, Math.min(320, safeMessage.length() * 7));
        return "<html><div style='width: " + wrapWidth + "px;'>" + safeMessage + "</div></html>";
    }

    private static int resolveDisplayTime(String message) {
        if (message == null) {
            return 2500;
        }
        int extraChars = Math.max(0, message.trim().length() - 60);
        return Math.min(4500, 2500 + (extraChars * 18));
    }
}
