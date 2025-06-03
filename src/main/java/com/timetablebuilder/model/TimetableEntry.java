package com.timetablebuilder.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TimetableEntry {
    // Store IDs temporarily during deserialization
    private String sectionId;
    private String classroomId;
    
    // Keep actual object references, linked later
    private Section section; 
    @JsonIdentityReference(alwaysAsId = true)
    private Classroom classroom;
    private TimeSlot timeSlot;

    // Constructor used by application logic AFTER linking
    public TimetableEntry(Section section, Classroom classroom, TimeSlot timeSlot) {
        this.section = section;
        this.classroom = classroom;
        this.sectionId = (section != null) ? section.getSectionID() : null;
        this.classroomId = (classroom != null) ? classroom.getClassroomID() : null;
        this.timeSlot = timeSlot;
    }
    
    // Constructor used by Jackson for DESERIALIZATION (accepts IDs)
    @JsonCreator
    public TimetableEntry(
            @JsonProperty("section") String sectionId, 
            @JsonProperty("classroom") String classroomId, 
            @JsonProperty("timeSlot") TimeSlot timeSlot) {
        this.sectionId = sectionId;
        this.classroomId = classroomId;
        this.timeSlot = timeSlot;
        // Actual objects start as null, linked by PersistenceService
        this.section = null; 
        this.classroom = null;
    }

    // Getters (return the linked objects)
    public Section getSection() {
        return section;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }
    
    // Getters for IDs (used during linking)
    public String getSectionId() { 
        return sectionId;
    }
    public String getClassroomId() {
        return classroomId;
    }

    // Setters for linking by PersistenceService
    public void linkSection(Section section) {
        this.section = section;
    }
    public void linkClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    // Equals and hashCode can be useful, based on the combined identity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimetableEntry that = (TimetableEntry) o;
        return Objects.equals(section, that.section) &&
               Objects.equals(classroom, that.classroom) &&
               Objects.equals(timeSlot, that.timeSlot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, classroom, timeSlot);
    }

    @Override
    public String toString() {
        return "Entry{" +
               "Section=" + (section != null ? section.getSectionID() : "null") +
               ", Classroom=" + (classroom != null ? classroom.getClassroomID() : "null") +
               ", TimeSlot=" + timeSlot +
               '}';
    }
} 