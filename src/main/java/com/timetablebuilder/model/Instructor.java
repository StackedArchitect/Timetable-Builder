package com.timetablebuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "instructorID")
public class Instructor {
    private String instructorID;
    private String name;

    // Removed no-argument constructor
    // private Instructor() {}

    @JsonCreator
    public Instructor(
            @JsonProperty("instructorID") String instructorID, 
            @JsonProperty("name") String name) {
        this.instructorID = instructorID;
        this.name = name;
    }

    // Getters
    public String getInstructorID() {
        return instructorID;
    }

    public String getName() {
        return name;
    }

    // Setters (optional, e.g., if name needs correction)
    public void setName(String name) {
        this.name = name;
    }

    // Equals and hashCode are important if instructors are used in Sets or as Map keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instructor that = (Instructor) o;
        return instructorID.equals(that.instructorID);
    }

    @Override
    public int hashCode() {
        return instructorID.hashCode();
    }
} 