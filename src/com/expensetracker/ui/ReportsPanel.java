package com.expensetracker.ui;

import com.expensetracker.data.DataManager;
import com.expensetracker.model.Category;
import com.expensetracker.model.Transaction;
import com.expensetracker.util.Theme;
import com.expensetracker.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Reports and analytics panel with charts and statistics
 */
public class ReportsPanel extends JPanel implements MainFrame.Refreshable, MainFrame.Themeable {

    private final DataManager dataManager;
    private Theme currentTheme;

    // UI Components
    private JPanel periodSelector;
    private JPanel chartPanel;
    private JPanel categoryBreakdownPanel;
    private JPanel monthlyTrendPanel;

    // Current selected period
    private String currentPeriod = "month";

    /**
     * Create the reports panel
     */
    public ReportsPanel(DataManager dataManager, Theme theme) {
        this.dataManager = dataManager;
        this.currentTheme = theme;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(theme.getBackgroundPrimary());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Period selector
        periodSelector = createPeriodSelector();
        add(periodSelector);

        add(Box.createRigidArea(new Dimension(0, 20)));

        // Summary stats cards
        JPanel statsPanel = createStatsCards();
        add(statsPanel);

        add(Box.createRigidArea(new Dimension(0, 20)));

        // Chart panel
        chartPanel = createChartPanel();
        add(chartPanel);

        add(Box.createRigidArea(new Dimension(0, 20)));

        // Category breakdown
        categoryBreakdownPanel = createCategoryBreakdownPanel();
        add(categoryBreakdownPanel);

        add(Box.createRigidArea(new Dimension(0, 20)));

        // Monthly trend
        monthlyTrendPanel = createMonthlyTrendPanel();
        add(monthlyTrendPanel);

        add(Box.createVerticalGlue());
    }

    /**
     * Create period selector buttons
     */
    private JPanel createPeriodSelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setBackground(currentTheme.getBackgroundPrimary());

        String[] periods = {"Week", "Month", "Year", "All"};
        String[] periodIds = {"week", "month", "year", "all"};

        for (int i = 0; i < periods.length; i++) {
            final String periodId = periodIds[i];
            JButton btn = new JButton(periods[i]);
            btn.setFont(currentTheme.getDefaultFont());
            btn.setBackground(currentTheme.getBackgroundTertiary());
            btn.setForeground(currentTheme.getTextSecondary());
            btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (periodId.equals(currentPeriod)) {
                btn.setBackground(currentTheme.getPrimaryColor());
                btn.setForeground(Color.WHITE);
            }

            btn.addActionListener(e -> {
                currentPeriod = periodId;
                for (Component c : panel.getComponents()) {
                    if (c instanceof JButton) {
                        JButton b = (JButton) c;
                        if (periodIds[((JButton) c).getText().equals("Week") ? 0 :
                                       ((JButton) c).getText().equals("Month") ? 1 :
                                       ((JButton) c).getText().equals("Year") ? 2 : 3]
                            .equals(currentPeriod)) {
                            b.setBackground(currentTheme.getPrimaryColor());
                            b.setForeground(Color.WHITE);
                        } else {
                            b.setBackground(currentTheme.getBackgroundTertiary());
                            b.setForeground(currentTheme.getTextSecondary());
                        }
                    }
                }
                refresh();
            });

            panel.add(btn);
        }

