package com.timetablebuilder.service;

import com.timetablebuilder.model.*; // Import all models for simplicity

import java.util.List; // Add other necessary imports later

// Initial stub for DataRepository. 
// Implementation details (in-memory, CSV, DB) will be added later.
public class DataRepository {

    // Placeholder methods - Define signatures based on what's needed

    public User findUserByUsername(String username) {
        System.out.println("DataRepository.findUserByUsername called for: " + username + " (Not implemented)");
        return null; // Placeholder
    }

    // --- Loading Data --- 
    public List<User> loadUsers() { 
        System.out.println("DataRepository.loadUsers called (Not implemented)");
        return List.of(); // Placeholder
    }
    public List<Classroom> loadClassrooms() { 
        System.out.println("DataRepository.loadClassrooms called (Not implemented)");
        return List.of(); // Placeholder
    }
    public List<Course> loadCourses() { 
        System.out.println("DataRepository.loadCourses called (Not implemented)");
        return List.of(); // Placeholder
    }
    public List<Instructor> loadInstructors() { 
        System.out.println("DataRepository.loadInstructors called (Not implemented)");
        return List.of(); // Placeholder
    }
    public List<Student> loadStudents() { 
        System.out.println("DataRepository.loadStudents called (Not implemented)");
        return List.of(); // Placeholder
    }
    public List<Section> loadSections() { 
        System.out.println("DataRepository.loadSections called (Not implemented)");
        return List.of(); // Placeholder
    }
    public Timetable loadTimetable() { 
        System.out.println("DataRepository.loadTimetable called (Not implemented)");
        return new Timetable(); // Placeholder
    }
    // Need methods for loading student enrollments too

    // --- Saving Data --- 
    public void saveUsers(List<User> users) { 
        System.out.println("DataRepository.saveUsers called (Not implemented)");
    }
    public void saveClassrooms(List<Classroom> classrooms) { 
        System.out.println("DataRepository.saveClassrooms called (Not implemented)");
    }
    public void saveCourses(List<Course> courses) { 
        System.out.println("DataRepository.saveCourses called (Not implemented)");
    }
    public void saveInstructors(List<Instructor> instructors) { 
        System.out.println("DataRepository.saveInstructors called (Not implemented)");
    }
    public void saveStudents(List<Student> students) { 
        System.out.println("DataRepository.saveStudents called (Not implemented)");
    }
    public void saveSections(List<Section> sections) { 
        System.out.println("DataRepository.saveSections called (Not implemented)");
    }
    public void saveTimetable(Timetable timetable) { 
        System.out.println("DataRepository.saveTimetable called (Not implemented)");
    }
    // Need methods for saving enrollments

    // Add methods for adding/updating/deleting individual items as needed later
    // e.g., public void addClassroom(Classroom classroom) { ... }
    // e.g., public void updateStudentEnrollments(Student student) { ... }
} 