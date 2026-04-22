package com.expensetracker.main;

import com.expensetracker.ui.MainFrame;
import com.expensetracker.data.DataManager;

/**
 * Main entry point for the Expense Tracker application
 * A modern desktop expense tracker with dark/light mode support
 */
public class Main {

    /**
     * Application entry point
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Initialize data manager to load saved transactions
        DataManager.getInstance();

        // Create and show the main application window
        // Using invokeLater ensures thread safety for Swing components
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
