package com.lms.service.impl;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.lms.model.User;
import com.lms.repository.UserRepository;
import com.lms.service.UserService;

/**
 * Service implementation for managing user-related operations.
 * This class handles user registration, profile retrieval, and security-related 
 * tasks like password hashing.
 */
public class UserServiceImpl implements UserService {

    // Setting up the logger to monitor user activities and security events
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    // Setter for Spring's password encoder to handle encryption
    public void setPasswordEncoder(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Setter for the user repository to manage database interactions
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Converts a raw password string into a secure BCrypt hash.
     * This is used during signup and password reset flows.
     */
    @Override
    public String passwordDigest(String input) {
        logger.debug("Generating secure hash for a user password");
        return passwordEncoder.encode(input);
    }

    /**
     * Persists a new user record in the system.
     */
    @Override
    public void saveUser(User user) {
        logger.info("Registering a new user with email: {}", user.getEmail());
        userRepository.saveUser(user);
    }

    /**
     * Locates a specific user based on their unique numeric ID.
     */
    @Override
    public Optional<User> getUserById(int userId) {
        logger.info("Attempting to find user by ID: {}", userId);
        return userRepository.getUserById(userId);
    }

    /**
     * Fetches every user registered on the CreditHub platform.
     */
    @Override
    public List<User> getAllUsers() {
        logger.info("Requesting full list of system users");
        return userRepository.getAllUsers();
    }

    /**
     * Retrieves only users who hold Administrative privileges.
     */
    @Override
    public List<User> getAllAdmins() {
        logger.info("Fetching all administrators for review");
        return userRepository.getAllAdmins();
    }

    /**
     * Finds a user profile associated with a specific email address.
     * Often used during login or account recovery.
     */
    @Override
    public User getUserByEmail(String email) {
        logger.info("Searching for account associated with email: {}", email);
        return userRepository.getUserByEmail(email);
    }

    /**
     * Updates an existing user's password. 
     * Note: The password should be hashed before reaching this method.
     */
    @Override
    public void updatePassword(int id, String newPassword) {
        logger.info("Updating password for user ID: {}", id);
        userRepository.updatePassword(id, newPassword);
    }
    
    /**
     * Returns a list of all users categorized as standard customers.
     */
    @Override
    public List<User> getAllCustomers() {
        logger.info("Fetching list of all registered customers");
        return userRepository.getAllCustomers();
    }
}