package com.lms.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import com.lms.model.Emi;
import com.lms.model.Loan;

/**
 * Service interface dedicated to EMI (Equated Monthly Installment) management.
 * This contract handles everything from initial schedule generation to 
 * payment processing and financial reporting for the dashboard.
 */
public interface EmiService {
    
    /**
     * Creates the full list of scheduled installments for a loan once it is approved.
     * * @param loan The approved loan object used to calculate installment amounts.
     */
    void generateEmiSchedule(Loan loan);

    /**
     * Retrieves all upcoming and pending installments for a specific customer.
     * * @param userId The unique ID of the customer.
     * @return A list of EMI records linked to the user.
     */
    List<Emi> getEmisForCurrentUser(int userId);

    /**
     * Finalizes a payment transaction after receiving a confirmation from the payment gateway.
     * * @param txnid The internal transaction reference.
     * @param paymentId The reference ID provided by PayU.
     * @param modeFromPayu The payment method used (CC, DC, NB, etc.).
     * @return true if the payment was successfully recorded in the system.
     */
    boolean processPayment(String txnid, String paymentId, String modeFromPayu);
    
    /**
     * Stat for Admin Dashboard: Counts how many loans across the system have missed payments.
     * * @return Total count of overdue loans.
     */
    int countOverdueLoans();
    
    /**
     * Determines the total amount the user needs to pay for their next upcoming installment.
     * * @param userId The unique ID of the customer.
     * @return The monetary amount due for the next EMI.
     */
    BigDecimal getNextEmiAmountDueForUser(int userId);

    /**
     * Generates a historical map of the last 5 months of successful payments.
     * Used for rendering the payment trend charts on the user dashboard.
     * * @param userId The unique ID of the customer.
     * @return A map where the key is the month and the value is the total amount paid.
     */
    LinkedHashMap<YearMonth, BigDecimal> getLast5PaidMonthsEmi(int userId);

    /**
     * Identifies the most recent month for which an EMI was successfully cleared.
     * * @param userId The unique ID of the customer.
     * @return The YearMonth of the last successful payment.
     */
    YearMonth getLatestPaidEmiMonth(int userId); 
}