package com.expensetracker.util;

import java.awt.Color;
import java.awt.Font;

/**
 * Theme enumeration for light and dark mode
 * Contains all color definitions for the application UI
 */
public enum Theme {
    LIGHT, DARK;

    // ============================================
    // Primary Colors
    // ============================================

    /**
     * Get primary accent color (purple/indigo)
     */
    public Color getPrimaryColor() {
        return new Color(99, 102, 241);
    }

    /**
     * Get primary color hover state (darker)
     */
    public Color getPrimaryHover() {
        return new Color(79, 70, 229);
    }

    /**
     * Get light version of primary color (for backgrounds)
     */
    public Color getPrimaryLight() {
        return new Color(99, 102, 241, 25);
    }

    // ============================================
    // Semantic Colors
    // ============================================

    /**
     * Get success color (green for income)
     */
    public Color getSuccessColor() {
        return new Color(16, 185, 129);
    }

    /**
     * Get success color light version
     */
    public Color getSuccessLight() {
        return new Color(16, 185, 129, 25);
    }

    /**
     * Get danger color (red for expenses)
     */
    public Color getDangerColor() {
        return new Color(239, 68, 68);
    }

    /**
     * Get danger color light version
     */
    public Color getDangerLight() {
        return new Color(239, 68, 68, 25);
    }

    /**
     * Get warning color (amber/yellow)
     */
    public Color getWarningColor() {
        return new Color(245, 158, 11);
    }

    // ============================================
    // Background Colors
    // ============================================

    /**
     * Get primary background color
     */
    public Color getBackgroundPrimary() {
        return this == LIGHT ? new Color(248, 250, 252) : new Color(15, 23, 42);
    }

    /**
     * Get secondary background color (cards, panels)
     */
    public Color getBackgroundSecondary() {
        return this == LIGHT ? Color.WHITE : new Color(30, 41, 59);
    }

    /**
     * Get tertiary background color (inputs, subtle areas)
     */
    public Color getBackgroundTertiary() {
        return this == LIGHT ? new Color(241, 245, 249) : new Color(51, 65, 85);
    }

    // ============================================
    // Text Colors
    // ============================================

    /**
     * Get primary text color
     */
    public Color getTextPrimary() {
        return this == LIGHT ? new Color(30, 41, 59) : new Color(241, 245, 249);
    }

    /**
     * Get secondary text color (labels, subtitles)
     */
    public Color getTextSecondary() {
        return this == LIGHT ? new Color(100, 116, 139) : new Color(148, 163, 184);
    }

    /**
     * Get muted text color (placeholders, hints)
     */
    public Color getTextMuted() {
        return this == LIGHT ? new Color(148, 163, 184) : new Color(100, 116, 139);
    }

    // ============================================
    // Border Colors
    // ============================================

    /**
     * Get border/divider color
     */
    public Color getBorderColor() {
        return this == LIGHT ? new Color(226, 232, 240) : new Color(51, 65, 85);
    }

    // ============================================
    // Fonts
    // ============================================

    /**
     * Get default UI font
     */
    public Font getDefaultFont() {
        return new Font("Segoe UI", Font.PLAIN, 14);
    }

    /**
     * Get bold font variant
     */
    public Font getBoldFont() {
        return new Font("Segoe UI", Font.BOLD, 14);
    }

    /**
     * Get large font for headings
     */
    public Font getHeadingFont() {
        return new Font("Segoe UI", Font.BOLD, 18);
    }

    /**
     * Get extra large font for balance display
     */
    public Font getBalanceFont() {
        return new Font("Segoe UI", Font.BOLD, 28);
    }

    /**
     * Get small font for labels
     */
    public Font getSmallFont() {
        return new Font("Segoe UI", Font.PLAIN, 12);
    }

    // ============================================
    // UI Helpers
    // ============================================

    /**
     * Get card shadow color
     */
    public Color getShadowColor() {
        return this == LIGHT
            ? new Color(0, 0, 0, 25)
            : new Color(0, 0, 0, 100);
    }

    /**
     * Get modal overlay color
     */
    public Color getModalOverlayColor() {
        return new Color(0, 0, 0, this == LIGHT ? 128 : 180);
    }

    /**
     * Get hover background color for list items
     */
    public Color getHoverBackground() {
        return this == LIGHT
            ? new Color(241, 245, 249)
            : new Color(51, 65, 85);
    }

    /**
     * Get selected background color
     */
    public Color getSelectedBackground() {
        return this == LIGHT
            ? new Color(226, 232, 240)
            : new Color(71, 85, 105);
    }
}
