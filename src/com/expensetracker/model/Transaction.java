// Package declaration — this class belongs to the 'model' package
// The model layer holds data structures (POJOs) that represent business entities
package com.expensetracker.model;

// LocalDate is from java.time package (introduced in Java 8)
// It stores a date without time or timezone — perfect for transaction dates
import java.time.LocalDate;

// UUID generates universally unique identifiers (128-bit values)
// Used here to create unique IDs for each transaction without a database
import java.util.UUID;

/**
 * Transaction.java — DATA MODEL (POJO)
 * ======================================
 * This class represents a single financial transaction (income or expense).
 * It is a Plain Old Java Object (POJO) — it only holds data and provides
 * getter/setter methods. It has NO business logic or database connections.
 *
 * Key OOP Concepts Used:
 *   - Encapsulation: All fields are 'private', accessed via public getters/setters
 *   - Enum: TransactionType is a type-safe constant (INCOME or EXPENSE)
 *   - Method Overriding: toString(), equals(), hashCode() are overridden from Object class
 *
 * Fields:
 *   - id          → Unique identifier (UUID string) — acts as a primary key
 *   - type        → INCOME or EXPENSE (enum for type safety)
 *   - amount      → The monetary value (double for decimal precision)
 *   - description → What the transaction was for (e.g., "Groceries")
 *   - category    → Category ID linking to Category.java (e.g., "food", "salary")
 *   - date        → When the transaction occurred (LocalDate)
 *   - notes       → Optional extra details
 *   - createdAt   → Unix timestamp when the object was created (for sorting)
 */
public class Transaction {

    // ============================================
    // PRIVATE FIELDS (Encapsulation)
    // All fields are private — they can only be accessed through getters/setters
    // This protects data integrity and allows validation in setters
    // ============================================

    // Unique identifier for each transaction — generated automatically using UUID
    // UUID produces a random 128-bit ID like "550e8400-e29b-41d4-a716-446655440000"
    private String id;

    // Transaction type: either INCOME or EXPENSE
    // Using an enum instead of a String prevents typos and invalid values
    private TransactionType type;

    // Monetary amount stored as double (supports decimal values like 45.99)
    // Note: For financial applications in production, BigDecimal is preferred for precision
    private double amount;

    // User-entered description explaining what the transaction was for
    private String description;

    // Category identifier — links to Category.java (e.g., "food", "transport", "salary")
    // Stored as String ID rather than Category object to keep serialization simple
    private String category;

    // Transaction date — uses java.time.LocalDate (immutable, no timezone issues)
    // LocalDate only stores year-month-day, not time — which is what we need for expenses
    private LocalDate date;

    // Optional additional notes the user can add for context
    // Can be null or empty — it's not a required field
    private String notes;

    // Unix timestamp (milliseconds since Jan 1, 1970) when this object was created
    // Used for secondary sorting — when two transactions have the same date
    private long createdAt;

    // ============================================
    // INNER ENUM — TransactionType
    // Enums are special classes in Java that represent fixed constants
    // They are type-safe — you can't accidentally pass an invalid value
    // ============================================

    /**
     * TransactionType enum defines the two possible types of transactions.
     * Using an enum guarantees only valid values can be used.
     * Each enum constant has a 'displayName' for showing in the UI.
     */
    public enum TransactionType {
        // Each constant calls the constructor with a display name
        INCOME("Income"),     // Money coming in (salary, freelance, etc.)
        EXPENSE("Expense");   // Money going out (food, bills, etc.)

        // The human-readable name shown in the UI
        private final String displayName;

        // Enum constructor — called automatically when each constant is created
        // Enum constructors are always private (even without the keyword)
        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        // Getter for the display name — used in UI labels
        public String getDisplayName() {
            return displayName;
        }
    }

    // ============================================
    // CONSTRUCTOR
    // Creates a new Transaction with all required fields
    // The ID and createdAt timestamp are generated automatically
    // ============================================

    /**
     * Parameterized Constructor — creates a new transaction with all required fields.
     * The 'id' and 'createdAt' fields are auto-generated inside the constructor.
     *
     * @param type        Whether this is INCOME or EXPENSE
     * @param amount      The monetary amount (must be positive)
     * @param description Brief text describing the transaction
     * @param category    Category ID from Category.java (e.g., "food")
     * @param date        The date of the transaction
     * @param notes       Optional additional notes (can be null)
     */
    public Transaction(TransactionType type, double amount, String description,
                       String category, LocalDate date, String notes) {

        // UUID.randomUUID() generates a globally unique 128-bit identifier
        // .toString() converts it to a readable string format
        // This ensures every transaction has a unique ID without needing a database
        this.id = UUID.randomUUID().toString();

        // Assign all the user-provided values to instance fields
        // 'this.' differentiates between instance variables and constructor parameters
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
        this.notes = notes;

        // System.currentTimeMillis() returns the current time as milliseconds since epoch
        // This timestamp helps sort transactions created on the same date
        this.createdAt = System.currentTimeMillis();
    }

