package com.lms.model;

import java.math.BigDecimal;
import java.util.Date;

import com.lms.constant.LoanApplicationStatus;
import com.lms.constant.LoanType;
import com.lms.constant.RepaymentType;

/**
 * LoanApplication POJO for CreditHub Loan Management System.
 * This class maps to the 6-step wizard fields.
 */
public class Loan{
    
    // STEP 1: Loan Details
    private String loanId;
    private LoanType loanType;         
    private BigDecimal loanAmount;   
    private Integer tenureMonths;    
    private Double interestRate;     
    private RepaymentType repaymentType;    
    private Integer preferredEmiDate; 
    
    // STEP 2: Personal Info
    private User user;     

    // STEP 3: Employment Details
    private EmploymentDetails employmentDetails;

    // STEP 4: Bank Details
    private AccountInfo accountInfo;    

    // STEP 5: Document Status (Tracking if uploaded)
    private UserLoanDocuments userDoc;

    // SYSTEM FIELDS
    private LoanApplicationStatus applicationStatus; 
    private Date submissionDate;

    // Default Constructor
    public Loan() {}

	public Loan(String loanId, LoanType loanType, BigDecimal loanAmount, Integer tenureMonths, Double interestRate,
			RepaymentType repaymentType, Integer preferredEmiDate, User user, EmploymentDetails employmentDetails,
			AccountInfo accountInfo, UserLoanDocuments userDoc, LoanApplicationStatus applicationStatus,
			Date submissionDate) {
		super();
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
}