package com.lms.model;

import java.io.Serializable;
import java.sql.Timestamp;

import com.lms.constant.PaymentMode;
import com.lms.constant.TransactionStatus;

public class EmiTransaction implements Serializable{
	
	private static final long serialVersionUID = -4619749105520759355L;
	
	private String txnId;       
    private Emi emi;            
    private Loan loan;          
    private String payuId;      
    private Double amount;
    private TransactionStatus status; 
    private PaymentMode paymentMode;
    private Timestamp createdAt;
    
	public EmiTransaction(String txnId, Emi emi, Loan loan, String payuId, Double amount, TransactionStatus status,
			PaymentMode paymentMode, Timestamp createdAt) {
		super();
		this.txnId = txnId;
		this.emi = emi;
		this.loan = loan;
		this.payuId = payuId;
		this.amount = amount;
		this.status = status;
		this.paymentMode = paymentMode;
		this.createdAt = createdAt;
	}
	public EmiTransaction() {
		super();
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
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
	public String getPayuId() {
		return payuId;
	}
	public void setPayuId(String payuId) {
		this.payuId = payuId;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public TransactionStatus getStatus() {
		return status;
	}
	public void setStatus(TransactionStatus status) {
		this.status = status;
	}
	public PaymentMode getPaymentMode() {
		return paymentMode;
	}
	public void setPaymentMode(PaymentMode paymentMode) {
		this.paymentMode = paymentMode;
	}
	public Timestamp getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}
	@Override
	public String toString() {
		return "EmiTransaction [txnId=" + txnId + ", emi=" + emi + ", loan=" + loan + ", payuId=" + payuId + ", amount="
				+ amount + ", status=" + status + ", paymentMode=" + paymentMode + ", createdAt=" + createdAt + "]";
	}
}