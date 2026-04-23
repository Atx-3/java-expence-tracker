// Package declaration — belongs to the 'model' layer alongside Transaction.java
package com.expensetracker.model;

// java.awt.Color is a class from Java's Abstract Window Toolkit (AWT)
// It represents an RGB color with optional alpha (transparency) channel
// Used here to assign a unique color to each category for UI display
import java.awt.Color;

/**
 * Category.java — CATEGORY DATA MODEL
 * =====================================
 * This class defines the predefined expense and income categories.
 * Each category has an ID, display name, icon, color, and type flag.
 *
 * Key Concepts:
 *   - 'final' keyword: All fields are declared final — they cannot be changed after construction
 *     This makes Category objects IMMUTABLE (safe to share across threads)
 *   - Static arrays: EXPENSE_CATEGORIES and INCOME_CATEGORIES are shared across all instances
 *   - Factory method: getById() acts as a lookup method to find a category by its ID
 *
 * Category is NOT an enum because we need flexible fields (icon, color) for each entry.
 */
public class Category {

    // ============================================
    // FIELDS — All 'final' (immutable after construction)
    // ============================================

    // Unique string identifier (e.g., "food", "transport", "salary")
    // Used as a foreign key in Transaction objects
    private final String id;

    // Human-readable name shown in the UI (e.g., "Food", "Transport")
    private final String name;

    // Font Awesome icon name used in the web version (e.g., "utensils", "car")
    // In the desktop version, these are mapped to Unicode symbols via getIconChar()
    private final String iconName;

    // The accent color for this category in the UI
    // Used for icon backgrounds, progress bars, and chart slices
    private final Color color;

