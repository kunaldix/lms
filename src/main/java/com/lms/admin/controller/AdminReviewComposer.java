package com.lms.admin.controller;

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

@VariableResolver(DelegatingVariableResolver.class)
public class AdminReviewComposer extends SelectorComposer<Component> {

    private static final long serialVersionUID = 992248780076033830L;
    
    @Wire private Button btnReject, btnApprove;
    
    @WireVariable
	private AdminLoanService adminLoanService;
    
    // Wire labels from review_docs.zul
    @Wire private Label lblUserName, lblLoanId, lblLoanType, lblLoanAmount, lblTenure;
    @Wire private Label lblEmail, lblPhone, lblEmpType, lblEmployer, lblIncome;
    @Wire private Label lblBankName, lblIfsc, lblAccNo;
    @Wire private Label lblAadhar, lblPan, lblBank, lblSalary, lblItr, lblPhoto;
    @Wire private Window modalReview;
    
    private Loan currentLoan;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // Retrieve the dynamic data passed in the 'args' map
        currentLoan = (Loan) Executions.getCurrent().getArg().get("loanData");

        if (currentLoan != null) {
            populateData(currentLoan);
        }
    }

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


    @Listen("onClick = #btnViewAadhar")
    public void viewAadhar() { streamFile(currentLoan.getUserDoc().getAadharUploaded()); }

    @Listen("onClick = #btnViewPan")
    public void viewPan() { streamFile(currentLoan.getUserDoc().getPanUploaded()); }

    @Listen("onClick = #btnViewBank")
    public void viewBank() { streamFile(currentLoan.getUserDoc().getBankStatementUploaded()); }

    @Listen("onClick = #btnViewSalary")
    public void viewSalary() { streamFile(currentLoan.getUserDoc().getSalarySlipUploaded()); }

    @Listen("onClick = #btnViewItr")
    public void viewItr() { streamFile(currentLoan.getUserDoc().getItrUploaded()); }

    @Listen("onClick = #btnViewPhoto")
    public void viewPhoto() { streamFile(currentLoan.getUserDoc().getPhotoUploaded()); }

    private void streamFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            Messagebox.show("No file has been uploaded for this document.", "Error", Messagebox.OK, Messagebox.ERROR);
            return;
        }
        File file = new File(fileName);

        if (!file.exists()) {
            Messagebox.show("File not found on server at: " + file.getAbsolutePath(), "Missing File", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        try {
            AMedia media = new AMedia(file, null, null);
            
            Executions.getCurrent().setAttribute("pdfContent", media);
            Window win = (Window) Executions.createComponents("/admin/preview_frame.zul", null, null);
            win.doModal();
            
        } catch (Exception e) {
            e.printStackTrace();
            Messagebox.show("Error opening file: " + e.getMessage());
        }
    }
    
    @Listen("onClick = #btnApprove")
    public void approveApplication() {
    	if (isStatusFinal()) return;
        Messagebox.show("Are you sure you want to approve this loan application?", 
            "Confirm Approval", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, event -> {
            
            if (Messagebox.ON_YES.equals(event.getName())) {
                boolean success = adminLoanService.approveLoan(currentLoan);
                
                if (success) {
                    Clients.showNotification("Application Approved Successfully!", "info", null, "middle_center", 2000);
                    modalReview.detach();
                    Executions.sendRedirect(null);
                } else {
                    Messagebox.show("Database update failed. Please check server logs.", "Error", Messagebox.OK, Messagebox.ERROR);
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
                boolean success = adminLoanService.rejectLoan(currentLoan.getLoanId());
                
                if (success) {
                    Clients.showNotification("Application Rejected", "error", null, "middle_center", 2000);
                    // Close the modal
                    modalReview.detach();
                    Executions.sendRedirect(null);
                } else {
                    Messagebox.show("Error updating database.", "Error", Messagebox.OK, Messagebox.ERROR);
                }
            }
        });
    }
    
    private boolean isStatusFinal() {
        if (currentLoan.getApplicationStatus() == LoanApplicationStatus.ACCEPTED || 
            currentLoan.getApplicationStatus() == LoanApplicationStatus.REJECTED) {
            Messagebox.show("This application has already been processed and cannot be modified.", 
                            "Action Denied", Messagebox.OK, Messagebox.INFORMATION);
            return true;
        }
        return false;
    }
}