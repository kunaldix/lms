package com.lms.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lms.dbutils.DBConnection;
import com.lms.constant.Role;
import com.lms.model.User;

/**
 * Repository class for managing User data.
 * Handles database operations for authentication, profile management,
 * and role-based user retrieval.
 */
public class UserRepository {

    private static final Logger logger = LogManager.getLogger(UserRepository.class);

    /**
     * Persists a new user record into the database during registration.
     */
    public void saveUser(User user) {
        String query = "INSERT INTO users(name, email, password, phone_number, role) VALUES(?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            logger.info("Attempting to register new user: {}", user.getEmail());

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getPhoneNumber());
            stmt.setString(5, user.getRole().name());

            stmt.executeUpdate();
            logger.info("User {} successfully registered.", user.getEmail());

        } catch (SQLException e) {
            logger.error("Failed to save user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Retrieves a user profile by their primary key ID.
     * Note: Password is set to null for security before returning.
     */
    public Optional<User> getUserById(int userId) {
        String query = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs, false));
                }
            }

        } catch (SQLException e) {
            logger.error("Error retrieving user by ID {}: {}", userId, e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Retrieves all users registered in the system.
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String query = "SELECT * FROM users";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapUser(rs, false));
            }
            logger.info("Successfully retrieved {} total users.", list.size());

        } catch (SQLException e) {
            logger.error("Error fetching all users: {}", e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves only users with the ADMIN role.
     * Logic updated to filter at the SQL level for efficiency.
     */
    public List<User> getAllAdmins() {
        List<User> list = new ArrayList<>();
        String query = "SELECT * FROM users WHERE role = 'ADMIN'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapUser(rs, false));
            }
            logger.info("Retrieved {} administrators.", list.size());

        } catch (SQLException e) {
            logger.error("Error fetching administrator list: {}", e.getMessage());
        }
        return list;
    }

    /**
     * Finds a user by their email address.
     * Used primarily for authentication (includes password for verification).
     */
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs, true);
                    user.setProfileImage(rs.getString("profile_image"));
                    return user;
                }
            }

        } catch (SQLException e) {
            logger.error("Error finding user by email {}: {}", email, e.getMessage());
        }
        return null;
    }

    /**
     * Updates the password for a specific user.
     */
    public void updatePassword(int id, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            logger.info("Updating password for User ID: {}", id);
            stmt.setString(1, newPassword);
            stmt.setInt(2, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Password updated successfully for User ID: {}", id);
            }

        } catch (SQLException e) {
            logger.error("Failed to update password for User ID {}: {}", id, e.getMessage());
        }
    }
    
    /**
     * Retrieves only users with the CUSTOMER role.
     * Logic updated to filter at the SQL level for efficiency.
     */
    public List<User> getAllCustomers() {
        List<User> list = new ArrayList<>();
        String query = "SELECT * FROM users WHERE role = 'CUSTOMER'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapUser(rs, false));
            }
            logger.info("Retrieved {} customers.", list.size());

        } catch (SQLException e) {
            logger.error("Error fetching customer list: {}", e.getMessage());
        }
        return list;
    } 

    /**
     * Helper method to map a ResultSet row to a User object.
     * @param rs The ResultSet to read from.
     * @param includePassword Whether to populate the password field.
     */
    private User mapUser(ResultSet rs, boolean includePassword) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setPassword(includePassword ? rs.getString("password") : null);
        return user;
    }
}