// Package declaration — this is the data/persistence layer of the application
package com.expensetracker.data;

// Importing the Transaction model class that we'll be storing and managing
import com.expensetracker.model.Transaction;

// Importing the Theme enum to manage dark/light mode preference
import com.expensetracker.util.Theme;

// java.io provides classes for input/output operations (reading/writing files)
import java.io.*;

// java.nio.file provides modern file system access (Paths, Files)
// NIO stands for "New I/O" — introduced in Java 7, it's the preferred way to work with files
import java.nio.file.*;

// LocalDate for date operations (filtering by date range)
import java.time.LocalDate;

// ArrayList is a resizable array implementation of the List interface
// We use it to store our transactions in memory
import java.util.ArrayList;

// List is the interface — we program to the interface, not the implementation
// This allows swapping ArrayList for LinkedList without changing other code
import java.util.List;

// Preferences API provides persistent storage for user settings
// Data is stored in the Windows Registry (on Windows) or plist files (on macOS)
import java.util.prefs.Preferences;

/**
 * DataManager.java — SINGLETON DATA PERSISTENCE LAYER
 * =====================================================
 * This is the central data management class for the entire application.
 * It follows the SINGLETON DESIGN PATTERN — only one instance exists.
 *
 * Responsibilities:
 *   1. CRUD Operations — Add, Read, Update, Delete transactions
 *   2. Data Persistence — Save/load transactions to/from a JSON file
 *   3. User Preferences — Store theme and currency settings via Java Preferences API
 *   4. Calculations — Compute totals (balance, income, expenses)
 *   5. Event System — Notify UI components when data changes (Observer Pattern)
 *
 * Design Patterns Used:
 *   - Singleton: Only one DataManager instance exists (thread-safe with synchronized)
 *   - Observer: DataChangeListener interface notifies subscribers of data changes
 *
 * Data Storage Locations:
 *   - Transactions: ~/.expensetracker/data.json (JSON file in user's home directory)
 *   - Preferences: Java Preferences API (OS-level storage — Windows Registry / macOS plist)
 */
public class DataManager {

    // ============================================
    // SINGLETON PATTERN IMPLEMENTATION
    // A static field holds the single instance of this class
    // 'static' means it belongs to the class itself, not any individual object
    // ============================================

    // The single instance — starts as null, created on first access
    private static DataManager instance;

    // ============================================
    // INSTANCE FIELDS
    // ============================================

    // In-memory list of all transactions — acts as a cache
    // All operations (add, update, delete) happen here first, then are saved to disk
    // 'final' means the list reference can't change, but items inside it can
    private final List<Transaction> transactions;

    // Java Preferences API object — stores small key-value pairs persistently
    // Used for theme ("LIGHT"/"DARK") and currency ("USD", "EUR", etc.)
    // On Windows, this is stored in the Windows Registry under HKEY_CURRENT_USER
    private final Preferences prefs;

    // Path to the JSON data file on disk
    // Stored as a NIO Path object for modern file operations
    // Typically: C:\Users\{name}\.expensetracker\data.json (Windows)
    private final Path dataFilePath;

    // ============================================
    // PRIVATE CONSTRUCTOR (Singleton Pattern)
    // 'private' prevents external code from calling 'new DataManager()'
    // The only way to get an instance is through getInstance()
    // ============================================

    /**
     * Private constructor — only called ONCE by getInstance().
     * Initializes the empty transaction list, preferences, file path,
     * and loads any existing data from the JSON file.
     */
    private DataManager() {
        // Create an empty ArrayList to hold transactions in memory
        transactions = new ArrayList<>();

        // Get a Preferences node for our application
        // userRoot() = per-user preferences (not system-wide)
        // .node() creates/accesses a named node in the preferences tree
        prefs = Preferences.userRoot().node("/com/expensetracker");

        // Build the file path: {userHome}/.expensetracker/data.json
        // System.getProperty("user.home") returns the user's home directory
        // Paths.get() creates a platform-independent file path
        String userHome = System.getProperty("user.home");
        dataFilePath = Paths.get(userHome, ".expensetracker", "data.json");

        // Load any previously saved transactions from the JSON file
        loadData();
    }

    // ============================================
    // SINGLETON ACCESS METHOD
    // This is the only way to get a DataManager instance
    // 'synchronized' makes it thread-safe — prevents two threads
    // from creating two instances simultaneously
    // ============================================

