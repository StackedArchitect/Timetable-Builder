package com.timetablebuilder.ui.model;

import com.timetablebuilder.model.Student;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class StudentTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Student ID", "Name" /*, "Enrolled Sections Count" */ }; // Enrollment count could be added
    private List<Student> students;

    public StudentTableModel() {
        this.students = new ArrayList<>();
    }

    public StudentTableModel(List<Student> students) {
        this.students = new ArrayList<>(students); // Use a copy
    }

    public void setStudents(List<Student> students) {
        this.students = new ArrayList<>(students);
        fireTableDataChanged();
    }

    public Student getStudentAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < students.size()) {
            return students.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return students.size();
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
        // if (columnIndex == 2) return Integer.class; // For enrollment count
        return String.class; // ID and Name are Strings
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student student = students.get(rowIndex);
        switch (columnIndex) {
            case 0: return student.getStudentID();
            case 1: return student.getName();
            // case 2: return student.getEnrolledSections().size(); // Example if count is added
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Edited via dialog
    }
} 