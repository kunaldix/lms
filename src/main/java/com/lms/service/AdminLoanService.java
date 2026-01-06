package com.lms.service;

import java.util.List;
import com.lms.model.Loan;

/**
 * Service interface defining administrative operations for loan management.
 * Provides the contract for retrieving, approving, and rejecting loan applications.
 */
public interface AdminLoanService {

    /**
     * Fetches a complete list of all loan applications present in the system.
     * * @return A list of all Loan objects.
     */
    public List<Loan> getAllLoans();

    /**
     * Finalizes the approval process for a specific loan application.
     * * @param loan The loan object containing details for approval.
     * @return true if the status was successfully updated, false otherwise.
     */
    public boolean approveLoan(Loan loan);

    /**
     * Rejects a loan application based on its unique identifier.
     * * @param loanId The unique string ID of the loan to be rejected.
     * @return true if the rejection was successful, false otherwise.
     */
    public boolean rejectLoan(String loanId);
}