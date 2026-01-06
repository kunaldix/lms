package com.lms.service.impl;

import com.lms.model.Loan;
import com.lms.repository.LoanRepository;
import com.lms.service.LoanService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.YearMonth;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service implementation for handling core loan operations.
 * This class serves as the bridge between the controllers and the data access layer
 * for application processing and statistical reporting.
 */
public class LoanServiceImpl implements LoanService {
    
    // Initializing Log4j for activity monitoring and debugging
    private static final Logger logger = LogManager.getLogger(LoanServiceImpl.class);
    
    private LoanRepository loanRepo; 

    // Dependency injection via setter for the Loan Repository
    public void setLoanRepo(LoanRepository loanRepo) {
        this.loanRepo = loanRepo;
    }

    /**
     * Forwards a new loan application to the repository for persistence.
     */
    @Override
    public void applyLoan(Loan loan) {
        logger.info("New loan application received for User ID: {}", loan.getUser().getId());
        loanRepo.applyLoan(loan);
    }
    
    /**
     * Generates a unique, human-readable identifier for each loan based on its type.
     * Example format: PL-2026-1234
     */
    @Override
    public String generateDisplayId(String loanType) {
        logger.debug("Generating unique display ID for loan type: {}", loanType);
        String prefix = "";
        
        // Categorizing the prefix based on specific loan products
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
        
        int year = Calendar.getInstance().get(Calendar.YEAR);
        
        // Creating a random 4-digit numeric component for uniqueness
        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(9000); 
        
        String generatedId = String.format("%s-%d-%d", prefix, year, randomNumber);
        logger.info("Assigned unique ID: {} to the new loan application", generatedId);
        
        return generatedId;
    }

    /**
     * Calculates the total financial liability of a specific user.
     */
    @Override
    public String getTotalDebt(int id) {
        logger.info("Calculating total debt for User ID: {}", id);
        return loanRepo.getAllDebt(id);
    }

    /**
     * Counts the number of active, non-closed loans for a specific user.
     */
    @Override
    public int getActiveLoans(int id) {
        logger.info("Retrieving active loan count for User ID: {}", id);
        return loanRepo.getActiveLoan(id);
    }

    /**
     * Fetches the total number of loans a user has ever applied for.
     */
    @Override
    public String getTotalLoanOfUser(int id) {
        logger.info("Fetching all-time loan count for User ID: {}", id);
        return loanRepo.getTotalLoan(id);
    }

    /**
     * Global stat: Returns the count of all active loans across the platform.
     */
    @Override
    public int getTotalActiveLoans() {
        logger.debug("Fetching platform-wide active loan statistics");
        return loanRepo.getTotalActiveLoans();
    }

    /**
     * Global stat: Returns the total volume of all loan applications.
     */
    @Override
    public int getTotalLoans() {
        logger.debug("Fetching platform-wide total loan statistics");
        return loanRepo.getTotalLoans();
    }

    /**
     * Global stat: Returns the number of applications currently awaiting admin review.
     */
    @Override
    public int getTotalPendingLoans() {
        logger.debug("Fetching platform-wide pending application statistics");
        return loanRepo.getPendingLoans();
    }

    /**
     * Retrieves the specific loan portfolio for a single customer.
     */
    @Override
    public List<Loan> getLoansByUserId(int id) {
        logger.info("Requesting loan history for User ID: {}", id);
        return loanRepo.getLoansByUserId(id);
    }
    
    /**
     * Retrieves all loans that have successfully passed the approval stage.
     */
    @Override
    public List<Loan> getALlApprovedLoans() {
        logger.info("Fetching all loans currently in APPROVED status");
        return loanRepo.getAllApprovedLoans();
    }

    /**
     * Aggregates data for the dashboard line chart showing application volume 
     * over the last five months.
     */
    @Override
    public Map<YearMonth, Integer> getLast5MonthsLoanCount() {
        logger.info("Aggregating loan application trends for the dashboard chart");
        return loanRepo.getLast5MonthsLoanCount();
    }
}