// This class belongs to the 'util' (utility) package — helper classes used across the app
package com.expensetracker.util;

// java.awt.Color represents an RGBA color (Red, Green, Blue, Alpha)
// Used extensively for defining UI colors for both light and dark themes
import java.awt.Color;

// java.awt.Font defines a typeface with family, style (BOLD/PLAIN), and size
// Used to create different font sizes for headings, labels, and body text
import java.awt.Font;

/**
 * Theme.java — THEME ENUMERATION (DESIGN SYSTEM)
 * =================================================
 * This enum defines the complete color palette and typography for the application.
 * It contains ALL visual constants — colors, fonts, and shadows — for both
 * LIGHT and DARK themes.
 *
 * Key Concepts:
 *   - Enum with methods: Enums in Java can have fields, constructors, and methods
 *   - Ternary operator: 'this == LIGHT ? lightValue : darkValue' (compact if-else)
 *   - Design tokens: Colors and fonts are abstracted as methods, not hardcoded in UI code
 *
 * Why use an enum instead of a class?
 *   - Enums guarantee only LIGHT and DARK exist — no random themes can be created
 *   - Enums are inherently singletons — safe with == comparison
 *   - Enums can't be subclassed — ensures the design system stays consistent
 *
 * Color System (matches the web app's CSS variables):
 *   - Primary: #6366f1 (Indigo) — buttons, active states, brand identity
 *   - Success: #10b981 (Green) — income, positive values
 *   - Danger:  #ef4444 (Red) — expenses, negative values, delete actions
 *   - Warning: #f59e0b (Amber) — alerts, food category
 */
public enum Theme {

    // The two enum constants — these are the only possible themes
    LIGHT,  // Bright backgrounds, dark text — default theme
    DARK;   // Dark backgrounds, light text — night mode

    // ============================================
    // PRIMARY COLORS — Brand Identity
    // These are the same in both themes (no theme switching needed)
    // ============================================

    /**
     * Primary accent color — Indigo (#6366f1)
     * Used for: active navigation buttons, primary action buttons, chart accents
     * RGB: Red=99, Green=102, Blue=241
     *
     * @return A new Color object with the primary indigo color
     */
    public Color getPrimaryColor() {
        // new Color(r, g, b) creates an opaque RGB color
        return new Color(99, 102, 241);
    }

    /**
     * Primary hover state — darker indigo (#4F46E5)
     * Used for: button hover effects, gradient endpoints
     *
     * @return Darker variant of the primary color
     */
    public Color getPrimaryHover() {
        return new Color(79, 70, 229);
    }

    /**
     * Primary light — very transparent indigo
     * Used for: subtle backgrounds behind primary-colored elements
     * Alpha = 25 out of 255 ≈ 10% opacity
     *
     * @return Semi-transparent version of the primary color
     */
    public Color getPrimaryLight() {
        // 4th parameter is alpha (transparency): 0 = invisible, 255 = fully opaque
        return new Color(99, 102, 241, 25);
    }

    // ============================================
    // SEMANTIC COLORS — Meaning-based Colors
    // Green = positive/good, Red = negative/bad, Amber = warning/attention
    // Same values in both themes for consistent meaning
    // ============================================

    /**
     * Success green (#10B981) — represents income and positive values
     * @return Green color for income indicators
     */
    public Color getSuccessColor() {
        return new Color(16, 185, 129);
    }

    /**
     * Light success — semi-transparent green for backgrounds
     * @return Transparent green for income card backgrounds
     */
    public Color getSuccessLight() {
        return new Color(16, 185, 129, 25);
    }

    /**
     * Danger red (#EF4444) — represents expenses and negative values
     * @return Red color for expense indicators and delete buttons
     */
    public Color getDangerColor() {
        return new Color(239, 68, 68);
    }

    /**
     * Light danger — semi-transparent red for backgrounds
     * @return Transparent red for expense card backgrounds
     */
    public Color getDangerLight() {
        return new Color(239, 68, 68, 25);
    }

    /**
     * Warning amber (#F59E0B) — represents alerts and the food category
     * @return Amber color for warning states
     */
    public Color getWarningColor() {
        return new Color(245, 158, 11);
    }

    // ============================================
    // BACKGROUND COLORS — Theme-dependent
    // These change based on LIGHT vs DARK mode
    // Uses the ternary operator: condition ? trueValue : falseValue
    // ============================================

    /**
     * Primary background — the main page/app background color.
     * LIGHT: Very light blue-gray (#F8FAFC) — easy on the eyes
     * DARK: Very dark navy blue (#0F172A) — reduces screen glare at night
     *
     * The ternary operator is a compact if-else:
     *   this == LIGHT ? lightColor : darkColor
     *
     * @return Background color based on current theme
     */
    public Color getBackgroundPrimary() {
        return this == LIGHT ? new Color(248, 250, 252) : new Color(15, 23, 42);
    }

    /**
     * Secondary background — used for cards, panels, and elevated surfaces.
     * LIGHT: Pure white (#FFFFFF) — cards stand out against the gray background
     * DARK: Dark slate (#1E293B) — subtle elevation in dark mode
     *
     * @return Card/panel background color based on current theme
     */
    public Color getBackgroundSecondary() {
        return this == LIGHT ? Color.WHITE : new Color(30, 41, 59);
    }

    /**
     * Tertiary background — used for inputs, subtle areas, and hover states.
     * LIGHT: Light gray (#F1F5F9) — slightly darker than the page background
     * DARK: Medium slate (#334155) — slightly lighter than dark cards
     *
     * @return Input/hover background color based on current theme
     */
    public Color getBackgroundTertiary() {
        return this == LIGHT ? new Color(241, 245, 249) : new Color(51, 65, 85);
    }

