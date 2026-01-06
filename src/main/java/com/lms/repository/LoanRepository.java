package com.lms.repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lms.constant.LoanApplicationStatus;
import com.lms.constant.LoanType;
import com.lms.constant.RepaymentType;
import com.lms.dbutils.DBConnection;
import com.lms.model.Loan;
import com.lms.model.User;

/**
 * Repository class responsible for managing the lifecycle of loan records.
 * It handles multi-table transaction management for new applications and 
 * provides statistical data for user and admin dashboards.
 */
public class LoanRepository {

    private static final Logger logger = LogManager.getLogger(LoanRepository.class);

    /**
     * Persists a complete loan application by coordinating across four distinct tables.
     * Uses a transaction block to ensure that if any step fails, no partial data is saved.
     */
    public void applyLoan(Loan loan) {
        Connection conn = null;
        logger.info("Initiating database transaction for new Loan Application: {}", loan.getLoanId());

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin manual transaction

            // Step 1: Record employment history
            int employmentId = saveEmployment(conn, loan);
            
            // Step 2: Record bank disbursement details
            int accountId = saveAccount(conn, loan);
            
            // Step 3: Record uploaded document metadata
            int documentId = saveDocuments(conn, loan);

            // Step 4: Create the primary loan record linking the IDs from steps 1-3
            saveMasterLoan(conn, loan, employmentId, accountId, documentId);

            conn.commit();
            logger.info("Successfully committed loan application transaction for ID: {}", loan.getLoanId());

        } catch (Exception e) {
            logger.error("Transaction failed for loan application {}. Executing rollback.", loan.getLoanId(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Rollback performed successfully to maintain data integrity.");
                } catch (SQLException se) {
                    logger.error("Critical: Failed to rollback the failed transaction.", se);
                }
            }
        } finally {
            closeQuietly(conn);
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
        String sql = "INSERT INTO loans (loan_id, loan_type, loan_amount, amount_paid, tenure_months, "
                + "interest_rate, repayment_type, preferred_emi_date, user_id, employment_id, "
                + "account_id, document_id, application_status, submission_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loan.getLoanId());
            pstmt.setString(2, loan.getLoanType().toString());
            pstmt.setBigDecimal(3, loan.getLoanAmount());
            pstmt.setBigDecimal(4, BigDecimal.ZERO);
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
            logger.debug("Successfully inserted master loan entry for ID: {}", loan.getLoanId());
        }
    }

    private int getGeneratedId(PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Critical Failure: Database did not return an auto-generated primary key.");
    }

    /**
     * Calculates the total remaining debt for a user by subtracting paid amounts 
     * from the principal of all accepted loans.
     */
    public String getAllDebt(int id) {
        BigDecimal totalDebt = BigDecimal.ZERO;
        String query = "SELECT loan_amount, amount_paid FROM loans WHERE user_id = ? and application_status = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            stmt.setString(2, LoanApplicationStatus.ACCEPTED.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal loanAmount = rs.getBigDecimal("loan_amount");
                    BigDecimal amountPaid = rs.getBigDecimal("amount_paid");

                    loanAmount = (loanAmount == null) ? BigDecimal.ZERO : loanAmount;
                    amountPaid = (amountPaid == null) ? BigDecimal.ZERO : amountPaid;

                    totalDebt = totalDebt.add(loanAmount.subtract(amountPaid));
                }
            }
        } catch (SQLException e) {
            logger.error("Error calculating total debt for User ID: {}", id, e);
        }
        return totalDebt.toString();
    }

    /**
     * Retrieves the count of all loans currently in 'ACCEPTED' status for a specific user.
     */
    public int getActiveLoan(int userId) {
        int activeLoanCount = 0;
        String sql = "SELECT COUNT(*) FROM loans WHERE application_status = 'ACCEPTED' AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) activeLoanCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error fetching active loan count for User ID: {}", userId, e);
        }
        return activeLoanCount;
    }

    /**
     * Global stat: Returns the total number of loans in 'ACCEPTED' status across the platform.
     */
    public int getTotalActiveLoans() {
        int activeLoanCount = 0;
        String sql = "SELECT COUNT(*) FROM loans WHERE application_status = 'ACCEPTED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) activeLoanCount = rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error fetching global active loan statistics.", e);
        }
        return activeLoanCount;
    }
    
    /**
     * Calculates the total principal amount of all accepted loans for a specific user.
     */
    public String getTotalLoan(int id) {
        BigDecimal totalLoan = BigDecimal.ZERO;
        String query = "SELECT loan_amount FROM loans WHERE user_id = ? AND application_status = 'ACCEPTED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal amount = rs.getBigDecimal("loan_amount");
                    totalLoan = totalLoan.add((amount == null) ? BigDecimal.ZERO : amount);
                }
            }
        } catch (SQLException e) {
            logger.error("Error calculating total loan principal for User ID: {}", id, e);
        }
        return totalLoan.toString();
    }
    
    /**
     * Global stat: Returns the all-time total count of loan applications received.
     */
    public int getTotalLoans() {
        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM loans");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error fetching total loan count statistics.", e);
        }
        return count;
    }

    /**
     * Global stat: Returns the count of applications currently in 'PENDING' status.
     */
    public int getPendingLoans() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM loans WHERE application_status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error fetching pending loan statistics.", e);
        }
        return count;
    }

    /**
     * Retrieves all loan applications associated with a specific user, including basic profile info.
     */
    public List<Loan> getLoansByUserId(int userId) {
        List<Loan> loanList = new ArrayList<>();
        String sql = "SELECT l.*, u.name, u.email, u.phone_number FROM loans l JOIN users u ON l.user_id = u.id WHERE l.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);          
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Loan loan = mapLoanBasic(rs);
                    User user = new User();
                    user.setId(userId);
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhoneNumber(rs.getString("phone_number"));
                    loan.setUser(user);
                    loanList.add(loan);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving loan list for User ID: {}", userId, e);
        }
        return loanList;
    }

    /**
     * Admin Tool: Retrieves all applications that have been approved across the platform.
     */
    public List<Loan> getAllApprovedLoans() {
        List<Loan> loanList = new ArrayList<>();
        String sql = "SELECT l.*, u.name, u.email, u.phone_number FROM loans l JOIN users u ON l.user_id = u.id WHERE application_status = 'ACCEPTED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Loan loan = mapLoanBasic(rs);
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                loan.setUser(user);
                loanList.add(loan);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving global list of approved loans.", e);
        }
        return loanList;
    }

    /**
     * Analytical Query: Aggregates the number of applications submitted per month for the last 5 months.
     * Provides data for dashboard trend charts.
     */
    public Map<YearMonth, Integer> getLast5MonthsLoanCount() {
        Map<YearMonth, Integer> dbData = new HashMap<>();
        String sql = "SELECT YEAR(submission_date) yr, MONTH(submission_date) mn, COUNT(*) total FROM loans GROUP BY yr, mn";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dbData.put(YearMonth.of(rs.getInt("yr"), rs.getInt("mn")), rs.getInt("total"));
            }
        } catch (Exception e) {
            logger.error("Error aggregating monthly loan application counts.", e);
        }

        Map<YearMonth, Integer> finalData = new LinkedHashMap<>();
        YearMonth current = YearMonth.now().minusMonths(4);

        for (int i = 0; i < 5; i++) {
            finalData.put(current, dbData.getOrDefault(current, 0));
            current = current.plusMonths(1);
        }
        return finalData;
    }

    private Loan mapLoanBasic(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setLoanId(rs.getString("loan_id"));
        loan.setLoanType(LoanType.valueOf(rs.getString("loan_type")));
        loan.setLoanAmount(rs.getBigDecimal("loan_amount"));
        loan.setTenureMonths(rs.getInt("tenure_months"));
        loan.setInterestRate(rs.getDouble("interest_rate"));
        loan.setRepaymentType(RepaymentType.valueOf(rs.getString("repayment_type")));
        loan.setPreferredEmiDate(rs.getInt("preferred_emi_date"));
        loan.setApplicationStatus(LoanApplicationStatus.valueOf(rs.getString("application_status")));
        loan.setSubmissionDate(rs.getTimestamp("submission_date"));
        loan.setAmountPaid(rs.getBigDecimal("amount_paid"));
        return loan;
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException se) {
                logger.error("Failed to close database connection gracefully.", se);
            }
        }
    }
}