        return panel;
    }

    /**
     * Create summary statistics cards
     */
    private JPanel createStatsCards() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBackground(currentTheme.getBackgroundPrimary());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Calculate stats based on period
        LocalDate startDate = getPeriodStartDate();
        List<Transaction> periodTransactions = dataManager.getTransactionsByDateRange(
            startDate, LocalDate.now());

        double income = periodTransactions.stream()
            .filter(Transaction::isIncome)
            .mapToDouble(Transaction::getAmount)
            .sum();

        double expense = periodTransactions.stream()
            .filter(Transaction::isExpense)
            .mapToDouble(Transaction::getAmount)
            .sum();

        double net = income - expense;

        // Income card
        panel.add(createStatCard("Income", income, currentTheme.getSuccessColor()));

        // Expense card
        panel.add(createStatCard("Expense", expense, currentTheme.getDangerColor()));

        // Net card
        panel.add(createStatCard("Net", net,
            net >= 0 ? currentTheme.getSuccessColor() : currentTheme.getDangerColor()));

        return panel;
    }

    /**
     * Create a single stat card
     */
    private JPanel createStatCard(String title, double amount, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 5));
        card.setBackground(currentTheme.getBackgroundSecondary());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(currentTheme.getSmallFont());
        titleLabel.setForeground(currentTheme.getTextMuted());

        JLabel amountLabel = new JLabel(UIUtil.formatCurrency(amount, dataManager.getCurrency()));
        amountLabel.setFont(currentTheme.getBoldFont());
        amountLabel.setForeground(color);

        card.add(titleLabel);
        card.add(amountLabel);

        return card;
    }

    /**
     * Create the pie chart panel
     */
    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(currentTheme.getBackgroundPrimary());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        panel.setPreferredSize(new Dimension(0, 280));

        JLabel title = new JLabel("Spending Distribution");
        title.setFont(currentTheme.getHeadingFont());
        title.setForeground(currentTheme.getTextPrimary());
        panel.add(title, BorderLayout.NORTH);

        // Custom pie chart component
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UIUtil.enableAntiAliasing(g2);

                // Get spending data
                LocalDate startDate = getPeriodStartDate();
                List<Transaction> expenses = dataManager.getTransactionsByDateRange(
                    startDate, LocalDate.now()).stream()
                    .filter(Transaction::isExpense)
                    .toList();

                if (expenses.isEmpty()) {
                    g2.setColor(currentTheme.getTextMuted());
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    g2.drawString("No expense data for this period",
                        getWidth() / 2 - 100, getHeight() / 2);
                    g2.dispose();
                    return;
                }

                // Calculate totals by category
                Map<String, Double> categoryTotals = new java.util.HashMap<>();
                double total = 0;
                for (Transaction t : expenses) {
                    categoryTotals.put(t.getCategory(),
                        categoryTotals.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
                    total += t.getAmount();
                }

                // Draw pie chart
                int size = Math.min(getWidth(), getHeight()) - 40;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2 + 20;

                double startAngle = -90;
                for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                    Category cat = Category.getById(entry.getKey());
                    double sliceAngle = 360 * entry.getValue() / total;

                    g2.setColor(cat.getColor());
                    g2.fillArc(x, y, size, size, (int) startAngle, (int) sliceAngle);

                    startAngle += sliceAngle;
                }

                // Draw center circle (donut effect)
                g2.setColor(currentTheme.getBackgroundSecondary());
                int holeSize = size / 2;
                int holeX = (getWidth() - holeSize) / 2;
                int holeY = (getHeight() - holeSize) / 2 + 20;
                g2.fillOval(holeX, holeY, holeSize, holeSize);

                // Draw total in center
                g2.setColor(currentTheme.getTextPrimary());
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String totalStr = UIUtil.formatCurrency(total, dataManager.getCurrency());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(totalStr,
                    (getWidth() - fm.stringWidth(totalStr)) / 2,
                    (getHeight() + fm.getHeight()) / 2 + 5);

                g2.dispose();
            }
        };

        chart.setBackground(currentTheme.getBackgroundPrimary());
        panel.add(chart, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create category breakdown panel
     */
    private JPanel createCategoryBreakdownPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(currentTheme.getBackgroundPrimary());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel title = new JLabel("Category Breakdown");
        title.setFont(currentTheme.getHeadingFont());
        title.setForeground(currentTheme.getTextPrimary());
        panel.add(title, BorderLayout.NORTH);

        categoryBreakdownPanel = new JPanel();
        categoryBreakdownPanel.setLayout(new BoxLayout(categoryBreakdownPanel, BoxLayout.Y_AXIS));
        categoryBreakdownPanel.setBackground(currentTheme.getBackgroundPrimary());

        JScrollPane scrollPane = new JScrollPane(categoryBreakdownPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(currentTheme.getBackgroundPrimary());
        scrollPane.getViewport().setBackground(currentTheme.getBackgroundPrimary());

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create monthly trend panel
     */
    private JPanel createMonthlyTrendPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(currentTheme.getBackgroundPrimary());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel title = new JLabel("Monthly Trend");
        title.setFont(currentTheme.getHeadingFont());
        title.setForeground(currentTheme.getTextPrimary());
        panel.add(title, BorderLayout.NORTH);

        monthlyTrendPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UIUtil.enableAntiAliasing(g2);

                // Get last 6 months of data
                LocalDate now = LocalDate.now();
                double[] monthlyExpenses = new double[6];

                for (int i = 0; i < 6; i++) {
                    LocalDate monthStart = now.minusMonths(5 - i).withDayOfMonth(1);
                    LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

                    List<Transaction> monthTransactions = dataManager.getTransactionsByDateRange(
                        monthStart, monthEnd);

                    monthlyExpenses[i] = monthTransactions.stream()
                        .filter(Transaction::isExpense)
                        .mapToDouble(Transaction::getAmount)
                        .sum();
                }

                // Find max for scaling
                double max = 0;
                for (double val : monthlyExpenses) {
                    if (val > max) max = val;
                }

                if (max == 0) {
                    g2.setColor(currentTheme.getTextMuted());
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    g2.drawString("No data available", 20, getHeight() / 2);
                    g2.dispose();
                    return;
                }

                // Draw bar chart
                int barWidth = 40;
                int gap = 20;
                int chartHeight = getHeight() - 50;
                int startX = 40;

                String[] monthLabels = {"6M", "5M", "4M", "3M", "2M", "1M"};

                for (int i = 0; i < 6; i++) {
                    int barHeight = (int) ((monthlyExpenses[i] / max) * (chartHeight - 20));
                    int x = startX + i * (barWidth + gap);
                    int y = getHeight() - 30 - barHeight;

                    // Draw bar
                    g2.setColor(currentTheme.getPrimaryColor());
                    g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

                    // Draw label
                    g2.setColor(currentTheme.getTextSecondary());
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g2.drawString(monthLabels[i], x + 5, getHeight() - 10);

                    // Draw value
                    g2.setColor(currentTheme.getTextPrimary());
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    String valStr = String.format("%.0f", monthlyExpenses[i] / 1000) + "k";
                    if (monthlyExpenses[i] < 1000) {
                        valStr = String.format("%.0f", monthlyExpenses[i]);
                    }
                    g2.drawString(valStr, x + (barWidth - g2.getFontMetrics().stringWidth(valStr)) / 2, y - 5);
                }

                g2.dispose();
            }
        };

        monthlyTrendPanel.setBackground(currentTheme.getBackgroundPrimary());
        monthlyTrendPanel.setPreferredSize(new Dimension(0, 150));

        panel.add(monthlyTrendPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Get start date for current period
     */
    private LocalDate getPeriodStartDate() {
        LocalDate now = LocalDate.now();
        switch (currentPeriod) {
            case "week": return now.minusWeeks(1);
            case "month": return now.minusMonths(1);
            case "year": return now.minusYears(1);
            default: return LocalDate.of(2000, 1, 1); // All
        }
    }

    /**
     * Update category breakdown display
     */
    private void updateCategoryBreakdown() {
        categoryBreakdownPanel.removeAll();

        LocalDate startDate = getPeriodStartDate();
        List<Transaction> expenses = dataManager.getTransactionsByDateRange(
            startDate, LocalDate.now()).stream()
            .filter(Transaction::isExpense)
            .toList();

        if (expenses.isEmpty()) {
            JLabel emptyLabel = new JLabel("No expense data for this period");
            emptyLabel.setFont(currentTheme.getDefaultFont());
            emptyLabel.setForeground(currentTheme.getTextMuted());
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            categoryBreakdownPanel.add(emptyLabel);
        } else {
            // Calculate totals
            Map<String, Double> categoryTotals = new java.util.HashMap<>();
            double totalAmount = 0;

            for (Transaction t : expenses) {
                categoryTotals.put(t.getCategory(),
                    categoryTotals.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
                totalAmount += t.getAmount();
            }

            double total = totalAmount; // Make final for lambda

            // Sort and display
            categoryTotals.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    Category cat = Category.getById(entry.getKey());
                    double percentage = (entry.getValue() / total) * 100;

                    JPanel item = new JPanel(new GridBagLayout());
                    item.setBackground(currentTheme.getBackgroundSecondary());
                    item.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true),
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)
                    ));
                    item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.weightx = 1;

                    // Icon
                    JPanel iconPanel = new JPanel(new GridBagLayout());
                    iconPanel.setBackground(cat.getLightColor());
                    iconPanel.setPreferredSize(new Dimension(40, 40));
                    JLabel iconLabel = new JLabel(getIconChar(cat.getIconName()));
                    iconLabel.setForeground(cat.getColor());
                    iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
                    iconPanel.add(iconLabel);

                    gbc.gridx = 0;
                    gbc.insets = new Insets(0, 0, 0, 12);
                    item.add(iconPanel, gbc);

                    // Info
                    JPanel infoPanel = new JPanel(new BorderLayout(10, 5));
                    infoPanel.setBackground(currentTheme.getBackgroundSecondary());
                    infoPanel.setOpaque(false);

                    JPanel topRow = new JPanel(new BorderLayout());
                    topRow.setBackground(currentTheme.getBackgroundSecondary());
                    topRow.setOpaque(false);

                    JLabel nameLabel = new JLabel(cat.getName());
                    nameLabel.setFont(currentTheme.getDefaultFont());
                    nameLabel.setForeground(currentTheme.getTextPrimary());

                    JLabel amountLabel = new JLabel(
                        UIUtil.formatCurrency(entry.getValue(), dataManager.getCurrency()));
                    amountLabel.setFont(currentTheme.getBoldFont());
                    amountLabel.setForeground(currentTheme.getTextPrimary());

                    JLabel pctLabel = new JLabel(String.format("%.1f%%", percentage));
                    pctLabel.setFont(currentTheme.getSmallFont());
                    pctLabel.setForeground(currentTheme.getTextMuted());

                    topRow.add(nameLabel, BorderLayout.WEST);
                    topRow.add(amountLabel, BorderLayout.CENTER);
                    topRow.add(pctLabel, BorderLayout.EAST);

                    // Progress bar
                    JProgressBar bar = new JProgressBar(0, 100);
                    bar.setValue((int) percentage);
                    bar.setString("");
                    bar.setForeground(cat.getColor());
                    bar.setBackground(currentTheme.getBackgroundTertiary());
                    bar.setPreferredSize(new Dimension(0, 6));
                    bar.setBorder(null);

                    infoPanel.add(topRow, BorderLayout.NORTH);
                    infoPanel.add(bar, BorderLayout.CENTER);

                    gbc.gridx = 1;
                    item.add(infoPanel, gbc);

                    categoryBreakdownPanel.add(item);
                    categoryBreakdownPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                });
        }

        categoryBreakdownPanel.revalidate();
        categoryBreakdownPanel.repaint();
    }

    @Override
    public void refresh() {
        repaint(); // Repaints chart and monthly trend
        updateCategoryBreakdown();
    }

    public void refreshData() {
        refresh();
    }

    /**
     * Convert icon name to display character
     * Maps icon names to Unicode characters for display
     */
    private String getIconChar(String iconName) {
        return switch (iconName) {
            case "food" -> "🍔";
            case "transport" -> "🚗";
            case "entertainment" -> "🎬";
            case "utilities" -> "💡";
            case "shopping" -> "🛍️";
            case "health" -> "⚕️";
            case "education" -> "📚";
            case "salary" -> "💼";
            case "bonus" -> "🎁";
            case "investment" -> "📈";
            default -> "•";
        };
    }

    @Override
    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        setBackground(theme.getBackgroundPrimary());
        refresh();
    }
}
