package com.lms.repository;

import java.sql.*;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lms.dbutils.DBConnection;

/**
 * Repository class for managing EMI transaction history data.
 * This class specifically handles fetching formatted transaction records 
 * for the user dashboard and report generation.
 */
public class EmiTransactionRepository {

    // Initializing Log4j to track database queries and potential connection issues
    private static final Logger logger = LogManager.getLogger(EmiTransactionRepository.class);

    /**
     * Retrieves a detailed list of all EMI payments made by a specific user.
     * Joins the transactions table with the loans table to provide context like loan type.
     * * @param userId The unique identifier of the customer.
     * @return A list of maps, where each map represents a single transaction row.
     */
    public List<Map<String, Object>> getTransactionsByUserId(int userId) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        
        // SQL query to pull audit details for payments, ordered by most recent first
        String sql = "SELECT t.created_at, l.loan_type, t.amount, t.payment_mode, t.txn_id, t.status " +
                     "FROM emi_transactions t " +
                     "JOIN loans l ON t.loan_id = l.loan_id " +
                     "WHERE l.user_id = ? ORDER BY t.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            logger.info("Fetching transaction history for User ID: {}", userId);
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    
                    // Formatting date to a standard readable format (e.g., 06-Jan-2026)
                    row.put("date", new java.text.SimpleDateFormat("dd-MMM-yyyy").format(rs.getTimestamp("created_at")));
                    
                    row.put("loanType", rs.getString("loan_type"));
                    
                    // Ensuring amount is consistently formatted with two decimal places
                    row.put("amount", String.format("%.2f", rs.getDouble("amount")));
                    
                    row.put("mode", rs.getString("payment_mode"));
                    row.put("txnId", rs.getString("txn_id"));
                    row.put("status", rs.getString("status"));
                    
                    // Generating a virtual path for the PDF receipt based on the unique Transaction ID
                    row.put("receipt", "/receipts/" + rs.getString("txn_id") + ".pdf"); 
                    
                    transactions.add(row);
                }
                logger.info("Successfully retrieved {} transactions for User ID: {}", transactions.size(), userId);
            }
        } catch (SQLException e) {
            // Logging the error with full context for easier debugging on the server
            logger.error("Database error while retrieving transactions for User {}: {}", userId, e.getMessage());
        }
        
        return transactions;
    }
}