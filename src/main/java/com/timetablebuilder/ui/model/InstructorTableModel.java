package com.timetablebuilder.ui.model;

import com.timetablebuilder.model.Instructor;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class InstructorTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Instructor ID", "Name"};
    private List<Instructor> instructors;

    public InstructorTableModel() {
        this.instructors = new ArrayList<>();
    }

    public InstructorTableModel(List<Instructor> instructors) {
        this.instructors = new ArrayList<>(instructors); // Use a copy
    }

    public void setInstructors(List<Instructor> instructors) {
        this.instructors = new ArrayList<>(instructors);
        fireTableDataChanged();
    }

    public Instructor getInstructorAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < instructors.size()) {
            return instructors.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return instructors.size();
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
        return String.class; // Both ID and Name are Strings
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Instructor instructor = instructors.get(rowIndex);
        switch (columnIndex) {
            case 0: return instructor.getInstructorID();
            case 1: return instructor.getName();
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Edited via dialog
    }
} 