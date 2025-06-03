package com.timetablebuilder.ui.dialogs;

import com.timetablebuilder.model.Classroom;
import java.awt.*;
import javax.swing.*;

// Dialog for Adding/Editing Classrooms
public class ClassroomDialog extends JDialog {
    private JTextField tfClassroomID;
    private JSpinner spinCapacity;
    private JCheckBox cbHasAV;
    private JSpinner spinNumComputers;
    private JCheckBox cbIsLab;

    private JButton btnSave;
    private JButton btnCancel;

    private Classroom currentClassroom;
    private boolean saved = false;

    public ClassroomDialog(Frame owner, Classroom classroom) {
        super(owner, (classroom == null ? "Add Classroom" : "Edit Classroom"), true);
        this.currentClassroom = classroom;

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(5, 5, 5, 5);

        // Classroom ID
        cs.gridx = 0; cs.gridy = 0; panel.add(new JLabel("Classroom ID:"), cs);
        tfClassroomID = new JTextField(20);
        cs.gridx = 1; cs.gridy = 0; panel.add(tfClassroomID, cs);

        // Capacity
        cs.gridx = 0; cs.gridy = 1; panel.add(new JLabel("Capacity:"), cs);
        spinCapacity = new JSpinner(new SpinnerNumberModel(50, 1, 1000, 1)); // Default 50, min 1, max 1000
        cs.gridx = 1; cs.gridy = 1; panel.add(spinCapacity, cs);

        // Resources
        cbHasAV = new JCheckBox("Has Audio/Video");
        cs.gridx = 0; cs.gridy = 2; cs.gridwidth=2; panel.add(cbHasAV, cs); cs.gridwidth=1; // Span checkbox

        cs.gridx = 0; cs.gridy = 3; panel.add(new JLabel("Number of Computers:"), cs);
        spinNumComputers = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1)); // Default 0, min 0, max 200
        cs.gridx = 1; cs.gridy = 3; panel.add(spinNumComputers, cs);

        cbIsLab = new JCheckBox("Is Lab Room");
        cs.gridx = 0; cs.gridy = 4; cs.gridwidth=2; panel.add(cbIsLab, cs); cs.gridwidth=1; // Span checkbox

        // Populate fields if editing
        if (currentClassroom != null) {
            tfClassroomID.setText(currentClassroom.getClassroomID());
            tfClassroomID.setEditable(false); // Don't allow editing ID
            spinCapacity.setValue(currentClassroom.getCapacity());
            cbHasAV.setSelected(currentClassroom.hasAudioVideo());
            spinNumComputers.setValue(currentClassroom.getNumComputers());
            cbIsLab.setSelected(currentClassroom.isLab());
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
        // Basic Validation
        String id = tfClassroomID.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Classroom ID cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // TODO: Add validation for ID uniqueness if creating new (needs DataRepository check)

        int capacity = (Integer) spinCapacity.getValue();
        int numComputers = (Integer) spinNumComputers.getValue();
        boolean hasAV = cbHasAV.isSelected();
        boolean isLab = cbIsLab.isSelected();

        if (currentClassroom == null) {
            currentClassroom = new Classroom(id, capacity, hasAV, numComputers, isLab);
        } else {
            currentClassroom.setCapacity(capacity);
            currentClassroom.setHasAudioVideo(hasAV);
            currentClassroom.setNumComputers(numComputers);
            currentClassroom.setIsLab(isLab);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public Classroom getClassroom() {
        return currentClassroom;
    }
} 