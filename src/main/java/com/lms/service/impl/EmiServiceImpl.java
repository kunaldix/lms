package com.lms.service.impl;

import java.util.Calendar;
import java.util.Date;

import com.lms.model.Emi;
import com.lms.model.Loan;
import com.lms.repository.EmiRepository;
import com.lms.service.EmiService;

public class EmiServiceImpl implements EmiService{
	
	private EmiRepository emiRepo;

	@Override
	public void generateEmiSchedule(Loan loan) {
        double principal = loan.getLoanAmount().doubleValue(); //
        double annualRate = loan.getInterestRate();
        double monthlyRate = annualRate / 12 / 100;
        int tenure = loan.getTenureMonths(); //

        // Standard EMI Formula: [P x r x (1+r)^n] / [(1+r)^n - 1]
        double emiAmount = (principal * monthlyRate * Math.pow(1 + monthlyRate, tenure)) 
                           / (Math.pow(1 + monthlyRate, tenure) - 1);

        // Round to 2 decimal places for currency
        emiAmount = Math.round(emiAmount * 100.0) / 100.0;

        for (int i = 1; i <= tenure; i++) {
            Emi emi = new Emi();
            
            // Format: LoanID-E-01
            emi.setEmiId(loan.getLoanId() + "-E-" + String.format("%02d", i));
            emi.setInstallments(tenure);
            emi.setEmiAmount(emiAmount);
            emi.setInterestRate(annualRate);
            emi.setStatus("PENDING");
            emi.setLoan(loan); //
            emi.setUser(loan.getUser()); //
            
            // Set Due Date: Next month + preferred date
            emi.setDueDate(calculateDueDate(loan.getPreferredEmiDate(), i));

            emiRepo.saveEmiRecord(emi);
        }
        
        
    }
	private Date calculateDueDate(Integer preferredDay, int installmentNumber) {
        Calendar cal = Calendar.getInstance();
        // Start from next month
        cal.add(Calendar.MONTH, installmentNumber);
        
        // Handle cases where preferredDay exceeds days in month (e.g., Feb 30)
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, Math.min(preferredDay, maxDay));
        
        // Reset time to midnight for cleaner date records
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        return cal.getTime();
    }

}
