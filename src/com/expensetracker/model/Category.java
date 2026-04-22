package com.expensetracker.model;

import java.awt.Color;

/**
 * Represents an expense/income category
 * Each category has an icon, name, and associated color
 */
public class Category {

    // Unique category identifier
    private final String id;

    // Display name for the category
    private final String name;

    // Font Awesome icon name (without 'fa-' prefix)
    private final String iconName;

    // Category color for UI highlighting
    private final Color color;

    // Whether this is an income category
    private final boolean incomeCategory;

    /**
     * Create a new category
     * @param id Unique identifier
     * @param name Display name
     * @param iconName Font Awesome icon name
     * @param color UI color
     * @param incomeCategory true for income, false for expense
     */
    public Category(String id, String name, String iconName, Color color, boolean incomeCategory) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
        this.color = color;
        this.incomeCategory = incomeCategory;
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconName() {
        return iconName;
    }

    public Color getColor() {
        return color;
    }

    public boolean isIncomeCategory() {
        return incomeCategory;
    }

    /**
     * Get lighter version of category color for backgrounds
     * @return Lighter color (50% opacity equivalent)
     */
    public Color getLightColor() {
        return new Color(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            50  // Semi-transparent
        );
    }

    /**
     * Predefined expense categories
     */
    public static final Category[] EXPENSE_CATEGORIES = {
        new Category("food", "Food", "utensils", new Color(245, 158, 11), false),
        new Category("transport", "Transport", "car", new Color(59, 130, 246), false),
        new Category("shopping", "Shopping", "shopping-bag", new Color(236, 72, 153), false),
        new Category("entertainment", "Entertainment", "film", new Color(139, 92, 246), false),
        new Category("bills", "Bills", "file-invoice-dollar", new Color(239, 68, 68), false),
        new Category("health", "Health", "heartbeat", new Color(16, 185, 129), false),
        new Category("education", "Education", "graduation-cap", new Color(6, 182, 212), false),
        new Category("personal", "Personal", "user", new Color(99, 102, 241), false),
        new Category("other", "Other", "ellipsis-h", new Color(100, 116, 139), false)
    };

    /**
     * Predefined income categories
     */
    public static final Category[] INCOME_CATEGORIES = {
        new Category("income", "Income", "arrow-up", new Color(16, 185, 129), true),
        new Category("other", "Other", "ellipsis-h", new Color(100, 116, 139), true)
    };

    /**
     * Get category by ID
     * @param id Category ID to find
     * @return Category if found, null otherwise
     */
    public static Category getById(String id) {
        for (Category cat : EXPENSE_CATEGORIES) {
            if (cat.id.equals(id)) return cat;
        }
        for (Category cat : INCOME_CATEGORIES) {
            if (cat.id.equals(id)) return cat;
        }
        return EXPENSE_CATEGORIES[EXPENSE_CATEGORIES.length - 1]; // Default to "other"
    }

    /**
     * Get all categories (both income and expense)
     * @return Combined array of all categories
     */
    public static Category[] getAllCategories() {
        Category[] all = new Category[EXPENSE_CATEGORIES.length + INCOME_CATEGORIES.length];
        System.arraycopy(EXPENSE_CATEGORIES, 0, all, 0, EXPENSE_CATEGORIES.length);
        System.arraycopy(INCOME_CATEGORIES, 0, all, EXPENSE_CATEGORIES.length, INCOME_CATEGORIES.length);
        return all;
    }
}
