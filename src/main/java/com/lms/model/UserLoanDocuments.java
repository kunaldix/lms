package com.lms.model;

import java.io.Serializable;

/**
 * Model class for tracking the status and storage paths of required loan documents.
 * This entity helps the system verify if a user has submitted all necessary 
 * paperwork before the admin review process begins.
 */
public class UserLoanDocuments implements Serializable{

    private static final long serialVersionUID = 2858512325465808242L;
    
	private int id;
    private User user;
    private String photoUploaded;
    private String salarySlipUploaded;
    private String itrUploaded;
    private String bankStatementUploaded;
    private String aadharUploaded;
    private String panUploaded;

    /**
     * Default constructor for framework compatibility and manual instantiation.
     */
    public UserLoanDocuments() {
    }

    /**
     * Parameterized constructor to initialize a complete document profile for a loan applicant.
     */
    public UserLoanDocuments(int id, User user, String photoUploaded, String salarySlipUploaded, String itrUploaded,
            String bankStatementUploaded, String aadharUploaded, String panUploaded) {
        this.id = id;
        this.user = user;
        this.photoUploaded = photoUploaded;
        this.salarySlipUploaded = salarySlipUploaded;
        this.itrUploaded = itrUploaded;
        this.bankStatementUploaded = bankStatementUploaded;
        this.aadharUploaded = aadharUploaded;
        this.panUploaded = panUploaded;
    }

    /* --- Getters and Setters --- */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPhotoUploaded() {
        return photoUploaded;
    }

    public void setPhotoUploaded(String photoUploaded) {
        this.photoUploaded = photoUploaded;
    }

    public String getSalarySlipUploaded() {
        return salarySlipUploaded;
    }

    public void setSalarySlipUploaded(String salarySlipUploaded) {
        this.salarySlipUploaded = salarySlipUploaded;
    }

    public String getItrUploaded() {
        return itrUploaded;
    }

    public void setItrUploaded(String itrUploaded) {
        this.itrUploaded = itrUploaded;
    }

    public String getBankStatementUploaded() {
        return bankStatementUploaded;
    }

    public void setBankStatementUploaded(String bankStatementUploaded) {
        this.bankStatementUploaded = bankStatementUploaded;
    }

    public String getAadharUploaded() {
        return aadharUploaded;
    }

    public void setAadharUploaded(String aadharUploaded) {
        this.aadharUploaded = aadharUploaded;
    }

    public String getPanUploaded() {
        return panUploaded;
    }

    public void setPanUploaded(String panUploaded) {
        this.panUploaded = panUploaded;
    }

    /**
     * Returns a string representation of the document status for debugging and audit logs.
     * Only the user ID is included to keep logs concise.
     */
    @Override
    public String toString() {
        return "UserLoanDocuments [Record ID=" + id + 
               ", User ID=" + (user != null ? user.getId() : "N/A") + 
               ", Photo=" + photoUploaded + 
               ", Salary Slip=" + salarySlipUploaded + 
               ", ITR=" + itrUploaded + 
               ", Bank Statement=" + bankStatementUploaded + 
               ", Aadhar=" + aadharUploaded + 
               ", PAN=" + panUploaded + "]";
    }
}