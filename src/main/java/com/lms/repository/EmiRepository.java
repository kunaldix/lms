package com.lms.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.lms.dbutils.DBConnection;
import com.lms.model.Emi; 

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
}
