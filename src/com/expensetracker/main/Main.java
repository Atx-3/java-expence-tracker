// Package declaration — organizes this class under the 'main' sub-package
// Java packages group related classes and prevent naming conflicts
package com.expensetracker.main;

// Importing the MainFrame class from the UI package — this is our main application window
import com.expensetracker.ui.MainFrame;

// Importing the DataManager class from the data package — this handles all data operations
import com.expensetracker.data.DataManager;

/**
 * Main.java — APPLICATION ENTRY POINT
 * ====================================
 * This is the first class that executes when the program starts.
 * Responsibilities:
 *   1. Initialize the data layer (DataManager singleton)
 *   2. Launch the Swing GUI on the Event Dispatch Thread (EDT)
 *
 * Design Pattern Used: Singleton (DataManager is initialized once here)
 * Threading: Swing's invokeLater ensures thread-safe GUI creation
 */
public class Main {

    /**
     * The main() method — Java runtime looks for this exact signature to start the program.
     * It is 'public' so the JVM can access it, 'static' so no object is needed,
     * and 'void' because it doesn't return anything.
     *
     * @param args Command line arguments passed when running the program (not used here)
     */
    public static void main(String[] args) {

        // Step 1: Initialize the DataManager singleton
        // This loads any previously saved transactions from the JSON file on disk
        // Using .getInstance() ensures only ONE DataManager exists throughout the app (Singleton Pattern)
        DataManager.getInstance();

        // Step 2: Create and display the main application window
        // SwingUtilities.invokeLater() is CRITICAL for Swing applications
        // It schedules the GUI creation on the Event Dispatch Thread (EDT)
        // This is necessary because Swing components are NOT thread-safe —
        // they must only be created and modified on the EDT to avoid race conditions
        javax.swing.SwingUtilities.invokeLater(() -> {

            // Create a new instance of our main window (contains navigation + all panels)
            MainFrame frame = new MainFrame();

            // Make the window visible to the user — until this call, nothing appears on screen
            frame.setVisible(true);
        });
    }
}
