package com.lms.repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lms.constant.EmiStatus;
import com.lms.constant.LoanType;
import com.lms.dbutils.DBConnection;
import com.lms.model.Emi;
import com.lms.model.EmiTransaction;
import com.lms.model.Loan;

/**
 * Repository class handling all database operations related to the EMI lifecycle.
 * This includes schedule generation, payment recording via transactions, and 
 * analytical queries for dashboard reporting.
 */
public class EmiRepository {

    private static final Logger logger = LogManager.getLogger(EmiRepository.class);

    /**
     * Inserts a single EMI record into the database.
     */
    public boolean saveEmiRecord(Emi emi) {
        String sql = "INSERT INTO emi_schedule (emi_id, installment_number, due_date, emi_amount, " +
                     "interest_rate, status, loan_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            logger.info("Saving single EMI record: {}", emi.getEmiId());
            
            ps.setString(1, emi.getEmiId());
            ps.setInt(2, emi.getInstallmentNumber());
            ps.setDate(3, new java.sql.Date(emi.getDueDate().getTime()));
            ps.setDouble(4, emi.getEmiAmount());
            ps.setDouble(5, emi.getInterestRate());
            ps.setString(6, emi.getStatus().name());
            ps.setString(7, emi.getLoan().getLoanId());
            ps.setInt(8, emi.getUser().getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("Error saving EMI record for ID {}: {}", emi.getEmiId(), e.getMessage());
            return false;
        }
    }

    /**
     * Efficiently saves multiple EMI installments using JDBC batch processing.
     * This minimizes database round-trips for large loan tenures.
     */
    public void saveEmiBatch(List<Emi> emiList) {
        String sql = "INSERT INTO emi_schedule (emi_id, installment_number, due_date, emi_amount, " +
                     "interest_rate, status, loan_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            logger.info("Initiating batch save for {} EMI installments", emiList.size());

            for (Emi emi : emiList) {
                ps.setString(1, emi.getEmiId());
                ps.setInt(2, emi.getInstallmentNumber());
                ps.setDate(3, new java.sql.Date(emi.getDueDate().getTime()));
                ps.setDouble(4, emi.getEmiAmount());
                ps.setDouble(5, emi.getInterestRate());
                ps.setString(6, emi.getStatus().name());
                ps.setString(7, emi.getLoan().getLoanId());
                ps.setInt(8, emi.getUser().getId());
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
            logger.info("Successfully committed EMI batch save.");

        } catch (SQLException e) {
            logger.error("Failed to execute EMI batch save: {}", e.getMessage());
        }
    }

