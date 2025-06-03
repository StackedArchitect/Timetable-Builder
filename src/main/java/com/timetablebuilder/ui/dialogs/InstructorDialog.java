package com.timetablebuilder.ui.dialogs;

import com.timetablebuilder.model.Instructor;
import java.awt.*;
import javax.swing.*;

// Dialog for Adding/Editing Instructors
public class InstructorDialog extends JDialog {
    private JTextField tfInstructorID;
    private JTextField tfName;
    private JButton btnSave;
    private JButton btnCancel;
    private Instructor currentInstructor;
    private boolean saved = false;

    public InstructorDialog(Frame owner, Instructor instructor) {
        super(owner, (instructor == null ? "Add Instructor" : "Edit Instructor"), true);
        this.currentInstructor = instructor;
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(5, 5, 5, 5);
        cs.gridx = 0; cs.gridy = 0; panel.add(new JLabel("Instructor ID:"), cs);
        tfInstructorID = new JTextField(20);
        cs.gridx = 1; cs.gridy = 0; panel.add(tfInstructorID, cs);
        cs.gridx = 0; cs.gridy = 1; panel.add(new JLabel("Name:"), cs);
        tfName = new JTextField(20);
        cs.gridx = 1; cs.gridy = 1; panel.add(tfName, cs);
        if (currentInstructor != null) {
            tfInstructorID.setText(currentInstructor.getInstructorID());
            tfInstructorID.setEditable(false);
            tfName.setText(currentInstructor.getName());
        }
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
        String id = tfInstructorID.getText().trim();
        String name = tfName.getText().trim();
        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID and Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (currentInstructor == null) {
            currentInstructor = new Instructor(id, name);
        } else {
            currentInstructor.setName(name);
        }
        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public Instructor getInstructor() {
        return currentInstructor;
    }
} 