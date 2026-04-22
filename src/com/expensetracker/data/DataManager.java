package com.expensetracker.data;

import com.expensetracker.model.Transaction;
import com.expensetracker.util.Theme;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Singleton class for managing application data
 * Handles loading, saving, and CRUD operations for transactions
 * Also manages user preferences (theme, currency)
 */
public class DataManager {

    // Singleton instance
    private static DataManager instance;

    // Storage for transactions
    private final List<Transaction> transactions;

    // Preferences storage (uses Java Preferences API)
    private final Preferences prefs;

    // Data file path
    private final Path dataFilePath;

    /**
     * Private constructor for singleton pattern
     */
    private DataManager() {
        transactions = new ArrayList<>();
        prefs = Preferences.userRoot().node("/com/expensetracker");

        // Determine data file location (user's home directory)
        String userHome = System.getProperty("user.home");
        dataFilePath = Paths.get(userHome, ".expensetracker", "data.json");

        // Load existing data
        loadData();
    }

    /**
     * Get singleton instance
     * @return DataManager instance
     */
    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    // ============================================
    // Transaction Operations
    // ============================================

    /**
     * Add a new transaction
     * @param transaction Transaction to add
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(0, transaction); // Add to beginning for reverse chronological order
        saveData();
        fireDataChanged();
    }

    /**
     * Update an existing transaction
     * @param transaction Updated transaction
     */
    public void updateTransaction(Transaction transaction) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(transaction.getId())) {
                transactions.set(i, transaction);
                saveData();
                fireDataChanged();
                return;
            }
        }
    }

    /**
     * Delete a transaction by ID
     * @param id Transaction ID to delete
     */
    public void deleteTransaction(String id) {
        transactions.removeIf(t -> t.getId().equals(id));
        saveData();
        fireDataChanged();
    }

    /**
     * Get all transactions
     * @return List of all transactions
     */
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Get transactions filtered by type
     * @param type Transaction type to filter
     * @return Filtered list of transactions
     */
    public List<Transaction> getTransactionsByType(Transaction.TransactionType type) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getType() == type) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Get transactions within a date range
     * @param start Start date (inclusive)
     * @param end End date (inclusive)
     * @return List of transactions in range
     */
    public List<Transaction> getTransactionsByDateRange(LocalDate start, LocalDate end) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Get recent transactions (for home screen)
     * @param limit Maximum number of transactions
     * @return List of recent transactions
     */
    public List<Transaction> getRecentTransactions(int limit) {
        if (transactions.size() <= limit) {
            return new ArrayList<>(transactions);
        }
        return new ArrayList<>(transactions.subList(0, limit));
    }

    /**
     * Clear all transactions
     */
    public void clearAllTransactions() {
        transactions.clear();
        saveData();
        fireDataChanged();
    }

    // ============================================
    // Calculations
    // ============================================

    /**
     * Calculate total balance (income - expenses)
     * @return Total balance
     */
    public double getTotalBalance() {
        double balance = 0;
        for (Transaction t : transactions) {
            if (t.isIncome()) {
                balance += t.getAmount();
            } else {
                balance -= t.getAmount();
            }
        }
        return balance;
    }

    /**
     * Calculate total income
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
     * Calculate total expenses
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
     * Get spending by category for a date range
     * @param start Start date
     * @param end End date
     * @return Array with spending per category
     */
    public double[] getSpendingByCategory(LocalDate start, LocalDate end) {
        double[] spending = new double[9]; // 9 expense categories
        for (Transaction t : getTransactionsByDateRange(start, end)) {
            if (t.isExpense()) {
                int catIndex = getCategoryIndex(t.getCategory());
                if (catIndex >= 0 && catIndex < spending.length) {
                    spending[catIndex] += t.getAmount();
                }
            }
        }
        return spending;
    }

    /**
     * Get category index for array storage
     */
    private int getCategoryIndex(String categoryId) {
        String[] categories = {"food", "transport", "shopping", "entertainment",
                              "bills", "health", "education", "personal", "other"};
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(categoryId)) {
                return i;
            }
        }
        return -1;
    }

    // ============================================
    // Preferences (Theme, Currency)
    // ============================================

    /**
     * Get current theme
     * @return Theme enum value
     */
    public Theme getTheme() {
        String themeStr = prefs.get("theme", "LIGHT");
        try {
            return Theme.valueOf(themeStr);
        } catch (IllegalArgumentException e) {
            return Theme.LIGHT;
        }
    }

    /**
     * Set current theme
     * @param theme Theme to set
     */
    public void setTheme(Theme theme) {
        prefs.put("theme", theme.name());
    }

    /**
     * Get selected currency
     * @return Currency code (USD, EUR, etc.)
     */
    public String getCurrency() {
        return prefs.get("currency", "USD");
    }

    /**
     * Set selected currency
     * @param currency Currency code
     */
    public void setCurrency(String currency) {
        prefs.put("currency", currency);
    }

    /**
     * Get currency symbol
     * @param currency Currency code
     * @return Currency symbol
     */
    public static String getCurrencySymbol(String currency) {
        switch (currency) {
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "INR": return "₹";
            default: return "$";
        }
    }

    // ============================================
    // Data Persistence
    // ============================================

    /**
     * Load transactions from file
     */
    private void loadData() {
        try {
            if (Files.exists(dataFilePath)) {
                String json = new String(Files.readAllBytes(dataFilePath));
                transactions.addAll(JsonUtil.fromJsonArray(json));
            }
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    /**
     * Save transactions to file
     */
    private void saveData() {
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(dataFilePath.getParent());

            // Write JSON data
            String json = JsonUtil.toJsonArray(transactions);
            Files.write(dataFilePath, json.getBytes());
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Export all data to a JSON file
     * @param exportPath Path to export file
     * @return true if successful
     */
    public boolean exportData(String exportPath) {
        try {
            String json = JsonUtil.toJsonArray(transactions);
            Files.write(Paths.get(exportPath), json.getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ============================================
    // Event Listeners
    // ============================================

    // Listeners for data change events
    private final List<DataChangeListener> listeners = new ArrayList<>();

    /**
     * Add data change listener
     * @param listener Listener to add
     */
    public void addDataChangeListener(DataChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove data change listener
     * @param listener Listener to remove
     */
    public void removeDataChangeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of data change
     */
    private void fireDataChanged() {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged();
        }
    }

    /**
     * Interface for data change notifications
     */
    public interface DataChangeListener {
        void onDataChanged();
    }
}
