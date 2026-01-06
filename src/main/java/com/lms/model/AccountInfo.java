package com.lms.model;

import java.io.Serializable;

/**
 * Model class representing a user's bank account details.
 * This is used to manage disbursement and repayment account information
 * within the CreditHub system.
 */
public class AccountInfo implements Serializable{
    
    private static final long serialVersionUID = -118442953670089429L;
    
	private int id;
    private String bankName;        
    private String branchCode;       
    private String ifscCode;         
    private String accountNumber;
    private String balance;
    private User user;
    
    /**
     * Default constructor for framework-level operations like reflection or ZK data binding.
     */
    public AccountInfo() {
        // Required for frameworks and manual instantiation
    }

    /**
     * Overloaded constructor to initialize a complete AccountInfo object.
     */
    public AccountInfo(int id, String bankName, String branchCode, String ifscCode, String accountNumber,
            String balance, User user) {
        this.id = id;
        this.bankName = bankName;
        this.branchCode = branchCode;
        this.ifscCode = ifscCode;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.user = user;
    }

    /* --- Standard Getters and Setters --- */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Generates a string representation of the account info for logging or debugging.
     */
    @Override
    public String toString() {
        return "AccountInfo [id=" + id + ", bankName=" + bankName + ", branchCode=" + branchCode + 
               ", ifscCode=" + ifscCode + ", accountNumber=" + accountNumber + 
               ", balance=" + balance + ", user=" + (user != null ? user.getId() : "null") + "]";
    }
}