package com.timetablebuilder.ui.dialogs;

import com.timetablebuilder.model.Course;
import java.awt.*;
import java.util.HashSet;
import java.util.List; // Needed for conflicting courses
import javax.swing.*;

// Dialog for Adding/Editing Courses
public class CourseDialog extends JDialog {
    private JTextField tfCourseCode;
    private JTextField tfCourseName;
    private JList<String> listConflictingCourses;
    private DefaultListModel<String> listModelAllCourses; // Contains all possible courses
    private JList<String> listSelectedConflictingCourses;
    private DefaultListModel<String> listModelSelectedConflicts; // Contains selected conflicts

    private JButton btnAddConflict, btnRemoveConflict;
    private JButton btnSave;
    private JButton btnCancel;

    private Course currentCourse;
    private List<Course> allCourses; // Needed to populate the list
    private boolean saved = false;

    public CourseDialog(Frame owner, Course course, List<Course> allCourses) {
        super(owner, (course == null ? "Add Course" : "Edit Course"), true);
        this.currentCourse = course;
        this.allCourses = allCourses;

        initComponents();
        populateFields();

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Top Panel: Course Info ---
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(2, 2, 2, 2);

        cs.gridx = 0; cs.gridy = 0; infoPanel.add(new JLabel("Course Code:"), cs);
        tfCourseCode = new JTextField(15);
        cs.gridx = 1; cs.gridy = 0; infoPanel.add(tfCourseCode, cs);

        cs.gridx = 0; cs.gridy = 1; infoPanel.add(new JLabel("Course Name:"), cs);
        tfCourseName = new JTextField(30);
        cs.gridx = 1; cs.gridy = 1; infoPanel.add(tfCourseName, cs);

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // --- Center Panel: Conflict Management ---
        JPanel conflictPanel = new JPanel(new GridBagLayout());
        conflictPanel.setBorder(BorderFactory.createTitledBorder("Conflicting Courses"));
        GridBagConstraints ccs = new GridBagConstraints();
        ccs.insets = new Insets(5, 5, 5, 5);
        ccs.fill = GridBagConstraints.BOTH;
        ccs.weightx = 1.0; ccs.weighty = 1.0;

        // Available Courses List
        ccs.gridx = 0; ccs.gridy = 1; conflictPanel.add(new JLabel("Available Courses:"), ccs);
        listModelAllCourses = new DefaultListModel<>();
        listConflictingCourses = new JList<>(listModelAllCourses);
        listConflictingCourses.setVisibleRowCount(8);
        ccs.gridx = 0; ccs.gridy = 2; conflictPanel.add(new JScrollPane(listConflictingCourses), ccs);

        // Add/Remove Buttons
        JPanel buttonMidPanel = new JPanel(new GridBagLayout());
        GridBagConstraints bcs = new GridBagConstraints();
        bcs.fill = GridBagConstraints.HORIZONTAL;
        bcs.insets = new Insets(5, 0, 5, 0);
        btnAddConflict = new JButton(">>");
        btnRemoveConflict = new JButton("<<");
        bcs.gridy = 0; buttonMidPanel.add(btnAddConflict, bcs);
        bcs.gridy = 1; buttonMidPanel.add(btnRemoveConflict, bcs);
        ccs.gridx = 1; ccs.gridy = 2; ccs.fill = GridBagConstraints.VERTICAL; ccs.weightx = 0; ccs.weighty = 0;
        conflictPanel.add(buttonMidPanel, ccs);
        ccs.fill = GridBagConstraints.BOTH; ccs.weightx = 1.0; ccs.weighty = 1.0; // Reset constraints

        // Selected Conflicting Courses List
        ccs.gridx = 2; ccs.gridy = 1; conflictPanel.add(new JLabel("Selected Conflicts:"), ccs);
        listModelSelectedConflicts = new DefaultListModel<>();
        listSelectedConflictingCourses = new JList<>(listModelSelectedConflicts);
        listSelectedConflictingCourses.setVisibleRowCount(8);
        ccs.gridx = 2; ccs.gridy = 2; conflictPanel.add(new JScrollPane(listSelectedConflictingCourses), ccs);

        mainPanel.add(conflictPanel, BorderLayout.CENTER);

        // --- Bottom Panel: Save/Cancel ---
        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- Add Actions ---
        btnAddConflict.addActionListener(e -> addConflict());
        btnRemoveConflict.addActionListener(e -> removeConflict());
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        getContentPane().add(mainPanel);
    }

