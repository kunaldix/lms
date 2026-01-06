package com.lms.service.impl;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lms.constant.LoanApplicationStatus;
import com.lms.model.Loan;
import com.lms.repository.AdminLoanRepository;
import com.lms.service.AdminLoanService;
import com.lms.service.EmiService;

/**
 * Implementation of the AdminLoanService interface.
 * This class handles the core business logic for an administrator to manage
 * loan applications, specifically the approval and rejection workflows.
 */
public class AdminLoanServiceImpl implements AdminLoanService {
    
    // Setting up Log4j for better traceability and debugging in production
    private static final Logger logger = LogManager.getLogger(AdminLoanServiceImpl.class);
    
    private AdminLoanRepository adminLoanRepo;
    private EmiService emiService;
    
    // Spring or manual setter to provide the EMI service dependency
    public void setEmiService(EmiService emiService) {
        this.emiService = emiService;
    }
    
    // Spring or manual setter to provide the Repository dependency
    public void setAdminLoanRepo(AdminLoanRepository adminLoanRepo) {
        this.adminLoanRepo = adminLoanRepo;
    }

    /**
     * Pulls every loan record from the database for administrative review.
     */
    @Override
    public List<Loan> getAllLoans() {
        logger.info("Admin user is requesting a full list of loan applications.");
        return adminLoanRepo.getAllLoans();
    }

    /**
     * Approves a loan application. If successful and the repayment is set 
     * to monthly EMI, it automatically generates the payment schedule.
     */
    @Override
    public boolean approveLoan(Loan loan) {
        logger.info("Processing loan approval for ID: {}", loan.getLoanId());
        
        // Update the loan status to accepted in our persistent storage
        boolean success = adminLoanRepo.updateLoanStatus(loan.getLoanId(), LoanApplicationStatus.ACCEPTED);
        
        if (success) {
            logger.info("Successfully updated status to ACCEPTED for loan ID: {}", loan.getLoanId());
            
            // We only need to generate the schedule if the customer chose the monthly payment model
            if ("MONTHLY_EMI".equalsIgnoreCase(loan.getRepaymentType().name())) {
                logger.debug("Generating EMI schedule for approved loan: {}", loan.getLoanId());
                emiService.generateEmiSchedule(loan);
            }
        } else {
            // Log an error if the database update failed for any reason
            logger.error("Failed to approve loan ID: {}. Repository update returned false.", loan.getLoanId());
        }
        
        return success;
    }
    
    /**
     * Marks a specific loan application as rejected.
     */
    @Override
    public boolean rejectLoan(String loanId) {
        logger.info("Admin is rejecting loan application: {}", loanId);
        
        boolean result = adminLoanRepo.updateLoanStatus(loanId, LoanApplicationStatus.REJECTED);
       
        if (result) {
            logger.info("Loan ID: {} was successfully moved to REJECTED status.", loanId);
        } else {
            // Log a warning in case the loan ID was not found or couldn't be updated
            logger.warn("Rejection attempt failed for loan ID: {}. It may not exist in the records.", loanId);
        }
        
        return result;
    }
}