package com.lms.service.impl;

import com.lms.model.Emi;
import com.lms.model.Loan;
import com.lms.constant.EmiStatus;
import com.lms.service.EmiService;
import com.lms.repository.EmiRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EmiServiceImpl implements EmiService {

    private EmiRepository emiRepo;
    
    public void setEmiRepo(EmiRepository emiRepo) {
        this.emiRepo = emiRepo;
    }

    @Override
    public void generateEmiSchedule(Loan loan) {
        double principal = loan.getLoanAmount().doubleValue(); 
        double annualRate = loan.getInterestRate();
        double monthlyRate = annualRate / 12 / 100;
        int tenure = loan.getTenureMonths();

        double emiAmount = (principal * monthlyRate * Math.pow(1 + monthlyRate, tenure)) 
                           / (Math.pow(1 + monthlyRate, tenure) - 1);

        emiAmount = Math.round(emiAmount * 100.0) / 100.0;

        // 1. Create a list to hold all installments
        List<Emi> emiList = new ArrayList<>();

        for (int i = 1; i <= tenure; i++) {
            Emi emi = new Emi();
            
            emi.setEmiId(loan.getLoanId() + "-E-" + String.format("%02d", i));
            emi.setInstallmentNumber(i); 
            emi.setEmiAmount(emiAmount);
            emi.setInterestRate(annualRate);
            emi.setStatus(EmiStatus.PENDING); 
            emi.setLoan(loan);
            emi.setUser(loan.getUser());
            emi.setDueDate(calculateDueDate(loan.getPreferredEmiDate(), i));

            // 2. Add to list instead of saving individually
            emiList.add(emi);
        }

        // 3. Call the Batch Method once
        if (!emiList.isEmpty()) {
            emiRepo.saveEmiBatch(emiList);
        }
    }

    private Date calculateDueDate(Integer preferredDay, int installmentNumber) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, installmentNumber);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, Math.min(preferredDay, maxDay));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

	@Override
	public List<Emi> getEmisForCurrentUser(int userId) {
		// TODO Auto-generated method stub
		return emiRepo.getUpcomingEmisForUser(userId);
	}

	@Override
	public boolean processPayment(Emi selectedEmi, String paymentId) {
		// TODO Auto-generated method stub
		return false;
	}
}