package com.timetablebuilder.ui.model;

import com.timetablebuilder.model.Course;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class CourseTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Code", "Name", "Conflicting Courses"};
    private List<Course> courses;

    public CourseTableModel() {
        this.courses = new ArrayList<>();
    }

    public CourseTableModel(List<Course> courses) {
        this.courses = new ArrayList<>(courses); // Use a copy
    }

    public void setCourses(List<Course> courses) {
        this.courses = new ArrayList<>(courses);
        fireTableDataChanged();
    }

    public Course getCourseAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < courses.size()) {
            return courses.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return courses.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class; // All columns display as Strings
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Course course = courses.get(rowIndex);
        switch (columnIndex) {
            case 0: return course.getCourseCode();
            case 1: return course.getCourseName();
            case 2: 
                // Join the list of conflicting course codes into a comma-separated string
                return String.join(", ", course.getConflictingCourses());
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Edited via dialog
    }
} 