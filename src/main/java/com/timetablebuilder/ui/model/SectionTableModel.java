package com.timetablebuilder.ui.model;

import com.timetablebuilder.model.Section;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class SectionTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Section ID", "Course", "Type", "Instructor", "Capacity", "Req AV", "Req Comp"};
    private List<Section> sections;

    public SectionTableModel() {
        this.sections = new ArrayList<>();
    }

    public SectionTableModel(List<Section> sections) {
        this.sections = new ArrayList<>(sections); // Use a copy
    }

    public void setSections(List<Section> sections) {
        this.sections = new ArrayList<>(sections);
        fireTableDataChanged();
    }

    public Section getSectionAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < sections.size()) {
            return sections.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return sections.size();
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
            case 0: return String.class;  // Section ID
            case 1: return String.class;  // Course (Code)
            case 2: return String.class;  // Type (Enum)
            case 3: return String.class;  // Instructor (Name)
            case 4: return Integer.class; // Capacity
            case 5: return Boolean.class; // Req AV
            case 6: return Integer.class; // Req Comp
            default: return Object.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Section section = sections.get(rowIndex);
        switch (columnIndex) {
            case 0: return section.getSectionID();
            case 1: return (section.getParentCourse() != null) ? section.getParentCourse().getCourseCode() : "N/A";
            case 2: return section.getType().toString();
            case 3: return (section.getInstructor() != null) ? section.getInstructor().getName() : "N/A";
            case 4: return section.getSectionCapacity();
            case 5: return section.requiresAV();
            case 6: return section.getRequiredComputers();
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Edited via dialog
    }
} 