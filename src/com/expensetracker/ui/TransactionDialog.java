package com.expensetracker.ui;

import com.expensetracker.data.DataManager;
import com.expensetracker.model.Category;
import com.expensetracker.model.Transaction;
import com.expensetracker.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dialog for adding or editing transactions
 * Provides form fields for all transaction details
 */
public class TransactionDialog extends JDialog {

    private final DataManager dataManager;
    private Theme currentTheme;

    // Form fields
    private JRadioButton expenseRadio;
    private JRadioButton incomeRadio;
    private JTextField amountField;
    private JTextField descriptionField;
    private JComboBox<String> categoryCombo;
    private JSpinner dateSpinner;
    private JTextArea notesArea;

    // Transaction being edited (null for new)
    private Transaction editingTransaction;

    // Category selection panel
    private JPanel categoryPanel;

    /**
     * Create the transaction dialog
     */
    public TransactionDialog(Frame parent, DataManager dataManager, Theme theme) {
        super(parent, "Add Transaction", true);
        this.dataManager = dataManager;
        this.currentTheme = theme;

        initializeDialog();
    }

    /**
     * Initialize dialog properties and components
     */
    private void initializeDialog() {
        setSize(450, 650);
        setMinimumSize(new Dimension(400, 550));
        setLocationRelativeTo(getOwner());
        setResizable(false);

        getContentPane().setBackground(currentTheme.getBackgroundSecondary());

        // Create main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(currentTheme.getBackgroundSecondary());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Type selector
        mainPanel.add(createTypeSelector());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Amount field
        mainPanel.add(createAmountField());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Description field
        mainPanel.add(createDescriptionField());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Category selector
        mainPanel.add(createCategorySelector());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Date field
        mainPanel.add(createDateField());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Notes field
        mainPanel.add(createNotesField());
        mainPanel.add(Box.createVerticalGlue());

        // Buttons
        mainPanel.add(createButtonPanel());

        add(mainPanel);
    }

    /**
     * Create transaction type selector (Income/Expense)
     */
    private JPanel createTypeSelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        panel.setBackground(currentTheme.getBackgroundSecondary());

        JLabel label = new JLabel("Type:");
        label.setFont(currentTheme.getDefaultFont());
        label.setForeground(currentTheme.getTextPrimary());

        // Custom radio buttons styled as buttons
        expenseRadio = new JRadioButton("Expense");
        expenseRadio.setFont(currentTheme.getDefaultFont());
        expenseRadio.setBackground(currentTheme.getBackgroundSecondary());
        expenseRadio.setForeground(currentTheme.getTextPrimary());
        expenseRadio.setSelected(true);
        expenseRadio.setCursor(new Cursor(Cursor.HAND_CURSOR));

        incomeRadio = new JRadioButton("Income");
        incomeRadio.setFont(currentTheme.getDefaultFont());
        incomeRadio.setBackground(currentTheme.getBackgroundSecondary());
        incomeRadio.setForeground(currentTheme.getTextPrimary());
        incomeRadio.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ButtonGroup group = new ButtonGroup();
        group.add(expenseRadio);
        group.add(incomeRadio);

        // Update category options when type changes
        expenseRadio.addActionListener(e -> updateCategoryOptions());
        incomeRadio.addActionListener(e -> updateCategoryOptions());

        panel.add(label);
        panel.add(expenseRadio);
        panel.add(incomeRadio);

