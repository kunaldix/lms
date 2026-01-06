package com.lms.service.impl;

import com.lms.model.Emi;
import com.lms.model.EmiTransaction;
import com.lms.model.Loan;
import com.lms.constant.EmiStatus;
import com.lms.constant.PaymentMode;
import com.lms.constant.TransactionStatus;
import com.lms.service.EmiService;
import com.lms.repository.EmiRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Service implementation for managing EMI schedules and payment processing.
 * This class handles the mathematical calculations for installments and coordinates 
 * with the repository for data persistence.
 */
public class EmiServiceImpl implements EmiService {

    private static final Logger logger = LogManager.getLogger(EmiServiceImpl.class);
    private EmiRepository emiRepo;
    
    public void setEmiRepo(EmiRepository emiRepo) {
        this.emiRepo = emiRepo;
    }

    /**
     * Calculates and generates the full repayment schedule for a newly approved loan.
     * It uses the standard EMI formula to determine fixed monthly installments.
     */
    @Override
    public void generateEmiSchedule(Loan loan) {
        logger.info("Starting EMI schedule generation for Loan ID: {}", loan.getLoanId());
        
        double principal = loan.getLoanAmount().doubleValue(); 
        double annualRate = loan.getInterestRate();
        double monthlyRate = annualRate / 12 / 100;
        int tenure = loan.getTenureMonths();

        // Standard Amortization Formula: [P x R x (1+R)^N]/[(1+R)^N-1]
        double emiAmount = (principal * monthlyRate * Math.pow(1 + monthlyRate, tenure)) 
                           / (Math.pow(1 + monthlyRate, tenure) - 1);

        // Rounding to 2 decimal places for financial accuracy
        emiAmount = Math.round(emiAmount * 100.0) / 100.0;
        logger.debug("Calculated Monthly EMI: {} for Loan: {}", emiAmount, loan.getLoanId());

        List<Emi> emiList = new ArrayList<>();

        for (int i = 1; i <= tenure; i++) {
            Emi emi = new Emi();
            
            // Unique ID generation for each installment
            emi.setEmiId(loan.getLoanId() + "-E-" + String.format("%02d", i));
            emi.setInstallmentNumber(i); 
            emi.setEmiAmount(emiAmount);
            emi.setInterestRate(annualRate);
            emi.setStatus(EmiStatus.PENDING); 
            emi.setLoan(loan);
            emi.setUser(loan.getUser());
            emi.setDueDate(calculateDueDate(loan.getPreferredEmiDate(), i));

            emiList.add(emi);
        }

        // Executing a batch save to minimize database round-trips
        if (!emiList.isEmpty()) {
            logger.info("Batch saving {} EMI installments for Loan ID: {}", emiList.size(), loan.getLoanId());
            emiRepo.saveEmiBatch(emiList);
        }
    }

    /**
     * Helper method to project future due dates based on the customer's preferred day of the month.
     */
    private Date calculateDueDate(Integer preferredDay, int installmentNumber) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, installmentNumber);
        
        // Ensure we don't set a day that doesn't exist in a shorter month (e.g., Feb 30th)
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, Math.min(preferredDay, maxDay));
        
        // Standardizing to the start of the day
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        return cal.getTime();
    }

    @Override
    public List<Emi> getEmisForCurrentUser(int userId) {
        logger.info("Retrieving upcoming EMI list for User ID: {}", userId);
        return emiRepo.getUpcomingEmisForUser(userId);
    }

    /**
     * Handles the post-payment logic once PayU returns a success response.
     * Maps gateway codes to internal payment modes and updates the transaction records.
     */
    @Override
    public boolean processPayment(String txnid, String paymentId, String mode) {
        logger.info("Processing successful payment for Transaction ID: {}. Gateway Ref: {}", txnid, paymentId);
        
        // Mapping standard gateway mode codes to our internal PaymentMode enum
        String mappedMode = "OTHER";
        if ("CC".equals(mode)) mappedMode = "CREDIT_CARD";
        else if ("DC".equals(mode)) mappedMode = "DEBIT_CARD";
        else if ("NB".equals(mode)) mappedMode = "NET_BANKING";
        else if ("UPI".equals(mode)) mappedMode = "UPI";

        // Extracting the internal EMI ID from the concatenated transaction string
        String emiId = txnid.replaceAll("TXN[0-9]{13}", "");
        logger.debug("Extracted EMI ID: {} from Transaction string", emiId);

        String[] details = emiRepo.getEmiDetails(emiId);
        if (details == null) {
            logger.error("Payment processing failed: Could not find EMI details for ID: {}", emiId);
            return false;
        }

        String loanId = details[0];
        Double amount = Double.parseDouble(details[1]);

        EmiTransaction txn = new EmiTransaction();
        txn.setTxnId(txnid);
        txn.setPayuId(paymentId);
        txn.setAmount(amount);
        txn.setStatus(TransactionStatus.SUCCESS);
        txn.setPaymentMode(PaymentMode.valueOf(mappedMode));

        // Persist transaction and update EMI/Loan status in a single atomic operation
        boolean result = emiRepo.recordPayment(txn, emiId, loanId);
        
        if(result) {
            logger.info("Payment successfully recorded for EMI: {}. Mode: {}", emiId, mappedMode);
        } else {
            logger.warn("Database failed to update records for successful payment of EMI: {}", emiId);
        }
        
        return result;
    }

    @Override
    public int countOverdueLoans() {
        logger.info("Counting all loans currently in overdue status");
        return emiRepo.countOverdueLoans();
    }

    @Override
    public BigDecimal getNextEmiAmountDueForUser(int userId) {
        logger.info("Fetching next scheduled EMI amount for User ID: {}", userId);
        return emiRepo.getNextEmiAmountDue(userId);
    }

    @Override
    public LinkedHashMap<YearMonth, BigDecimal> getLast5PaidMonthsEmi(int userId) {
        logger.info("Generating 5-month payment history for User ID: {}", userId);
        return emiRepo.getLast5PaidMonthsEmi(userId);
    }

    @Override
    public YearMonth getLatestPaidEmiMonth(int userId) {
        return emiRepo.getLatestPaidEmiMonth(userId);
    }
}