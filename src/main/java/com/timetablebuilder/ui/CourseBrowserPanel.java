package com.timetablebuilder.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.timetablebuilder.model.Course;
import com.timetablebuilder.model.Section;
import com.timetablebuilder.model.Student;
import com.timetablebuilder.service.PersistenceService;
import com.timetablebuilder.service.TimetableService;
import com.timetablebuilder.ui.model.CourseTableModel;
import com.timetablebuilder.ui.model.SectionTableModel;

// Placeholder Panel for Student Course Browser/Enrollment Tab
public class CourseBrowserPanel extends JPanel {

    private Student currentStudent;
    private PersistenceService persistenceService;
    private TimetableService timetableService;

    private JTable courseTable;
    private CourseTableModel courseTableModel;

    private JTable sectionTable;
    private SectionTableModel sectionTableModel;

    private JButton btnEnroll;
    private JButton btnDrop;
    private JButton btnRefreshSchedule;

    public CourseBrowserPanel(Student student, PersistenceService persistenceService, TimetableService timetableService) {
        super(new BorderLayout(10, 10)); // Add gaps
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (student == null || persistenceService == null || timetableService == null) {
            add(new JLabel("Error: Student data or required services not available."), BorderLayout.CENTER);
            return;
        }

        this.currentStudent = student;
        this.persistenceService = persistenceService;
        this.timetableService = timetableService;

        // --- Left Panel: Course List ---
        JPanel coursePanel = createCoursePanel();
        
        // --- Right Panel: Section List for Selected Course ---
        JPanel sectionPanel = createSectionPanel();
        
        // --- Split Pane to combine them ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, coursePanel, sectionPanel);
        splitPane.setDividerLocation(300); // Initial divider location
        add(splitPane, BorderLayout.CENTER);

        // --- Bottom Panel: Actions ---
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);

