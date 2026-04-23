// Utility package — contains helper/utility classes used across the entire application
package com.expensetracker.util;

// Import DataManager to access the getCurrencySymbol() method for formatting
import com.expensetracker.data.DataManager;

// javax.swing provides Swing UI components (JButton, JLabel, JSeparator, etc.)
import javax.swing.*;

// java.awt provides base-level graphics classes (Color, Font, Cursor, Graphics2D, etc.)
import java.awt.*;

// RoundRectangle2D is a shape class for drawing rectangles with rounded corners
// Used in the custom RoundedBorder class to create modern-looking card edges
import java.awt.geom.RoundRectangle2D;

/**
 * UIUtil.java — UI UTILITY / HELPER CLASS
 * =========================================
 * A collection of static utility methods for common UI operations.
 * All methods are 'static' — they belong to the class, not instances.
 * This class is never instantiated — it's used as UIUtil.methodName().
 *
 * Key Concepts:
 *   - Static utility class: All methods are static (like java.lang.Math)
 *   - Inner class: RoundedBorder is a nested static class
 *   - Custom painting: RoundedBorder overrides paintBorder() for custom visuals
 *   - Factory methods: createStyledButton() and createLineSeparator() create pre-configured components
 *
 * Provides:
 *   1. Rounded borders for modern card-style UI
 *   2. Currency formatting with locale-appropriate symbols
 *   3. Anti-aliasing configuration for smooth rendering
 *   4. Component factory methods for consistent styling
 */
public class UIUtil {

    // ============================================
    // ROUNDED BORDER — Custom Border Implementation
    // Swing's default borders are square/rectangular
    // This custom border draws rounded corners for a modern look
    // ============================================

    /**
     * Applies a rounded border to any Swing component (JPanel, JButton, etc.).
     * This is a convenience method that creates a RoundedBorder and assigns it.
     *
     * @param component The Swing component to modify
     * @param radius    Corner radius in pixels (higher = more rounded)
     */
    public static void setRoundedBorder(JComponent component, int radius) {
        component.setBorder(new RoundedBorder(radius));
    }

    /**
     * INNER CLASS — RoundedBorder
     * ============================
     * A custom border that draws rounded rectangle outlines.
     * Extends AbstractBorder — the base class for all Swing borders.
     *
     * Inner class: defined INSIDE UIUtil. 'static' means it doesn't need
     * a reference to the outer class. It can be instantiated independently.
     *
     * To use: new UIUtil.RoundedBorder(12)
     *
     * Overridden Methods:
     *   - paintBorder(): draws the actual rounded rectangle shape
     *   - getBorderInsets(): tells Swing how much space the border needs
     */
    public static class RoundedBorder extends javax.swing.border.AbstractBorder {

        // Corner radius in pixels — stored as 'final' (set once, never changed)
        private final int radius;

        // Constructor — takes the corner radius as a parameter
        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        /**
         * paintBorder() — called by Swing to draw the border on the component.
         * This method is called automatically during the component's paint cycle.
         *
         * We use Graphics2D (an advanced version of Graphics) to draw a
         * RoundRectangle2D shape with anti-aliasing enabled for smooth edges.
         *
         * @param c      The component being painted
         * @param g      The Graphics context provided by Swing
         * @param x, y   Top-left corner coordinates of the component
         * @param width   Component width in pixels
         * @param height  Component height in pixels
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            // Create a copy of the Graphics context (g.create()) to avoid modifying the original
            // This is a best practice — always create a copy and dispose it when done
            Graphics2D g2 = (Graphics2D) g.create();

            // Enable anti-aliasing for smooth curved edges (without this, corners look jagged)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Set the drawing color to match the component's background
            g2.setColor(c.getBackground());

            // Draw a filled rounded rectangle
            // RoundRectangle2D.Double takes: x, y, width, height, arcWidth, arcHeight
            // The 'radius' parameter controls how rounded the corners are
            g2.fill(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));

            // IMPORTANT: Dispose the graphics copy to free system resources
            // Failing to dispose can cause memory leaks with graphics contexts
            g2.dispose();
        }

        /**
         * getBorderInsets() — tells Swing how much internal padding the border needs.
         * Swing uses this to know where to place child components inside the border.
         * We use radius/2 on each side so content doesn't overlap the rounded corners.
         *
         * @param c The component this border is applied to
         * @return Insets object defining top/left/bottom/right padding
         */
        @Override
        public Insets getBorderInsets(Component c) {
            // Insets(top, left, bottom, right) — each value is radius/2 pixels
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        /**
         * Overloaded version of getBorderInsets that REUSES an existing Insets object.
         * This is an optimization to avoid creating a new Insets object on every call
         * during frequent repaints.
         *
         * @param c      The component
         * @param insets Existing Insets object to modify and return
         * @return The modified Insets object
         */
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            // .set(top, left, bottom, right) modifies the existing object in-place
            insets.set(radius / 2, radius / 2, radius / 2, radius / 2);
            return insets;
        }
    }

