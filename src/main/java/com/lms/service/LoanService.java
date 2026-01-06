package com.lms.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import com.lms.model.Loan;

/**
 * Service interface for core loan lifecycle operations.
 * This contract defines the methods for processing applications, 
 * calculating debt statistics, and generating data for administrative reports.
 */
public interface LoanService {

    /**
     * Submits a new loan application to the system for processing.
     * @param loan The loan application details provided by the customer.
     */
    void applyLoan(Loan loan);

    /**
     * Creates a unique, user-friendly identifier for a loan based on its category.
     * @param loanType The specific type of loan (e.g., Home, Personal).
     * @return A formatted string ID (e.g., HL-2026-XXXX).
     */
    String generateDisplayId(String loanType);

    /**
     * Calculates the total financial liability currently held by a specific user.
     * @param id The unique identifier of the user.
     * @return The total debt amount as a formatted string.
     */
    String getTotalDebt(int id);

    /**
     * Retrieves the count of all active, non-closed loans for a specific user.
     * @param id The unique identifier of the user.
     * @return Number of active loans.
     */
    int getActiveLoans(int id);

    /**
     * Fetches the total number of loan applications (all statuses) submitted by a user.
     * @param id The unique identifier of the user.
     * @return Total loan count as a string.
     */
    String getTotalLoanOfUser(int id);

    /**
     * Global Statistic: Returns the count of all active loans across the entire platform.
     * @return Total active loans in the system.
     */
    int getTotalActiveLoans();

    /**
     * Global Statistic: Returns the total count of all loan applications received.
     * @return All-time total loan count.
     */
    int getTotalLoans();

    /**
     * Global Statistic: Returns the count of applications currently awaiting administrative review.
     * @return Total number of pending applications.
     */
    int getTotalPendingLoans();

    /**
     * Pulls a detailed history of all loans associated with a specific user.
     * @param id The unique identifier of the user.
     * @return A list of loan objects belonging to the user.
     */
    List<Loan> getLoansByUserId(int id);

    /**
     * Retrieves all loan applications that have successfully passed the approval stage.
     * @return A list of all approved loans in the system.
     */
    List<Loan> getALlApprovedLoans();

    /**
     * Aggregates monthly application volume data for the last 5 months.
     * Used primarily for rendering administrative trend charts.
     * @return A map linking YearMonth to the number of loans applied for.
     */
    Map<YearMonth, Integer> getLast5MonthsLoanCount();
}