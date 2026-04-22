package com.expensetracker.util;

import com.expensetracker.data.DataManager;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Utility class for UI operations
 * Provides common styling and helper methods for Swing components
 */
public class UIUtil {

    /**
     * Apply rounded corners to a component
     * @param component Component to modify
     * @param radius Corner radius in pixels
     */
    public static void setRoundedBorder(JComponent component, int radius) {
        component.setBorder(new RoundedBorder(radius));
    }

    /**
     * Create a rounded border instance
     */
    public static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getBackground());
            g2.fill(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(radius / 2, radius / 2, radius / 2, radius / 2);
            return insets;
        }
    }

    /**
     * Set a component to be transparent (no background painting)
     * @param component Component to make transparent
     */
    public static void makeTransparent(JComponent component) {
        component.setOpaque(false);
        component.setBackground(new Color(0, 0, 0, 0));
    }

    /**
     * Create a horizontal separator line
     * @param color Line color
     * @return JSeparator component
     */
    public static JSeparator createLineSeparator(Color color) {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(color);
        return separator;
    }

    /**
     * Format a double value as currency
     * @param amount Amount to format
     * @param currency Currency code
     * @return Formatted string
     */
    public static String formatCurrency(double amount, String currency) {
        String symbol = DataManager.getCurrencySymbol(currency);
        return String.format("%s%.2f", symbol, Math.abs(amount));
    }

    /**
     * Format amount with sign (positive/negative)
     * @param amount Amount
     * @param currency Currency code
     * @param positivePrefix Prefix for positive amounts
     * @param negativePrefix Prefix for negative amounts
     * @return Formatted string with sign
     */
    public static String formatCurrencyWithSign(double amount, String currency,
                                                 String positivePrefix, String negativePrefix) {
        String formatted = formatCurrency(amount, currency);
        if (amount >= 0) {
            return positivePrefix + formatted;
        } else {
            return negativePrefix + formatted;
        }
    }

    /**
     * Scale an icon to a specific size
     * @param icon Original icon
     * @param size Target size
     * @return Scaled icon
     */
    public static Icon scaleIcon(Icon icon, int size) {
        if (icon instanceof ImageIcon) {
            Image img = ((ImageIcon) icon).getImage();
            return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));
        }
        return icon;
    }

    /**
     * Center a window on screen
     * @param window Window to center
     */
    public static void centerOnScreen(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - window.getWidth()) / 2;
        int y = (screenSize.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }

    /**
     * Create a gradient paint for cards
     * @param color1 Start color
     * @param color2 End color
     * @param width Gradient width
     * @return GradientPaint object
     */
    public static GradientPaint createGradient(Color color1, Color color2, int width) {
        return new GradientPaint(0, 0, color1, width, 0, color2);
    }

    /**
     * Apply anti-aliasing to graphics context
     * @param g Graphics context
     */
    public static void enableAntiAliasing(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }

    /**
     * Add padding to a component
     * @param component Component to pad
     * @param padding Padding in pixels
     */
    public static void addPadding(JComponent component, int padding) {
        component.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }

    /**
     * Create a styled button with rounded corners
     * @param text Button text
     * @param bg Background color
     * @param fg Foreground color
     * @return Styled JButton
     */
    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }
}
