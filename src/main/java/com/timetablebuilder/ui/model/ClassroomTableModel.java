package com.timetablebuilder.ui.model;

import com.timetablebuilder.model.Classroom;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ClassroomTableModel extends AbstractTableModel {

    private final String[] columnNames = {"ID", "Capacity", "Has AV", "Computers", "Is Lab"};
    private List<Classroom> classrooms;

    public ClassroomTableModel() {
        this.classrooms = new ArrayList<>();
    }

    public ClassroomTableModel(List<Classroom> classrooms) {
        this.classrooms = new ArrayList<>(classrooms); // Use a copy
    }

    public void setClassrooms(List<Classroom> classrooms) {
        this.classrooms = new ArrayList<>(classrooms);
        fireTableDataChanged(); // Notify the table view of the data change
    }

    public Classroom getClassroomAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < classrooms.size()) {
            return classrooms.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return classrooms.size();
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
        switch (columnIndex) {
            case 0: return String.class;  // ID
            case 1: return Integer.class; // Capacity
            case 2: return Boolean.class; // Has AV
            case 3: return Integer.class; // Computers
            case 4: return Boolean.class; // Is Lab
            default: return Object.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Classroom classroom = classrooms.get(rowIndex);
        switch (columnIndex) {
            case 0: return classroom.getClassroomID();
            case 1: return classroom.getCapacity();
            case 2: return classroom.hasAudioVideo();
            case 3: return classroom.getNumComputers();
            case 4: return classroom.isLab();
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Classrooms are edited via a dialog, not directly in the table
        return false;
    }
} 