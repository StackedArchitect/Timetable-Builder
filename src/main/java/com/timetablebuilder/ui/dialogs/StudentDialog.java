package com.timetablebuilder.ui.dialogs;

import com.timetablebuilder.model.Student;

import javax.swing.*;
import java.awt.*;

public class StudentDialog extends JDialog {
    private JTextField tfStudentID;
    private JTextField tfName;

    private JButton btnSave;
    private JButton btnCancel;

    private Student currentStudent;
    private boolean saved = false;

    public StudentDialog(Frame owner, Student student) {
        super(owner, (student == null ? "Add Student" : "Edit Student"), true);
        this.currentStudent = student;

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(5, 5, 5, 5);

        // Student ID
        cs.gridx = 0; cs.gridy = 0; panel.add(new JLabel("Student ID:"), cs);
        tfStudentID = new JTextField(20);
        cs.gridx = 1; cs.gridy = 0; panel.add(tfStudentID, cs);

        // Name
        cs.gridx = 0; cs.gridy = 1; panel.add(new JLabel("Name:"), cs);
        tfName = new JTextField(20);
        cs.gridx = 1; cs.gridy = 1; panel.add(tfName, cs);

        // Populate fields if editing
        if (currentStudent != null) {
            tfStudentID.setText(currentStudent.getStudentID());
            tfStudentID.setEditable(false); // Don't allow editing ID
            tfName.setText(currentStudent.getName());
        }

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

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void onSave() {
        String id = tfStudentID.getText().trim();
        String name = tfName.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Student ID and Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // TODO: Add validation for ID uniqueness if creating new

        if (currentStudent == null) {
            currentStudent = new Student(id, name);
        } else {
            // Only name is editable usually
            currentStudent.setName(name);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public Student getStudent() {
        return currentStudent;
    }
} 