    /**
     * Returns the single DataManager instance.
     * If it doesn't exist yet, it creates one (lazy initialization).
     *
     * 'synchronized' keyword ensures only one thread can execute this method at a time.
     * This prevents a race condition where two threads could both see instance == null
     * and create two separate instances.
     *
     * @return The single DataManager instance
     */
    public static synchronized DataManager getInstance() {
        // Lazy initialization — only create the instance when first needed
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    // ============================================
    // CRUD OPERATIONS — Create, Read, Update, Delete
    // These are the core data manipulation methods
    // Each modifying operation: (1) updates memory, (2) saves to disk, (3) notifies listeners
    // ============================================

    /**
     * CREATE — Adds a new transaction to the list.
     * Inserted at index 0 (beginning) for reverse chronological order.
     * Then saves to disk and notifies all UI listeners to refresh.
     *
     * @param transaction The new Transaction object to add
     */
    public void addTransaction(Transaction transaction) {
        // add(0, ...) inserts at the beginning — newest transactions appear first
        transactions.add(0, transaction);

        // Persist the updated list to the JSON file on disk
        saveData();

        // Notify all registered listeners (UI panels) that data has changed
        // This triggers the Observer Pattern — panels will refresh their displays
        fireDataChanged();
    }

    /**
     * UPDATE — Modifies an existing transaction identified by its ID.
     * Iterates through the list to find the matching ID, then replaces it.
     *
     * @param transaction The updated Transaction object (must have the same ID as the original)
     */
    public void updateTransaction(Transaction transaction) {
        // Linear search through the list to find the transaction with matching ID
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(transaction.getId())) {
                // Replace the old transaction with the updated one at the same index
                transactions.set(i, transaction);

                // Save changes to disk and notify UI
                saveData();
                fireDataChanged();
                return; // Exit early — IDs are unique, no need to continue searching
            }
        }
    }

    /**
     * DELETE — Removes a transaction by its unique ID.
     * Uses removeIf() with a lambda predicate for concise code.
     *
     * @param id The unique ID of the transaction to delete
     */
    public void deleteTransaction(String id) {
        // removeIf() iterates through the list and removes elements matching the predicate
        // The lambda 't -> t.getId().equals(id)' returns true for the transaction to remove
        transactions.removeIf(t -> t.getId().equals(id));

        // Save changes to disk and notify UI
        saveData();
        fireDataChanged();
    }

    /**
     * READ ALL — Returns a copy of all transactions.
     * Returns a NEW ArrayList to prevent external code from modifying our internal list.
     * This is called "defensive copying" — an important practice for encapsulation.
     *
     * @return A new list containing all transactions
     */
    public List<Transaction> getAllTransactions() {
        // 'new ArrayList<>(transactions)' creates a shallow copy
        // External code can modify this copy without affecting our master list
        return new ArrayList<>(transactions);
    }

    /**
     * READ FILTERED BY TYPE — Returns only INCOME or only EXPENSE transactions.
     * Manually iterates and filters (could also use Java Streams for brevity).
     *
     * @param type The TransactionType to filter by (INCOME or EXPENSE)
     * @return Filtered list of transactions
     */
    public List<Transaction> getTransactionsByType(Transaction.TransactionType type) {
        List<Transaction> result = new ArrayList<>();

        // Iterate through all transactions and collect only matching ones
        for (Transaction t : transactions) {
            if (t.getType() == type) {  // Enum comparison uses == (not .equals())
                result.add(t);
            }
        }
        return result;
    }

    /**
     * READ FILTERED BY DATE RANGE — Returns transactions within a date range (inclusive).
     * Uses LocalDate's isBefore() and isAfter() for date comparison.
     *
     * @param start Start date (inclusive) — transactions on or after this date
     * @param end   End date (inclusive) — transactions on or before this date
     * @return List of transactions within the specified range
     */
    public List<Transaction> getTransactionsByDateRange(LocalDate start, LocalDate end) {
        List<Transaction> result = new ArrayList<>();

        for (Transaction t : transactions) {
            // !isBefore = on or after start date
            // !isAfter = on or before end date
            // Combined: start <= t.date <= end
            if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * READ RECENT — Returns the N most recent transactions.
     * Since transactions are stored in reverse chronological order (newest first),
     * we simply return the first 'limit' elements.
     *
     * @param limit Maximum number of transactions to return
     * @return List of the most recent transactions
     */
    public List<Transaction> getRecentTransactions(int limit) {
        // If total transactions <= limit, return all of them
        if (transactions.size() <= limit) {
            return new ArrayList<>(transactions);
        }

        // subList(0, limit) returns a view of the first 'limit' elements
        // Wrapping in new ArrayList creates an independent copy
        return new ArrayList<>(transactions.subList(0, limit));
    }

    /**
     * DELETE ALL — Clears all transactions from memory and disk.
     * Used in the Settings dialog when user clicks "Clear All Data".
     */
    public void clearAllTransactions() {
        transactions.clear();  // Remove all elements from the ArrayList
        saveData();            // Save the now-empty list to disk
        fireDataChanged();     // Notify UI to show empty state
    }

    // ============================================
    // CALCULATION METHODS
    // These compute financial summaries from the transactions list
    // They iterate through all transactions and aggregate values
    // ============================================

    /**
     * Calculates the total balance: sum of all income minus sum of all expenses.
     * This is the main financial indicator shown on the dashboard.
     *
     * @return Net balance (positive = surplus, negative = deficit)
     */
    public double getTotalBalance() {
        double balance = 0;

        for (Transaction t : transactions) {
            if (t.isIncome()) {
                balance += t.getAmount();  // Income adds to balance
            } else {
                balance -= t.getAmount();  // Expense subtracts from balance
            }
        }
        return balance;
    }

    /**
     * Calculates total income — sum of all INCOME transactions.
     *
     * @return Total income amount
     */
    public double getTotalIncome() {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.isIncome()) {
                total += t.getAmount();
            }
        }
        return total;
    }

    /**
     * Calculates total expenses — sum of all EXPENSE transactions.
     *
     * @return Total expense amount
     */
    public double getTotalExpenses() {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.isExpense()) {
                total += t.getAmount();
            }
        }
        return total;
    }

    /**
     * Calculates spending breakdown by category for a given date range.
     * Returns an array where index 0 = food, 1 = transport, etc.
     * Used by the Reports panel to draw the pie/donut chart.
     *
     * @param start Range start date
     * @param end   Range end date
     * @return Array of 9 doubles, one per expense category
     */
    public double[] getSpendingByCategory(LocalDate start, LocalDate end) {
        // 9 expense categories defined in Category.java
        double[] spending = new double[9];

        // Get transactions in the date range, then filter for expenses only
        for (Transaction t : getTransactionsByDateRange(start, end)) {
            if (t.isExpense()) {
                // Map the category ID string to an array index
                int catIndex = getCategoryIndex(t.getCategory());

                // Only update if found (index >= 0)
                if (catIndex >= 0 && catIndex < spending.length) {
                    spending[catIndex] += t.getAmount();
                }
            }
        }
        return spending;
    }

    /**
     * Helper method — maps a category ID string to its numeric index in the array.
     * This mapping must match the order in Category.EXPENSE_CATEGORIES.
     *
     * @param categoryId Category ID string (e.g., "food")
     * @return Index (0-8), or -1 if not found
     */
    private int getCategoryIndex(String categoryId) {
        // This array order MUST match Category.EXPENSE_CATEGORIES
        String[] categories = {"food", "transport", "shopping", "entertainment",
                              "bills", "health", "education", "personal", "other"};

        // Linear search to find the matching index
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(categoryId)) {
                return i;
            }
        }
        return -1; // Not found
    }

    // ============================================
    // PREFERENCES — Theme and Currency
    // Uses Java Preferences API for lightweight key-value storage
    // Unlike files, Preferences survive app uninstalls on most platforms
    // ============================================

    /**
     * Retrieves the saved theme preference.
     * Uses prefs.get() with a default value of "LIGHT" if no preference is stored.
     * Theme.valueOf() converts the string back to the Theme enum.
     *
     * @return The saved Theme (LIGHT or DARK)
     */
    public Theme getTheme() {
        // prefs.get(key, defaultValue) — reads a string preference
        String themeStr = prefs.get("theme", "LIGHT");
        try {
            // Enum.valueOf() converts a string to an enum constant
            // Throws IllegalArgumentException if the string doesn't match any constant
            return Theme.valueOf(themeStr);
        } catch (IllegalArgumentException e) {
            // If stored value is invalid, default to LIGHT theme
            return Theme.LIGHT;
        }
    }

    /**
     * Saves the theme preference persistently.
     * theme.name() returns the enum constant name as a String ("LIGHT" or "DARK").
     *
     * @param theme The Theme enum value to save
     */
    public void setTheme(Theme theme) {
        // prefs.put(key, value) — stores a string preference
        prefs.put("theme", theme.name());
    }

    /**
     * Retrieves the saved currency code.
     * Default is "USD" if no preference has been set.
     *
     * @return Currency code string (e.g., "USD", "EUR", "INR")
     */
    public String getCurrency() {
        return prefs.get("currency", "USD");
    }

    /**
     * Saves the currency preference.
     *
     * @param currency Currency code to save (e.g., "EUR")
     */
    public void setCurrency(String currency) {
        prefs.put("currency", currency);
    }

    /**
     * Static utility — converts a currency code to its symbol.
     * Uses a switch statement (could also use a Map for extensibility).
     *
     * @param currency Currency code (e.g., "EUR")
     * @return Currency symbol (e.g., "€")
     */
    public static String getCurrencySymbol(String currency) {
        switch (currency) {
            case "EUR": return "€";   // Euro
            case "GBP": return "£";   // British Pound
            case "JPY": return "¥";   // Japanese Yen
            case "INR": return "₹";   // Indian Rupee
            default: return "$";      // US Dollar (default)
        }
    }

    // ============================================
    // FILE PERSISTENCE — JSON Read/Write
    // Transactions are serialized to JSON and stored in a file
    // JsonUtil handles the conversion between Java objects and JSON strings
    // ============================================

    /**
     * LOAD — Reads transactions from the JSON file on disk into memory.
     * Called once during initialization (in the private constructor).
     *
     * Uses NIO Files API:
     *   - Files.exists() checks if the file exists before attempting to read
     *   - Files.readAllBytes() reads the entire file into a byte array
     *   - String constructor converts bytes to a String (assuming UTF-8)
     */
    private void loadData() {
        try {
            // Only attempt to read if the file actually exists
            if (Files.exists(dataFilePath)) {
                // Read the entire file content as a string
                String json = new String(Files.readAllBytes(dataFilePath));

                // Parse the JSON string into a list of Transaction objects
                // addAll() adds all parsed transactions to our in-memory list
                transactions.addAll(JsonUtil.fromJsonArray(json));
            }
        } catch (IOException e) {
            // IOException occurs if file can't be read (permissions, corruption, etc.)
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    /**
     * SAVE — Writes all in-memory transactions to the JSON file on disk.
     * Called after every add, update, or delete operation.
     *
     * Uses NIO Files API:
     *   - Files.createDirectories() creates parent folders if they don't exist
     *   - Files.write() atomically writes bytes to a file (creates or overwrites)
     */
    private void saveData() {
        try {
            // Ensure the .expensetracker directory exists
            // createDirectories() creates all missing parent directories
            // Unlike mkdir(), it doesn't throw if directories already exist
            Files.createDirectories(dataFilePath.getParent());

            // Convert the transaction list to a pretty-printed JSON string
            String json = JsonUtil.toJsonArray(transactions);

            // Write the JSON string to the file
            // .getBytes() converts the String to byte[] using default charset (UTF-8)
            Files.write(dataFilePath, json.getBytes());
        } catch (IOException e) {
            // IOException occurs if file can't be written (permissions, disk full, etc.)
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * EXPORT — Saves all transactions to a user-specified file path.
     * Unlike saveData(), this writes to a custom location chosen by the user.
     * Used in the Settings dialog for data backup.
     *
     * @param exportPath Full path where the backup file should be created
     * @return true if export succeeded, false if it failed
     */
    public boolean exportData(String exportPath) {
        try {
            String json = JsonUtil.toJsonArray(transactions);
            Files.write(Paths.get(exportPath), json.getBytes());
            return true;   // Success
        } catch (IOException e) {
            return false;  // Failure — caller should show an error dialog
        }
    }

    // ============================================
    // OBSERVER PATTERN — Data Change Listeners
    // This allows UI panels to subscribe to data changes
    // When data changes, all subscribers are automatically notified
    // This decouples the data layer from the UI layer
    // ============================================

    // List of all registered listeners — each one will be notified on data changes
    private final List<DataChangeListener> listeners = new ArrayList<>();

    /**
     * Register a new listener to be notified when data changes.
     * Called by UI panels (HomePanel, TransactionsPanel, etc.) during initialization.
     *
     * @param listener The listener to register
     */
    public void addDataChangeListener(DataChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregister a listener so it no longer receives notifications.
     *
     * @param listener The listener to remove
     */
    public void removeDataChangeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify ALL registered listeners that the data has changed.
     * This triggers each listener's onDataChanged() callback.
     * Called internally after every add, update, delete, or clear operation.
     */
    private void fireDataChanged() {
        // Iterate through all listeners and call their callback method
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged();
        }
    }

    /**
     * OBSERVER INTERFACE — DataChangeListener
     * =========================================
     * Any class that wants to be notified of data changes must implement this interface.
     * It defines a single method: onDataChanged().
     *
     * Implementing classes:
     *   - HomePanel → refreshes balance and recent transactions
     *   - TransactionsPanel → refreshes the transaction list
     *   - ReportsPanel → redraws charts and category breakdown
     *
     * This is the Observer Pattern in action:
     *   - Subject (DataManager) maintains a list of observers
     *   - Observers (UI panels) implement DataChangeListener
     *   - When data changes, Subject notifies all Observers
     */
    public interface DataChangeListener {
        // This method is called whenever transaction data changes
        void onDataChanged();
    }
}
