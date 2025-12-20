package com.lms.service;

import com.lms.model.Loan;

public interface LoanService {
	public void applyLoan(Loan loan);
	public String generateDisplayId(String loanType);
}
