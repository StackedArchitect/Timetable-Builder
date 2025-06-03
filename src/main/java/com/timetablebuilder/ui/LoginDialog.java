package com.timetablebuilder.ui;

import com.timetablebuilder.model.User;
import com.timetablebuilder.service.AuthService;
// Import MainFrame once it exists
import com.timetablebuilder.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginDialog extends JDialog {

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JLabel lbUsername;
    private JLabel lbPassword;
    private JButton btnLogin;
    private JButton btnCancel;

    private AuthService authService;
    private User authenticatedUser = null;

    public LoginDialog(Frame parent, AuthService authService) {
        super(parent, "Login", true); // true makes it modal
        this.authService = authService;

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(2, 2, 2, 2); // Add some padding

        lbUsername = new JLabel("Username: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        tfUsername = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        lbPassword = new JLabel("Password: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        pfPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pfPassword, cs);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnLogin = new JButton("Login");
        btnCancel = new JButton("Cancel");

        // --- Action Listeners ---
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = tfUsername.getText().trim();
                String password = new String(pfPassword.getPassword());

                // Call the authentication service
                authenticatedUser = authService.authenticate(username, password);

                if (authenticatedUser != null) {
                    // Don't show message here, MainFrame will show welcome
                    dispose(); // Close the login dialog first

                    // --- Create and show the MainFrame --- 
                    // Ensure this runs on the Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> {
                         MainFrame mainFrame = new MainFrame(authenticatedUser);
                         mainFrame.setVisible(true);
                     });
                    // ----------------------------------------

                } else {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Invalid username or password",
                            "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                    // reset username and password
                    tfUsername.setText("");
                    pfPassword.setText("");
                    authenticatedUser = null;
                }
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticatedUser = null; // Ensure user is null on cancel
                dispose();
                // Let the Main method handle exiting if login is cancelled
            }
        });

        // Set Enter key press to trigger login button
        getRootPane().setDefaultButton(btnLogin);

        // --- Button Panel ---
        JPanel bp = new JPanel();
        bp.add(btnLogin);
        bp.add(btnCancel);

        // --- Layout --- 
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    /**
     * Returns the authenticated user after the dialog is closed.
     * Can be null if login failed or was cancelled.
     * @return The authenticated User object, or null.
     */
    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}