    // ============================================
    // GETTERS AND SETTERS
    // These follow the JavaBean convention:
    //   - Getter: getFieldName() — returns the field value
    //   - Setter: setFieldName(value) — sets the field value
    // Some fields (id, createdAt) only have getters — they're read-only
    // ============================================

    // Returns the unique transaction ID — no setter because IDs should never change
    public String getId() {
        return id;
    }

    // Returns the transaction type (INCOME or EXPENSE)
    public TransactionType getType() {
        return type;
    }

    // Sets the transaction type — used when editing a transaction
    public void setType(TransactionType type) {
        this.type = type;
    }

    // Returns the monetary amount
    public double getAmount() {
        return amount;
    }

    // Sets a new amount — used when editing a transaction
    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Returns the user-entered description
    public String getDescription() {
        return description;
    }

    // Sets a new description — used when editing
    public void setDescription(String description) {
        this.description = description;
    }

    // Returns the category ID (e.g., "food", "transport")
    public String getCategory() {
        return category;
    }

    // Sets a new category — used when editing
    public void setCategory(String category) {
        this.category = category;
    }

    // Returns the transaction date as a LocalDate object
    public LocalDate getDate() {
        return date;
    }

    // Sets a new date — used when editing
    public void setDate(LocalDate date) {
        this.date = date;
    }

    // Returns the optional notes (can be null)
    public String getNotes() {
        return notes;
    }

    // Sets new notes — used when editing
    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Returns the creation timestamp — no setter because this should never change
    public long getCreatedAt() {
        return createdAt;
    }

    // ============================================
    // CONVENIENCE METHODS
    // These are helper methods that encapsulate common checks
    // They make the code more readable: t.isExpense() vs t.getType() == EXPENSE
    // ============================================

    /**
     * Checks if this transaction is an expense.
     * Convenience method — avoids writing getType() == TransactionType.EXPENSE everywhere.
     *
     * @return true if this is an expense, false if it's income
     */
    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }

    /**
     * Checks if this transaction is income.
     * Convenience method for readable code.
     *
     * @return true if this is income, false if it's an expense
     */
    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    // ============================================
    // OVERRIDDEN METHODS FROM Object CLASS
    // Every Java class implicitly extends Object, which provides
    // toString(), equals(), and hashCode() — we override them for custom behavior
    // ============================================

    /**
     * toString() — returns a human-readable string representation of this object.
     * Called automatically when printing the object or during debugging.
     * String.format() creates a formatted string with placeholders:
     *   %s = String, %.2f = double with 2 decimal places
     *
     * @return Formatted string showing key transaction details
     */
    @Override
    public String toString() {
        return String.format("Transaction{id=%s, type=%s, amount=%.2f, description='%s'}",
                           id, type, amount, description);
    }

    /**
     * equals() — determines if two Transaction objects are "equal".
     * We define equality based on the 'id' field — two transactions are equal
     * if and only if they have the same unique ID.
     *
     * This is important because:
     *   - Collections like List and Set use equals() for .contains() and .remove()
     *   - Without this, Java would compare memory addresses (reference equality)
     *
     * @param o The object to compare against
     * @return true if both objects have the same ID
     */
    @Override
    public boolean equals(Object o) {
        // Step 1: Check if both references point to the same object in memory
        if (this == o) return true;

        // Step 2: Check if the other object is null or a different class
        if (o == null || getClass() != o.getClass()) return false;

        // Step 3: Cast to Transaction and compare IDs
        Transaction that = (Transaction) o;
        return id != null && id.equals(that.id);
    }

    /**
     * hashCode() — returns a numeric hash value for this object.
     * RULE: If two objects are equal (equals() returns true), they MUST have the same hashCode.
     * This is required for proper behavior in HashMap, HashSet, etc.
     *
     * We base it on the 'id' field to match our equals() implementation.
     *
     * @return Hash code based on the transaction ID
     */
    @Override
    public int hashCode() {
        // Uses the ID's hashCode, or 0 if ID is null
        return id != null ? id.hashCode() : 0;
    }
}
