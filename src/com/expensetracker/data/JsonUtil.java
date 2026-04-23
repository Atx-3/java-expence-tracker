// This class belongs to the 'data' package — responsible for data persistence operations
package com.expensetracker.data;

// Importing the Transaction model class that we'll be serializing/deserializing
import com.expensetracker.model.Transaction;

// org.json is a third-party library (from Maven) that provides JSON manipulation classes
// JSONArray represents an ordered list: [item1, item2, ...]
// JSONObject represents a key-value map: { "key": "value", ... }
import org.json.JSONArray;
import org.json.JSONObject;

// LocalDate for date parsing — ISO format like "2026-04-23"
import java.time.LocalDate;

// DateTimeFormatter defines how dates are formatted/parsed as strings
import java.time.format.DateTimeFormatter;

// ArrayList and List for working with collections of Transaction objects
import java.util.ArrayList;
import java.util.List;

/**
 * JsonUtil.java — JSON SERIALIZATION / DESERIALIZATION UTILITY
 * =============================================================
 * This utility class converts between Java Transaction objects and JSON strings.
 *
 * Key Concepts:
 *   - Serialization: Converting Java objects → JSON string (for saving to disk)
 *   - Deserialization: Converting JSON string → Java objects (for loading from disk)
 *   - Static methods: All methods are 'static' — no need to create a JsonUtil instance
 *   - Reflection API: Used to set private fields (id, createdAt) that have no setters
 *
 * This is the bridge between Java's object-oriented world and
 * JSON's text-based storage format. Without this, we couldn't save data to files.
 *
 * JSON Format Example:
 * [
 *   {
 *     "id": "550e8400-e29b-41d4-a716-446655440000",
 *     "type": "EXPENSE",
 *     "amount": 45.99,
 *     "description": "Groceries",
 *     "category": "food",
 *     "date": "2026-04-23",
 *     "notes": "Weekly shopping",
 *     "createdAt": 1745387094000
 *   }
 * ]
 */
public class JsonUtil {

    // Date format constant — ISO_LOCAL_DATE formats dates as "yyyy-MM-dd" (e.g., "2026-04-23")
    // 'static final' = constant shared by the entire class, never changes
    // ISO-8601 is the international standard for date representation
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    // ============================================
    // SERIALIZATION — Java Object → JSON
    // ============================================

    /**
     * Converts a single Transaction object into a JSONObject.
     *
     * JSONObject is essentially a Java Map that can be converted to a JSON string.
     * json.put(key, value) adds a key-value pair to the object.
     *
     * The resulting JSON looks like:
     * { "id": "abc123", "type": "EXPENSE", "amount": 45.99, ... }
     *
     * @param transaction The Transaction object to convert
     * @return JSONObject containing all transaction data
     */
    public static JSONObject toJson(Transaction transaction) {
        // Create a new empty JSON object (like an empty {})
        JSONObject json = new JSONObject();

        // Add each field as a key-value pair
        // json.put("key", value) handles type conversion automatically
        json.put("id", transaction.getId());                          // String
        json.put("type", transaction.getType().name());               // Enum → String ("EXPENSE" or "INCOME")
        json.put("amount", transaction.getAmount());                  // double
        json.put("description", transaction.getDescription());        // String
        json.put("category", transaction.getCategory());              // String
        json.put("date", transaction.getDate().format(DATE_FORMAT));  // LocalDate → "2026-04-23"
        json.put("notes", transaction.getNotes());                    // String (can be null)
        json.put("createdAt", transaction.getCreatedAt());            // long (milliseconds)

        return json;
    }

    // ============================================
    // DESERIALIZATION — JSON → Java Object
    // ============================================

