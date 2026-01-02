package com.lms.service;

import java.util.List;

import com.lms.model.Loan;

public interface AdminLoanService {
	public List<Loan> getAllLoans();
	public boolean approveLoan(Loan loan);
	public boolean rejectLoan(String loanId);
}
