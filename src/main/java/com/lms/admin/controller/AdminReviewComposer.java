package com.lms.admin.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.lms.constant.LoanApplicationStatus;
import com.lms.model.Loan;
import com.lms.service.AdminLoanService;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Composer for the Admin Loan Review Modal.
 * Facilitates document verification and final approval/rejection of loan applications.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class AdminReviewComposer extends SelectorComposer<Component> {

    private static final long serialVersionUID = 992248780076033830L;
    
    // Logger for auditing admin decisions and document access
    private static final Logger logger = LogManager.getLogger(AdminReviewComposer.class);

    @Wire private Button btnReject, btnApprove;
    @WireVariable private AdminLoanService adminLoanService;
    
    // UI components wired from review_docs.zul
    @Wire private Label lblUserName, lblLoanId, lblLoanType, lblLoanAmount, lblTenure;
    @Wire private Label lblEmail, lblPhone, lblEmpType, lblEmployer, lblIncome;
    @Wire private Label lblBankName, lblIfsc, lblAccNo;
    @Wire private Window modalReview;
    
    private Loan currentLoan;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // Retrieve the loan object passed from the parent composer
        currentLoan = (Loan) Executions.getCurrent().getArg().get("loanData");

        if (currentLoan != null) {
            logger.info("Opening review modal for Loan ID: {} (Applicant: {})", 
                currentLoan.getLoanId(), currentLoan.getUser().getName());
            populateData(currentLoan);
        } else {
            logger.error("Failed to retrieve loan data for review modal.");
        }
    }

    /**
     * Maps the loan model data to the UI labels.
     */
    private void populateData(Loan loan) {
        @SuppressWarnings("deprecation")
		NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        lblUserName.setValue(loan.getUser().getName());
        lblLoanId.setValue("#" + loan.getLoanId());
        lblLoanType.setValue(loan.getLoanType().toString());
        lblLoanAmount.setValue(currencyFormat.format(loan.getLoanAmount()));
        lblTenure.setValue(loan.getTenureMonths() + " Months");
        lblEmail.setValue(loan.getUser().getEmail());
        lblPhone.setValue(loan.getUser().getPhoneNumber());
        lblEmpType.setValue(loan.getEmploymentDetails().getEmploymentType());
        lblEmployer.setValue(loan.getEmploymentDetails().getEmployerName());
        lblIncome.setValue(currencyFormat.format(loan.getEmploymentDetails().getMonthlyIncome()));
        lblBankName.setValue(loan.getAccountInfo().getBankName());
        lblIfsc.setValue(loan.getAccountInfo().getIfscCode());
        lblAccNo.setValue(loan.getAccountInfo().getAccountNumber());
    }

    /* ==========================================================================
       Document Viewing Listeners
       ========================================================================== */

    @Listen("onClick = #btnViewAadhar")
    public void viewAadhar() { streamFile(currentLoan.getUserDoc().getAadharUploaded(), "Aadhar"); }

    @Listen("onClick = #btnViewPan")
    public void viewPan() { streamFile(currentLoan.getUserDoc().getPanUploaded(), "PAN"); }

    @Listen("onClick = #btnViewBank")
    public void viewBank() { streamFile(currentLoan.getUserDoc().getBankStatementUploaded(), "Bank Statement"); }

    @Listen("onClick = #btnViewSalary")
    public void viewSalary() { streamFile(currentLoan.getUserDoc().getSalarySlipUploaded(), "Salary Slip"); }

    @Listen("onClick = #btnViewItr")
    public void viewItr() { streamFile(currentLoan.getUserDoc().getItrUploaded(), "ITR"); }

    @Listen("onClick = #btnViewPhoto")
    public void viewPhoto() { streamFile(currentLoan.getUserDoc().getPhotoUploaded(), "Photo"); }

    /**
     * Handles the streaming of PDF/Image files into the preview modal.
     */
    private void streamFile(String filePath, String docType) {
        if (filePath == null || filePath.isEmpty()) {
            logger.warn("Admin attempted to view missing document: {}", docType);
            Messagebox.show("No file has been uploaded for this document.", "Error", Messagebox.OK, Messagebox.ERROR);
            return;
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("File not found on Linux server path: {}", filePath);
            Messagebox.show("File not found on server.", "Missing File", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        try {
            logger.debug("Streaming document: {} for Loan ID: {}", docType, currentLoan.getLoanId());
            AMedia media = new AMedia(file, null, null);
            Executions.getCurrent().setAttribute("pdfContent", media);
            Window win = (Window) Executions.createComponents("/admin/preview_frame.zul", null, null);
            win.doModal();
        } catch (Exception e) {
            logger.error("Exception occurred while streaming {}: {}", docType, e.getMessage(), e);
            Messagebox.show("Error opening file: " + e.getMessage());
        }
    }

    /* ==========================================================================
       Approval and Rejection Logic
       ========================================================================== */

    @Listen("onClick = #btnApprove")
    public void approveApplication() {
        if (isStatusFinal()) return;
        
        Messagebox.show("Are you sure you want to approve this loan application?", 
            "Confirm Approval", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, event -> {
            
            if (Messagebox.ON_YES.equals(event.getName())) {
                logger.info("Admin initiated APPROVAL for Loan ID: {}", currentLoan.getLoanId());
                boolean success = adminLoanService.approveLoan(currentLoan);
                
                if (success) {
                    logger.info("Loan ID: {} successfully APPROVED.", currentLoan.getLoanId());
                    Clients.showNotification("Application Approved Successfully!", "info", null, "middle_center", 2000);
                    modalReview.detach();
                    Executions.sendRedirect(null);
                } else {
                    logger.error("Database update failed during approval for Loan ID: {}", currentLoan.getLoanId());
                    Messagebox.show("Database update failed.", "Error", Messagebox.OK, Messagebox.ERROR);
                }
            }
        });
    }

    @Listen("onClick = #btnReject")
    public void rejectApplication() {
        if (isStatusFinal()) return;
        
        Messagebox.show("Are you sure you want to REJECT this loan application?", 
            "Confirm Rejection", Messagebox.YES | Messagebox.NO, Messagebox.EXCLAMATION, event -> {
            
            if (Messagebox.ON_YES.equals(event.getName())) {
                logger.info("Admin initiated REJECTION for Loan ID: {}", currentLoan.getLoanId());
                boolean success = adminLoanService.rejectLoan(currentLoan.getLoanId());
                
                if (success) {
                    logger.info("Loan ID: {} successfully REJECTED.", currentLoan.getLoanId());
                    Clients.showNotification("Application Rejected", "error", null, "middle_center", 2000);
                    modalReview.detach();
                    Executions.sendRedirect(null);
                } else {
                    logger.error("Database update failed during rejection for Loan ID: {}", currentLoan.getLoanId());
                    Messagebox.show("Error updating database.", "Error", Messagebox.OK, Messagebox.ERROR);
                }
            }
        });
    }

    /**
     * Guard method to prevent double-processing of loans.
     */
    private boolean isStatusFinal() {
        if (currentLoan.getApplicationStatus() == LoanApplicationStatus.ACCEPTED || 
            currentLoan.getApplicationStatus() == LoanApplicationStatus.REJECTED) {
            logger.warn("Blocked attempt to modify finalized Loan ID: {}", currentLoan.getLoanId());
            Messagebox.show("This application has already been processed.", 
                            "Action Denied", Messagebox.OK, Messagebox.INFORMATION);
            return true;
        }
        return false;
    }
}