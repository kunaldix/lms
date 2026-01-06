package com.lms.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import com.lms.model.Loan;

public interface LoanService {
	public void applyLoan(Loan loan);
	public String generateDisplayId(String loanType);
	public String getTotalDebt(int id);
    public int getActiveLoans(int id);
    public String getTotalLoanOfUser(int id);
    public  int getTotalActiveLoans();
    public int getTotalLoans();
    public int getTotalPendingLoans();
	public List<Loan> getLoansByUserId(int id);
	List<Loan> getALlApprovedLoans();
	Map<YearMonth, Integer> getLast5MonthsLoanCount();
}
