package com.lms.model;

import java.io.Serializable;
import java.util.Date;

import com.lms.constant.EmiStatus;

/**
 * Model class representing an Equated Monthly Installment (EMI).
 * This entity tracks the lifecycle of a single payment installment,
 * including its due date, amount, and current payment status.
 */
public class Emi implements Serializable {
	
    private static final long serialVersionUID = 420221472511437536L;
    
    private String emiId;
    private int installmentNumber;
    private Date dueDate;
    private Double emiAmount;
    private double interestRate;
    private EmiStatus status;
    
    // Relationships to the parent loan and the borrower
    private Loan loan;
    private User user;

    /**
     * Default constructor for framework use (ZK data binding/Reflection).
     */
    public Emi() {
        super();
    }

    /**
     * Parameterized constructor to initialize a specific installment record.
     */
    public Emi(String emiId, int installmentNumber, Date dueDate, Double emiAmount, double interestRate,
            EmiStatus status, Loan loan, User user) {
        super();
        this.emiId = emiId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.emiAmount = emiAmount;
        this.interestRate = interestRate;
        this.status = status;
        this.loan = loan;
        this.user = user;
    }

    /* --- Standard Getters and Setters --- */

    public String getEmiId() {
        return emiId;
    }

    public void setEmiId(String emiId) {
        this.emiId = emiId;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(int installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Double getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(Double emiAmount) {
        this.emiAmount = emiAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public EmiStatus getStatus() {
        return status;
    }

    public void setStatus(EmiStatus status) {
        this.status = status;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns a string summary of the EMI installment for logging purposes.
     */
    @Override
    public String toString() {
        return "Emi [ID=" + emiId + ", Installment #" + installmentNumber + 
               ", Due Date=" + dueDate + ", Amount=" + emiAmount + 
               ", Status=" + status + ", Loan ID=" + (loan != null ? loan.getLoanId() : "N/A") + "]";
    }
}