        // Initial population
        refreshCourseList();
        // Section list initially empty, populated on course selection
        System.out.println("CourseBrowserPanel initialized for: " + currentStudent.getName());
    }
    
    private JPanel createCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Available Courses"));
        
        courseTableModel = new CourseTableModel();
        courseTable = new JTable(courseTableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setAutoCreateRowSorter(true);
        
        // Add listener to update sections when a course is selected
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshSectionListForSelectedCourse();
            }
        });
        
        panel.add(new JScrollPane(courseTable), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createSectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Sections for Selected Course"));
        
        sectionTableModel = new SectionTableModel(); // Use existing model for now
        sectionTable = new JTable(sectionTableModel);
        sectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // TODO: Maybe customize columns/renderer later (e.g., show enrollment status/button)
        
        panel.add(new JScrollPane(sectionTable), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createActionPanel() {
         JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         btnEnroll = new JButton("Enroll in Selected Section");
         btnDrop = new JButton("Drop Selected Section");
         btnRefreshSchedule = new JButton("Refresh My Schedule");
         
         btnEnroll.addActionListener(e -> enrollSelectedSection());
         btnDrop.addActionListener(e -> dropSelectedSection());
         btnRefreshSchedule.addActionListener(e -> refreshStudentSchedule());
         
         panel.add(btnEnroll);
         panel.add(btnDrop);
         panel.add(btnRefreshSchedule);
         return panel;
    }

    private void refreshCourseList() {
        System.out.println("Refreshing course list...");
        List<Course> courses = persistenceService.getCourses();
        courseTableModel.setCourses(courses != null ? courses : new ArrayList<>());
        System.out.println("Course list refreshed with " + courseTableModel.getRowCount() + " courses.");
        // Clear section table as no course is selected initially
        sectionTableModel.setSections(new ArrayList<>()); 
    }
    
    private void refreshSectionListForSelectedCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow != -1) {
             int modelRow = courseTable.convertRowIndexToModel(selectedRow);
             Course selectedCourse = courseTableModel.getCourseAt(modelRow);
             if (selectedCourse != null) {
                  System.out.println("Selected course: " + selectedCourse.getCourseCode() + ". Refreshing section list...");
                  List<Section> allSections = persistenceService.getSections();
                  List<Section> courseSections = allSections.stream()
                        .filter(s -> s.getParentCourse() != null && s.getParentCourse().equals(selectedCourse))
                        .collect(Collectors.toList());
                  sectionTableModel.setSections(courseSections);
                   System.out.println("Section list refreshed with " + courseSections.size() + " sections for " + selectedCourse.getCourseCode());
             } else {
                  sectionTableModel.setSections(new ArrayList<>()); // Clear if selection invalid
             }
        } else {
            System.out.println("No course selected, clearing section list.");
            sectionTableModel.setSections(new ArrayList<>()); // Clear section table
        }
    }

    private void enrollSelectedSection() {
        int selectedSectionRow = sectionTable.getSelectedRow();
        if (selectedSectionRow == -1) {
             JOptionPane.showMessageDialog(this, "Please select a section to enroll in.", "Enrollment Error", JOptionPane.WARNING_MESSAGE);
             return;
        }
        int modelRow = sectionTable.convertRowIndexToModel(selectedSectionRow);
        Section sectionToEnroll = sectionTableModel.getSectionAt(modelRow);
        
        if (sectionToEnroll == null) {
            System.err.println("Error: Could not get Section object from table model.");
            JOptionPane.showMessageDialog(this, "An error occurred retrieving section details.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

        System.out.println("Attempting enrollment for Student " + currentStudent.getStudentID() + 
                           " in Section " + sectionToEnroll.getSectionID());

        // Perform checks using TimetableService
        List<String> conflicts = timetableService.checkStudentEnrollmentConflicts(currentStudent, sectionToEnroll);

        if (conflicts.isEmpty()) {
            // No conflicts, proceed with enrollment
            try {
                // Update model objects (ideally this logic could be in a service too)
                boolean sectionUpdated = sectionToEnroll.addStudent(currentStudent);
                currentStudent.enrollInSection(sectionToEnroll);
                
                if (sectionUpdated) { // Check if section actually added the student (e.g., wasn't full)
                     System.out.println("Enrollment successful. Saving data...");
                     persistenceService.saveAllData(); // Save changes
                     JOptionPane.showMessageDialog(this, 
                         "Successfully enrolled in section: " + sectionToEnroll.getSectionID(), 
                         "Enrollment Successful", JOptionPane.INFORMATION_MESSAGE);
                     
                     // Refresh the section list (might show updated capacity/status later)
                     refreshSectionListForSelectedCourse(); 
                     
                     // Refresh the schedule panel automatically
                     refreshStudentSchedule();
                } else {
                    // This might happen if capacity check failed inside addStudent despite passing service check
                     System.err.println("Enrollment failed: Section reported student not added (check capacity). Section: " + sectionToEnroll.getSectionID());
                     JOptionPane.showMessageDialog(this, 
                        "Enrollment failed. The section might be full.", 
                        "Enrollment Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                 System.err.println("Exception during enrollment update: " + ex.getMessage());
                 ex.printStackTrace();
                 JOptionPane.showMessageDialog(this, 
                        "An unexpected error occurred during enrollment.", 
                        "Enrollment Error", JOptionPane.ERROR_MESSAGE);
                 // Optional: Consider reverting model changes if partially done?
            }
        } else {
            // Conflicts found, show them
            System.out.println("Enrollment failed. Conflicts found: " + conflicts.size());
            JOptionPane.showMessageDialog(this,
                "Could not enroll in section due to conflicts:\n" + String.join("\n", conflicts),
                "Enrollment Conflict",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void dropSelectedSection() {
        int selectedSectionRow = sectionTable.getSelectedRow();
        if (selectedSectionRow == -1) {
             JOptionPane.showMessageDialog(this, "Please select a section to drop.", "Drop Error", JOptionPane.WARNING_MESSAGE);
             return;
        }
        int modelRow = sectionTable.convertRowIndexToModel(selectedSectionRow);
        Section sectionToDrop = sectionTableModel.getSectionAt(modelRow);
                           
        if (sectionToDrop == null) {
             System.err.println("Error: Could not get Section object from table model for dropping.");
             JOptionPane.showMessageDialog(this, "An error occurred retrieving section details.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        
        System.out.println("Attempting drop for Student " + currentStudent.getStudentID() + 
                           " from Section " + sectionToDrop.getSectionID());

        // Check if student is actually enrolled before attempting to drop
        if (currentStudent.getEnrolledSections().contains(sectionToDrop)) {
             try {
                 // Update model objects
                 sectionToDrop.removeStudent(currentStudent);
                 currentStudent.dropSection(sectionToDrop);
                 
                 System.out.println("Drop successful. Saving data...");
                 persistenceService.saveAllData(); // Save changes
                 JOptionPane.showMessageDialog(this, 
                        "Successfully dropped section: " + sectionToDrop.getSectionID(), 
                        "Drop Successful", JOptionPane.INFORMATION_MESSAGE);
                 
                 // Refresh the section list
                 refreshSectionListForSelectedCourse();
                 
                 // Refresh the schedule panel automatically
                 refreshStudentSchedule();
             } catch (Exception ex) {
                 System.err.println("Exception during drop update: " + ex.getMessage());
                 ex.printStackTrace();
                 JOptionPane.showMessageDialog(this, 
                        "An unexpected error occurred while dropping the section.", 
                        "Drop Error", JOptionPane.ERROR_MESSAGE);
             }
        } else {
            // Student wasn't enrolled according to Student object
             System.out.println("Student not enrolled in the selected section.");
             JOptionPane.showMessageDialog(this, 
                    "You are not currently enrolled in section: " + sectionToDrop.getSectionID(), 
                    "Drop Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // New method to refresh the student schedule tab
    private void refreshStudentSchedule() {
        // Find the parent JTabbedPane
        javax.swing.JTabbedPane tabbedPane = findTabbedPane(this);
        if (tabbedPane != null) {
            // Look for the StudentTimetablePanel tab
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (tabbedPane.getComponentAt(i) instanceof StudentTimetablePanel) {
                    StudentTimetablePanel schedulePanel = (StudentTimetablePanel) tabbedPane.getComponentAt(i);
                    schedulePanel.refreshStudentTimetable(); // Make this method public in StudentTimetablePanel
                    System.out.println("Refreshed student schedule view");
                    
                    // Select the My Schedule tab to show the updated schedule
                    tabbedPane.setSelectedIndex(i);
                    return;
                }
            }
            System.out.println("Could not find StudentTimetablePanel in tabs");
        } else {
            System.out.println("Could not find parent tabbed pane");
        }
    }
    
    // Utility method to find parent JTabbedPane
    private javax.swing.JTabbedPane findTabbedPane(java.awt.Component component) {
        if (component == null) return null;
        if (component instanceof javax.swing.JTabbedPane) 
            return (javax.swing.JTabbedPane) component;
        return findTabbedPane(component.getParent());
    }
} 