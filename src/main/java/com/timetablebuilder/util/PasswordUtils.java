package com.timetablebuilder.util;

// Placeholder for password hashing utilities.
// TODO: Integrate a library like jBCrypt.

public class PasswordUtils {

    /**
     * Hashes a plain text password.
     * Replace with actual hashing logic (e.g., BCrypt.hashpw).
     *
     * @param plainPassword The password to hash.
     * @return The hashed password string.
     */
    public static String hashPassword(String plainPassword) {
        System.out.println("PasswordUtils.hashPassword called (Not implemented - returning plain text!)");
        // !!! SECURITY RISK: Replace this with actual hashing !!!
        return plainPassword; // Placeholder - VERY INSECURE
    }

    /**
     * Verifies a plain text password against a stored hash.
     * Replace with actual verification logic (e.g., BCrypt.checkpw).
     *
     * @param plainPassword  The password entered by the user.
     * @param hashedPassword The stored hash from the database/repository.
     * @return true if the password matches the hash, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        System.out.println("PasswordUtils.verifyPassword called (Not implemented - comparing plain text!)");
        // !!! SECURITY RISK: Replace this with actual hash comparison !!!
        return plainPassword != null && plainPassword.equals(hashedPassword); // Placeholder - VERY INSECURE
    }
} 