package com.lms.service;

import java.util.List;

import com.lms.model.Emi;
import com.lms.model.Loan;

public interface EmiService {
	
	public void generateEmiSchedule(Loan loan);
	public List<Emi> getEmisForCurrentUser(int userId);
	public boolean processPayment(Emi selectedEmi, String paymentId);
	
}
