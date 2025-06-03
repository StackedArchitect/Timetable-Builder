package com.timetablebuilder.ui;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import com.timetablebuilder.model.Section;
import com.timetablebuilder.model.Student;
import com.timetablebuilder.model.Timetable;
import com.timetablebuilder.model.TimetableEntry;
import com.timetablebuilder.service.TimetableService;
import com.timetablebuilder.ui.model.ManualTimetableModel;
import com.timetablebuilder.ui.renderer.TimetableCellRenderer;

// Placeholder Panel for Student Timetable View Tab
public class StudentTimetablePanel extends JPanel {

    private JTable timetableGrid;
    private ManualTimetableModel timetableModel;
    private TimetableService timetableService;
    private Student currentStudent;

    public StudentTimetablePanel(Student student, TimetableService service) {
        super(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (student == null || service == null) {
            add(new JLabel("Error: Student data or Timetable service not available."), BorderLayout.CENTER);
            return;
        }

        this.currentStudent = student;
        this.timetableService = service;

        setupTimetableGrid();
        add(new JScrollPane(timetableGrid), BorderLayout.CENTER);

        refreshStudentTimetable();

        System.out.println("StudentTimetablePanel initialized for: " + currentStudent.getName());
    }

    private void setupTimetableGrid() {
        timetableModel = new ManualTimetableModel();
        timetableGrid = new JTable(timetableModel);

        timetableGrid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timetableGrid.setCellSelectionEnabled(true);
        timetableGrid.setRowHeight(60);
        timetableGrid.getTableHeader().setReorderingAllowed(false);
        timetableGrid.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Apply the custom renderer to day columns
        TimetableCellRenderer cellRenderer = new TimetableCellRenderer();
        for (int i = 1; i < timetableGrid.getColumnCount(); i++) {
            TableColumn dayColumn = timetableGrid.getColumnModel().getColumn(i);
            dayColumn.setCellRenderer(cellRenderer);
            dayColumn.setPreferredWidth(120);
        }
        // Set width for the first column (Hour)
        TableColumn timeColumn = timetableGrid.getColumnModel().getColumn(0);
        timeColumn.setPreferredWidth(60);
    }

    public void refreshStudentTimetable() {
        System.out.println("Refreshing timetable for student: " + currentStudent.getName());
        Timetable masterTimetable = timetableService.getMasterTimetable();
        if (masterTimetable == null) {
            System.err.println("StudentTimetablePanel: Master timetable is null.");
            timetableModel.loadTimetable(new Timetable()); // Load empty
            return;
        }
        
        // Get the set of sections this student is enrolled in
        // Note: Assumes currentStudent.getEnrolledSections() returns the correct list of Section objects!
        // If PersistenceService linking for Student<->Section wasn't fully implemented, this might be empty.
        Set<Section> enrolledSections = new HashSet<>(currentStudent.getEnrolledSections()); 
        if (enrolledSections.isEmpty()) {
            System.out.println("Student " + currentStudent.getName() + " is not enrolled in any sections according to the Student object.");
        }

        // Filter the master timetable based on enrolled sections
        Timetable studentTimetable = new Timetable();
        for (TimetableEntry entry : masterTimetable.getEntries()) {
            if (entry.getSection() != null && enrolledSections.contains(entry.getSection())) {
                studentTimetable.addEntry(entry);
            }
        }

        System.out.println("Found " + studentTimetable.getEntries().size() + " timetable entries for " + currentStudent.getName());
        timetableModel.loadTimetable(studentTimetable);
    }
} 