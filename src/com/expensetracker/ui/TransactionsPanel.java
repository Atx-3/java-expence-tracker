package com.expensetracker.ui;

import com.expensetracker.data.DataManager;
import com.expensetracker.model.Category;
import com.expensetracker.model.Transaction;
import com.expensetracker.util.Theme;
import com.expensetracker.util.UIUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel displaying all transactions with search and filter capabilities
 */
public class TransactionsPanel extends JPanel implements MainFrame.Refreshable, MainFrame.Themeable {

    private final DataManager dataManager;
    private Theme currentTheme;

    // UI Components
    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JPanel transactionsList;
    private JLabel emptyLabel;

    // Current filter state
    private String currentSearch = "";
    private String currentType = "all";

    /**
     * Create the transactions panel
     */
    public TransactionsPanel(DataManager dataManager, Theme theme) {
        this.dataManager = dataManager;
        this.currentTheme = theme;

        setLayout(new BorderLayout(15, 15));
        setBackground(theme.getBackgroundPrimary());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Top panel with search and filter
        JPanel topPanel = createFilterPanel();
        add(topPanel, BorderLayout.NORTH);

        // Transactions list
        transactionsList = new JPanel();
        transactionsList.setLayout(new BoxLayout(transactionsList, BoxLayout.Y_AXIS));
        transactionsList.setBackground(currentTheme.getBackgroundPrimary());

        JScrollPane scrollPane = new JScrollPane(transactionsList);
        scrollPane.setBorder(null);
        scrollPane.setBackground(currentTheme.getBackgroundPrimary());
        scrollPane.getViewport().setBackground(currentTheme.getBackgroundPrimary());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        // Empty state label (hidden by default)
        emptyLabel = new JLabel("No transactions found");
        emptyLabel.setFont(currentTheme.getDefaultFont());
        emptyLabel.setForeground(currentTheme.getTextMuted());
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    /**
     * Create the filter panel with search and type selector
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(currentTheme.getBackgroundPrimary());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 10);

        // Search field
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        searchPanel.setBackground(currentTheme.getBackgroundTertiary());
        searchPanel.setBorder(BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true));

        searchField = new JTextField(15);
        searchField.setFont(currentTheme.getDefaultFont());
        searchField.setBackground(currentTheme.getBackgroundTertiary());
        searchField.setForeground(currentTheme.getTextPrimary());
        searchField.setBorder(null);
        searchField.putClientProperty("placeholder", "Search transactions...");

        // Add document listener for real-time search
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSearch(); }
            public void removeUpdate(DocumentEvent e) { updateSearch(); }
            public void changedUpdate(DocumentEvent e) { updateSearch(); }

            private void updateSearch() {
                currentSearch = searchField.getText().trim().toLowerCase();
                refresh();
            }
        });

        searchPanel.add(searchField);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(searchPanel, gbc);

        // Type filter combo box
        typeFilter = new JComboBox<>(new String[]{"All Types", "Income", "Expense"});
        typeFilter.setFont(currentTheme.getDefaultFont());
        typeFilter.setBackground(currentTheme.getBackgroundTertiary());
        typeFilter.setForeground(currentTheme.getTextPrimary());
        typeFilter.setBorder(BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true));
        typeFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));

        typeFilter.addActionListener(e -> {
            String selected = typeFilter.getSelectedItem().toString().toLowerCase();
            currentType = selected.equals("all types") ? "all" : selected;
            refresh();
        });

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(typeFilter, gbc);

        return panel;
    }

    /**
     * Create a transaction list item
     */
    private JPanel createTransactionItem(Transaction transaction) {
        JPanel item = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };

        Category category = Category.getById(transaction.getCategory());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        // Icon
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setBackground(category.getLightColor());
        iconPanel.setPreferredSize(new Dimension(50, 50));

        JLabel iconLabel = new JLabel(getIconChar(category.getIconName()));
        iconLabel.setForeground(category.getColor());
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
        iconPanel.add(iconLabel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 15);
        item.add(iconPanel, gbc);

        // Details
        JPanel detailsPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        detailsPanel.setBackground(currentTheme.getBackgroundSecondary());
        detailsPanel.setOpaque(false);

        // Description
        JLabel descLabel = new JLabel(transaction.getDescription());
        descLabel.setFont(currentTheme.getBoldFont());
        descLabel.setForeground(currentTheme.getTextPrimary());

        // Category
        JLabel catLabel = new JLabel(category.getName());
        catLabel.setFont(currentTheme.getSmallFont());
        catLabel.setForeground(category.getColor());

        // Date and notes indicator
        String dateStr = formatDate(transaction.getDate());
        if (transaction.getNotes() != null && !transaction.getNotes().isEmpty()) {
            dateStr += " \u2022 \uF086"; // Note icon
        }
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(currentTheme.getSmallFont());
        dateLabel.setForeground(currentTheme.getTextMuted());

        detailsPanel.add(descLabel);
        detailsPanel.add(catLabel);
        detailsPanel.add(dateLabel);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 0, 5, 10);
        item.add(detailsPanel, gbc);

