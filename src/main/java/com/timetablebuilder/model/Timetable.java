package com.timetablebuilder.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Timetable {
    private List<TimetableEntry> entries;

    public Timetable() {
        this.entries = new ArrayList<>();
    }

    public Timetable(List<TimetableEntry> entries) {
        // Create a new list to avoid external modification issues
        this.entries = new ArrayList<>(entries);
    }

    public void addEntry(TimetableEntry entry) {
        if (entry != null) {
            // Optional: Could add checks here to prevent adding duplicates
            // if (!this.entries.contains(entry)) {
            this.entries.add(entry);
            // }
        }
    }

    public void removeEntry(TimetableEntry entry) {
        this.entries.remove(entry);
    }

    public void clear() {
        this.entries.clear();
    }

    /**
     * Gets an unmodifiable view of the timetable entries.
     * This prevents external code from directly modifying the internal list.
     * @return An unmodifiable list of TimetableEntry objects.
     */
    public List<TimetableEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    // You might add more methods here later, e.g.,
    // findEntriesByInstructor(Instructor instructor)
    // findEntriesByStudent(Student student)
    // findEntryForSection(Section section)
    // findEntriesAtTimeSlot(TimeSlot timeSlot)
} 