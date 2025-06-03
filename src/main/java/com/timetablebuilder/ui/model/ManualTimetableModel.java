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

// TableModel for displaying and potentially managing the timetable grid manually
public class ManualTimetableModel extends AbstractTableModel {

    private final List<DayOfWeek> days = Arrays.asList(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    );
    // Define hour range directly for the grid display
    private final int displayStartHour = 8; // e.g., 8 AM
    private final int displayEndHour = 17; // e.g., 5 PM (inclusive start, exclusive end for loop)

    // Use a Map for easier access and modification by cell coordinate
    // Store entries based on their actual TimeSlot
    private Map<TimeSlot, TimetableEntry> schedule;
    // Map for quick lookup by grid coordinates (Day, Hour) -> Entry that STARTS at this hour
    private Map<GridCoordinate, TimetableEntry> gridLookup;

    public ManualTimetableModel() {
        this.schedule = new HashMap<>();
        this.gridLookup = new HashMap<>();
    }

    // Helper class for grid coordinate key
    private static class GridCoordinate {
        final DayOfWeek day;
        final int hour;
        GridCoordinate(DayOfWeek day, int hour) { this.day = day; this.hour = hour; }
        @Override public boolean equals(Object o) { /* standard equals */ return (o instanceof GridCoordinate gc) && day == gc.day && hour == gc.hour; }
        @Override public int hashCode() { /* standard hashCode */ return Objects.hash(day, hour); }
    }

    // Method to load data from an existing Timetable object
    public void loadTimetable(Timetable timetable) {
        schedule.clear();
        gridLookup.clear();
        if (timetable != null) {
            for (TimetableEntry entry : timetable.getEntries()) {
                TimeSlot slot = entry.getTimeSlot();
                if (slot != null) {
                    schedule.put(slot, entry);
                    // Add to grid lookup based on start hour
                    GridCoordinate coord = new GridCoordinate(slot.getDayOfWeek(), slot.getStartHour());
                    gridLookup.put(coord, entry); 
                    // TODO: Handle entries spanning multiple hours if needed for display logic
                }
            }
        }
        fireTableDataChanged();
    }

    // Method to add/update an entry at a specific slot (primarily for internal use)
    public void addEntry(TimetableEntry entry) {
         TimeSlot slot = entry.getTimeSlot();
         if (slot != null) {
             schedule.put(slot, entry);
             GridCoordinate coord = new GridCoordinate(slot.getDayOfWeek(), slot.getStartHour());
             gridLookup.put(coord, entry);
             // TODO: Update relevant cells
             fireTableDataChanged(); // Simple refresh for now
         }
    }
    
    // Method to remove an entry
    public void removeEntry(TimetableEntry entry) {
        TimeSlot slot = entry.getTimeSlot();
        if (slot != null && schedule.containsKey(slot)) {
            schedule.remove(slot);
             GridCoordinate coord = new GridCoordinate(slot.getDayOfWeek(), slot.getStartHour());
            gridLookup.remove(coord);
            // TODO: Update relevant cells
            fireTableDataChanged(); // Simple refresh for now
        }
    }

    // Method to get an entry that starts at a specific grid slot
    public TimetableEntry getEntryAt(DayOfWeek day, int hour) {
        return gridLookup.get(new GridCoordinate(day, hour));
    }

    // Method to clear the schedule
    public void clear() {
        schedule.clear();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return (displayEndHour - displayStartHour); // Number of hour slots
    }

    @Override
    public int getColumnCount() {
        return days.size() + 1; // +1 for Hour column
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Hour";
        } else {
            DayOfWeek day = days.get(column - 1);
            String dayName = day.toString();
            return dayName.charAt(0) + dayName.substring(1).toLowerCase();
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int hour = displayStartHour + rowIndex; // The hour this row represents

        if (columnIndex == 0) {
            // Display hour range (e.g., "08:00 - 09:00")
            return String.format("%02d:00", hour); 
        }

        DayOfWeek day = days.get(columnIndex - 1);
        // Get the entry that STARTS at this specific day/hour slot
        TimetableEntry entry = gridLookup.get(new GridCoordinate(day, hour));
        
        // Return the TimetableEntry object itself for the renderer
        // The renderer will handle displaying based on the entry's actual duration
        return entry; 
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Usually false, modifications happen via drag/drop or other actions
        return false;
    }

    // Add getters for display parameters needed by the panel
    public int getDisplayStartHour() {
        return displayStartHour;
    }
    public List<DayOfWeek> getDays() {
        return days; // Return the internal list
    }
} 