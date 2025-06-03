package com.timetablebuilder.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "studentID")
public class Student {
    private String studentID;
    private String name;
    
    // Store section IDs for JSON serialization
    @JsonProperty("enrolledSections")
    private List<String> enrolledSectionIds;
    
    // The actual section objects (not serialized)
    @JsonIgnore
    private List<Section> enrolledSections;

    // Removed no-argument constructor
    // private Student() {}

    @JsonCreator
    public Student(
            @JsonProperty("studentID") String studentID, 
            @JsonProperty("name") String name) {
        this.studentID = studentID;
        this.name = name;
        this.enrolledSections = new ArrayList<>();
        this.enrolledSectionIds = new ArrayList<>();
    }

    // Getters
    public String getStudentID() {
        return studentID;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public List<Section> getEnrolledSections() {
        return enrolledSections;
    }
    
    // Getter for section IDs (used by Jackson)
    public List<String> getEnrolledSectionIds() {
        return enrolledSectionIds;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    // Methods to manage enrolled sections
    public void enrollInSection(Section section) {
        if (section != null && !this.enrolledSections.contains(section)) {
            this.enrolledSections.add(section);
            
            // Also add the section ID
            String sectionId = section.getSectionID();
            if (!this.enrolledSectionIds.contains(sectionId)) {
                this.enrolledSectionIds.add(sectionId);
            }
        }
    }

    public void dropSection(Section section) {
        this.enrolledSections.remove(section);
        
        // Also remove the section ID
        if (section != null) {
            this.enrolledSectionIds.remove(section.getSectionID());
        }
    }
    
    // Method for PersistenceService to link section objects based on IDs
    public void linkEnrolledSections(Map<String, Section> sectionMap) {
        if (enrolledSectionIds == null || sectionMap == null) return;
        
        // Clear the current list to rebuild it
        enrolledSections.clear();
        
        // Add sections from the map based on IDs
        for (String sectionId : enrolledSectionIds) {
            Section section = sectionMap.get(sectionId);
            if (section != null) {
                enrolledSections.add(section);
            } else {
                System.err.println("Warning: Could not find Section with ID: " + sectionId 
                                  + " for Student: " + studentID);
            }
        }
        System.out.println("Student " + studentID + ": Linked " + enrolledSections.size() 
                          + " of " + enrolledSectionIds.size() + " section IDs");
    }

    // Equals and hashCode based on studentID for uniqueness
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return studentID.equals(student.studentID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentID);
    }
} 