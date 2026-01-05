
package com.lms.service.impl;

import com.lms.model.Loan;
import com.lms.repository.LoanRepository;
import com.lms.service.LoanService;
import java.util.Calendar;
import java.util.Random;

public class LoanServiceImpl implements LoanService{
    
    private LoanRepository loanRepo; 

    public void setLoanRepo(LoanRepository loanRepo) {
        this.loanRepo = loanRepo;
    }

    @Override
    public void applyLoan(Loan loan) {
        loanRepo.applyLoan(loan);
    }
    
    @Override
    public String generateDisplayId(String loanType) {
        String prefix = "";
        
        // 1. Determine Prefix based on Loan Type
        switch (loanType.toUpperCase()) {
            case "PERSONAL_LOAN":
                prefix = "PL";
                break;
            case "HOME_LOAN":
                prefix = "HL";
                break;
            case "CAR_LOAN":
                prefix = "CL";
                break;
            default:
                prefix = "LN";
        }
        
        // 2. Get Current Year
        int year = Calendar.getInstance().get(Calendar.YEAR);
        
        // 3. Generate a Random 4-digit number (between 1000 and 9999)
        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(9000); 
        
        // 4. Format: PREFIX-YEAR-RANDOM (e.g., PL-2025-4582)
        return String.format("%s-%d-%d", prefix, year, randomNumber);
    }

    @Override
    public String getTotalDebt(int id) {
        // TODO Auto-generated method stub
        return loanRepo.getAllDebt(id);
    }

    @Override
    public int getActiveLoans(int id) {
        
        return loanRepo.getActiveLoan(id);
    }

    @Override
    public String getTotalLoanOfUser(int id) {
        // TODO Auto-generated method stub
        return loanRepo.getTotalLoan(id);
    }
}
