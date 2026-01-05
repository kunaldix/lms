package com.lms.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import com.lms.constant.EmiStatus;
import com.lms.constant.LoanType;
import com.lms.dbutils.DBConnection;
import com.lms.model.Emi;
import com.lms.model.EmiTransaction;
import com.lms.model.Loan; 

public class EmiRepository {

    public boolean saveEmiRecord(Emi emi) {
        String sql = "INSERT INTO emi_schedule (emi_id, installment_number, due_date, emi_amount, " +
                     "interest_rate, status, loan_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
            e.printStackTrace();
            return false;
        }
    }

    public void saveEmiBatch(List<Emi> emiList) {
        String sql = "INSERT INTO emi_schedule (emi_id, installment_number, due_date, emi_amount, " +
                     "interest_rate, status, loan_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
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
                
                // Map the Enum from the VARCHAR column
                emi.setStatus(EmiStatus.valueOf(rs.getString("status")));

                // Map Loan details for the UI
                Loan loan = new Loan();
                loan.setLoanId(rs.getString("loan_id"));
                loan.setLoanType(LoanType.valueOf(rs.getString("loan_type")));
                loan.setLoanAmount(rs.getBigDecimal("loan_amount")); // Get total loan
                loan.setAmountPaid(rs.getBigDecimal("amount_paid")); // Get amount already paid
                loan.setTenureMonths(rs.getInt("tenure_months"));
                emi.setLoan(loan);

                emis.add(emi);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return emis;
    }
    
    public boolean recordPayment(EmiTransaction txn, String emiId, String loanId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert Transaction Record
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

            // 2. Update EMI Schedule Status
            String sql2 = "UPDATE emi_schedule SET status = 'PAID' WHERE emi_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setString(1, emiId);
                ps2.executeUpdate();
            }

            // 3. Update Loans table (Accumulate amount_paid)
            String sql3 = "UPDATE loans SET amount_paid = amount_paid + ? WHERE loan_id = ?";
            try (PreparedStatement ps3 = conn.prepareStatement(sql3)) {
                ps3.setDouble(1, txn.getAmount());
                ps3.setString(2, loanId);
                ps3.executeUpdate();
            }

            conn.commit(); // Save all changes
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Helper to get loan_id and amount before starting the transaction
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
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    
    public int countOverdueLoans() {

        int count = 0;

        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
                SELECT COUNT(*)
                FROM emi_schedule
                WHERE status = ?
                  AND due_date < CURRENT_DATE
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "PENDING");

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }
    
    public BigDecimal getNextEmiAmountDue(int userId) {

        BigDecimal emiAmount = BigDecimal.ZERO;

        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
                SELECT emi_amount
                FROM emi_schedule
                WHERE user_id = ?
                  AND status = ?
                ORDER BY 
                    CASE WHEN due_date < CURRENT_DATE THEN 0 ELSE 1 END,
                    due_date ASC
                LIMIT 1
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, "PENDING");

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                emiAmount = rs.getBigDecimal("emi_amount");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return emiAmount;
    }

    public LinkedHashMap<YearMonth, BigDecimal> getLast5PaidMonthsEmi(int userId) {

        LinkedHashMap<YearMonth, BigDecimal> result = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
                SELECT 
                    YEAR(due_date) AS yr,
                    MONTH(due_date) AS mn,
                    SUM(emi_amount) AS total_paid
                FROM emi_schedule
                WHERE user_id = ?
                  AND status = ?
                GROUP BY yr, mn
                ORDER BY yr DESC, mn DESC
                LIMIT 5
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, "PAID");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                YearMonth ym = YearMonth.of(
                    rs.getInt("yr"),
                    rs.getInt("mn")
                );
                result.put(ym, rs.getBigDecimal("total_paid"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public YearMonth getLatestPaidEmiMonth(int userId) {

        YearMonth latest = null;

        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
                SELECT YEAR(due_date) AS yr, MONTH(due_date) AS mn
                FROM emi_schedule
                WHERE user_id = ?
                  AND status = ?
                ORDER BY due_date DESC
                LIMIT 1
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, "PAID");

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                latest = YearMonth.of(rs.getInt("yr"), rs.getInt("mn"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return latest;
    }

    
}