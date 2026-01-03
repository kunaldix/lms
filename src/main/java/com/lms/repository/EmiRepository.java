package com.lms.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.lms.constant.EmiStatus;
import com.lms.constant.LoanType;
import com.lms.dbutils.DBConnection;
import com.lms.model.Emi;
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
}
