package com.lms.repository;

import java.sql.*;
import java.util.*;

import com.lms.dbutils.DBConnection;

public class EmiTransactionRepository {

    public List<Map<String, Object>> getTransactionsByUserId(int userId) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        String sql = "SELECT t.created_at, l.loan_type, t.amount, t.payment_mode, t.txn_id, t.status " +
                     "FROM emi_transactions t " +
                     "JOIN loans l ON t.loan_id = l.loan_id " +
                     "WHERE l.user_id = ? ORDER BY t.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("date", new java.text.SimpleDateFormat("dd-MMM-yyyy").format(rs.getTimestamp("created_at")));
                    row.put("loanType", rs.getString("loan_type"));
                    row.put("amount", String.format("%.2f", rs.getDouble("amount")));
                    row.put("mode", rs.getString("payment_mode"));
                    row.put("txnId", rs.getString("txn_id"));
                    row.put("status", rs.getString("status"));
                    // Assuming receipts are generated based on TXN ID
                    row.put("receipt", "/receipts/" + rs.getString("txn_id") + ".pdf"); 
                    transactions.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}