        // Amount
        double signedAmount = transaction.isIncome()
            ? transaction.getAmount()
            : -transaction.getAmount();

        JLabel amountLabel = new JLabel(UIUtil.formatCurrencyWithSign(
            signedAmount, dataManager.getCurrency(), "+", "-"));
        amountLabel.setFont(currentTheme.getBoldFont());
        amountLabel.setForeground(transaction.isIncome()
            ? currentTheme.getSuccessColor()
            : currentTheme.getDangerColor());
        amountLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets = new Insets(5, 10, 5, 5);
        item.add(amountLabel, gbc);

        // Styling
        item.setBackground(currentTheme.getBackgroundSecondary());
        item.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Click to edit
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    editTransaction(transaction);
                }
            }
        });

        return item;
    }

    /**
     * Format date for display
     */
    private String formatDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        if (date.equals(today)) {
            return "Today";
        } else if (date.equals(yesterday)) {
            return "Yesterday";
        } else {
            return date.toString();
        }
    }

    /**
     * Open edit dialog for a transaction
     */
    private void editTransaction(Transaction transaction) {
        // Get parent frame
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        if (parent instanceof MainFrame) {
            // In a full implementation, you would pass the transaction to edit
            // For now, just show a confirmation dialog
            int result = JOptionPane.showConfirmDialog(
                this,
                "Edit or delete this transaction?",
                "Transaction Options",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                // Edit - would open dialog in full implementation
                JOptionPane.showMessageDialog(this,
                    "Edit functionality would open here",
                    "Edit",
                    JOptionPane.INFORMATION_MESSAGE);
            } else if (result == JOptionPane.NO_OPTION) {
                // Delete
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this transaction?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    dataManager.deleteTransaction(transaction.getId());
                }
            }
        }
    }

    /**
     * Refresh the transactions list
     */
    @Override
    public void refresh() {
        transactionsList.removeAll();

        List<Transaction> allTransactions = dataManager.getAllTransactions();

        // Apply filters
        java.util.stream.Stream<Transaction> stream = allTransactions.stream();

        // Filter by type
        if (!currentType.equals("all")) {
            Transaction.TransactionType type = currentType.equals("income")
                ? Transaction.TransactionType.INCOME
                : Transaction.TransactionType.EXPENSE;
            stream = stream.filter(t -> t.getType() == type);
        }

        // Filter by search
        if (!currentSearch.isEmpty()) {
            stream = stream.filter(t ->
                t.getDescription().toLowerCase().contains(currentSearch) ||
                (t.getNotes() != null && t.getNotes().toLowerCase().contains(currentSearch)) ||
                Category.getById(t.getCategory()).getName().toLowerCase().contains(currentSearch)
            );
        }

        List<Transaction> filtered = stream.toList();

        if (filtered.isEmpty()) {
            transactionsList.add(emptyLabel);
        } else {
            // Group by date
            java.util.Map<LocalDate, List<Transaction>> grouped = new java.util.LinkedHashMap<>();
            for (Transaction t : filtered) {
                grouped.computeIfAbsent(t.getDate(), k -> new java.util.ArrayList<>()).add(t);
            }

            for (java.util.Map.Entry<LocalDate, List<Transaction>> entry : grouped.entrySet()) {
                // Date header
                JLabel dateHeader = new JLabel(formatDate(entry.getKey()));
                dateHeader.setFont(currentTheme.getBoldFont());
                dateHeader.setForeground(currentTheme.getTextSecondary());
                dateHeader.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
                dateHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
                dateHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                transactionsList.add(dateHeader);

                // Transactions for this date
                for (Transaction t : entry.getValue()) {
                    JPanel item = createTransactionItem(t);
                    item.setAlignmentX(Component.CENTER_ALIGNMENT);
                    transactionsList.add(item);
                    transactionsList.add(Box.createRigidArea(new Dimension(0, 8)));
                }
            }
        }

        transactionsList.revalidate();
        transactionsList.repaint();
    }

    /**
     * Refresh data (called from navigation)
     */
    public void refreshData() {
        refresh();
    }

    @Override
    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        setBackground(theme.getBackgroundPrimary());
        refresh();
    }

    /**
     * Get a simple unicode character to represent an icon
     */
    private String getIconChar(String iconName) {
        switch (iconName) {
            case "utensils": return "\u2638";
            case "car": return "\u23F3";
            case "shopping-bag": return "\u2733";
            case "film": return "\u25A0";
            case "file-invoice-dollar": return "\u2665";
            case "heartbeat": return "\u2665";
            case "graduation-cap": return "\u273F";
            case "user": return "\u263A";
            case "arrow-up": return "\u2191";
            case "arrow-down": return "\u2193";
            case "ellipsis-h": return "\u2026";
            default: return "\u25CF";
        }
    }
}