    // ============================================
    // TRANSPARENCY HELPER
    // Makes a component's background fully transparent
    // ============================================

    /**
     * Makes a Swing component completely transparent (invisible background).
     * setOpaque(false) tells Swing not to paint the background.
     * Setting background to Color(0,0,0,0) sets alpha=0 (fully transparent).
     *
     * Used for: overlay panels, floating labels, transparent containers
     *
     * @param component The component to make transparent
     */
    public static void makeTransparent(JComponent component) {
        // setOpaque(false) = don't paint background — let parent show through
        component.setOpaque(false);

        // Set background color with alpha=0 (fully invisible)
        component.setBackground(new Color(0, 0, 0, 0));
    }

    // ============================================
    // SEPARATOR FACTORY
    // Creates a horizontal line divider
    // ============================================

    /**
     * Creates a horizontal separator line (divider) with a custom color.
     * Used to visually separate sections in panels and dialogs.
     *
     * @param color The line color
     * @return A configured JSeparator component
     */
    public static JSeparator createLineSeparator(Color color) {
        // SwingConstants.HORIZONTAL = horizontal line (as opposed to VERTICAL)
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);

        // Set the foreground color (the visible line color)
        separator.setForeground(color);
        return separator;
    }

    // ============================================
    // CURRENCY FORMATTING
    // Converts numeric amounts into display-ready currency strings
    // ============================================

    /**
     * Formats a double amount as a currency string with the appropriate symbol.
     * Example: formatCurrency(45.99, "USD") → "$45.99"
     * Example: formatCurrency(-120.50, "INR") → "₹120.50"
     *
     * Math.abs() ensures the amount is always positive (sign is handled separately).
     * String.format() with "%.2f" ensures exactly 2 decimal places.
     *
     * @param amount   The monetary amount (can be negative)
     * @param currency Currency code (e.g., "USD", "EUR", "INR")
     * @return Formatted string like "$45.99"
     */
    public static String formatCurrency(double amount, String currency) {
        // Get the symbol (e.g., "$", "€", "₹") from DataManager's static method
        String symbol = DataManager.getCurrencySymbol(currency);

        // String.format with %s (string) and %.2f (2-decimal float)
        // Math.abs() removes the negative sign — callers handle the sign separately
        return String.format("%s%.2f", symbol, Math.abs(amount));
    }

    /**
     * Formats a currency amount WITH a sign prefix (+ or -).
     * Example: formatCurrencyWithSign(45.99, "USD", "+", "-") → "+$45.99"
     * Example: formatCurrencyWithSign(-120.50, "USD", "+", "-") → "-$120.50"
     *
     * @param amount         The monetary amount (positive or negative)
     * @param currency       Currency code
     * @param positivePrefix Prefix for positive amounts (e.g., "+")
     * @param negativePrefix Prefix for negative amounts (e.g., "-")
     * @return Signed formatted currency string
     */
    public static String formatCurrencyWithSign(double amount, String currency,
                                                 String positivePrefix, String negativePrefix) {
        // First, format the absolute amount with currency symbol
        String formatted = formatCurrency(amount, currency);

        // Then prepend the appropriate sign prefix
        if (amount >= 0) {
            return positivePrefix + formatted;   // e.g., "+$45.99"
        } else {
            return negativePrefix + formatted;   // e.g., "-$120.50"
        }
    }

    // ============================================
    // ICON SCALING
    // Resizes icons to a uniform size for consistent UI
    // ============================================

    /**
     * Scales an ImageIcon to a specific pixel size.
     * Uses Image.SCALE_SMOOTH for high-quality bilinear interpolation.
     *
     * @param icon The original icon (must be an ImageIcon)
     * @param size Target width and height in pixels (square)
     * @return Scaled icon, or the original if it's not an ImageIcon
     */
    public static Icon scaleIcon(Icon icon, int size) {
        // Check if the icon is an ImageIcon (not all Icons have an underlying Image)
        if (icon instanceof ImageIcon) {
            // Get the underlying Image from the ImageIcon
            Image img = ((ImageIcon) icon).getImage();

            // Scale the image to the target size using smooth interpolation
            // SCALE_SMOOTH uses bilinear interpolation — slower but higher quality
            return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));
        }
        // If it's not an ImageIcon (e.g., a custom Icon), return it unchanged
        return icon;
    }

    // ============================================
    // WINDOW POSITIONING
    // Centers a window on the user's screen
    // ============================================

    /**
     * Centers a window (JFrame, JDialog) on the screen.
     * Calculates the center position based on screen dimensions and window size.
     *
     * @param window The window to center
     */
    public static void centerOnScreen(Window window) {
        // Get the screen dimensions from the OS via AWT Toolkit
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Calculate center position: (screenWidth - windowWidth) / 2
        int x = (screenSize.width - window.getWidth()) / 2;
        int y = (screenSize.height - window.getHeight()) / 2;

        // Move the window to the calculated center position
        window.setLocation(x, y);
    }

    // ============================================
    // GRADIENT FACTORY
    // Creates linear gradient paints for card backgrounds
    // ============================================

    /**
     * Creates a horizontal gradient from color1 to color2.
     * Used for the balance card's gradient background (indigo → deep indigo).
     *
     * GradientPaint creates a smooth color transition between two points.
     * Here: from (0,0) starting with color1, to (width,0) ending with color2.
     *
     * @param color1 Start color (left side)
     * @param color2 End color (right side)
     * @param width  Width of the gradient area
     * @return A GradientPaint object ready to use with Graphics2D.setPaint()
     */
    public static GradientPaint createGradient(Color color1, Color color2, int width) {
        // GradientPaint(startX, startY, startColor, endX, endY, endColor)
        // This creates a left-to-right horizontal gradient
        return new GradientPaint(0, 0, color1, width, 0, color2);
    }

    // ============================================
    // ANTI-ALIASING CONFIGURATION
    // Makes rendered graphics smooth (no jagged edges)
    // ============================================

    /**
     * Enables anti-aliasing on a Graphics context for smooth rendering.
     * Anti-aliasing smooths the edges of shapes and text by blending edge pixels.
     * Without anti-aliasing, diagonal and curved lines appear "stair-stepped" (jagged).
     *
     * Three rendering hints are set:
     *   1. KEY_ANTIALIASING → smooth shape edges
     *   2. KEY_TEXT_ANTIALIASING → smooth text rendering
     *   3. KEY_RENDERING → prioritize quality over speed
     *
     * @param g The Graphics context (typically from paintComponent)
     */
    public static void enableAntiAliasing(Graphics g) {
        // Only apply if the Graphics object is a Graphics2D instance
        // (Graphics2D adds rendering hints support to the base Graphics class)
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;

            // Enable shape anti-aliasing (smooth circles, curves, rounded rectangles)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Enable text anti-aliasing (smooth font rendering)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Prefer rendering quality over speed (better visuals, slightly slower)
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }

    // ============================================
    // PADDING HELPER
    // Adds empty space around a component's content
    // ============================================

    /**
     * Adds uniform padding (empty space) around a component's content.
     * Uses an EmptyBorder which reserves space but doesn't draw anything.
     *
     * @param component The component to add padding to
     * @param padding   Padding size in pixels (applied equally to all 4 sides)
     */
    public static void addPadding(JComponent component, int padding) {
        // EmptyBorder creates invisible space: (top, left, bottom, right)
        component.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }

    // ============================================
    // BUTTON FACTORY
    // Creates pre-styled buttons for consistent design
    // ============================================

    /**
     * Factory method — creates a fully styled JButton with custom colors.
     * All visual properties are set: font, colors, border, cursor.
     *
     * Factory Method Pattern: instead of creating and configuring a button
     * in 8 separate lines every time, this method does it in one call.
     *
     * @param text Button label text
     * @param bg   Background color
     * @param fg   Text (foreground) color
     * @return A fully configured JButton ready to add to a container
     */
    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);

        // Set colors
        button.setBackground(bg);    // Button background color
        button.setForeground(fg);    // Button text color

        // Remove default Swing visual effects
        button.setFocusPainted(false);   // No dotted focus border when clicked
        button.setBorderPainted(false);  // No default raised border
        button.setOpaque(true);          // Ensure background color is painted

        // Set the cursor to a hand pointer (indicates the element is clickable)
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Set font to bold Segoe UI at 14px for readability
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Add internal padding so the button text doesn't touch the edges
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        return button;
    }
}
