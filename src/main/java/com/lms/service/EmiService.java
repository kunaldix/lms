package com.lms.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import com.lms.model.Emi;
import com.lms.model.Loan;

public interface EmiService {
	
	public void generateEmiSchedule(Loan loan);
	public List<Emi> getEmisForCurrentUser(int userId);
	public boolean processPayment(String txnid, String paymentId, String modeFromPayu);
	
	public int countOverdueLoans();
	
	public BigDecimal getNextEmiAmountDueForUser(int userId);
	public LinkedHashMap<YearMonth, BigDecimal> getLast5PaidMonthsEmi(int userId);
	public YearMonth getLatestPaidEmiMonth(int userId); 
	
	
}
