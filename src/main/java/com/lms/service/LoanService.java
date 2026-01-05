package com.lms.service;

import com.lms.model.Loan;

public interface LoanService {
	public void applyLoan(Loan loan);
    public String generateDisplayId(String loanType);
    public String getTotalDebt(int id);
    public int getActiveLoans(int id);
    public String getTotalLoanOfUser(int id);
	
}
