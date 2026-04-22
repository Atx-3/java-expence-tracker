package com.expensetracker.data;

import com.expensetracker.model.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for JSON serialization/deserialization
 * Converts between Transaction objects and JSON format
 */
public class JsonUtil {

    // Date format for JSON storage (ISO-8601)
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Convert a single transaction to JSON
     * @param transaction Transaction to convert
     * @return JSONObject representation
     */
    public static JSONObject toJson(Transaction transaction) {
        JSONObject json = new JSONObject();
        json.put("id", transaction.getId());
        json.put("type", transaction.getType().name());
        json.put("amount", transaction.getAmount());
        json.put("description", transaction.getDescription());
        json.put("category", transaction.getCategory());
        json.put("date", transaction.getDate().format(DATE_FORMAT));
        json.put("notes", transaction.getNotes());
        json.put("createdAt", transaction.getCreatedAt());
        return json;
    }

    /**
     * Convert JSON to a transaction object
     * @param json JSONObject to parse
     * @return Transaction object
     */
    public static Transaction fromJson(JSONObject json) {
        Transaction.TransactionType type = Transaction.TransactionType.valueOf(
            json.getString("type")
        );

        double amount = json.getDouble("amount");
        String description = json.getString("description");
        String category = json.getString("category");
        LocalDate date = LocalDate.parse(json.getString("date"), DATE_FORMAT);
        String notes = json.optString("notes", null);
        long createdAt = json.optLong("createdAt", System.currentTimeMillis());

        Transaction transaction = new Transaction(
            type, amount, description, category, date, notes
        );

        // Use reflection to set the ID (normally generated)
        try {
            java.lang.reflect.Field idField = Transaction.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(transaction, json.getString("id"));
        } catch (Exception e) {
            // Ignore, will use generated ID
        }

        try {
            java.lang.reflect.Field createdAtField = Transaction.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.setLong(transaction, createdAt);
        } catch (Exception e) {
            // Ignore
        }

        return transaction;
    }

    /**
     * Convert list of transactions to JSON array
     * @param transactions List of transactions
     * @return JSONArray string
     */
    public static String toJsonArray(List<Transaction> transactions) {
        JSONArray array = new JSONArray();
        for (Transaction t : transactions) {
            array.put(toJson(t));
        }
        return array.toString(2); // Pretty print with 2-space indent
    }

    /**
     * Parse JSON array to list of transactions
     * @param json JSONArray string
     * @return List of Transaction objects
     */
    public static List<Transaction> fromJsonArray(String json) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                transactions.add(fromJson(obj));
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
        return transactions;
    }
}
