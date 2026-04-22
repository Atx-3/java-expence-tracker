package com.expensetracker.ui;

import com.expensetracker.data.DataManager;
import com.expensetracker.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Settings dialog for application preferences
 * Allows users to change theme, currency, and manage data
 */
public class SettingsDialog extends JDialog {

    private final DataManager dataManager;
    private Theme currentTheme;

    // UI Components
    private JToggleButton darkModeToggle;
    private JComboBox<String> currencyCombo;
    private JLabel versionLabel;

    /**
     * Create the settings dialog
     */
    public SettingsDialog(Frame parent, DataManager dataManager, Theme theme) {
        super(parent, "Settings", true);
        this.dataManager = dataManager;
        this.currentTheme = theme;

        initializeDialog();
    }

    /**
     * Initialize dialog properties and components
     */
    private void initializeDialog() {
        setSize(400, 500);
        setMinimumSize(new Dimension(350, 400));
        setLocationRelativeTo(getOwner());
        setResizable(false);

        getContentPane().setBackground(currentTheme.getBackgroundSecondary());

        // Create scrollable content
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(currentTheme.getBackgroundSecondary());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Appearance section
        mainPanel.add(createSectionHeader("Appearance"));
        mainPanel.add(createAppearanceSection());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Currency section
        mainPanel.add(createSectionHeader("Currency"));
        mainPanel.add(createCurrencySection());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Data management section
        mainPanel.add(createSectionHeader("Data Management"));
        mainPanel.add(createDataSection());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // About section
        mainPanel.add(createSectionHeader("About"));
        mainPanel.add(createAboutSection());
        mainPanel.add(Box.createVerticalGlue());

        // Close button
        mainPanel.add(createCloseButton());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(currentTheme.getBackgroundSecondary());
        scrollPane.getViewport().setBackground(currentTheme.getBackgroundSecondary());

        add(scrollPane);
    }

    /**
     * Create section header label
     */
    private JLabel createSectionHeader(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(currentTheme.getTextMuted());
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Create appearance settings section
     */
    private JPanel createAppearanceSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(currentTheme.getBackgroundSecondary());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Dark Mode");
        titleLabel.setFont(currentTheme.getDefaultFont());
        titleLabel.setForeground(currentTheme.getTextPrimary());

        // Custom toggle switch
        darkModeToggle = new JToggleButton();
        darkModeToggle.setPreferredSize(new Dimension(50, 28));
        darkModeToggle.setMaximumSize(new Dimension(50, 28));
        darkModeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        darkModeToggle.setBorderPainted(false);
        darkModeToggle.setFocusPainted(false);
        darkModeToggle.setContentAreaFilled(false);

        // Set initial state
        darkModeToggle.setSelected(currentTheme == Theme.DARK);

        // Custom painting for toggle
        darkModeToggle.addActionListener(e -> {
            MainFrame parent = (MainFrame) SwingUtilities.getWindowAncestor(this);
            if (parent != null) {
                parent.toggleTheme();
                currentTheme = parent.getCurrentTheme();
            }
        });

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(darkModeToggle, BorderLayout.EAST);

        return panel;
    }

    /**
     * Create currency settings section
     */
    private JPanel createCurrencySection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(currentTheme.getBackgroundSecondary());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] currencies = {"USD - US Dollar ($)", "EUR - Euro (€)", "GBP - British Pound (£)",
                              "JPY - Japanese Yen (¥)", "INR - Indian Rupee (₹)"};
        String[] codes = {"USD", "EUR", "GBP", "JPY", "INR"};

        currencyCombo = new JComboBox<>(currencies);
        currencyCombo.setFont(currentTheme.getDefaultFont());
        currencyCombo.setBackground(currentTheme.getBackgroundTertiary());
        currencyCombo.setForeground(currentTheme.getTextPrimary());
        currencyCombo.setBorder(BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true));
        currencyCombo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        currencyCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Set current currency
        String currentCode = dataManager.getCurrency();
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(currentCode)) {
                currencyCombo.setSelectedIndex(i);
                break;
            }
        }

        // Save on change
        currencyCombo.addActionListener(e -> {
            int index = currencyCombo.getSelectedIndex();
            if (index >= 0 && index < codes.length) {
                dataManager.setCurrency(codes[index]);
                // Notify data change listeners to refresh UI
                dataManager.addDataChangeListener(new DataManager.DataChangeListener() {
                    public void onDataChanged() {}
                });

                // Refresh all open windows
                for (Window window : Window.getWindows()) {
                    if (window instanceof MainFrame.Refreshable) {
                        ((MainFrame.Refreshable) window).refresh();
                    }
                }

                JOptionPane.showMessageDialog(
                    SettingsDialog.this,
                    "Currency changed to " + codes[index],
                    "Currency Updated",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        panel.add(currencyCombo, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create data management section
     */
    private JPanel createDataSection() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 10));
        panel.setBackground(currentTheme.getBackgroundSecondary());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Export button
        JButton exportBtn = createStyledButton("Export Data", "\uF019", currentTheme.getPrimaryColor());
        exportBtn.addActionListener(e -> exportData());

        // Clear data button
        JButton clearBtn = createStyledButton("Clear All Data", "\uF1F8", currentTheme.getDangerColor());
        clearBtn.addActionListener(e -> clearData());

        panel.add(exportBtn);
        panel.add(clearBtn);

        return panel;
    }

    /**
     * Create a styled action button
     */
    private JButton createStyledButton(String text, String icon, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(currentTheme.getDefaultFont());
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return btn;
    }

    /**
     * Create about section
     */
    private JPanel createAboutSection() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 0, 8));
        panel.setBackground(currentTheme.getBackgroundSecondary());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // App name and version
        JLabel nameLabel = new JLabel("ExpenseTracker v1.0.0");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(currentTheme.getTextPrimary());

        // Description
        JLabel descLabel = new JLabel("A modern expense tracking application");
        descLabel.setFont(currentTheme.getDefaultFont());
        descLabel.setForeground(currentTheme.getTextSecondary());

        // Copyright
        versionLabel = new JLabel("\u00A9 2024 Built with Java Swing");
        versionLabel.setFont(currentTheme.getSmallFont());
        versionLabel.setForeground(currentTheme.getTextMuted());

        panel.add(nameLabel);
        panel.add(descLabel);
        panel.add(versionLabel);

        return panel;
    }

    /**
     * Create close button
     */
    private JPanel createCloseButton() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(currentTheme.getBackgroundSecondary());

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setBackground(currentTheme.getBackgroundTertiary());
        closeBtn.setForeground(currentTheme.getTextPrimary());
        closeBtn.setBorder(BorderFactory.createLineBorder(currentTheme.getBorderColor(), 1, true));
        closeBtn.setPreferredSize(new Dimension(150, 40));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        panel.add(closeBtn);
        return panel;
    }

    /**
     * Export data to JSON file
     */
    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Data");
        fileChooser.setSelectedFile(new File("expense-tracker-backup.json"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();

            // Ensure .json extension
            if (!filePath.endsWith(".json")) {
                filePath += ".json";
            }

            if (dataManager.exportData(filePath)) {
                JOptionPane.showMessageDialog(this,
                    "Data exported successfully to:\n" + filePath,
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to export data. Please check the file path.",
                    "Export Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clear all transaction data
     */
    private void clearData() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete ALL transaction data?\n\nThis action cannot be undone!",
            "Confirm Clear Data",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.clearAllTransactions();
            JOptionPane.showMessageDialog(this,
                "All data has been cleared",
                "Data Cleared",
                JOptionPane.INFORMATION_MESSAGE);
        }
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
