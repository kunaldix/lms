package com.lms.model;

import java.io.Serializable;
import java.util.Date;

public class Payment implements Serializable{
	
	private static final long serialVersionUID = 1597225703811627980L;
	
	private String paymentId;
    private Emi emi;
    private Loan loan;
    private String paymentType;
    private int installmentNumber; 
    private double paidAmount;
    private Date paymentDate;
    private String transactionId;
	public Payment() {
		super();
	}
	
	public Payment(String paymentId, Emi emi, Loan loan, String paymentType, int installmentNumber, double paidAmount,
			Date paymentDate, String transactionId) {
		super();
		this.paymentId = paymentId;
		this.emi = emi;
		this.loan = loan;
		this.paymentType = paymentType;
		this.installmentNumber = installmentNumber;
		this.paidAmount = paidAmount;
		this.paymentDate = paymentDate;
		this.transactionId = transactionId;
	}

	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	public Emi getEmi() {
		return emi;
	}
	public void setEmi(Emi emi) {
		this.emi = emi;
	}
	public Loan getLoan() {
		return loan;
	}
	public void setLoan(Loan loan) {
		this.loan = loan;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public int getInstallmentNumber() {
		return installmentNumber;
	}
	public void setInstallmentNumber(int installmentNumber) {
		this.installmentNumber = installmentNumber;
	}
	public double getPaidAmount() {
		return paidAmount;
	}
	public void setPaidAmount(double paidAmount) {
		this.paidAmount = paidAmount;
	}
	public Date getPaymentDate() {
		return paymentDate;
	}
	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	@Override
	public String toString() {
		return "Payment [paymentId=" + paymentId + ", emi=" + emi + ", loan=" + loan + ", paymentType=" + paymentType
				+ ", installmentNumber=" + installmentNumber + ", paidAmount=" + paidAmount + ", paymentDate="
				+ paymentDate + ", transactionId=" + transactionId + "]";
	}
	
}
