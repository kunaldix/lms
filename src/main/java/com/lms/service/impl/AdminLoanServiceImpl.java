package com.lms.service.impl;

import java.util.List;

import com.lms.constant.LoanApplicationStatus;
import com.lms.model.Loan;
import com.lms.repository.AdminLoanRepository;
import com.lms.service.AdminLoanService;

public class AdminLoanServiceImpl implements AdminLoanService{
	
	private AdminLoanRepository adminLoanRepo;
	
	
	
	public void setAdminLoanRepo(AdminLoanRepository adminLoanRepo) {
        this.adminLoanRepo = adminLoanRepo;
    }

	@Override
	public List<Loan> getAllLoans() {
		// TODO Auto-generated method stub
		return adminLoanRepo.getAllLoans();
	}

	@Override
	public boolean approveLoan(Loan loan) {
	    // You could also add logic here to send an email notification
		boolean success = adminLoanRepo.updateLoanStatus(loan.getLoanId(), LoanApplicationStatus.ACCEPTED);
	    
	    if (success) {
	        
	        // 3. Conditional EMI Creation
	        if ("MONTHLY_EMI".equalsIgnoreCase(loan.getRepaymentType().name())) {
	           //generateEmiSchedule(loan);
	        }
	    }
	    return success;
	}
	
	// In AdminLoanServiceImpl.java
	@Override
	public boolean rejectLoan(String loanId) {
	    // Logic for rejection
	    return adminLoanRepo.updateLoanStatus(loanId, LoanApplicationStatus.REJECTED);
	}
	
}
