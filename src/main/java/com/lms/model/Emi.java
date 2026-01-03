package com.lms.model;

import java.io.Serializable;
import java.util.Date;

import com.lms.constant.EmiStatus;

public class Emi implements Serializable{

	private static final long serialVersionUID = 420221472511437536L;
	
	private String emiId;
	private int installmentNumber;
	private Date dueDate;
	private Double emiAmount;
	private double interestRate;
	private EmiStatus status;
	
	private Loan loan;
	private User user;
	public Emi() {
		super();
		// TODO Auto-generated constructor stub
	}
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
	@Override
	public String toString() {
		return "Emi [emiId=" + emiId + ", installmentNumber=" + installmentNumber + ", dueDate=" + dueDate
				+ ", emiAmount=" + emiAmount + ", interestRate=" + interestRate + ", status=" + status + ", loan="
				+ loan + ", user=" + user + "]";
	}
}
