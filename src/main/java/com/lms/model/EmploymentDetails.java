package com.lms.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Model class that stores the employment and income information of a user.
 * This data is essential for the credit appraisal process to determine 
 * the repayment capacity of the loan applicant.
 */
public class EmploymentDetails implements Serializable{
    
    private static final long serialVersionUID = 5568063736180994791L;
    
	private Integer id;
    private String employmentType;   
    private String employerName;     
    private String businessType;     
    private BigDecimal monthlyIncome; 
    private User user;

    /**
     * Default constructor for framework compatibility (e.g., ZK, Hibernate, or Reflection).
     */
    public EmploymentDetails() {
        super();
    }

    /* --- Standard Getters and Setters --- */

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Provides a string representation of the employment profile for logging purposes.
     * Note: We log the user ID instead of the full user object to avoid circular references.
     */
    @Override
    public String toString() {
        return "EmploymentDetails [Record ID=" + id + 
               ", Type=" + employmentType + 
               ", Employer=" + employerName + 
               ", Business=" + businessType + 
               ", Income=" + monthlyIncome + 
               ", UserID=" + (user != null ? user.getId() : "N/A") + "]";
    }
}