    // ============================================
    // TEXT COLORS — Theme-dependent
    // Text colors invert between themes to maintain readability
    // LIGHT mode: dark text on light backgrounds
    // DARK mode: light text on dark backgrounds
    // ============================================

    /**
     * Primary text — used for headings and important content.
     * LIGHT: Near-black (#1E293B) — high contrast against white
     * DARK: Near-white (#F1F5F9) — high contrast against dark backgrounds
     *
     * @return Primary text color based on current theme
     */
    public Color getTextPrimary() {
        return this == LIGHT ? new Color(30, 41, 59) : new Color(241, 245, 249);
    }

    /**
     * Secondary text — used for labels, subtitles, and less important content.
     * Lower contrast than primary text to create visual hierarchy.
     *
     * @return Secondary text color based on current theme
     */
    public Color getTextSecondary() {
        return this == LIGHT ? new Color(100, 116, 139) : new Color(148, 163, 184);
    }

    /**
     * Muted text — used for placeholders, hints, and disabled states.
     * Lowest contrast — used for non-essential information.
     *
     * @return Muted text color based on current theme
     */
    public Color getTextMuted() {
        return this == LIGHT ? new Color(148, 163, 184) : new Color(100, 116, 139);
    }

    // ============================================
    // BORDER COLORS — Theme-dependent
    // Borders separate visual sections (cards, inputs, dividers)
    // ============================================

    /**
     * Border/divider color used for card outlines and separators.
     * LIGHT: Light gray (#E2E8F0) — subtle separation
     * DARK: Dark slate (#334155) — visible but not harsh
     *
     * @return Border color based on current theme
     */
    public Color getBorderColor() {
        return this == LIGHT ? new Color(226, 232, 240) : new Color(51, 65, 85);
    }

    // ============================================
    // TYPOGRAPHY — Font Definitions
    // All fonts use "Segoe UI" which is the default Windows system font
    // Different weights (PLAIN, BOLD) and sizes create visual hierarchy
    // ============================================

    /**
     * Default body text font — Segoe UI, Regular, 14px
     * Used for: paragraph text, form inputs, list items
     *
     * Font constructor: new Font(name, style, size)
     *   - name: font family ("Segoe UI", "Arial", etc.)
     *   - style: Font.PLAIN, Font.BOLD, Font.ITALIC, or Font.BOLD | Font.ITALIC
     *   - size: font size in points (1 point ≈ 1.333 pixels)
     *
     * @return Default font for body text
     */
    public Font getDefaultFont() {
        return new Font("Segoe UI", Font.PLAIN, 14);
    }

    /**
     * Bold font — Segoe UI, Bold, 14px
     * Used for: transaction amounts, card titles, emphasis text
     *
     * @return Bold version of the default font
     */
    public Font getBoldFont() {
        return new Font("Segoe UI", Font.BOLD, 14);
    }

    /**
     * Heading font — Segoe UI, Bold, 18px
     * Used for: section titles like "Recent Transactions", "Settings"
     *
     * @return Larger bold font for headings
     */
    public Font getHeadingFont() {
        return new Font("Segoe UI", Font.BOLD, 18);
    }

    /**
     * Balance font — Segoe UI, Bold, 28px
     * Used for: the large balance display on the dashboard
     * This is the biggest font in the app — draws immediate attention
     *
     * @return Extra-large bold font for the balance display
     */
    public Font getBalanceFont() {
        return new Font("Segoe UI", Font.BOLD, 28);
    }

    /**
     * Small font — Segoe UI, Regular, 12px
     * Used for: category labels, date stamps, hint text
     *
     * @return Smaller font for less prominent text
     */
    public Font getSmallFont() {
        return new Font("Segoe UI", Font.PLAIN, 12);
    }

    // ============================================
    // UI HELPER COLORS — Theme-dependent
    // These are specialized colors used for specific UI effects
    // ============================================

    /**
     * Shadow color for card elevation effects.
     * LIGHT: Very subtle black shadow (alpha=25, about 10% opacity)
     * DARK: Stronger black shadow (alpha=100, about 40% opacity) because dark
     *       backgrounds need more contrast to perceive depth
     *
     * @return Shadow color based on current theme
     */
    public Color getShadowColor() {
        return this == LIGHT
            ? new Color(0, 0, 0, 25)    // Subtle shadow on white
            : new Color(0, 0, 0, 100);  // Stronger shadow on dark
    }

    /**
     * Modal overlay color — the semi-transparent backdrop behind modal dialogs.
     * Both themes use black, but dark mode uses higher opacity for more dimming.
     *
     * @return Overlay color for modal backgrounds
     */
    public Color getModalOverlayColor() {
        // alpha = 128 (50% opacity) for light, 180 (70% opacity) for dark
        return new Color(0, 0, 0, this == LIGHT ? 128 : 180);
    }

    /**
     * Hover background — shown when the mouse hovers over a list item.
     * Creates a subtle visual feedback that the item is interactive.
     *
     * @return Background color for hovered items
     */
    public Color getHoverBackground() {
        return this == LIGHT
            ? new Color(241, 245, 249)   // Light gray highlight
            : new Color(51, 65, 85);     // Dark slate highlight
    }

    /**
     * Selected/active background — shown for currently selected list items.
     * Slightly more prominent than hover to indicate active selection.
     *
     * @return Background color for selected items
     */
    public Color getSelectedBackground() {
        return this == LIGHT
            ? new Color(226, 232, 240)   // Medium gray = selected
            : new Color(71, 85, 105);    // Medium slate = selected
    }
}