        return panel;
    }

    /**
     * Create amount input field
     */
    private JPanel createAmountField() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(currentTheme.getBackgroundSecondary());

        JLabel label = new JLabel("Amount:");
        label.setFont(currentTheme.getDefaultFont());
        label.setForeground(currentTheme.getTextPrimary());

        // Amount field with currency symbol
        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        amountPanel.setBackground(currentTheme.getBackgroundTertiary());
        amountPanel.setBorder(BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true));

        String currency = dataManager.getCurrency();
        JLabel currencyLabel = new JLabel(DataManager.getCurrencySymbol(currency));
        currencyLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        currencyLabel.setForeground(currentTheme.getTextSecondary());

        amountField = new JTextField("0.00", 12);
        amountField.setFont(new Font("Segoe UI", Font.BOLD, 20));
        amountField.setBackground(currentTheme.getBackgroundTertiary());
        amountField.setForeground(currentTheme.getTextPrimary());
        amountField.setBorder(null);
        amountField.setHorizontalAlignment(JTextField.RIGHT);

        // Focus styling
        amountField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                amountPanel.setBorder(BorderFactory.createLineBorder(currentTheme.getPrimaryColor(), 2, true));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                amountPanel.setBorder(BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true));
            }
        });

        amountPanel.add(currencyLabel);
        amountPanel.add(amountField);

        panel.add(label, BorderLayout.NORTH);
        panel.add(amountPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create description input field
     */
    private JPanel createDescriptionField() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(currentTheme.getBackgroundSecondary());

        JLabel label = new JLabel("Description:");
        label.setFont(currentTheme.getDefaultFont());
        label.setForeground(currentTheme.getTextPrimary());

        descriptionField = new JTextField();
        descriptionField.setFont(currentTheme.getDefaultFont());
        descriptionField.setBackground(currentTheme.getBackgroundTertiary());
        descriptionField.setForeground(currentTheme.getTextPrimary());
        descriptionField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        panel.add(label, BorderLayout.NORTH);
        panel.add(descriptionField, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create category selector with visual options
     */
    private JPanel createCategorySelector() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(currentTheme.getBackgroundSecondary());

        JLabel label = new JLabel("Category:");
        label.setFont(currentTheme.getDefaultFont());
        label.setForeground(currentTheme.getTextPrimary());

        categoryPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        categoryPanel.setBackground(currentTheme.getBackgroundSecondary());

        updateCategoryOptions();

        panel.add(label, BorderLayout.NORTH);
        panel.add(categoryPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Update category options based on selected type
     */
    private void updateCategoryOptions() {
        categoryPanel.removeAll();

        Category[] categories = expenseRadio.isSelected()
            ? Category.EXPENSE_CATEGORIES
            : Category.INCOME_CATEGORIES;

        ButtonGroup group = new ButtonGroup();

        for (Category cat : categories) {
            JToggleButton btn = createCategoryButton(cat);
            categoryPanel.add(btn);
            group.add(btn);

            // Select first category by default
            if (categoryPanel.getComponentCount() == 1) {
                btn.setSelected(true);
            }
        }

        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    /**
     * Create a category selection button
     */
    private JToggleButton createCategoryButton(Category category) {
        JToggleButton btn = new JToggleButton();
        btn.setLayout(new BorderLayout(5, 5));
        btn.setBackground(currentTheme.getBackgroundTertiary());
        btn.setForeground(currentTheme.getTextPrimary());
        btn.setBorder(BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true));
        btn.setPreferredSize(new Dimension(100, 70));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon label
        JLabel iconLabel = new JLabel(getIconChar(category.getIconName()), SwingConstants.CENTER);
        iconLabel.setForeground(category.getColor());
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));

        // Name label
        JLabel nameLabel = new JLabel(category.getName(), SwingConstants.CENTER);
        nameLabel.setFont(currentTheme.getSmallFont());
        nameLabel.setForeground(currentTheme.getTextSecondary());

        btn.add(iconLabel, BorderLayout.CENTER);
        btn.add(nameLabel, BorderLayout.SOUTH);

        // Selection styling
        btn.addItemListener(e -> {
            if (btn.isSelected()) {
                btn.setBorder(BorderFactory.createLineBorder(category.getColor(), 2, true));
                btn.setBackground(category.getLightColor());
            } else {
                btn.setBorder(BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true));
                btn.setBackground(currentTheme.getBackgroundTertiary());
            }
        });

        return btn;
    }

    /**
     * Get selected category from visual buttons
     */
    private String getSelectedCategory() {
        for (Component comp : categoryPanel.getComponents()) {
            if (comp instanceof JToggleButton && ((JToggleButton) comp).isSelected()) {
                // Find category by matching icon/name
                Category[] categories = expenseRadio.isSelected()
                    ? Category.EXPENSE_CATEGORIES
                    : Category.INCOME_CATEGORIES;

                for (Category cat : categories) {
                    if (cat.getName().equals(
                        ((JLabel) ((JToggleButton) comp).getComponent(1)).getText())) {
                        return cat.getId();
                    }
                }
            }
        }
        return expenseRadio.isSelected() ? "other" : "income";
    }

    /**
     * Create date spinner field
     */
    private JPanel createDateField() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(currentTheme.getBackgroundSecondary());

        JLabel label = new JLabel("Date:");
        label.setFont(currentTheme.getDefaultFont());
        label.setForeground(currentTheme.getTextPrimary());

        // Date spinner with custom editor
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setFont(currentTheme.getDefaultFont());

        // Custom date editor
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setBackground(currentTheme.getBackgroundTertiary());
        dateSpinner.setForeground(currentTheme.getTextPrimary());

        // Set to today
        dateSpinner.setValue(new java.util.Date());

        panel.add(label, BorderLayout.NORTH);
        panel.add(dateSpinner, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create notes text area
     */
    private JPanel createNotesField() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(currentTheme.getBackgroundSecondary());

        JLabel label = new JLabel("Notes (optional):");
        label.setFont(currentTheme.getDefaultFont());
        label.setForeground(currentTheme.getTextPrimary());

        notesArea = new JTextArea(3, 20);
        notesArea.setFont(currentTheme.getDefaultFont());
        notesArea.setBackground(currentTheme.getBackgroundTertiary());
        notesArea.setForeground(currentTheme.getTextPrimary());
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(notesArea);
        scrollPane.setBorder(null);

        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create action buttons panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBackground(currentTheme.getBackgroundSecondary());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(currentTheme.getDefaultFont());
        cancelButton.setBackground(currentTheme.getBackgroundTertiary());
        cancelButton.setForeground(currentTheme.getTextPrimary());
        cancelButton.setBorder(BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        // Save button
        JButton saveButton = new JButton("Save Transaction");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(currentTheme.getPrimaryColor());
        saveButton.setForeground(Color.WHITE);
        saveButton.setBorder(null);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> saveTransaction());

        panel.add(cancelButton);
        panel.add(saveButton);

        return panel;
    }

    /**
     * Clear form for new transaction
     */
    public void clearForm() {
        editingTransaction = null;
        setTitle("Add Transaction");

        expenseRadio.setSelected(true);
        amountField.setText("0.00");
        descriptionField.setText("");
        notesArea.setText("");
        dateSpinner.setValue(new java.util.Date());

        updateCategoryOptions();
    }

    /**
     * Populate form for editing
     */
    public void setTransaction(Transaction transaction) {
        editingTransaction = transaction;
        setTitle("Edit Transaction");

        if (transaction.isIncome()) {
            incomeRadio.setSelected(true);
        } else {
            expenseRadio.setSelected(true);
        }

        amountField.setText(String.format("%.2f", transaction.getAmount()));
        descriptionField.setText(transaction.getDescription());
        notesArea.setText(transaction.getNotes() != null ? transaction.getNotes() : "");

        // Set date
        try {
            java.util.Date date = java.util.Date.from(
                transaction.getDate().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
            dateSpinner.setValue(date);
        } catch (Exception e) {
            dateSpinner.setValue(new java.util.Date());
        }

        updateCategoryOptions();

        // Select appropriate category
        for (Component comp : categoryPanel.getComponents()) {
            if (comp instanceof JToggleButton) {
                JToggleButton btn = (JToggleButton) comp;
                JLabel nameLabel = (JLabel) btn.getComponent(1);
                Category cat = Category.getById(transaction.getCategory());
                if (cat != null && nameLabel.getText().equals(cat.getName())) {
                    btn.setSelected(true);
                    // Trigger styling
                    btn.setBorder(BorderFactory.createLineBorder(cat.getColor(), 2, true));
                    btn.setBackground(cat.getLightColor());
                }
            }
        }
    }

    /**
     * Save transaction (new or update)
     */
    private void saveTransaction() {
        // Validate inputs
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid amount greater than 0",
                    "Invalid Amount",
                    JOptionPane.WARNING_MESSAGE);
                amountField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid numeric amount",
                "Invalid Amount",
                JOptionPane.WARNING_MESSAGE);
            amountField.requestFocus();
            return;
        }

        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a description",
                "Missing Description",
                JOptionPane.WARNING_MESSAGE);
            descriptionField.requestFocus();
            return;
        }

        String category = getSelectedCategory();
        java.util.Date dateVal = (java.util.Date) dateSpinner.getValue();
        LocalDate date = LocalDate.ofInstant(dateVal.toInstant(), java.time.ZoneId.systemDefault());
        String notes = notesArea.getText().trim();

        Transaction.TransactionType type = expenseRadio.isSelected()
            ? Transaction.TransactionType.EXPENSE
            : Transaction.TransactionType.INCOME;

        if (editingTransaction != null) {
            // Update existing
            editingTransaction.setType(type);
            editingTransaction.setAmount(amount);
            editingTransaction.setDescription(description);
            editingTransaction.setCategory(category);
            editingTransaction.setDate(date);
            editingTransaction.setNotes(notes);
            dataManager.updateTransaction(editingTransaction);
        } else {
            // Create new
            Transaction transaction = new Transaction(type, amount, description, category, date, notes);
            dataManager.addTransaction(transaction);
        }

        dispose();
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

    /**
     * Update dialog theme
     */
    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        getContentPane().setBackground(theme.getBackgroundSecondary());
        SwingUtilities.updateComponentTreeUI(this);
    }
}
