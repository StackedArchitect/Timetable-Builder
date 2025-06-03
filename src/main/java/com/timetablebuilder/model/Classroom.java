package com.timetablebuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "classroomID")
public class Classroom {
    private String classroomID;
    private int capacity;
    private boolean hasAudioVideo;
    private int numComputers;
    
    @JsonProperty("lab") // Map JSON field "lab" to this field
    private boolean isLab;

    // Removed private no-argument constructor
    // private Classroom() {}

    @JsonCreator // Indicate this constructor should be used by Jackson
    public Classroom(
            @JsonProperty("classroomID") String classroomID, 
            @JsonProperty("capacity") int capacity, 
            @JsonProperty("hasAudioVideo") boolean hasAudioVideo, 
            @JsonProperty("numComputers") int numComputers, 
            @JsonProperty("lab") boolean isLab) { // Use "lab" to match JSON
        this.classroomID = classroomID;
        this.capacity = capacity;
        this.hasAudioVideo = hasAudioVideo;
        this.numComputers = numComputers;
        this.isLab = isLab;
    }

    // Getters
    public String getClassroomID() {
        return classroomID;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean hasAudioVideo() {
        return hasAudioVideo;
    }

    public int getNumComputers() {
        return numComputers;
    }

    public boolean isLab() {
        return isLab;
    }

    // Setters (optional, depends if classroom details can be modified after creation)
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setHasAudioVideo(boolean hasAudioVideo) {
        this.hasAudioVideo = hasAudioVideo;
    }

    public void setNumComputers(int numComputers) {
        this.numComputers = numComputers;
    }

    public void setIsLab(boolean isLab) {
        this.isLab = isLab;
    }

    @Override
    public String toString() {
        // Example: "CR1 (Cap: 60, AV, 0 Comp, Non-Lab)"
        return String.format("%s (Cap: %d, %s, %d Comp, %s)",
                classroomID,
                capacity,
                hasAudioVideo ? "AV" : "No AV",
                numComputers,
                isLab ? "Lab" : "Non-Lab");
    }
} 