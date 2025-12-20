package com.lms.model;

import java.math.BigDecimal;

public class EmploymentDetails {
	
	private Integer id;
	private String employmentType;   
    private String employerName;     
    private String businessType;     
    private BigDecimal monthlyIncome; 
    private User user;
    
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
	@Override
	public String toString() {
		return "EmploymentDetails [id=" + id + ", employmentType=" + employmentType + ", employerName=" + employerName
				+ ", businessType=" + businessType + ", monthlyIncome=" + monthlyIncome + ", user=" + user + "]";
	}
}
