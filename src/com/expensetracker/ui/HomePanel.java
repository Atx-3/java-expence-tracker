package com.expensetracker.ui;

import com.expensetracker.data.DataManager;
import com.expensetracker.model.Category;
import com.expensetracker.model.Transaction;
import com.expensetracker.util.Theme;
import com.expensetracker.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Home screen panel showing balance overview and recent transactions
 */
public class HomePanel extends JPanel implements MainFrame.Refreshable, MainFrame.Themeable {

    private final DataManager dataManager;
    private Theme currentTheme;

    // UI Components
    private JLabel balanceLabel;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JPanel transactionsContainer;
    private JPanel categoriesContainer;

    /**
     * Create the home panel
     */
    public HomePanel(DataManager dataManager, Theme theme) {
        this.dataManager = dataManager;
        this.currentTheme = theme;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(theme.getBackgroundPrimary());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
    }

    /**
     * Initialize all UI components
     */
    private void initializeComponents() {
        // Balance Card (gradient background)
        JPanel balanceCard = createBalanceCard();
        add(balanceCard);

        add(Box.createRigidArea(new Dimension(0, 20)));

        // Summary cards (Income/Expense)
        JPanel summaryPanel = createSummaryCards();
        add(summaryPanel);

        add(Box.createRigidArea(new Dimension(0, 20)));

        // Recent Transactions section
        JPanel transactionsSection = createTransactionsSection();
        add(transactionsSection);

        add(Box.createRigidArea(new Dimension(0, 20)));

        // Category breakdown section
        JPanel categoriesSection = createCategoriesSection();
        add(categoriesSection);

        add(Box.createVerticalGlue()); // Push everything to top
    }

    /**
     * Create the main balance display card
     */
    private JPanel createBalanceCard() {
        JPanel card = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIUtil.enableAntiAliasing(g2);

                // Draw gradient background
                GradientPaint gradient = UIUtil.createGradient(
                    currentTheme.getPrimaryColor(),
                    currentTheme.getPrimaryHover(),
                    getWidth()
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                g2.dispose();
            }
        };

        card.setPreferredSize(new Dimension(0, 140));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Balance info panel
        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Total Balance");
        titleLabel.setForeground(new Color(255, 255, 255, 230));
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        balanceLabel = new JLabel(UIUtil.formatCurrency(dataManager.getTotalBalance(),
            dataManager.getCurrency()));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setFont(currentTheme.getBalanceFont());

        infoPanel.add(titleLabel, BorderLayout.NORTH);
        infoPanel.add(balanceLabel, BorderLayout.CENTER);

        card.add(infoPanel, BorderLayout.CENTER);

