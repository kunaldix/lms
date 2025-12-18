package com.lms.repository;

import java.sql.*;

import com.lms.dbutils.DBConnection;
import com.lms.model.Loan;

public class LoanRepository {

    public void applyLoan(Loan loan) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Save Employment Details
            int employmentId = saveEmployment(conn, loan);

            // 2. Save Bank Account Info
            int accountId = saveAccount(conn, loan);

            // 3. Save Document Status
            int documentId = saveDocuments(conn, loan);

            // 4. Save Master Loan Record
            saveMasterLoan(conn, loan, employmentId, accountId, documentId);

            conn.commit(); // Finalize transaction
            System.out.println("Loan application persisted successfully.");

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException se) { se.printStackTrace(); }
        }
    }

    private int saveEmployment(Connection conn, Loan loan) throws SQLException {
        String sql = "INSERT INTO employment_details (employment_type, employer_name, business_type, monthly_income, user_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, loan.getEmploymentDetails().getEmploymentType());
            pstmt.setString(2, loan.getEmploymentDetails().getEmployerName());
            pstmt.setString(3, loan.getEmploymentDetails().getBusinessType());
            pstmt.setBigDecimal(4, loan.getEmploymentDetails().getMonthlyIncome());
            pstmt.setInt(5, loan.getUser().getId());
            pstmt.executeUpdate();
            return getGeneratedId(pstmt);
        }
    }

    private int saveAccount(Connection conn, Loan loan) throws SQLException {
        String sql = "INSERT INTO account_info (bank_name, branch_code, ifsc_code, account_number, user_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, loan.getAccountInfo().getBankName());
            pstmt.setString(2, loan.getAccountInfo().getBranchCode());
            pstmt.setString(3, loan.getAccountInfo().getIfscCode());
            pstmt.setString(4, loan.getAccountInfo().getAccountNumber());
            pstmt.setInt(5, loan.getUser().getId());
            pstmt.executeUpdate();
            return getGeneratedId(pstmt);
        }
    }

    private int saveDocuments(Connection conn, Loan loan) throws SQLException {
        String sql = "INSERT INTO user_loan_documents (user_id, photo_uploaded, salary_slip_uploaded, itr_uploaded, bank_statement_uploaded, aadhar_uploaded, pan_uploaded) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, loan.getUser().getId());
            pstmt.setString(2, loan.getUserDoc().getPhotoUploaded());
            pstmt.setString(3, loan.getUserDoc().getSalarySlipUploaded());
            pstmt.setString(4, loan.getUserDoc().getItrUploaded());
            pstmt.setString(5, loan.getUserDoc().getBankStatementUploaded());
            pstmt.setString(6, loan.getUserDoc().getAadharUploaded());
            pstmt.setString(7, loan.getUserDoc().getPanUploaded());
            pstmt.executeUpdate();
            return getGeneratedId(pstmt);
        }
    }

    private void saveMasterLoan(Connection conn, Loan loan, int empId, int accId, int docId) throws SQLException {
        String sql = "INSERT INTO loans (loan_id, loan_type, loan_amount, tenure_months, interest_rate, repayment_type, preferred_emi_date, user_id, employment_id, account_id, document_id, application_status, submission_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loan.getLoanId());
            pstmt.setString(2, loan.getLoanType().toString());
            pstmt.setBigDecimal(3, loan.getLoanAmount());
            pstmt.setInt(4, loan.getTenureMonths());
            pstmt.setDouble(5, loan.getInterestRate());
            pstmt.setString(6, loan.getRepaymentType().toString());
            pstmt.setInt(7, loan.getPreferredEmiDate());
            pstmt.setInt(8, loan.getUser().getId());
            pstmt.setInt(9, empId);
            pstmt.setInt(10, accId);
            pstmt.setInt(11, docId);
            pstmt.setString(12, loan.getApplicationStatus().toString());
            pstmt.setTimestamp(13, new Timestamp(loan.getSubmissionDate().getTime()));
            pstmt.executeUpdate();
        }
    }

    private int getGeneratedId(PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Failed to retrieve generated ID.");
    }
}