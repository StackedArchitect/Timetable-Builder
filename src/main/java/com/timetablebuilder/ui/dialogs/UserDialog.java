package com.timetablebuilder.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets; // For hashing new passwords
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox; // May need for ID validation
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.timetablebuilder.model.Instructor;
import com.timetablebuilder.model.Student;
import com.timetablebuilder.model.User;
import com.timetablebuilder.model.UserRole;
import com.timetablebuilder.util.PasswordUtils;

public class UserDialog extends JDialog {
    private JTextField tfUsername;
    private JPasswordField pfPassword; // For entering new/reset password
    private JPasswordField pfConfirmPassword;
    private JComboBox<UserRole> comboRole;
    private JTextField tfAssociatedID; // Instructor ID, Student ID, or Admin ID

    private JButton btnSave;
    private JButton btnCancel;

    private User currentUser;
    private boolean saved = false;
    private boolean isNewUser; // Track if adding or editing

    // Added fields to hold lists for validation
    private List<Instructor> instructorList;
    private List<Student> studentList;
    private List<User> userList; // Added user list

    // Updated constructor to accept lists
    public UserDialog(Frame owner, User user, List<Instructor> instructors, List<Student> students, List<User> users) {
        super(owner, (user == null ? "Add User" : "Edit User"), true);
        this.currentUser = user;
        this.isNewUser = (user == null);
        this.instructorList = instructors;
        this.studentList = students;
        this.userList = users; // Store user list

        initComponents();
        populateFields();

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(5, 5, 5, 5);
        int gridy = 0;

        // Username
        cs.gridx = 0; cs.gridy = gridy; panel.add(new JLabel("Username:"), cs);
        tfUsername = new JTextField(20);
        cs.gridx = 1; cs.gridy = gridy++; panel.add(tfUsername, cs);

        // Password (only required/shown for new user or password reset)
        JLabel passLabel = new JLabel(isNewUser ? "Password:" : "New Password (Optional):");
        cs.gridx = 0; cs.gridy = gridy; panel.add(passLabel, cs);
        pfPassword = new JPasswordField(20);
        cs.gridx = 1; cs.gridy = gridy++; panel.add(pfPassword, cs);

        JLabel confirmPassLabel = new JLabel(isNewUser ? "Confirm Password:" : "Confirm New Password:");
        cs.gridx = 0; cs.gridy = gridy; panel.add(confirmPassLabel, cs);
        pfConfirmPassword = new JPasswordField(20);
        cs.gridx = 1; cs.gridy = gridy++; panel.add(pfConfirmPassword, cs);

        // Role
        cs.gridx = 0; cs.gridy = gridy; panel.add(new JLabel("Role:"), cs);
        comboRole = new JComboBox<>(UserRole.values());
        cs.gridx = 1; cs.gridy = gridy++; panel.add(comboRole, cs);

        // Associated ID (Instructor ID, Student ID, or unique ID for Admin)
        cs.gridx = 0; cs.gridy = gridy; panel.add(new JLabel("Associated ID:"), cs);
        tfAssociatedID = new JTextField(20);
        cs.gridx = 1; cs.gridy = gridy++; panel.add(tfAssociatedID, cs);

        // Buttons
        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
    }

    private void populateFields() {
        if (currentUser != null) {
            tfUsername.setText(currentUser.getUsername());
            tfUsername.setEditable(false); // Username usually not editable
            comboRole.setSelectedItem(currentUser.getRole());
            comboRole.setEnabled(false); // Role usually not editable after creation
            tfAssociatedID.setText(currentUser.getUserID());
            tfAssociatedID.setEditable(false); // Associated ID usually fixed
        }
    }

    private void onSave() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());
        String confirmPassword = new String(pfConfirmPassword.getPassword());
        UserRole role = (UserRole) comboRole.getSelectedItem();
        String associatedId = tfAssociatedID.getText().trim();

        // Basic Validation (unchanged)
        if (username.isEmpty()) { showError("Username cannot be empty."); return; }
        if (isNewUser && password.isEmpty()) { showError("Password cannot be empty for a new user."); return; }
        if (!password.isEmpty() && !password.equals(confirmPassword)) { showError("Passwords do not match."); return; }
        if (role == null) { showError("Please select a role."); return; }
        if (associatedId.isEmpty()) { showError("Associated ID cannot be empty."); return; }

        // --- Associated ID Validation --- 
        boolean idValid = true;
        if (role == UserRole.TEACHER) {
            if (instructorList == null || instructorList.stream().noneMatch(i -> i.getInstructorID().equals(associatedId))) {
                 idValid = false;
                 showError("No Instructor found with Associated ID: " + associatedId);
            }
        } else if (role == UserRole.STUDENT) {
             if (studentList == null || studentList.stream().noneMatch(s -> s.getStudentID().equals(associatedId))) {
                 idValid = false;
                 showError("No Student found with Associated ID: " + associatedId);
            }
        } // No specific ID validation needed for ADMIN, assuming associatedId is just a unique identifier
        
        if (!idValid) {
            return; // Stop saving if ID validation failed
        }
        // -------------------------------

        // --- Username Uniqueness Validation (for new users) ---
        if (isNewUser) {
             if (userList != null && userList.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username))) {
                 showError("Username '" + username + "' already exists. Please choose a different username.");
                 return; // Stop saving
             }
        }
        // ---------------------------------------------------

        String hashedPassword = null;
        if (isNewUser) {
            hashedPassword = PasswordUtils.hashPassword(password);
        } else if (!password.isEmpty()) {
            hashedPassword = PasswordUtils.hashPassword(password);
        } else {
            hashedPassword = currentUser.getHashedPassword();
        }

        if (hashedPassword == null) { showError("Error processing password."); return; }

        currentUser = new User(username, hashedPassword, role, associatedId);
        saved = true;
        dispose();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isSaved() {
        return saved;
    }

    public User getUser() {
        return currentUser;
    }
} 