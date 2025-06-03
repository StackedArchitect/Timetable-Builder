package com.timetablebuilder;

import com.timetablebuilder.service.AuthService;
import com.timetablebuilder.ui.LoginDialog;
import com.timetablebuilder.ui.MainFrame; // Need this if LoginDialog shows it directly
import com.timetablebuilder.model.User;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting Timetable Builder Application...");

        // Initialize services (using dummy data in AuthService for now)
        // DataRepository dataRepository = new DataRepository(); // Not needed yet for login
        AuthService authService = new AuthService();
        // TimetableService timetableService = new TimetableService(dataRepository); // Needed later

        // Show Login Dialog on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null, authService);
            loginDialog.setVisible(true);

            // After LoginDialog is closed (disposed), check the result
            User authenticatedUser = loginDialog.getAuthenticatedUser();

            if (authenticatedUser != null) {
                // If login was successful, LoginDialog already created MainFrame
                System.out.println("Login successful. MainFrame should be visible.");
                // The application continues running because MainFrame is open.
            } else {
                // Login failed or was cancelled
                System.out.println("Login failed or cancelled. Exiting application.");
                System.exit(0); // Exit if login doesn't succeed
            }
        });

        System.out.println("Main method finished setup. Handing over to EDT.");
    }
} 