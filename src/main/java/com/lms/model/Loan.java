package com.lms.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.lms.constant.LoanApplicationStatus;
import com.lms.constant.LoanType;
import com.lms.constant.RepaymentType;

/**
 * Main entity representing a Loan application within CreditHub.
 * This class encapsulates the entire application lifecycle, linking the core loan 
 * parameters with the user's personal, employment, and banking information.
 */
public class Loan implements Serializable{
    
    private static final long serialVersionUID = 1718457507165011577L;
	// Core financial parameters of the loan
    private String loanId;
    private LoanType loanType;         
    private BigDecimal loanAmount;   
    private Integer tenureMonths;    
    private Double interestRate;     
    private RepaymentType repaymentType;    
    private Integer preferredEmiDate; 
    
    // Links to supporting entities for a complete profile
    private User user;     
    private EmploymentDetails employmentDetails;
    private AccountInfo accountInfo;    
    private UserLoanDocuments userDoc;

    // Internal system tracking fields
    private LoanApplicationStatus applicationStatus; 
    private Date submissionDate;
    private BigDecimal amountPaid;

    /**
     * Default constructor required for framework operations and reflection.
     */
    public Loan() {
        // Essential for ZK data binding and persistence frameworks
    }

    /**
     * Comprehensive constructor used to initialize a new loan application.
     */
    public Loan(String loanId, LoanType loanType, BigDecimal loanAmount, Integer tenureMonths, Double interestRate,
            RepaymentType repaymentType, Integer preferredEmiDate, User user, EmploymentDetails employmentDetails,
            AccountInfo accountInfo, UserLoanDocuments userDoc, LoanApplicationStatus applicationStatus,
            Date submissionDate) {
        this.loanId = loanId;
        this.loanType = loanType;
        this.loanAmount = loanAmount;
        this.tenureMonths = tenureMonths;
        this.interestRate = interestRate;
        this.repaymentType = repaymentType;
        this.preferredEmiDate = preferredEmiDate;
        this.user = user;
        this.employmentDetails = employmentDetails;
        this.accountInfo = accountInfo;
        this.userDoc = userDoc;
        this.applicationStatus = applicationStatus;
        this.submissionDate = submissionDate;
    }

    /* --- Standard Getters and Setters --- */

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public RepaymentType getRepaymentType() {
        return repaymentType;
    }

    public void setRepaymentType(RepaymentType repaymentType) {
        this.repaymentType = repaymentType;
    }

    public Integer getPreferredEmiDate() {
        return preferredEmiDate;
    }

    public void setPreferredEmiDate(Integer preferredEmiDate) {
        this.preferredEmiDate = preferredEmiDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EmploymentDetails getEmploymentDetails() {
        return employmentDetails;
    }

    public void setEmploymentDetails(EmploymentDetails employmentDetails) {
        this.employmentDetails = employmentDetails;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public UserLoanDocuments getUserDoc() {
        return userDoc;
    }

    public void setUserDoc(UserLoanDocuments userDoc) {
        this.userDoc = userDoc;
    }

    public LoanApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(LoanApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    /**
     * Generates a descriptive string for the loan application.
     * Useful for debugging and audit logs.
     */
    @Override
    public String toString() {
        return "Loan Record [ID=" + loanId + ", Type=" + loanType + ", Amount=" + loanAmount + 
               ", Status=" + applicationStatus + ", Submitted On=" + submissionDate + "]";
    }
}