        return card;
    }

    /**
     * Create income and expense summary cards
     */
    private JPanel createSummaryCards() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBackground(currentTheme.getBackgroundPrimary());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Income card
        JPanel incomeCard = createSummaryCard("Income", dataManager.getTotalIncome(),
            currentTheme.getSuccessColor(), currentTheme.getSuccessLight(), "\uF062");
        incomeLabel = (JLabel) ((JPanel) incomeCard.getComponent(1)).getComponent(1);

        // Expense card
        JPanel expenseCard = createSummaryCard("Expense", dataManager.getTotalExpenses(),
            currentTheme.getDangerColor(), currentTheme.getDangerLight(), "\uF063");
        expenseLabel = (JLabel) ((JPanel) expenseCard.getComponent(1)).getComponent(1);

        panel.add(incomeCard);
        panel.add(expenseCard);

        return panel;
    }

    /**
     * Create a single summary card
     */
    private JPanel createSummaryCard(String title, double amount, Color accentColor,
                                     Color lightColor, String icon) {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        card.setBackground(currentTheme.getBackgroundSecondary());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Icon circle
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setBackground(lightColor);
        iconPanel.setPreferredSize(new Dimension(40, 40));
        iconPanel.setMaximumSize(new Dimension(40, 40));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setForeground(accentColor);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        iconPanel.add(iconLabel);

        // Amount info
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setBackground(currentTheme.getBackgroundSecondary());
        infoPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(currentTheme.getSmallFont());
        titleLabel.setForeground(currentTheme.getTextMuted());

        JLabel amountLabel = new JLabel(UIUtil.formatCurrency(amount, dataManager.getCurrency()));
        amountLabel.setFont(currentTheme.getBoldFont());
        amountLabel.setForeground(accentColor);

        infoPanel.add(titleLabel);
        infoPanel.add(amountLabel);

        card.add(iconPanel);
        card.add(infoPanel);

        return card;
    }

    /**
     * Create recent transactions section
     */
    private JPanel createTransactionsSection() {
        JPanel section = new JPanel(new BorderLayout(10, 10));
        section.setBackground(currentTheme.getBackgroundPrimary());
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(currentTheme.getBackgroundPrimary());

        JLabel title = new JLabel("Recent Transactions");
        title.setFont(currentTheme.getHeadingFont());
        title.setForeground(currentTheme.getTextPrimary());

        header.add(title, BorderLayout.WEST);

        section.add(header, BorderLayout.NORTH);

        // Transactions container
        transactionsContainer = new JPanel();
        transactionsContainer.setLayout(new BoxLayout(transactionsContainer, BoxLayout.Y_AXIS));
        transactionsContainer.setBackground(currentTheme.getBackgroundPrimary());

        JScrollPane scrollPane = new JScrollPane(transactionsContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(currentTheme.getBackgroundPrimary());
        scrollPane.getViewport().setBackground(currentTheme.getBackgroundPrimary());

        section.add(scrollPane, BorderLayout.CENTER);

        return section;
    }

    /**
     * Create category breakdown section
     */
    private JPanel createCategoriesSection() {
        JPanel section = new JPanel(new BorderLayout(10, 10));
        section.setBackground(currentTheme.getBackgroundPrimary());
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(currentTheme.getBackgroundPrimary());

        JLabel title = new JLabel("Spending by Category");
        title.setFont(currentTheme.getHeadingFont());
        title.setForeground(currentTheme.getTextPrimary());

        header.add(title, BorderLayout.WEST);

        section.add(header, BorderLayout.NORTH);

        // Categories container
        categoriesContainer = new JPanel();
        categoriesContainer.setLayout(new BoxLayout(categoriesContainer, BoxLayout.Y_AXIS));
        categoriesContainer.setBackground(currentTheme.getBackgroundPrimary());

        JScrollPane scrollPane = new JScrollPane(categoriesContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(currentTheme.getBackgroundPrimary());
        scrollPane.getViewport().setBackground(currentTheme.getBackgroundPrimary());

        section.add(scrollPane, BorderLayout.CENTER);

        return section;
    }

    /**
     * Create a transaction item component
     */
    private JPanel createTransactionItem(Transaction transaction) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    UIUtil.enableAntiAliasing(g2);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                }
            }
        };

        Category category = Category.getById(transaction.getCategory());

        // Category icon
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setBackground(category.getLightColor());
        iconPanel.setPreferredSize(new Dimension(48, 48));
        iconPanel.setMaximumSize(new Dimension(48, 48));

        JLabel iconLabel = new JLabel(getIconChar(category.getIconName()));
        iconLabel.setForeground(category.getColor());
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
        iconPanel.add(iconLabel);

        // Transaction details
        JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        detailsPanel.setBackground(currentTheme.getBackgroundPrimary());
        detailsPanel.setOpaque(false);

        JLabel descLabel = new JLabel(transaction.getDescription());
        descLabel.setFont(currentTheme.getDefaultFont());
        descLabel.setForeground(currentTheme.getTextPrimary());

        JLabel dateLabel = new JLabel(transaction.getDate().toString());
        dateLabel.setFont(currentTheme.getSmallFont());
        dateLabel.setForeground(currentTheme.getTextMuted());

        detailsPanel.add(descLabel);
        detailsPanel.add(dateLabel);

        // Amount
        JLabel amountLabel = new JLabel(
            UIUtil.formatCurrencyWithSign(
                transaction.isIncome() ? transaction.getAmount() : -transaction.getAmount(),
                dataManager.getCurrency(),
                "",
                ""
            )
        );
        amountLabel.setFont(currentTheme.getBoldFont());
        amountLabel.setForeground(transaction.isIncome()
            ? currentTheme.getSuccessColor()
            : currentTheme.getDangerColor());

        item.add(iconPanel);
        item.add(detailsPanel);
        item.add(amountLabel);

        // Styling
        item.setBackground(currentTheme.getBackgroundSecondary());
        item.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                item.setBorder(BorderFactory.createLineBorder(currentTheme.getPrimaryColor(), 1, true));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                item.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            }
        });

        return item;
    }

    /**
     * Create a category spending item
     */
    private JPanel createCategoryItem(String categoryId, double amount, double total) {
        Category category = Category.getById(categoryId);

        JPanel item = new JPanel(new GridBagLayout());
        item.setBackground(currentTheme.getBackgroundSecondary());
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Icon
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setBackground(category.getLightColor());
        iconPanel.setPreferredSize(new Dimension(40, 40));

        JLabel iconLabel = new JLabel(getIconChar(category.getIconName()));
        iconLabel.setForeground(category.getColor());
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        iconPanel.add(iconLabel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 12);
        item.add(iconPanel, gbc);

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout(10, 5));
        infoPanel.setBackground(currentTheme.getBackgroundSecondary());
        infoPanel.setOpaque(false);

        // Top row: name and amount
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(currentTheme.getBackgroundSecondary());
        topRow.setOpaque(false);

        JLabel nameLabel = new JLabel(category.getName());
        nameLabel.setFont(currentTheme.getDefaultFont());
        nameLabel.setForeground(currentTheme.getTextPrimary());

        JLabel amountLabel = new JLabel(UIUtil.formatCurrency(amount, dataManager.getCurrency()));
        amountLabel.setFont(currentTheme.getBoldFont());
        amountLabel.setForeground(currentTheme.getTextPrimary());

        topRow.add(nameLabel, BorderLayout.WEST);
        topRow.add(amountLabel, BorderLayout.EAST);

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(total > 0 ? (int) ((amount / total) * 100) : 0);
        progressBar.setString("");
        progressBar.setForeground(category.getColor());
        progressBar.setBackground(currentTheme.getBackgroundTertiary());
        progressBar.setPreferredSize(new Dimension(0, 6));
        progressBar.setBorder(null);

        infoPanel.add(topRow, BorderLayout.NORTH);
        infoPanel.add(progressBar, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 1;
        item.add(infoPanel, gbc);

        return item;
    }

    @Override
    public void refresh() {
        updateBalanceDisplay();
        updateTransactionsList();
        updateCategoriesDisplay();
    }

    /**
     * Update balance and summary labels
     */
    private void updateBalanceDisplay() {
        String currency = dataManager.getCurrency();
        balanceLabel.setText(UIUtil.formatCurrency(dataManager.getTotalBalance(), currency));
        incomeLabel.setText(UIUtil.formatCurrency(dataManager.getTotalIncome(), currency));
        expenseLabel.setText(UIUtil.formatCurrency(dataManager.getTotalExpenses(), currency));
    }

    /**
     * Update recent transactions list
     */
    private void updateTransactionsList() {
        transactionsContainer.removeAll();

        List<Transaction> recent = dataManager.getRecentTransactions(5);

        if (recent.isEmpty()) {
            JLabel emptyLabel = new JLabel("No transactions yet");
            emptyLabel.setFont(currentTheme.getDefaultFont());
            emptyLabel.setForeground(currentTheme.getTextMuted());
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            transactionsContainer.add(emptyLabel);
        } else {
            for (Transaction t : recent) {
                JPanel item = createTransactionItem(t);
                item.setAlignmentX(Component.CENTER_ALIGNMENT);
                item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
                transactionsContainer.add(item);
                transactionsContainer.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        transactionsContainer.revalidate();
        transactionsContainer.repaint();
    }

    /**
     * Update category spending display
     */
    private void updateCategoriesDisplay() {
        categoriesContainer.removeAll();

        // Get expenses from last month
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate oneMonthAgo = now.minusMonths(1);

        List<Transaction> expenses = dataManager.getTransactionsByDateRange(oneMonthAgo, now)
            .stream()
            .filter(Transaction::isExpense)
            .toList();

        if (expenses.isEmpty()) {
            JLabel emptyLabel = new JLabel("No expense data yet");
            emptyLabel.setFont(currentTheme.getDefaultFont());
            emptyLabel.setForeground(currentTheme.getTextMuted());
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            categoriesContainer.add(emptyLabel);
        } else {
            // Calculate spending by category
            java.util.Map<String, Double> categoryTotals = new java.util.HashMap<>();
            double totalAmount = 0;

            for (Transaction t : expenses) {
                categoryTotals.put(t.getCategory(),
                    categoryTotals.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
                totalAmount += t.getAmount();
            }

            double total = totalAmount; // Make final for lambda

            // Sort by amount and display top 5
            categoryTotals.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .forEach(entry -> {
                    JPanel item = createCategoryItem(entry.getKey(), entry.getValue(), total);
                    item.setAlignmentX(Component.CENTER_ALIGNMENT);
                    item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
                    categoriesContainer.add(item);
                    categoriesContainer.add(Box.createRigidArea(new Dimension(0, 8)));
                });
        }

        categoriesContainer.revalidate();
        categoriesContainer.repaint();
    }

    @Override
    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        setBackground(theme.getBackgroundPrimary());
        refresh();
    }

    /**
     * Get a simple unicode character to represent an icon
     * Maps Font Awesome icon names to similar unicode symbols
     */
    private String getIconChar(String iconName) {
        switch (iconName) {
            case "utensils": return "\u2638"; // Flower/food symbol
            case "car": return "\u23F3"; // Hourglass for transport
            case "shopping-bag": return "\u2733"; // Shopping symbol
            case "film": return "\u25A0"; // Film frame
            case "file-invoice-dollar": return "\u2665"; // Payment symbol
            case "heartbeat": return "\u2665"; // Heart
            case "graduation-cap": return "\u273F"; // Education symbol
            case "user": return "\u263A"; // Person
            case "arrow-up": return "\u2191"; // Up arrow
            case "arrow-down": return "\u2193"; // Down arrow
            case "ellipsis-h": return "\u2026"; // Ellipsis
            default: return "\u25CF"; // Default circle
        }
    }
}