    private void populateFields() {
        // Populate Available Courses List (excluding the current course if editing)
        listModelAllCourses.clear();
        String currentCode = (currentCourse != null) ? currentCourse.getCourseCode() : "";
        for (Course c : allCourses) {
            if (!c.getCourseCode().equals(currentCode)) {
                listModelAllCourses.addElement(c.getCourseCode() + " (" + c.getCourseName() + ")");
            }
        }

        // Populate fields if editing an existing course
        listModelSelectedConflicts.clear();
        if (currentCourse != null) {
            tfCourseCode.setText(currentCourse.getCourseCode());
            tfCourseCode.setEditable(false); // Don't allow editing code
            tfCourseName.setText(currentCourse.getCourseName());

            // Populate selected conflicts
            for (String conflictingCode : currentCourse.getConflictingCourses()) {
                // Find the full name to display
                String displayName = conflictingCode;
                for(Course c : allCourses) {
                    if (c.getCourseCode().equals(conflictingCode)) {
                        displayName = c.getCourseCode() + " (" + c.getCourseName() + ")";
                        break;
                    }
                }
                listModelSelectedConflicts.addElement(displayName);
                // Also remove it from the available list
                listModelAllCourses.removeElement(displayName);
            }
        }
    }

    private void addConflict() {
        List<String> selected = listConflictingCourses.getSelectedValuesList();
        for (String item : selected) {
            listModelSelectedConflicts.addElement(item);
            listModelAllCourses.removeElement(item);
        }
    }

    private void removeConflict() {
        List<String> selected = listSelectedConflictingCourses.getSelectedValuesList();
        for (String item : selected) {
            listModelAllCourses.addElement(item);
            listModelSelectedConflicts.removeElement(item);
        }
        // Sort the available list? Optional.
        // Object[] elements = listModelAllCourses.toArray();
        // Arrays.sort(elements);
        // listModelAllCourses.clear();
        // for(Object o : elements) listModelAllCourses.addElement((String)o);
    }

    private void onSave() {
        String code = tfCourseCode.getText().trim();
        String name = tfCourseName.getText().trim();

        if (code.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course Code and Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // TODO: Validate code uniqueness if creating new

        // Collect selected conflict codes (extract code from display name)
        HashSet<String> selectedConflictCodes = new HashSet<>();
        for (int i = 0; i < listModelSelectedConflicts.size(); i++) {
            String displayName = listModelSelectedConflicts.getElementAt(i);
            // Use indexOf instead of split to avoid regex issues
            int index = displayName.indexOf(" (");
            String conflictCode = (index != -1) ? displayName.substring(0, index) : displayName;
            selectedConflictCodes.add(conflictCode);
        }

        if (currentCourse == null) {
            currentCourse = new Course(code, name);
            // Add conflicts one by one
            for (String conflictCode : selectedConflictCodes) {
                 currentCourse.addConflictingCourse(conflictCode);
            }
        } else {
            currentCourse.setCourseName(name); // Code not editable
            // Clear existing conflicts and add new ones
            currentCourse.getConflictingCourses().clear(); // Assuming getter returns the modifiable list
            for (String conflictCode : selectedConflictCodes) {
                 currentCourse.addConflictingCourse(conflictCode);
            }
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public Course getCourse() {
        return currentCourse;
    }
} 