    /**
     * Fetches the next upcoming installments for a user across all their loans.
     * It ensures only the earliest pending EMI per loan is returned.
     */
    public List<Emi> getUpcomingEmisForUser(int userId) {
        List<Emi> emis = new ArrayList<>();
        String sql = "SELECT e.*, l.* FROM emi_schedule e " +
                     "JOIN loans l ON e.loan_id = l.loan_id " +
                     "WHERE e.user_id = ? AND e.status IN ('PENDING', 'OVERDUE') " +
                     "AND e.due_date = (" +
                     "    SELECT MIN(e2.due_date) FROM emi_schedule e2 " +
                     "    WHERE e2.loan_id = e.loan_id AND e2.status IN ('PENDING', 'OVERDUE')" +
                     ") " +
                     "ORDER BY e.due_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Emi emi = new Emi();
                emi.setEmiId(rs.getString("emi_id"));
                emi.setInstallmentNumber(rs.getInt("installment_number"));
                emi.setDueDate(rs.getDate("due_date"));
                emi.setEmiAmount(rs.getDouble("emi_amount"));
                emi.setInterestRate(rs.getDouble("interest_rate"));
                emi.setStatus(EmiStatus.valueOf(rs.getString("status")));

                Loan loan = new Loan();
                loan.setLoanId(rs.getString("loan_id"));
                loan.setLoanType(LoanType.valueOf(rs.getString("loan_type")));
                loan.setLoanAmount(rs.getBigDecimal("loan_amount"));
                loan.setAmountPaid(rs.getBigDecimal("amount_paid"));
                loan.setTenureMonths(rs.getInt("tenure_months"));
                emi.setLoan(loan);

                emis.add(emi);
            }
            logger.info("Retrieved {} upcoming installments for user ID: {}", emis.size(), userId);
        } catch (SQLException ex) {
            logger.error("Error fetching upcoming EMIs for user {}: {}", userId, ex.getMessage());
        }
        return emis;
    }

    /**
     * Executes an atomic database transaction to record a payment.
     * It inserts the transaction record, marks the EMI as PAID, and updates the accumulated paid amount on the loan.
     */
    public boolean recordPayment(EmiTransaction txn, String emiId, String loanId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            logger.info("Starting payment transaction for EMI: {} and Loan: {}", emiId, loanId);

            // Insert audit record for the transaction
            String sql1 = "INSERT INTO emi_transactions (txn_id, emi_id, loan_id, payu_id, amount, status, payment_mode) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                ps1.setString(1, txn.getTxnId());
                ps1.setString(2, emiId);
                ps1.setString(3, loanId);
                ps1.setString(4, txn.getPayuId());
                ps1.setDouble(5, txn.getAmount());
                ps1.setString(6, txn.getStatus().name());
                ps1.setString(7, txn.getPaymentMode().name());
                ps1.executeUpdate();
            }

            // Update individual installment status
            String sql2 = "UPDATE emi_schedule SET status = 'PAID' WHERE emi_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setString(1, emiId);
                ps2.executeUpdate();
            }

            // Update global loan paid balance
            String sql3 = "UPDATE loans SET amount_paid = amount_paid + ? WHERE loan_id = ?";
            try (PreparedStatement ps3 = conn.prepareStatement(sql3)) {
                ps3.setDouble(1, txn.getAmount());
                ps3.setString(2, loanId);
                ps3.executeUpdate();
            }

            conn.commit(); 
            logger.info("Payment transaction committed successfully for EMI: {}", emiId);
            return true;

        } catch (SQLException e) {
            logger.error("Transaction failed for EMI ID {}. Rolling back... {}", emiId, e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { logger.error("Rollback failed!", ex); }
            }
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { logger.error("Closing connection failed!", e); }
        }
    }

    /**
     * Retrieves specific installment details required before initiating a payment transaction.
     */
    public String[] getEmiDetails(String emiId) {
        String sql = "SELECT loan_id, emi_amount FROM emi_schedule WHERE emi_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emiId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("loan_id"), String.valueOf(rs.getDouble("emi_amount"))};
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching EMI details for ID {}: {}", emiId, e.getMessage());
        }
        return null;
    }

    /**
     * Admin Dashboard: Counts how many pending installments have crossed their due date.
     */
    public int countOverdueLoans() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM emi_schedule WHERE status = 'PENDING' AND due_date < CURRENT_DATE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt(1);
            }
            logger.info("Found {} overdue installments across the platform.", count);

        } catch (SQLException e) {
            logger.error("Error counting overdue loans: {}", e.getMessage());
        }
        return count;
    }

    /**
     * User Dashboard: Calculates the next amount the user needs to pay, prioritizing overdue amounts.
     */
    public BigDecimal getNextEmiAmountDue(int userId) {
        BigDecimal emiAmount = BigDecimal.ZERO;
        String sql = """
            SELECT emi_amount
            FROM emi_schedule
            WHERE user_id = ? AND status = 'PENDING'
            ORDER BY CASE WHEN due_date < CURRENT_DATE THEN 0 ELSE 1 END, due_date ASC
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    emiAmount = rs.getBigDecimal("emi_amount");
                }
            }
            logger.info("Next due amount for User {}: {}", userId, emiAmount);

        } catch (SQLException e) {
            logger.error("Error fetching next EMI amount for user {}: {}", userId, e.getMessage());
        }
        return emiAmount;
    }

    /**
     * User Dashboard: Retrieves the last 5 successful payments for charting purposes.
     */
    public LinkedHashMap<YearMonth, BigDecimal> getLast5PaidMonthsEmi(int userId) {
        LinkedHashMap<YearMonth, BigDecimal> result = new LinkedHashMap<>();
        String sql = """
            SELECT YEAR(due_date) AS yr, MONTH(due_date) AS mn, SUM(emi_amount) AS total_paid
            FROM emi_schedule
            WHERE user_id = ? AND status = 'PAID'
            GROUP BY yr, mn
            ORDER BY yr DESC, mn DESC
            LIMIT 5
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    YearMonth ym = YearMonth.of(rs.getInt("yr"), rs.getInt("mn"));
                    result.put(ym, rs.getBigDecimal("total_paid"));
                }
            }
            logger.info("Successfully generated last 5 paid months history for user {}.", userId);

        } catch (SQLException e) {
            logger.error("Error generating payment history chart for user {}: {}", userId, e.getMessage());
        }
        return result;
    }

    /**
     * Identifies the most recent month where a payment was successfully recorded.
     */
    public YearMonth getLatestPaidEmiMonth(int userId) {
        YearMonth latest = null;
        String sql = """
            SELECT YEAR(due_date) AS yr, MONTH(due_date) AS mn
            FROM emi_schedule
            WHERE user_id = ? AND status = 'PAID'
            ORDER BY due_date DESC
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    latest = YearMonth.of(rs.getInt("yr"), rs.getInt("mn"));
                }
            }

        } catch (SQLException e) {
            logger.error("Error fetching latest paid month for user {}: {}", userId, e.getMessage());
        }
        return latest;
    }
}