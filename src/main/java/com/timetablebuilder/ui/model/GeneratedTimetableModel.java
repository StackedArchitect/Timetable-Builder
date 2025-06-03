package com.timetablebuilder.ui.model;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.table.AbstractTableModel;

import com.timetablebuilder.model.TimeSlot;
import com.timetablebuilder.model.Timetable;
import com.timetablebuilder.model.TimetableEntry;

// TableModel for displaying the generated timetable grid
public class GeneratedTimetableModel extends AbstractTableModel {

    // Define days and time slots (customize as needed)
    private final List<DayOfWeek> days = Arrays.asList(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    ); // Columns
    // Define hour range directly for the grid display
    private final int displayStartHour = 8; // e.g., 8 AM
    private final int displayEndHour = 17; // e.g., 5 PM (inclusive start, exclusive end for loop)

    // Use a Map for quick lookup by grid coordinates (Day, Hour) -> Entry that STARTS at this hour
    private Map<GridCoordinate, TimetableEntry> gridLookup;

    // Helper class for grid coordinate key (can be shared or duplicated from ManualTimetableModel)
    private static class GridCoordinate {
        final DayOfWeek day;
        final int hour;
        GridCoordinate(DayOfWeek day, int hour) { this.day = day; this.hour = hour; }
        @Override public boolean equals(Object o) { /* standard equals */ return (o instanceof GridCoordinate gc) && day == gc.day && hour == gc.hour; }
        @Override public int hashCode() { /* standard hashCode */ return Objects.hash(day, hour); }
    }

    public GeneratedTimetableModel() {
        this.gridLookup = new HashMap<>();
    }

    public void setTimetable(Timetable timetable) {
        gridLookup.clear();
        if (timetable != null && timetable.getEntries() != null) {
             for (TimetableEntry entry : timetable.getEntries()) {
                TimeSlot slot = entry.getTimeSlot();
                if (slot != null) {
                    // Add to grid lookup based on start hour
                    GridCoordinate coord = new GridCoordinate(slot.getDayOfWeek(), slot.getStartHour());
                    gridLookup.put(coord, entry); 
                    // TODO: Handle entries spanning multiple hours if needed for display logic
                }
            }
        }
        fireTableDataChanged(); // Refresh the entire table
    }

    @Override
    public int getRowCount() {
        return (displayEndHour - displayStartHour);
    }

    @Override
    public int getColumnCount() {
        // Add 1 for the hour header column
        return days.size() + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Hour";
        } else {
            // Capitalize day name
            DayOfWeek day = days.get(column - 1);
            String dayName = day.toString();
            return dayName.charAt(0) + dayName.substring(1).toLowerCase();
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int hour = displayStartHour + rowIndex; // Calculate the hour for this row

        if (columnIndex == 0) {
            // First column is the hour label
            return String.format("%02d:00", hour); // Display hour range
        }

        DayOfWeek day = days.get(columnIndex - 1);

        // Get the entry that STARTS at this specific day/hour slot from the lookup map
        TimetableEntry entry = gridLookup.get(new GridCoordinate(day, hour));

        // Return the TimetableEntry object itself for the renderer
        // The renderer will handle displaying based on the entry's actual duration
        return entry; 
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Display only
    }
} 