    // Flag to distinguish income categories from expense categories
    // true = income category (Salary, Freelance), false = expense category (Food, Bills)
    private final boolean incomeCategory;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    /**
     * Parameterized constructor — creates a new category with all required fields.
     * All parameters are assigned to 'final' fields, making this object immutable.
     *
     * @param id             Unique identifier string (e.g., "food")
     * @param name           Display name for the UI (e.g., "Food")
     * @param iconName       Font Awesome icon name (e.g., "utensils")
     * @param color          AWT Color object for UI highlighting
     * @param incomeCategory true if this is an income category, false for expense
     */
    public Category(String id, String name, String iconName, Color color, boolean incomeCategory) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
        this.color = color;
        this.incomeCategory = incomeCategory;
    }

    // ============================================
    // GETTERS (No setters — object is immutable)
    // ============================================

    // Returns the unique ID used to reference this category in transactions
    public String getId() {
        return id;
    }

    // Returns the display name shown in the UI
    public String getName() {
        return name;
    }

    // Returns the Font Awesome icon name for rendering
    public String getIconName() {
        return iconName;
    }

    // Returns the accent color (java.awt.Color) for UI elements
    public Color getColor() {
        return color;
    }

    // Returns whether this is an income category
    public boolean isIncomeCategory() {
        return incomeCategory;
    }

    // ============================================
    // DERIVED COLOR METHOD
    // ============================================

    /**
     * Creates a lighter, semi-transparent version of the category color.
     * Used for background highlighting (e.g., icon backgrounds in the dashboard).
     *
     * The Color constructor accepts (red, green, blue, alpha) where:
     *   - alpha = 0 is fully transparent
     *   - alpha = 255 is fully opaque
     *   - alpha = 50 gives a subtle tinted background
     *
     * @return A new Color object with the same RGB values but low opacity (50/255 ≈ 20%)
     */
    public Color getLightColor() {
        return new Color(
            color.getRed(),     // Keep the same red component
            color.getGreen(),   // Keep the same green component
            color.getBlue(),    // Keep the same blue component
            50                  // Alpha = 50 out of 255 → semi-transparent
        );
    }

    // ============================================
    // STATIC ARRAYS — Predefined Category Definitions
    // 'static final' means these arrays:
    //   - 'static': Belong to the CLASS, not any individual object
    //   - 'final': The array reference can't be reassigned (but contents theoretically could be)
    // These serve as the "database" of available categories
    // ============================================

    /**
     * Predefined EXPENSE categories — 9 categories covering common spending types.
     * Each is created with: new Category(id, name, iconName, color, isIncome=false)
     * The Color values are chosen to match the web app's design palette.
     */
    public static final Category[] EXPENSE_CATEGORIES = {
        new Category("food", "Food", "utensils", new Color(245, 158, 11), false),           // Amber
        new Category("transport", "Transport", "car", new Color(59, 130, 246), false),       // Blue
        new Category("shopping", "Shopping", "shopping-bag", new Color(236, 72, 153), false), // Pink
        new Category("entertainment", "Entertainment", "film", new Color(139, 92, 246), false), // Purple
        new Category("bills", "Bills", "file-invoice-dollar", new Color(239, 68, 68), false),   // Red
        new Category("health", "Health", "heartbeat", new Color(16, 185, 129), false),       // Green
        new Category("education", "Education", "graduation-cap", new Color(6, 182, 212), false), // Cyan
        new Category("personal", "Personal", "user", new Color(99, 102, 241), false),        // Indigo
        new Category("other", "Other", "ellipsis-h", new Color(100, 116, 139), false)        // Slate gray
    };

    /**
     * Predefined INCOME categories — 2 categories for money coming in.
     * In a production app, you might add: Salary, Freelance, Investment, Gift, Refund, etc.
     */
    public static final Category[] INCOME_CATEGORIES = {
        new Category("income", "Income", "arrow-up", new Color(16, 185, 129), true),   // Green (success)
        new Category("other", "Other", "ellipsis-h", new Color(100, 116, 139), true)   // Slate gray
    };

    // ============================================
    // STATIC LOOKUP METHODS
    // These are utility methods shared by the entire app
    // They search through the predefined arrays to find categories
    // ============================================

    /**
     * Looks up a category by its unique ID string.
     * It searches EXPENSE_CATEGORIES first, then INCOME_CATEGORIES.
     * If no match is found, it returns the last expense category ("Other") as a fallback.
     *
     * This is essentially a "find" operation — similar to SELECT * FROM categories WHERE id = ?
     *
     * @param id The category ID to search for (e.g., "food", "salary")
     * @return The matching Category object, or "Other" if not found
     */
    public static Category getById(String id) {
        // Search through all expense categories using a for-each loop
        for (Category cat : EXPENSE_CATEGORIES) {
            if (cat.id.equals(id)) return cat;  // Found it — return immediately
        }

        // If not found in expenses, search income categories
        for (Category cat : INCOME_CATEGORIES) {
            if (cat.id.equals(id)) return cat;
        }

        // Fallback: return "Other" (last element in expense categories)
        // This prevents NullPointerException when a category ID is invalid
        return EXPENSE_CATEGORIES[EXPENSE_CATEGORIES.length - 1];
    }

    /**
     * Combines both arrays into a single array containing ALL categories.
     * Used when the UI needs to display all categories regardless of type.
     *
     * System.arraycopy() is a native method that efficiently copies array elements.
     * Parameters: (sourceArray, sourceStartIndex, destArray, destStartIndex, length)
     *
     * @return A new array containing all 11 categories (9 expense + 2 income)
     */
    public static Category[] getAllCategories() {
        // Create a new array large enough to hold both arrays
        Category[] all = new Category[EXPENSE_CATEGORIES.length + INCOME_CATEGORIES.length];

        // Copy all expense categories to positions 0..8
        System.arraycopy(EXPENSE_CATEGORIES, 0, all, 0, EXPENSE_CATEGORIES.length);

        // Copy all income categories starting at position 9
        System.arraycopy(INCOME_CATEGORIES, 0, all, EXPENSE_CATEGORIES.length, INCOME_CATEGORIES.length);

        return all;
    }
}
