package com.lms.admin.controller;

import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.lms.model.Loan;
import com.lms.model.UserLoanDocuments;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.Locale;

public class AdminReviewComposer extends SelectorComposer<Component> {

    private static final long serialVersionUID = 992248780076033830L;
    
    // Wire labels from review_docs.zul
    @Wire private Label lblUserName, lblLoanId, lblLoanType, lblLoanAmount, lblTenure;
    @Wire private Label lblEmail, lblPhone, lblEmpType, lblEmployer, lblIncome;
    @Wire private Label lblBankName, lblIfsc, lblAccNo;
    @Wire private Label lblAadhar, lblPan, lblBank, lblSalary, lblItr, lblPhoto;
    
    private Loan currentLoan;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // Retrieve the dynamic data passed in the 'args' map
        currentLoan = (Loan) Executions.getCurrent().getArg().get("loanData");

        if (currentLoan != null) {
            populateData(currentLoan);
            populateDocuments(currentLoan);
        }
    }

    private void populateData(Loan loan) {
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
    
    private void populateDocuments(Loan loan) {
        UserLoanDocuments docs = loan.getUserDoc();
        if (docs != null) {
            lblAadhar.setValue(getFileName(docs.getAadharUploaded()));
            lblPan.setValue(getFileName(docs.getPanUploaded()));
            lblBank.setValue(getFileName(docs.getBankStatementUploaded()));
            lblSalary.setValue(getFileName(docs.getSalarySlipUploaded()));
            lblItr.setValue(getFileName(docs.getItrUploaded()));
            lblPhoto.setValue(getFileName(docs.getPhotoUploaded()));
        }
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

    /**
     * Helper to read file from Linux path and push to Browser preview
     */
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
            // AMedia identifies the content type (PDF/JPG/PNG) automatically
            AMedia media = new AMedia(file, null, null);
            
            // Open the file in a new browser tab for preview
            Executions.getCurrent().setAttribute("pdfContent", media);
            Window win = (Window) Executions.createComponents("/admin/preview_frame.zul", null, null);
            win.doModal();
            
        } catch (Exception e) {
            e.printStackTrace();
            Messagebox.show("Error opening file: " + e.getMessage());
        }
    }

    private String getFileName(String path) {
        if (path == null || path.isEmpty() || path.length() < 5) return "Not Uploaded";
        return path.substring(path.lastIndexOf("/") + 1);
    }
}