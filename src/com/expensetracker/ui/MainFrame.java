package com.expensetracker.ui;

import com.expensetracker.data.DataManager;
import com.expensetracker.model.Transaction;
import com.expensetracker.util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window
 * Contains the navigation bar and content panels
 */
public class MainFrame extends JFrame {

    // Current application theme
    private Theme currentTheme;

    // Data manager instance
    private final DataManager dataManager;

    // Main content panel (switched based on navigation)
    private CardLayout cardLayout;
    private JPanel contentPanel;

    // Navigation buttons
    private JButton homeButton;
    private JButton transactionsButton;
    private JButton addButton;
    private JButton reportsButton;
    private JButton settingsButton;

    // Dialogs
    private TransactionDialog transactionDialog;
    private SettingsDialog settingsDialog;

    /**
     * Create and initialize the main frame
     */
    public MainFrame() {
        this.dataManager = DataManager.getInstance();
        this.currentTheme = dataManager.getTheme();

        initializeFrame();
        initializeComponents();
        applyTheme();
        setupNavigation();

        // Register for data changes
        dataManager.addDataChangeListener(this::refreshCurrentView);
    }

    /**
     * Initialize basic frame properties
     */
    private void initializeFrame() {
        setTitle("Expense Tracker");
        setSize(480, 800);
        setMinimumSize(new Dimension(400, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // Set window icon (optional)
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // No icon available
        }
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Main layout
        setLayout(new BorderLayout());

        // Create content panel with CardLayout for view switching
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(currentTheme.getBackgroundPrimary());

        // Add different views
        contentPanel.add(new HomePanel(dataManager, currentTheme), "home");
        contentPanel.add(new TransactionsPanel(dataManager, currentTheme), "transactions");
        contentPanel.add(new ReportsPanel(dataManager, currentTheme), "reports");

        add(contentPanel, BorderLayout.CENTER);

        // Create navigation bar at bottom
        createNavigationBar();

        // Initialize dialogs
        transactionDialog = new TransactionDialog(this, dataManager, currentTheme);
        settingsDialog = new SettingsDialog(this, dataManager, currentTheme);
    }

    /**
     * Create the bottom navigation bar
     */
    private void createNavigationBar() {
        JPanel navBar = new JPanel(new GridLayout(1, 5));
        navBar.setBackground(currentTheme.getBackgroundSecondary());
        navBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, currentTheme.getBorderColor()));
        navBar.setPreferredSize(new Dimension(0, 60));

        // Home button
        homeButton = createNavButton("Home", "\uF015");
        homeButton.putClientProperty("view", "home");

        // Transactions button
        transactionsButton = createNavButton("Transactions", "\uF03A");
        transactionsButton.putClientProperty("view", "transactions");

        // Add button (center, emphasized)
        addButton = createNavButton("Add", "\uF055");
        addButton.putClientProperty("view", "add");

        // Reports button
        reportsButton = createNavButton("Reports", "\uF080");
        reportsButton.putClientProperty("view", "reports");

        // Settings button
        settingsButton = createNavButton("Settings", "\uF013");
        settingsButton.putClientProperty("view", "settings");

        navBar.add(homeButton);
        navBar.add(transactionsButton);
        navBar.add(addButton);
        navBar.add(reportsButton);
        navBar.add(settingsButton);

        add(navBar, BorderLayout.SOUTH);

        // Set home as active
        updateNavSelection("home");
    }

    /**
     * Create a navigation button with icon and text
     */
    private JButton createNavButton(String text, String iconChar) {
        JButton button = new JButton(text);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setBackground(currentTheme.getBackgroundSecondary());
        button.setForeground(currentTheme.getTextMuted());
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Try to use a simple text icon as placeholder
        // In production, you would use actual icon fonts
        button.setIconTextGap(4);

        return button;
    }

    /**
     * Set up navigation event handlers
     */
    private void setupNavigation() {
        // Home button
        homeButton.addActionListener(e -> {
            updateNavSelection("home");
            cardLayout.show(contentPanel, "home");
        });

        // Transactions button
        transactionsButton.addActionListener(e -> {
            updateNavSelection("transactions");
            cardLayout.show(contentPanel, "transactions");
            ((TransactionsPanel) contentPanel.getComponent(1)).refreshData();
        });

        // Add button - opens transaction dialog
        addButton.addActionListener(e -> {
            transactionDialog.clearForm();
            transactionDialog.setVisible(true);
        });

        // Reports button
        reportsButton.addActionListener(e -> {
            updateNavSelection("reports");
            cardLayout.show(contentPanel, "reports");
            ((ReportsPanel) contentPanel.getComponent(2)).refreshData();
        });

        // Settings button
        settingsButton.addActionListener(e -> {
            settingsDialog.setVisible(true);
        });
    }

    /**
     * Update navigation button selection state
     */
    private void updateNavSelection(String view) {
        JButton[] buttons = {homeButton, transactionsButton, addButton, reportsButton, settingsButton};
        String[] views = {"home", "transactions", "add", "reports", "settings"};

        for (int i = 0; i < buttons.length; i++) {
            if (views[i].equals(view)) {
                buttons[i].setForeground(currentTheme.getPrimaryColor());
                buttons[i].setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else {
                buttons[i].setForeground(currentTheme.getTextMuted());
                buttons[i].setFont(new Font("Segoe UI", Font.PLAIN, 11));
            }
        }
    }

    /**
     * Apply current theme to all components
     */
    public void applyTheme() {
        getContentPane().setBackground(currentTheme.getBackgroundPrimary());

        // Update all child components
        SwingUtilities.updateComponentTreeUI(this);

        // Refresh panels
        if (contentPanel.getComponentCount() > 0) {
            for (Component comp : contentPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    ((JPanel) comp).setBackground(currentTheme.getBackgroundPrimary());
                }
            }
        }

        repaint();
    }

    /**
     * Refresh the currently visible view
     */
    private void refreshCurrentView() {
        SwingUtilities.invokeLater(() -> {
            for (Component comp : contentPanel.getComponents()) {
                if (comp.isVisible() && comp instanceof Refreshable) {
                    ((Refreshable) comp).refresh();
                }
            }
        });
    }

    /**
     * Toggle between light and dark theme
     */
    public void toggleTheme() {
        currentTheme = currentTheme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT;
        dataManager.setTheme(currentTheme);
        applyTheme();
        transactionDialog.setTheme(currentTheme);
        settingsDialog.setTheme(currentTheme);

        // Update all panels
        for (Component comp : contentPanel.getComponents()) {
            if (comp instanceof Themeable) {
                ((Themeable) comp).setTheme(currentTheme);
            }
        }

        repaint();
    }

    /**
     * Get current theme
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Interface for components that can be refreshed
     */
    public interface Refreshable {
        void refresh();
    }

    /**
     * Interface for components that can have their theme changed
     */
    public interface Themeable {
        void setTheme(Theme theme);
    }
}
