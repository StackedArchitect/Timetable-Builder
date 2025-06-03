package com.timetablebuilder.service;

import java.util.HashMap;
import java.util.Map;

import com.timetablebuilder.model.User;
import com.timetablebuilder.model.UserRole;
import com.timetablebuilder.util.PasswordUtils;

public class AuthService {

    // private DataRepository dataRepository; // Keep commented if not used yet

    // --- Temporary Dummy Data Store ---
    private Map<String, User> dummyUserStore = new HashMap<>();

    public AuthService(/*DataRepository dataRepository*/) {
        // this.dataRepository = dataRepository;

        // Populate dummy data
        // IMPORTANT: Replace plain text passwords with hashed ones once PasswordUtils is implemented
        String adminPassHash = PasswordUtils.hashPassword("admin123");
        String teacherPassHash = PasswordUtils.hashPassword("teacher123");
        String studentPassHash = PasswordUtils.hashPassword("student123");

        dummyUserStore.put("admin", new User("admin", adminPassHash, UserRole.ADMIN, "A001"));
        dummyUserStore.put("profX", new User("profX", teacherPassHash, UserRole.TEACHER, "T001")); // Assuming T001 is Instructor ID
        dummyUserStore.put("alice", new User("alice", studentPassHash, UserRole.STUDENT, "S001")); // Assuming S001 is Student ID
        System.out.println("AuthService initialized with dummy users.");
        System.out.println("Admin hash: " + adminPassHash);
        System.out.println("Teacher hash: " + teacherPassHash);
        System.out.println("Student hash: " + studentPassHash);
    }

    /**
     * Authenticates a user based on username and password.
     * Uses dummy data store for now.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @return The authenticated User object if successful, null otherwise.
     */
    public User authenticate(String username, String password) {
        System.out.println("Attempting authentication for user: " + username);
        // 1. Retrieve user by username (using dummy store for now)
        // User user = dataRepository.findUserByUsername(username);
        User user = dummyUserStore.get(username);

        if (user != null) {
            System.out.println("User found: " + user.getUsername() + ", Role: " + user.getRole());
            // 2. Verify password using PasswordUtils
            if (PasswordUtils.verifyPassword(password, user.getHashedPassword())) {
                System.out.println("Password verified successfully for " + username);
                return user;
            } else {
                System.out.println("Password verification failed for " + username);
            }
        } else {
            System.out.println("User not found: " + username);
        }

        return null; // Authentication failed
    }
} 