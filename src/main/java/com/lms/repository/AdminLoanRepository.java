package com.lms.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.lms.model.*;
import com.lms.constant.*;
import com.lms.dbutils.DBConnection;

public class AdminLoanRepository {

    public static List<Loan> getAllLoans() {
        List<Loan> loanList = new ArrayList<>();
        
        String sql = "SELECT l.*, u.name, u.email, u.phone_number, u.role, u.profile_image, " +
                     "e.employment_type, e.employer_name, e.business_type, e.monthly_income, " +
                     "a.bank_name, a.branch_code, a.ifsc_code, a.account_number, a.balance, " +
                     "d.photo_uploaded, d.salary_slip_uploaded, d.itr_uploaded, " +
                     "d.bank_statement_uploaded, d.aadhar_uploaded, d.pan_uploaded " +
                     "FROM loans l " +
                     "LEFT JOIN users u ON l.user_id = u.id " +
                     "LEFT JOIN employment_details e ON l.employment_id = e.id " +
                     "LEFT JOIN account_info a ON l.account_id = a.id " +
                     "LEFT JOIN user_loan_documents d ON l.document_id = d.id " +
                     "ORDER BY l.submission_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
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

                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setProfileImage(rs.getString("profile_image"));
                user.setRole(Role.valueOf(rs.getString("role")));
                loan.setUser(user);

                EmploymentDetails emp = new EmploymentDetails();
                emp.setId(rs.getInt("employment_id"));
                emp.setEmploymentType(rs.getString("employment_type"));
                emp.setEmployerName(rs.getString("employer_name"));
                emp.setBusinessType(rs.getString("business_type"));
                emp.setMonthlyIncome(rs.getBigDecimal("monthly_income"));
                emp.setUser(user);
                loan.setEmploymentDetails(emp);

                AccountInfo acc = new AccountInfo();
                acc.setId(rs.getInt("account_id"));
                acc.setBankName(rs.getString("bank_name"));
                acc.setBranchCode(rs.getString("branch_code"));
                acc.setIfscCode(rs.getString("ifsc_code"));
                acc.setAccountNumber(rs.getString("account_number"));
                acc.setBalance(rs.getString("balance"));
                acc.setUser(user);
                loan.setAccountInfo(acc);

                UserLoanDocuments docs = new UserLoanDocuments();
                docs.setId(rs.getInt("document_id"));
                docs.setPhotoUploaded(rs.getString("photo_uploaded"));
                docs.setSalarySlipUploaded(rs.getString("salary_slip_uploaded"));
                docs.setItrUploaded(rs.getString("itr_uploaded"));
                docs.setBankStatementUploaded(rs.getString("bank_statement_uploaded"));
                docs.setAadharUploaded(rs.getString("aadhar_uploaded"));
                docs.setPanUploaded(rs.getString("pan_uploaded"));
                docs.setUser(user);
                loan.setUserDoc(docs);

                loanList.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching loans: " + e.getMessage());
            e.printStackTrace();
        }
        return loanList;
    }
    
    public static void main(String[] args) {
		List<Loan> allLoans = getAllLoans();
		
		for(Loan l : allLoans)
			System.out.println(l);
	}
}