package com.lms.service.impl;

import java.util.List;

import com.lms.model.Loan;
import com.lms.repository.AdminLoanRepository;
import com.lms.service.AdminLoanService;

public class AdminLoanServiceImpl implements AdminLoanService{
	
	private AdminLoanRepository adminLoanRepo;

	@Override
	public List<Loan> getAllLoans() {
		// TODO Auto-generated method stub
		return adminLoanRepo.getAllLoans();
	}
	
}
