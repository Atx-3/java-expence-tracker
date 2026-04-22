package com.expensetracker.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single transaction (income or expense)
 * Contains all transaction details including amount, category, and notes
 */
public class Transaction {

    // Unique identifier for each transaction
    private String id;

    // Transaction type: INCOME or EXPENSE
    private TransactionType type;

    // Monetary amount
    private double amount;

    // User-entered description
    private String description;

    // Category identifier (food, transport, etc.)
    private String category;

    // Transaction date
    private LocalDate date;

    // Optional additional notes
    private String notes;

    // Timestamp when transaction was created
    private long createdAt;

    /**
     * Transaction types enum
     */
    public enum TransactionType {
        INCOME("Income"),
        EXPENSE("Expense");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Create a new transaction with all fields
     * @param type Transaction type (income/expense)
     * @param amount Monetary amount
     * @param description Brief description
     * @param category Category ID
     * @param date Transaction date
     * @param notes Optional notes
     */
    public Transaction(TransactionType type, double amount, String description,
                       String category, LocalDate date, String notes) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Check if this transaction is an expense
     * @return true if expense, false if income
     */
    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }

    /**
     * Check if this transaction is income
     * @return true if income, false if expense
     */
    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    @Override
    public String toString() {
        return String.format("Transaction{id=%s, type=%s, amount=%.2f, description='%s'}",
                           id, type, amount, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
