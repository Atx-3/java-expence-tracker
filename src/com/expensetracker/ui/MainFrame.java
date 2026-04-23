// This class belongs to the 'ui' package — all user interface components live here
package com.expensetracker.ui;

// Import DataManager to access stored data and preferences
import com.expensetracker.data.DataManager;

// Import Transaction model for type references
import com.expensetracker.model.Transaction;

// Import Theme enum for applying dark/light mode colors
import com.expensetracker.util.Theme;

// javax.swing provides all the UI components: JFrame, JPanel, JButton, etc.
import javax.swing.*;

// java.awt provides layout managers, colors, fonts, and other base graphics classes
import java.awt.*;

/**
 * MainFrame.java — MAIN APPLICATION WINDOW (App Shell)
 * =====================================================
 * This is the primary JFrame that hosts the entire application.
 * It acts as the "app shell" — containing the navigation bar and
 * switching between different content panels.
 *
 * Key Concepts:
 *   - JFrame: Top-level window container in Swing — represents the application window
 *   - CardLayout: A layout manager that stacks panels like a deck of cards,
 *     showing only one at a time (used for view switching without recreating panels)
 *   - Observer Pattern: Registers as a DataChangeListener to auto-refresh views
 *   - Interface definitions: Refreshable and Themeable are defined here for polymorphism
 *
 * Architecture:
 *   ┌──────────────────────────┐
 *   │     MainFrame (JFrame)    │
 *   │  ┌──────────────────────┐ │
 *   │  │  contentPanel        │ │ ← CardLayout switches between views
 *   │  │  ┌────────────────┐  │ │
 *   │  │  │  HomePanel     │  │ │ ← View 1: Dashboard
 *   │  │  │  TxnPanel      │  │ │ ← View 2: Transactions (hidden)
 *   │  │  │  ReportsPanel  │  │ │ ← View 3: Reports (hidden)
 *   │  │  └────────────────┘  │ │
 *   │  └──────────────────────┘ │
 *   │  ┌──────────────────────┐ │
 *   │  │  Navigation Bar      │ │ ← Bottom bar with 5 buttons
 *   │  └──────────────────────┘ │
 *   └──────────────────────────┘
 */
public class MainFrame extends JFrame {

    // Current application theme — either Theme.LIGHT or Theme.DARK
    private Theme currentTheme;

    // Reference to the DataManager singleton — provides access to all data operations
    private final DataManager dataManager;

    // CardLayout manager — controls which panel is currently visible
    // Only ONE card (panel) is shown at a time; the rest are hidden "behind" it
    private CardLayout cardLayout;

    // The content panel that HOLDS all the view panels (Home, Transactions, Reports)
    // CardLayout is applied to this panel to switch between views
    private JPanel contentPanel;

    // Navigation bar buttons — one for each view + the Add button
    private JButton homeButton;
    private JButton transactionsButton;
    private JButton addButton;          // Center button — opens the Add Transaction dialog
    private JButton reportsButton;
    private JButton settingsButton;

    // Modal dialogs — created once and reused (more efficient than recreating each time)
    private TransactionDialog transactionDialog;
    private SettingsDialog settingsDialog;

    // ============================================
    // CONSTRUCTOR — Builds the entire application window
    // ============================================

    /**
     * Constructor — initializes the main application window.
     * Follows this sequence:
     *   1. Get DataManager instance and load theme preference
     *   2. Set up the JFrame properties (title, size, position)
     *   3. Create and arrange all UI components
     *   4. Apply the saved theme colors
     *   5. Wire up navigation event handlers
     *   6. Register for data change notifications (Observer Pattern)
     */
    public MainFrame() {
        // Get the DataManager singleton — this was already initialized in Main.java
        this.dataManager = DataManager.getInstance();

        // Load the user's saved theme preference (defaults to LIGHT if none saved)
        this.currentTheme = dataManager.getTheme();

        // Step 1: Set basic frame properties (title, size, close behavior)
        initializeFrame();

        // Step 2: Create all UI components (content panels + navigation bar)
        initializeComponents();

        // Step 3: Apply theme colors to all components
        applyTheme();

        // Step 4: Attach click event handlers to navigation buttons
        setupNavigation();

        // Step 5: Register as a data change listener
        // When transactions change (add/edit/delete), refreshCurrentView() is called
        // 'this::refreshCurrentView' is a method reference — shorthand for a lambda
        dataManager.addDataChangeListener(this::refreshCurrentView);
    }

