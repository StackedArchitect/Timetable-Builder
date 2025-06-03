package com.timetablebuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String username;
    
    @JsonProperty("passwordHash") // Map JSON field "passwordHash" to this field
    private String hashedPassword;
    
    private UserRole role;
    
    @JsonProperty("associatedID") // Map JSON field "associatedID" to this field
    private String userID; // Links to Instructor/Student ID or unique Admin ID
    
    // private String associatedID; // Removed redundant field

    // Removed no-argument constructor
    // private User() {}

    @JsonCreator
    public User(
            @JsonProperty("username") String username, 
            @JsonProperty("passwordHash") String hashedPassword, // Map JSON field to this parameter
            @JsonProperty("role") UserRole role, 
            @JsonProperty("associatedID") String userID) { // Map JSON field to this parameter
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.role = role;
        this.userID = userID;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public UserRole getRole() {
        return role;
    }

    public String getUserID() {
        return userID;
    }

    // No setters for security-sensitive fields like password hash or role typically
    // Username might be changeable depending on requirements, but often fixed
} 