    /**
     * Converts a JSONObject back into a Transaction Java object.
     * This is the reverse of toJson() — reads key-value pairs and creates a Transaction.
     *
     * IMPORTANT: Uses Java Reflection to set the 'id' and 'createdAt' fields.
     * Normally these fields are auto-generated in the Transaction constructor,
     * but when loading from a file, we need to restore the ORIGINAL values.
     * Since these fields are private and have no setters, Reflection is the only way.
     *
     * @param json JSONObject to parse
     * @return A fully populated Transaction object
     */
    public static Transaction fromJson(JSONObject json) {
        // Parse the transaction type from the JSON string
        // Transaction.TransactionType.valueOf("EXPENSE") → TransactionType.EXPENSE
        // valueOf() is a built-in enum method that converts a string to the matching constant
        Transaction.TransactionType type = Transaction.TransactionType.valueOf(
            json.getString("type")
        );

        // Extract each field from the JSON object using the appropriate getter method
        double amount = json.getDouble("amount");               // Parse as double
        String description = json.getString("description");     // Parse as String
        String category = json.getString("category");           // Parse as String

        // Parse the date string "2026-04-23" back into a LocalDate object
        LocalDate date = LocalDate.parse(json.getString("date"), DATE_FORMAT);

        // optString() returns a default value (null) if the key doesn't exist
        // This handles older data files that might not have the "notes" field
        String notes = json.optString("notes", null);

        // optLong() returns a default value if the key doesn't exist
        // Fallback: use current time if createdAt is missing from old data
        long createdAt = json.optLong("createdAt", System.currentTimeMillis());

        // Create a new Transaction using the parsed values
        // NOTE: The constructor auto-generates a NEW id and createdAt — we'll fix that below
        Transaction transaction = new Transaction(
            type, amount, description, category, date, notes
        );

        // ============================================
        // JAVA REFLECTION — Restoring Private Fields
        // Reflection allows us to access private fields at runtime
        // This is necessary because Transaction.id has no setter method (it's read-only)
        // ============================================

        // Restore the original 'id' from the JSON (instead of the auto-generated one)
        try {
            // getDeclaredField("id") gets the Field object for the private 'id' field
            java.lang.reflect.Field idField = Transaction.class.getDeclaredField("id");

            // setAccessible(true) bypasses Java's access control — allows writing to a private field
            // Without this, we'd get an IllegalAccessException
            idField.setAccessible(true);

            // Set the field value on the transaction object to the original ID from JSON
            idField.set(transaction, json.getString("id"));
        } catch (Exception e) {
            // If reflection fails (unlikely), the auto-generated ID will be used instead
            // This is a safe fallback — the transaction still works, just with a new ID
        }

        // Restore the original 'createdAt' timestamp from JSON
        try {
            java.lang.reflect.Field createdAtField = Transaction.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);

            // setLong() is used for primitive long fields (not Long objects)
            createdAtField.setLong(transaction, createdAt);
        } catch (Exception e) {
            // Safe fallback — transaction will use the time it was parsed, not the original time
        }

        return transaction;
    }

    // ============================================
    // BATCH OPERATIONS — Convert entire lists
    // ============================================

    /**
     * Converts a List of Transaction objects into a JSON array string.
     * This is the method called by DataManager.saveData() to write to disk.
     *
     * JSONArray is an ordered collection: [obj1, obj2, obj3]
     * toString(2) produces "pretty-printed" JSON with 2-space indentation for readability.
     *
     * @param transactions List of Transaction objects to serialize
     * @return Pretty-printed JSON string representing the entire list
     */
    public static String toJsonArray(List<Transaction> transactions) {
        // Create an empty JSON array (like an empty [])
        JSONArray array = new JSONArray();

        // Convert each Transaction to a JSONObject and add to the array
        for (Transaction t : transactions) {
            array.put(toJson(t));  // toJson(t) returns a JSONObject, put() adds it to the array
        }

        // toString(2) converts to a formatted string with 2-space indentation
        // Without the parameter, it would produce a compact single-line string
        return array.toString(2);
    }

    /**
     * Parses a JSON array string and converts it back into a List of Transaction objects.
     * This is the method called by DataManager.loadData() when reading from disk.
     *
     * Process:
     *   1. Parse the raw string into a JSONArray
     *   2. Iterate through each element (JSONObject) in the array
     *   3. Convert each JSONObject into a Transaction using fromJson()
     *   4. Collect all parsed Transactions into an ArrayList
     *
     * @param json Raw JSON string containing an array of transaction objects
     * @return List of parsed Transaction objects (empty list if parsing fails)
     */
    public static List<Transaction> fromJsonArray(String json) {
        // Create an empty list to hold the parsed transactions
        List<Transaction> transactions = new ArrayList<>();

        try {
            // Parse the raw string into a JSONArray object
            // This validates the JSON syntax and creates a structured representation
            JSONArray array = new JSONArray(json);

            // Iterate through each element in the array by index
            for (int i = 0; i < array.length(); i++) {
                // Get the JSONObject at index i
                JSONObject obj = array.getJSONObject(i);

                // Convert the JSONObject to a Transaction and add to our list
                transactions.add(fromJson(obj));
            }
        } catch (Exception e) {
            // Catches both JSONException (malformed JSON) and other parsing errors
            // Prints error for debugging but returns whatever was parsed successfully
            System.err.println("Error parsing JSON: " + e.getMessage());
        }

        return transactions;
    }
}
