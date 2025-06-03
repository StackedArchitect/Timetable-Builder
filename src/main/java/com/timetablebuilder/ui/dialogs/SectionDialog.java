package com.timetablebuilder.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.timetablebuilder.model.ComponentType;
import com.timetablebuilder.model.Course;
import com.timetablebuilder.model.Instructor;
import com.timetablebuilder.model.Section;

public class SectionDialog extends JDialog {
    private JTextField tfSectionID;
    private JComboBox<Course> comboCourse;
    private JComboBox<ComponentType> comboType;
    private JComboBox<Instructor> comboInstructor;
    private JSpinner spinCapacity;
    private JCheckBox cbRequiresAV;
    private JSpinner spinRequiredComputers;

    private JButton btnSave;
    private JButton btnCancel;

    private Section currentSection;
    private List<Course> availableCourses;
    private List<Instructor> availableInstructors;
    private boolean saved = false;

    // Constructor needs lists of available courses and instructors to populate combos
    public SectionDialog(Frame owner, Section section, List<Course> courses, List<Instructor> instructors) {
        super(owner, (section == null ? "Add Section" : "Edit Section"), true);
        this.currentSection = section;
        this.availableCourses = courses;
        this.availableInstructors = instructors;

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

        // Section ID
        cs.gridx = 0; cs.gridy = gridy; panel.add(new JLabel("Section ID:"), cs);
        tfSectionID = new JTextField(20);
        cs.gridx = 1; cs.gridy = gridy++; panel.add(tfSectionID, cs);

        // Course
        cs.gridx = 0; cs.gridy = gridy; panel.add(new JLabel("Course:"), cs);
        comboCourse = new JComboBox<>(new Vector<>(availableCourses)); // Use Vector for JComboBox model
        // TODO: Add renderer for better Course display in combo box if needed
        cs.gridx = 1; cs.gridy = gridy++; panel.add(comboCourse, cs);

        // Type (Lecture/Lab/Tutorial)
        cs.gridx = 0; cs.gridy = gridy; panel.add(new JLabel("Component Type:"), cs);
        comboType = new JComboBox<>(ComponentType.values());
        cs.gridx = 1; cs.gridy = gridy++; panel.add(comboType, cs);

        // Instructor
        cs.gridx = 0; cs.gridy = gridy; panel.add(new JLabel("Instructor:"), cs);
        // Add a "None" option potentially, or handle null instructors
        comboInstructor = new JComboBox<>(new Vector<>(availableInstructors)); 
        // TODO: Add renderer for better Instructor display
        cs.gridx = 1; cs.gridy = gridy++; panel.add(comboInstructor, cs);

        // Capacity
        cs.gridx = 0; cs.gridy = gridy; panel.add(new JLabel("Section Capacity:"), cs);
        spinCapacity = new JSpinner(new SpinnerNumberModel(30, 1, 500, 1)); // Default 30, min 1
        cs.gridx = 1; cs.gridy = gridy++; panel.add(spinCapacity, cs);

        // Requires AV
        cbRequiresAV = new JCheckBox("Requires Audio/Video");
        cs.gridx = 0; cs.gridy = gridy; cs.gridwidth = 2; panel.add(cbRequiresAV, cs); cs.gridwidth = 1;
        gridy++;

        // Required Computers
        cs.gridx = 0; cs.gridy = gridy; panel.add(new JLabel("Required Computers:"), cs);
        spinRequiredComputers = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1)); // Default 0, min 0
        cs.gridx = 1; cs.gridy = gridy++; panel.add(spinRequiredComputers, cs);

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
        if (currentSection != null) {
            tfSectionID.setText(currentSection.getSectionID());
            tfSectionID.setEditable(false); // Don't edit Section ID typically

            comboCourse.setSelectedItem(currentSection.getParentCourse());
            comboType.setSelectedItem(currentSection.getType());
            comboInstructor.setSelectedItem(currentSection.getInstructor());
            spinCapacity.setValue(currentSection.getSectionCapacity());
            cbRequiresAV.setSelected(currentSection.requiresAV());
            spinRequiredComputers.setValue(currentSection.getRequiredComputers());

             // Disable course/type editing if needed (often fixed once created)
             // comboCourse.setEnabled(false);
             // comboType.setEnabled(false);
        }
    }

    private void onSave() {
        String sectionId = tfSectionID.getText().trim();
        Course selectedCourse = (Course) comboCourse.getSelectedItem();
        ComponentType selectedType = (ComponentType) comboType.getSelectedItem();
        Instructor selectedInstructor = (Instructor) comboInstructor.getSelectedItem();
        int capacity = (Integer) spinCapacity.getValue();
        boolean requiresAV = cbRequiresAV.isSelected();
        int requiredComputers = (Integer) spinRequiredComputers.getValue();

        // Validation
        if (sectionId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Section ID cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedCourse == null) {
             JOptionPane.showMessageDialog(this, "Please select a Course.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Instructor can potentially be null/unassigned

        // TODO: Validate Section ID uniqueness if creating new

        if (currentSection == null) {
            // Create new Section - Use IDs and pass null for preferredTimeSlot
            String parentCourseCode = (selectedCourse != null) ? selectedCourse.getCourseCode() : null;
            String instructorId = (selectedInstructor != null) ? selectedInstructor.getInstructorID() : null; // Instructor can be null
            
            currentSection = new Section(sectionId, parentCourseCode, instructorId, selectedType,
                                       capacity, null, requiresAV, requiredComputers); // Pass null for TimeSlot
        } else {
            // Update existing Section (some fields might be fixed)
            // Parent course and type are generally fixed after creation
            // currentSection.linkParentCourse(selectedCourse); // Use linking method if needed
            currentSection.linkInstructor(selectedInstructor); // Use linking method
            currentSection.setSectionCapacity(capacity);
            currentSection.setRequiresAV(requiresAV);
            currentSection.setRequiredComputers(requiredComputers);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public Section getSection() {
        return currentSection;
    }
} 