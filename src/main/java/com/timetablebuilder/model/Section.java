package com.timetablebuilder.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

// @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "sectionID") // REMOVED
public class Section {
    private String sectionID; // e.g., "CSF213-L1"
    
    @JsonIdentityReference(alwaysAsId = true) // Serialize as ID
    private Course parentCourse; // This will be linked later
    
    private ComponentType type; // LECTURE, LAB, TUTORIAL
    
    @JsonIdentityReference(alwaysAsId = true) // Serialize as ID
    private Instructor instructor; // This will be linked later
    
    private int sectionCapacity;
    private TimeSlot timeSlot; // Added field
    
    // Store studentIDs as strings to avoid circular references
    @JsonProperty("enrolledStudents")
    private List<String> enrolledStudentIds;
    
    // The actual student objects (not serialized to JSON)
    @JsonIgnore
    private List<Student> enrolledStudents;
    
    private boolean requiresAV;
    private int requiredComputers;

    // Store IDs temporarily during deserialization
    private String parentCourseCode; 
    private String instructorId;

    // Use @JsonCreator on the main constructor
    @JsonCreator
    public Section(
            @JsonProperty("sectionID") String sectionID, 
            @JsonProperty("parentCourse") String parentCourseCode, // Accept ID from JSON
            @JsonProperty("instructor") String instructorId, // Accept ID from JSON
            @JsonProperty("type") ComponentType type, 
            @JsonProperty("sectionCapacity") int sectionCapacity, 
            @JsonProperty("timeSlot") TimeSlot timeSlot, 
            @JsonProperty("requiresAV") boolean requiresAV, 
            @JsonProperty("requiredComputers") int requiredComputers) {
        this.sectionID = sectionID;
        this.parentCourseCode = parentCourseCode; // Store ID
        this.instructorId = instructorId;       // Store ID
        this.type = type;
        this.instructor = null; // Set actual object to null initially
        this.parentCourse = null; // Set actual object to null initially
        this.sectionCapacity = sectionCapacity;
        this.timeSlot = timeSlot; 
        this.requiresAV = requiresAV;
        this.requiredComputers = requiredComputers;
        // Ensure lists are initialized
        this.enrolledStudents = new ArrayList<>(); 
        this.enrolledStudentIds = new ArrayList<>();
    }

    // Getters
    public String getSectionID() { return sectionID; }
    public Course getParentCourse() { return parentCourse; }
    public ComponentType getType() { return type; }
    public Instructor getInstructor() { return instructor; }
    public int getSectionCapacity() { return sectionCapacity; }
    public TimeSlot getTimeSlot() { return timeSlot; } // Added getter
    
    @JsonIgnore // Don't serialize actual student objects
    public List<Student> getEnrolledStudents() { return enrolledStudents; }
    
    // Getter for student IDs (used by Jackson)
    public List<String> getEnrolledStudentIds() { return enrolledStudentIds; }
    
    public boolean requiresAV() { return requiresAV; }
    public int getRequiredComputers() { return requiredComputers; }
    
    @JsonIgnore // Don't serialize calculated field
    public int getCurrentEnrollment() { return enrolledStudents.size(); }

    // Getters for the stored IDs (might be useful, but ignore for JSON)
    @JsonIgnore // Prevent saving this temporary field
    public String getParentCourseCode() { return parentCourseCode; } 
    @JsonIgnore // Prevent saving this temporary field
    public String getInstructorId() { return instructorId; }

    // Renamed setter used by PersistenceService for linking
    public void linkInstructor(Instructor instructor) { 
        this.instructor = instructor; 
        // Optionally clear the temporary ID field if no longer needed
        // this.instructorId = null; 
    }
    
    // Setter for parent course used by PersistenceService for linking
    public void linkParentCourse(Course course) { 
        this.parentCourse = course; 
        // Optionally clear the temporary ID field if no longer needed
        // this.parentCourseCode = null; 
    }

    // Setters (Use cautiously - ID, parentCourse, type usually fixed)
    public void setInstructor(Instructor instructor) { this.instructor = instructor; }
    public void setSectionCapacity(int sectionCapacity) { this.sectionCapacity = sectionCapacity; }
    public void setRequiresAV(boolean requiresAV) { this.requiresAV = requiresAV; }
    public void setRequiredComputers(int requiredComputers) { this.requiredComputers = requiredComputers; }

    // Methods to manage enrolled students (maintain consistency)
    public boolean addStudent(Student student) {
        if (student != null && enrolledStudents.size() < sectionCapacity && !enrolledStudents.contains(student)) {
            enrolledStudents.add(student);
            // Also add the student ID to the ID list for serialization
            if (!enrolledStudentIds.contains(student.getStudentID())) {
                enrolledStudentIds.add(student.getStudentID());
            }
            // Ensure student's list is also updated elsewhere (e.g., in Student class or service)
            return true;
        }
        return false;
    }

    public boolean removeStudent(Student student) {
        boolean removed = enrolledStudents.remove(student);
        // Also remove from the ID list
        if (student != null) {
            enrolledStudentIds.remove(student.getStudentID());
        }
        // Ensure student's list is also updated elsewhere
        return removed;
    }

    // Jackson will need a way to set enrolledStudents (not in constructor args)
    @JsonIgnore
    public void setEnrolledStudents(List<Student> enrolledStudents) {
        this.enrolledStudents = (enrolledStudents != null) ? enrolledStudents : new ArrayList<>();
        // Update the ID list to match
        this.enrolledStudentIds = new ArrayList<>();
        for (Student student : this.enrolledStudents) {
            if (student != null) {
                this.enrolledStudentIds.add(student.getStudentID());
            }
        }
    }
    
    // Method for PersistenceService to link student objects based on IDs
    public void linkEnrolledStudents(Map<String, Student> studentMap) {
        if (enrolledStudentIds == null || studentMap == null) return;
        
        // Clear the current list to rebuild it
        enrolledStudents.clear();
        
        // Add students from the map based on IDs
        for (String studentId : enrolledStudentIds) {
            Student student = studentMap.get(studentId);
            if (student != null) {
                enrolledStudents.add(student);
            } else {
                System.err.println("Warning: Could not find Student with ID: " + studentId 
                                  + " for Section: " + sectionID);
            }
        }
        System.out.println("Section " + sectionID + ": Linked " + enrolledStudents.size() 
                          + " of " + enrolledStudentIds.size() + " student IDs");
    }

    // Equals and hashCode based on sectionID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return sectionID.equals(section.sectionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectionID);
    }

    @Override
    public String toString() {
        // Example: "CSF213-L1 (LEC by Prof. Xavier)"
        String instructorName = (instructor != null) ? instructor.getName() : "Unassigned";
        String courseCode = (parentCourse != null) ? parentCourse.getCourseCode() : "NoCourse";
        return String.format("%s (%s by %s) - %s",
                sectionID,
                type.toString().substring(0, 3), // LECTURE -> LEC
                instructorName,
                courseCode);
    }
} 