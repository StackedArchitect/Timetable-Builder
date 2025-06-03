package com.timetablebuilder.model;

import java.time.DayOfWeek;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TimeSlot {
    private DayOfWeek dayOfWeek;
    private int startHour; // 0-23
    private int startMinute; // 0-59
    private int durationMinutes; // Duration in minutes

    @JsonCreator
    public TimeSlot(
            @JsonProperty("dayOfWeek") DayOfWeek dayOfWeek, 
            @JsonProperty("startHour") int startHour, 
            @JsonProperty("startMinute") int startMinute, 
            @JsonProperty("durationMinutes") int durationMinutes) {
        // Basic validation (can be enhanced)
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("Day cannot be null.");
        }
        if (startHour < 0 || startHour > 23 || startMinute < 0 || startMinute > 59) {
            throw new IllegalArgumentException("Invalid start time.");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        // Check if slot crosses midnight (optional, maybe disallow for simplicity?)
        // if (startHour * 60 + startMinute + durationMinutes > 24 * 60) { ... }
        
        this.dayOfWeek = dayOfWeek;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.durationMinutes = durationMinutes;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    // Calculate end time (useful for display or overlap checks)
    @JsonIgnore
    public int getEndHour() {
        int totalMinutes = startHour * 60 + startMinute + durationMinutes;
        return (totalMinutes / 60) % 24; // Handle crossing midnight if needed
    }

    @JsonIgnore
    public int getEndMinute() {
        int totalMinutes = startHour * 60 + startMinute + durationMinutes;
        return totalMinutes % 60;
    }
    
    /**
     * Checks if this TimeSlot overlaps with another TimeSlot.
     * Basic implementation: assumes slots are on the same day for simplicity.
     * More complex checks needed for multi-day or recurring events.
     * @param other The other TimeSlot to compare against.
     * @return true if they overlap in time on the same day, false otherwise.
     */
    public boolean overlaps(TimeSlot other) {
        if (other == null || this.dayOfWeek != other.dayOfWeek) {
            return false;
        }

        int thisStart = this.startHour * 60 + this.startMinute;
        int thisEnd = thisStart + this.durationMinutes;
        int otherStart = other.startHour * 60 + other.startMinute;
        int otherEnd = otherStart + other.durationMinutes;

        // Check for overlap: !(otherEnd <= thisStart || otherStart >= thisEnd)
        return thisStart < otherEnd && otherStart < thisEnd;
    }

    @Override
    public String toString() {
        // Format like "Monday 09:00 - 09:50"
        String dayName = dayOfWeek.toString().charAt(0) + dayOfWeek.toString().substring(1).toLowerCase();
        return String.format("%s %02d:%02d - %02d:%02d", 
                             dayName, startHour, startMinute, getEndHour(), getEndMinute());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return startHour == timeSlot.startHour &&
               startMinute == timeSlot.startMinute &&
               durationMinutes == timeSlot.durationMinutes &&
               dayOfWeek == timeSlot.dayOfWeek;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, startHour, startMinute, durationMinutes);
    }
} 