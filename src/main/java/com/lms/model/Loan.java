package com.lms.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * LoanApplication POJO for CreditHub Loan Management System.
 * This class maps to the 6-step wizard fields.
 */
public class Loan implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // STEP 1: Loan Details
    private String loanId;
    private String loanType;         
    private BigDecimal loanAmount;   
    private Integer tenureMonths;    
    private Double interestRate;     
    private String repaymentType;    
    private Integer preferredEmiDate; 
    
    // STEP 2: Personal Info
    private User user;     

    // STEP 3: Employment Details
    private EmploymentDetails employmentDetails;

    // STEP 4: Bank Details
    private AccountInfo accountInfo;    

    // STEP 5: Document Status (Tracking if uploaded)
    private String isPhotoUploaded;
    private String isSalarySlipUploaded;
    private String isItrUploaded;
    private String isBankStatementUploaded;
    private String isAadharUploaded;
    private String isPanUploaded;

    // SYSTEM FIELDS
    private String applicationStatus; // e.g., "Pending Review"
    private Date submissionDate;

    // Default Constructor
    public Loan() {}

	public String getLoanId() {
		return loanId;
	}

	public void setLoanId(String loanId) {
		this.loanId = loanId;
	}

	public String getLoanType() {
		return loanType;
	}

	public void setLoanType(String loanType) {
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

	public String getRepaymentType() {
		return repaymentType;
	}

	public void setRepaymentType(String repaymentType) {
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

	public String getIsPhotoUploaded() {
		return isPhotoUploaded;
	}

	public void setIsPhotoUploaded(String isPhotoUploaded) {
		this.isPhotoUploaded = isPhotoUploaded;
	}

	public String getIsSalarySlipUploaded() {
		return isSalarySlipUploaded;
	}

	public void setIsSalarySlipUploaded(String isSalarySlipUploaded) {
		this.isSalarySlipUploaded = isSalarySlipUploaded;
	}

	public String getIsItrUploaded() {
		return isItrUploaded;
	}

	public void setIsItrUploaded(String isItrUploaded) {
		this.isItrUploaded = isItrUploaded;
	}

	public String getIsBankStatementUploaded() {
		return isBankStatementUploaded;
	}

	public void setIsBankStatementUploaded(String isBankStatementUploaded) {
		this.isBankStatementUploaded = isBankStatementUploaded;
	}

	public String getIsAadharUploaded() {
		return isAadharUploaded;
	}

	public void setIsAadharUploaded(String isAadharUploaded) {
		this.isAadharUploaded = isAadharUploaded;
	}

	public String getIsPanUploaded() {
		return isPanUploaded;
	}

	public void setIsPanUploaded(String isPanUploaded) {
		this.isPanUploaded = isPanUploaded;
	}

	public String getApplicationStatus() {
		return applicationStatus;
	}

	public void setApplicationStatus(String applicationStatus) {
		this.applicationStatus = applicationStatus;
	}

	public Date getSubmissionDate() {
		return submissionDate;
	}

	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}
}