    // ============================================
    // FRAME INITIALIZATION
    // Sets up the JFrame's basic properties
    // ============================================

    /**
     * Configures the JFrame window properties.
     * These settings control the window's title bar, size, and behavior.
     */
    private void initializeFrame() {
        // setTitle() sets the text shown in the window's title bar
        setTitle("Expense Tracker");

        // setSize(width, height) sets the initial window dimensions in pixels
        // 480x800 mimics a mobile phone aspect ratio for the desktop version
        setSize(480, 800);

        // setMinimumSize() prevents the user from shrinking the window too small
        setMinimumSize(new Dimension(400, 600));

        // EXIT_ON_CLOSE means the JVM terminates when the window is closed
        // Other options: HIDE_ON_CLOSE, DISPOSE_ON_CLOSE, DO_NOTHING_ON_CLOSE
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // setLocationRelativeTo(null) centers the window on the screen
        // Passing null means "relative to the screen center"
        setLocationRelativeTo(null);

        // Try to load a custom window icon (shown in taskbar and title bar)
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // No icon file found — silently continue with the default Java icon
        }
    }

    // ============================================
    // COMPONENT INITIALIZATION
    // Creates the content panel (with CardLayout) and navigation bar
    // ============================================

    /**
     * Creates and arranges all the UI components inside the JFrame.
     * Uses BorderLayout: content in CENTER, navigation at SOUTH (bottom).
     */
    private void initializeComponents() {
        // BorderLayout divides the frame into 5 regions: NORTH, SOUTH, EAST, WEST, CENTER
        // We use CENTER for the main content and SOUTH for the navigation bar
        setLayout(new BorderLayout());

        // Create CardLayout — this layout manager shows only ONE child at a time
        // It's like a stack of index cards — you can only see the top card
        cardLayout = new CardLayout();

        // Create the content panel that will hold all view panels
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(currentTheme.getBackgroundPrimary());

        // Add view panels to the content panel — each with a unique string name
        // The name is used by cardLayout.show() to switch between panels
        contentPanel.add(new HomePanel(dataManager, currentTheme), "home");
        contentPanel.add(new TransactionsPanel(dataManager, currentTheme), "transactions");
        contentPanel.add(new ReportsPanel(dataManager, currentTheme), "reports");

        // Add the content panel to the CENTER region (fills all available space)
        add(contentPanel, BorderLayout.CENTER);

        // Create and add the navigation bar at the bottom
        createNavigationBar();

        // Create dialog windows (they're hidden by default until opened)
        // Creating them once and reusing avoids the overhead of recreating each time
        transactionDialog = new TransactionDialog(this, dataManager, currentTheme);
        settingsDialog = new SettingsDialog(this, dataManager, currentTheme);
    }

    // ============================================
    // NAVIGATION BAR — Bottom button bar
    // ============================================

    /**
     * Creates the bottom navigation bar with 5 buttons.
     * Uses GridLayout(1, 5) to arrange buttons in a single row with equal width.
     */
    private void createNavigationBar() {
        // GridLayout(1, 5) = 1 row, 5 columns — each button gets equal width
        JPanel navBar = new JPanel(new GridLayout(1, 5));
        navBar.setBackground(currentTheme.getBackgroundSecondary());

        // createMatteBorder(top, left, bottom, right) creates a border with different widths per side
        // Here: only the top edge has a 1px border line (visual separator from content)
        navBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, currentTheme.getBorderColor()));

        // setPreferredSize() suggests the ideal size (height of 60px for the nav bar)
        navBar.setPreferredSize(new Dimension(0, 60));

        // Create each navigation button with a text label and icon character
        // putClientProperty() attaches custom metadata to the button — we store the view name
        homeButton = createNavButton("Home", "\uF015");
        homeButton.putClientProperty("view", "home");

        transactionsButton = createNavButton("Transactions", "\uF03A");
        transactionsButton.putClientProperty("view", "transactions");

        // The Add button is special — it opens a dialog instead of switching views
        addButton = createNavButton("Add", "\uF055");
        addButton.putClientProperty("view", "add");

        reportsButton = createNavButton("Reports", "\uF080");
        reportsButton.putClientProperty("view", "reports");

        settingsButton = createNavButton("Settings", "\uF013");
        settingsButton.putClientProperty("view", "settings");

        // Add all buttons to the navigation bar panel
        navBar.add(homeButton);
        navBar.add(transactionsButton);
        navBar.add(addButton);
        navBar.add(reportsButton);
        navBar.add(settingsButton);

        // Add the nav bar to the SOUTH (bottom) region of the BorderLayout
        add(navBar, BorderLayout.SOUTH);

        // Set "home" as the initially active (highlighted) navigation button
        updateNavSelection("home");
    }

    /**
     * Creates a single navigation button with consistent styling.
     * All nav buttons share the same font, colors, cursor, and layout properties.
     *
     * @param text     Button label (e.g., "Home", "Reports")
     * @param iconChar Unicode character representing the icon
     * @return A fully configured JButton
     */
    private JButton createNavButton(String text, String iconChar) {
        JButton button = new JButton(text);

        // Text below icon (vertical layout within the button)
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);

        // Small font for navigation labels
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        // Style the button to blend with the navigation bar
        button.setBackground(currentTheme.getBackgroundSecondary());
        button.setForeground(currentTheme.getTextMuted());  // Inactive = muted color

        // Remove default Swing button visual effects
        button.setFocusPainted(false);    // No dotted focus rectangle
        button.setBorderPainted(false);   // No raised/lowered border

        // Change cursor to hand pointer (indicates clickability)
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Spacing between icon and text
        button.setIconTextGap(4);

        return button;
    }

    // ============================================
    // NAVIGATION EVENT HANDLERS
    // Each button switches to a different view or opens a dialog
    // ============================================

    /**
     * Attaches click event listeners (ActionListeners) to each navigation button.
     * Lambda expressions provide a concise way to define the event handler:
     *   button.addActionListener(e -> { ... })
     * is equivalent to creating an anonymous class implementing ActionListener.
     */
    private void setupNavigation() {
        // Home button — switch to the dashboard view
        homeButton.addActionListener(e -> {
            updateNavSelection("home");                    // Highlight this button
            cardLayout.show(contentPanel, "home");         // Show the HomePanel card
        });

        // Transactions button — switch to the full transaction list
        transactionsButton.addActionListener(e -> {
            updateNavSelection("transactions");
            cardLayout.show(contentPanel, "transactions");

            // Cast the second component (index 1) to TransactionsPanel and refresh its data
            // This ensures the list is up-to-date when navigating to it
            ((TransactionsPanel) contentPanel.getComponent(1)).refreshData();
        });

        // Add button — opens the TransactionDialog as a modal popup
        addButton.addActionListener(e -> {
            transactionDialog.clearForm();       // Reset the form fields to empty
            transactionDialog.setVisible(true);  // Show the dialog (blocks interaction with MainFrame)
        });

        // Reports button — switch to the analytics view with charts
        reportsButton.addActionListener(e -> {
            updateNavSelection("reports");
            cardLayout.show(contentPanel, "reports");

            // Refresh the charts and category breakdown data
            ((ReportsPanel) contentPanel.getComponent(2)).refreshData();
        });

        // Settings button — opens the SettingsDialog as a modal popup
        settingsButton.addActionListener(e -> {
            settingsDialog.setVisible(true);
        });
    }

    /**
     * Updates the visual selection state of navigation buttons.
     * The active button gets the primary (indigo) color and bold font.
     * All other buttons get the muted color and normal font.
     *
     * @param view The name of the currently active view (e.g., "home", "reports")
     */
    private void updateNavSelection(String view) {
        // Arrays of buttons and their corresponding view names
        JButton[] buttons = {homeButton, transactionsButton, addButton, reportsButton, settingsButton};
        String[] views = {"home", "transactions", "add", "reports", "settings"};

        // Loop through all buttons and update their appearance
        for (int i = 0; i < buttons.length; i++) {
            if (views[i].equals(view)) {
                // Active button: primary color + bold font
                buttons[i].setForeground(currentTheme.getPrimaryColor());
                buttons[i].setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else {
                // Inactive button: muted color + normal font
                buttons[i].setForeground(currentTheme.getTextMuted());
                buttons[i].setFont(new Font("Segoe UI", Font.PLAIN, 11));
            }
        }
    }

    // ============================================
    // THEME MANAGEMENT
    // Applies and toggles between light and dark themes
    // ============================================

    /**
     * Applies the current theme's colors to ALL UI components.
     * SwingUtilities.updateComponentTreeUI() recursively updates the entire component tree.
     */
    public void applyTheme() {
        // Set the frame's content pane background
        getContentPane().setBackground(currentTheme.getBackgroundPrimary());

        // Recursively update all child components with the current Look & Feel
        SwingUtilities.updateComponentTreeUI(this);

        // Update each view panel's background color
        if (contentPanel.getComponentCount() > 0) {
            for (Component comp : contentPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    ((JPanel) comp).setBackground(currentTheme.getBackgroundPrimary());
                }
            }
        }

        // Request a full repaint to apply all color changes
        repaint();
    }

    /**
     * Refreshes whichever view is currently visible.
     * Called by the DataManager's observer system when data changes.
     * Uses SwingUtilities.invokeLater() to ensure UI updates happen on the EDT.
     */
    private void refreshCurrentView() {
        // invokeLater() schedules the UI update to run on the Event Dispatch Thread
        // This is required because data change events may fire from non-EDT threads
        SwingUtilities.invokeLater(() -> {
            for (Component comp : contentPanel.getComponents()) {
                // Check if the component is both visible AND implements our Refreshable interface
                if (comp.isVisible() && comp instanceof Refreshable) {
                    // Call the refresh method — polymorphism in action
                    // The actual method called depends on the runtime type (HomePanel, TransactionsPanel, etc.)
                    ((Refreshable) comp).refresh();
                }
            }
        });
    }

    /**
     * Toggles between LIGHT and DARK theme.
     * Called when the user clicks the dark mode toggle in Settings.
     * Updates the theme for all components including dialogs.
     */
    public void toggleTheme() {
        // Ternary operator: if currently LIGHT → switch to DARK, and vice versa
        currentTheme = currentTheme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT;

        // Save the new theme preference to persistent storage
        dataManager.setTheme(currentTheme);

        // Apply the new theme to the main frame
        applyTheme();

        // Update theme in dialog windows as well
        transactionDialog.setTheme(currentTheme);
        settingsDialog.setTheme(currentTheme);

        // Update theme in all view panels that implement the Themeable interface
        for (Component comp : contentPanel.getComponents()) {
            if (comp instanceof Themeable) {
                ((Themeable) comp).setTheme(currentTheme);
            }
        }

        // Final repaint to ensure all changes are rendered
        repaint();
    }

    /**
     * Returns the current theme — used by dialogs to synchronize their theme.
     * @return Current Theme enum value (LIGHT or DARK)
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    // ============================================
    // INTERFACES — Refreshable & Themeable
    // These define contracts that UI panels can implement
    // This is POLYMORPHISM — we can call refresh() or setTheme()
    // on any component without knowing its specific class
    // ============================================

    /**
     * Interface for components that can refresh their data/display.
     * Any panel implementing this will have its refresh() method called
     * when transaction data changes (via the Observer Pattern).
     *
     * Implementors: HomePanel, TransactionsPanel, ReportsPanel
     */
    public interface Refreshable {
        // Single abstract method — implementing classes must define this
        void refresh();
    }

    /**
     * Interface for components that can change their visual theme.
     * Implementing classes must update their colors and fonts when setTheme() is called.
     *
     * Implementors: HomePanel, TransactionsPanel, ReportsPanel
     */
    public interface Themeable {
        // Called when the user toggles between LIGHT and DARK mode
        void setTheme(Theme theme);
    }
}
