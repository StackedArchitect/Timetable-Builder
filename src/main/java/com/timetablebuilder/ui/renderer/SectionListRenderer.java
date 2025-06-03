package com.timetablebuilder.ui.renderer;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.timetablebuilder.model.Section;

public class SectionListRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        // Call superclass method first to get default styling
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof Section) {
            Section section = (Section) value;
            String instructorName = (section.getInstructor() != null) ? 
                                     section.getInstructor().getName() : "Unassigned";
            String courseCode = (section.getParentCourse() != null) ?
                                   section.getParentCourse().getCourseCode() : "NoCourse";
            String type = (section.getType() != null) ? 
                           section.getType().toString().substring(0, 3) : "???"; // e.g., LEC, LAB, TUT
                           
            // Format: SectionID (CourseCode - Type by InstructorName)
            String displayText = String.format("%s (%s - %s by %s)", 
                                             section.getSectionID(),
                                             courseCode,
                                             type,
                                             instructorName);
            setText(displayText);
            
            // Optional: Add tooltip with more details?
            // setToolTipText("Capacity: " + section.getSectionCapacity() + ", AV: " + section.requiresAV());
        }

        return this;
    }
} 