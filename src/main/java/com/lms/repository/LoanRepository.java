package com.lms.repository;

import java.math.BigDecimal;
import java.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lms.constant.LoanApplicationStatus;
import com.lms.dbutils.DBConnection;
import com.lms.model.Loan;

public class LoanRepository {

    private static final Logger logger = LogManager.getLogger(LoanRepository.class);

    public void applyLoan(Loan loan) {
        Connection conn = null;
        logger.info("Starting loan application process for User ID: {} and Loan ID: {}", loan.getUser().getId(),
                loan.getLoanId());

        try {
            conn = DBConnection.getConnection();
            logger.debug("Database connection established successfully.");
            conn.setAutoCommit(false);

            // 1. Save Employment Details
            int employmentId = saveEmployment(conn, loan);
            logger.debug("Employment details saved with ID: {}", employmentId);

            // 2. Save Bank Account Info
            int accountId = saveAccount(conn, loan);
            logger.debug("Account info saved with ID: {}", accountId);

            // 3. Save Document Status
            int documentId = saveDocuments(conn, loan);
            logger.debug("Document records saved with ID: {}", documentId);

            // 4. Save Master Loan Record
            saveMasterLoan(conn, loan, employmentId, accountId, documentId);

            conn.commit();
            logger.info("Loan application {} successfully persisted and committed.", loan.getLoanId());

        } catch (Exception e) {
            logger.error("Error occurred while persisting loan application {}. Initiating rollback.", loan.getLoanId(),
                    e);
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Transaction rollback completed successfully.");
                } catch (SQLException se) {
                    logger.error("Failed to rollback transaction.", se);
                }
            }
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    logger.debug("Database connection closed.");
                }
            } catch (SQLException se) {
                logger.error("Error while closing database connection.", se);
            }
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
            int id = getGeneratedId(pstmt);
            logger.trace("Employment record created: User ID {}", loan.getUser().getId());
            return id;
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
        String sql = "INSERT INTO loans (loan_id, loan_type, loan_amount, amount_paid, tenure_months, "
                + "interest_rate, repayment_type, preferred_emi_date, user_id, employment_id, "
                + "account_id, document_id, application_status, submission_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loan.getLoanId());
            pstmt.setString(2, loan.getLoanType().toString());
            pstmt.setBigDecimal(3, loan.getLoanAmount());
            pstmt.setBigDecimal(4, java.math.BigDecimal.ZERO);
            pstmt.setInt(5, loan.getTenureMonths());
            pstmt.setDouble(6, loan.getInterestRate());
            pstmt.setString(7, loan.getRepaymentType().toString());
            pstmt.setInt(8, loan.getPreferredEmiDate());
            pstmt.setInt(9, loan.getUser().getId());
            pstmt.setInt(10, empId);
            pstmt.setInt(11, accId);
            pstmt.setInt(12, docId);
            pstmt.setString(13, loan.getApplicationStatus().toString());
            pstmt.setTimestamp(14, new java.sql.Timestamp(loan.getSubmissionDate().getTime()));

            pstmt.executeUpdate();
            logger.debug("Master loan record inserted: {}", loan.getLoanId());
        }
    }

    private int getGeneratedId(PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            if (rs.next())
                return rs.getInt(1);
        }
        logger.error("Database failed to return a generated key.");
        throw new SQLException("Failed to retrieve generated ID.");
    }

    public String getAllDebt(int id) {

        BigDecimal totalDebt = BigDecimal.ZERO;

        try (Connection conn = DBConnection.getConnection()) {

            String query = "SELECT loan_amount, amount_paid FROM loans WHERE user_id = ? and application_status = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.setString(2, LoanApplicationStatus.ACCEPTED.name());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BigDecimal loanAmount = rs.getBigDecimal("loan_amount");
                BigDecimal amountPaid = rs.getBigDecimal("amount_paid");

                if (loanAmount == null)
                    loanAmount = BigDecimal.ZERO;
                if (amountPaid == null)
                    amountPaid = BigDecimal.ZERO;

                BigDecimal remainingDebt = loanAmount.subtract(amountPaid);
                totalDebt = totalDebt.add(remainingDebt);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalDebt.toString(); // return as String
    }

    public int getActiveLoan(int userId) {

        int activeLoanCount = 0;

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT COUNT(*) FROM loans " + "WHERE application_status = ? AND user_id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "ACCEPTED");
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                activeLoanCount = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activeLoanCount;
    }

    public String getTotalLoan(int id) {

        BigDecimal totalLoan = BigDecimal.ZERO;

        try (Connection conn = DBConnection.getConnection()) {

            String query = "SELECT loan_amount FROM loans WHERE user_id = ? AND application_status = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.setString(2, LoanApplicationStatus.ACCEPTED.name());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BigDecimal loanAmount = rs.getBigDecimal("loan_amount");

                if (loanAmount == null) {
                    loanAmount = BigDecimal.ZERO;
                }

                totalLoan = totalLoan.add(loanAmount);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalLoan.toString();
    }

}