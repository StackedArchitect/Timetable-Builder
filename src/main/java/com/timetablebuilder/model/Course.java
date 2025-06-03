package com.timetablebuilder.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "courseCode")
public class Course {
    private String courseCode;
    private String courseName;
    private List<String> conflictingCourses; // List of course codes

    // Removed no-argument constructor
    // private Course() {
    //     this.conflictingCourses = new ArrayList<>(); // Initialize list
    // }

    @JsonCreator
    public Course(
            @JsonProperty("courseCode") String courseCode, 
            @JsonProperty("courseName") String courseName) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        // Ensure conflictingCourses is initialized even when Jackson uses this constructor
        this.conflictingCourses = new ArrayList<>();
    }
    
    // Need to handle conflictingCourses separately as it's not in the creator args
    // Jackson will use the setter or direct field access if available.
    public void setConflictingCourses(List<String> conflictingCourses) {
        this.conflictingCourses = (conflictingCourses != null) ? conflictingCourses : new ArrayList<>();
    }

    // Getters
    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public List<String> getConflictingCourses() {
        return conflictingCourses; // Consider returning unmodifiable list
    }

    // Setters
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    // Methods to manage conflicts
    public void addConflictingCourse(String conflictingCourseCode) {
        if (conflictingCourseCode != null && !conflictingCourseCode.isEmpty() && !this.conflictingCourses.contains(conflictingCourseCode)) {
            this.conflictingCourses.add(conflictingCourseCode);
        }
    }

    public void removeConflictingCourse(String conflictingCourseCode) {
        this.conflictingCourses.remove(conflictingCourseCode);
    }

    // Equals and hashCode based on courseCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return courseCode.equals(course.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }
} 