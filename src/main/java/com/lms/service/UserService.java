package com.lms.service;

import java.util.List;
import java.util.Optional;

import com.lms.model.User;

/**
 * Service interface for managing user-related operations within the CreditHub system.
 * This contract defines methods for account creation, security (password hashing), 
 * and retrieving categorized lists of members like admins and customers.
 */
public interface UserService {
    
    /**
     * Converts a raw password string into a secure, encrypted format.
     * This is typically used during registration or when updating a password.
     * * @param input The plain-text password provided by the user.
     * @return The securely hashed version of the password.
     */
    public String passwordDigest(String input);
    
    /**
     * Persists a new user record into the system database.
     * * @param user The user object containing registration details.
     */
    public void saveUser(User user);
    
    /**
     * Locates a specific user based on their unique numeric identifier.
     * * @param userId The unique ID assigned to the user.
     * @return An Optional containing the User if found, or empty if not.
     */
    public Optional<User> getUserById(int userId);
    
    /**
     * Finds a user profile associated with a specific email address.
     * Commonly used during the authentication and login process.
     * * @param email The email address used as a login credential.
     * @return The User object linked to that email.
     */
    public User getUserByEmail(String email);
    
    /**
     * Retrieves a complete list of every person registered on the platform.
     * * @return A list of all User objects in the database.
     */
    public List<User> getAllUsers();
    
    /**
     * Filters and returns only those users who hold Administrative privileges.
     * * @return A list of all users with the ADMIN role.
     */
    public List<User> getAllAdmins();
    
    /**
     * Filters and returns only the standard users (borrowers) of the system.
     * * @return A list of all users with the CUSTOMER role.
     */
    public List<User> getAllCustomers();
    
    /**
     * Updates the password for an existing user account.
     * * @param id The ID of the user whose password is being changed.
     * @param newPassword The new hashed password to be stored.
     */
    public void updatePassword(int id, String newPassword);
    
}