package com.timetablebuilder.ui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import com.timetablebuilder.model.Instructor;
import com.timetablebuilder.model.Timetable;
import com.timetablebuilder.model.TimetableEntry;
import com.timetablebuilder.service.TimetableService;
import com.timetablebuilder.ui.model.ManualTimetableModel;
import com.timetablebuilder.ui.renderer.TimetableCellRenderer;

// Placeholder Panel for Teacher Timetable View Tab
public class TeacherTimetablePanel extends JPanel {

    private JTable timetableGrid;
    private ManualTimetableModel timetableModel;
    private TimetableService timetableService;
    private Instructor currentInstructor;

    public TeacherTimetablePanel(Instructor instructor, TimetableService service) {
        super(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        if (instructor == null || service == null) {
             add(new JLabel("Error: Instructor data or Timetable service not available."), BorderLayout.CENTER);
             return;
        }
        
        this.currentInstructor = instructor;
        this.timetableService = service;

        setupTimetableGrid();
        add(new JScrollPane(timetableGrid), BorderLayout.CENTER);
        
        refreshTeacherTimetable();
        
        System.out.println("TeacherTimetablePanel initialized for: " + currentInstructor.getName());
    }

    private void setupTimetableGrid() {
        timetableModel = new ManualTimetableModel();
        timetableGrid = new JTable(timetableModel);

        timetableGrid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Allow selecting cells
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

    private void refreshTeacherTimetable() {
        System.out.println("Refreshing timetable for teacher: " + currentInstructor.getName());
        Timetable masterTimetable = timetableService.getMasterTimetable();
        if (masterTimetable == null) {
             System.err.println("TeacherTimetablePanel: Master timetable is null.");
             // Optionally clear the model or show an error
             timetableModel.loadTimetable(new Timetable()); // Load empty
             return;
        }
        
        // Filter the master timetable to get only entries for this instructor
        Timetable teacherTimetable = new Timetable();
        for (TimetableEntry entry : masterTimetable.getEntries()) {
            // Check if the entry's section has an instructor and if it matches the current one
            if (entry.getSection() != null && 
                entry.getSection().getInstructor() != null &&
                entry.getSection().getInstructor().equals(currentInstructor)) {
                    teacherTimetable.addEntry(entry);
            }
        }
        
        System.out.println("Found " + teacherTimetable.getEntries().size() + " timetable entries for " + currentInstructor.getName());
        timetableModel.loadTimetable(teacherTimetable); // Load the filtered timetable
    }
} 