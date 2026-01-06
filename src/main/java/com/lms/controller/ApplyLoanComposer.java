package com.lms.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import com.lms.constant.LoanApplicationStatus;
import com.lms.constant.LoanType;
import com.lms.constant.RepaymentType;
import com.lms.model.AccountInfo;
import com.lms.model.EmploymentDetails;
import com.lms.model.Loan;
import com.lms.model.User;
import com.lms.model.UserLoanDocuments;
import com.lms.service.LoanService;

/**
 * Composer for the Multi-step Loan Application process.
 * Manages 6 steps including data entry, file uploads, and final review.
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ApplyLoanComposer extends SelectorComposer<Component> {
	
	private static final long serialVersionUID = -5630649158860218775L;
    
    // Initialize Log4j Logger for application auditing
    private static final Logger logger = LogManager.getLogger(ApplyLoanComposer.class);

    /* --- STEP 1: LOAN PREFERENCES --- */
    @Wire private Combobox cmbLoanType, cmbRepay;
    @Wire private Decimalbox decAmount, decInterest;
    @Wire private Intbox intTenure, dateEmi;

    /* --- STEP 3: EMPLOYMENT DETAILS --- */
    @Wire private Radiogroup rgEmpType;
    @Wire private Row rowEmployer, rowBusiness;
    @Wire private Textbox txtEmployerName; 
    @Wire private Combobox cmbBusinessType; 
    @Wire private Decimalbox decMonthlyIncome; 

    /* --- STEP 4: BANK ACCOUNT INFO --- */
    @Wire private Textbox txtBankName, txtBranchCode, txtIfscCode, txtAccountNo;

    /* --- STEP 6: FINAL REVIEW --- */
    @Wire private Checkbox chkConfirm;

    /* --- STEPPER NAVIGATION UI --- */
    @Wire Div step1, step2, step3, step4, step5, step6;
    @Wire Div step1Indicator, step2Indicator, step3Indicator, step4Indicator, step5Indicator, step6Indicator;
    @Wire Button btnBack, btnNext, btnSubmit;
    @Wire Label revLoanType, revAmount, revTenure, revEmployer, revIncome, revBank, revAccount;

    /* --- FILE UPLOAD UI --- */
    @Wire private Label lblStatusPhoto, lblStatusSalary, lblStatusAadhar, lblStatusItr, lblStatusBank, lblStatusPan; 
    @Wire private Button btnUploadPhoto, btnUploadSalary, btnUploadAadhar, btnUploadItr, btnUploadBank, btnUploadPan;

    @WireVariable private LoanService loanService;
    @Wire private Textbox username, useremail, userphone;

    // Internal state management
    private int currentStep = 1;
    private final int MAX_STEP = 6;
    private String pathPhoto, pathSalary, pathAadhar, pathItr, pathBank, pathPan;
    
    // Storage path for uploaded documents on the Linux server
    private final String UPLOAD_DIR = "/var/credithub/uploads/";

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		logger.info("Initializing ApplyLoanComposer.");
        updateUI();
		
		User currUser = (User)Sessions.getCurrent().getAttribute("user");
		if(currUser != null) {
            logger.debug("Pre-populating user data for: {}", currUser.getEmail());
			username.setValue(currUser.getName());
			useremail.setValue(currUser.getEmail());
			userphone.setValue(currUser.getPhoneNumber());
		}
	}

    /* ==========================================================================
       FILE UPLOAD PROCESSING
       ========================================================================== */
	
	@Listen("onUpload = #btnUploadPhoto") public void onUploadPhoto(UploadEvent event) { processUpload(event.getMedia(), "photo", lblStatusPhoto, btnUploadPhoto); }
	@Listen("onUpload = #btnUploadSalary") public void onUploadSalary(UploadEvent event) { processUpload(event.getMedia(), "salary", lblStatusSalary, btnUploadSalary); }
	@Listen("onUpload = #btnUploadAadhar") public void onUploadAadhar(UploadEvent event) { processUpload(event.getMedia(), "aadhar", lblStatusAadhar, btnUploadAadhar); }
	@Listen("onUpload = #btnUploadItr") public void onUploadItr(UploadEvent event) { processUpload(event.getMedia(), "itr", lblStatusItr, btnUploadItr); }
	@Listen("onUpload = #btnUploadBank") public void onUploadBank(UploadEvent event) { processUpload(event.getMedia(), "bank", lblStatusBank, btnUploadBank); }
	@Listen("onUpload = #btnUploadPan") public void onUploadPan(UploadEvent event) { processUpload(event.getMedia(), "pan", lblStatusPan, btnUploadPan); }
	
	/**
     * Handles file validation and saving to the local Linux filesystem.
     */
    private void processUpload(Media media, String docType, Label statusLabel, Button uploadBtn) {
	    if (media == null) return;

	    String format = media.getFormat().toLowerCase();
        logger.info("Processing upload for docType: [{}], format: [{}]", docType, format);
	    
	    // Format Validation
	    if (docType.equals("photo")) {
	        if (!"jpg".equals(format) && !"jpeg".equals(format) && !"png".equals(format)) {
	            Clients.showNotification("Only JPG or PNG for Photo!", "error", uploadBtn, "end_center", 3000);
	            return;
	        }
	    } else {
	        if (!"pdf".equals(format)) {
	            Clients.showNotification("Only PDF files allowed!", "error", uploadBtn, "end_center", 3000);
	            return;
	        }
	    }

	    try {
	        User currentUser = (User) Executions.getCurrent().getSession().getAttribute("user");
	        String fileName = currentUser.getId() + "_" + docType + "_" + System.currentTimeMillis() + "." + format;
	        File file = new File(UPLOAD_DIR + fileName);

            // Ensure directory exists
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

	        try (InputStream in = media.getStreamData();
	             FileOutputStream out = new FileOutputStream(file)) {
	            byte[] buffer = new byte[1024];
	            int length;
	            while ((length = in.read(buffer)) > 0) {
	                out.write(buffer, 0, length);
	            }
	        }

	        // Update absolute paths for model persistence
	        if (docType.equals("photo")) pathPhoto = file.getAbsolutePath();
	        else if (docType.equals("salary")) pathSalary = file.getAbsolutePath();
	        else if (docType.equals("aadhar")) pathAadhar = file.getAbsolutePath();
	        else if (docType.equals("itr")) pathItr = file.getAbsolutePath();
	        else if (docType.equals("bank")) pathBank = file.getAbsolutePath();
	        else if (docType.equals("pan")) pathPan = file.getAbsolutePath();

	        statusLabel.setValue("Uploaded");
	        statusLabel.setSclass("status-badge success");
	        uploadBtn.setLabel("Remove");
	        uploadBtn.setSclass("remove-btn");
	        uploadBtn.setUpload("false");
	        
            logger.info("File saved successfully to: {}", file.getAbsolutePath());
	        Clients.showNotification("File saved successfully.");

	    } catch (Exception e) {
	        logger.error("File upload failed for {}: {}", docType, e.getMessage(), e);
	        Clients.showNotification("Upload failed.", "error", null, "middle_center", 2000);
	    }
	}

    /**
     * Handles document removal logic and resets upload buttons.
     */
	@Listen("onClick = #btnUploadPhoto, #btnUploadSalary, #btnUploadAadhar, #btnUploadItr, #btnUploadBank, #btnUploadPan")
	public void handleRemove(org.zkoss.zk.ui.event.Event event) {
	    Button btn = (Button) event.getTarget();
	    
	    if ("Remove".equals(btn.getLabel())) {
	        Label statusLabel = null;
	        String uploadConfig = "true,maxsize=2048";

	        if (btn == btnUploadPhoto) { statusLabel = lblStatusPhoto; pathPhoto = null; uploadConfig += ",accept=image/*"; }
	        else if (btn == btnUploadSalary) { statusLabel = lblStatusSalary; pathSalary = null; uploadConfig += ",accept=application/pdf"; }
	        else if (btn == btnUploadAadhar) { statusLabel = lblStatusAadhar; pathAadhar = null; uploadConfig += ",accept=application/pdf"; }
	        else if (btn == btnUploadItr) { statusLabel = lblStatusItr; pathItr = null; uploadConfig += ",accept=application/pdf"; }
	        else if (btn == btnUploadBank) { statusLabel = lblStatusBank; pathBank = null; uploadConfig += ",accept=application/pdf"; }
	        else if (btn == btnUploadPan) { statusLabel = lblStatusPan; pathPan = null; uploadConfig += ",accept=application/pdf"; }

	        if (statusLabel != null) {
	            statusLabel.setValue("Pending");
	            statusLabel.setSclass("status-badge pending");
	        }
	        
	        btn.setLabel("Upload");
	        btn.setSclass("upload-btn");
	        btn.setUpload(uploadConfig); 
	        
            logger.debug("User removed a document.");
	        Clients.showNotification("File removed", "warning", null, "middle_center", 1500);
	    }
	}
	
	@Listen("onCheck = #rgEmpType")
	public void empTypeChange() {
		Radio selectedItem = rgEmpType.getSelectedItem();
		String value = (String)selectedItem.getValue();
        logger.debug("Employment type changed to: {}", value);
		
		switch(value) {
			case "Salaried" :
				rowEmployer.setVisible(true);
				rowBusiness.setVisible(false);
				break;
			case "Business" :
				rowBusiness.setVisible(true);
				rowEmployer.setVisible(true);
				break;
		}
	}
	
	@Listen("onChange = #cmbLoanType")
	public void loanTypeUpdates() {
		Comboitem selectedItem = cmbLoanType.getSelectedItem();
		String value = (String) selectedItem.getValue();
        logger.debug("Loan type selected: {}", value);
		switch (value) {
			case "Personal Loan": decInterest.setValue("12.0"); break;
			case "Home Loan": decInterest.setValue("8.5"); break;
			case "Car Loan": decInterest.setValue("9.0"); break;
		}
	}
	
    /* ==========================================================================
       NAVIGATION & VALIDATION
       ========================================================================== */

	@Listen("onClick = #btnNext")
	public void nextStep() {
        logger.debug("Navigating from step {} to next.", currentStep);
        String errorMessage = getValidationError(currentStep);

        if (errorMessage == null) {
            if (currentStep < MAX_STEP) {
                currentStep++;
                updateUI();
                if (currentStep == 6) prepareReviewStep();
            }
        } else {
            logger.warn("Validation failed at step {}: {}", currentStep, errorMessage);
        	Clients.showNotification(errorMessage, Clients.NOTIFICATION_TYPE_ERROR, null, "middle_center", 3000);
        }
    }
	
	private void prepareReviewStep() {
        logger.info("Preparing Final Review step for user.");
        revLoanType.setValue(cmbLoanType.getValue());
        revAmount.setValue(decAmount.getText());
        revTenure.setValue(intTenure.getText() + " Months");
        revEmployer.setValue(txtEmployerName.getValue());
        revIncome.setValue(decMonthlyIncome.getText());
        revBank.setValue(txtBankName.getValue());
        revAccount.setValue(txtAccountNo.getValue());
    }
	
	private String getValidationError(int step) {
        switch (step) {
            case 1:
                if (cmbLoanType.getSelectedIndex() == -1) return "Please select a Loan Type.";
                if (decAmount.getValue() == null || decAmount.getValue().doubleValue() <= 0) return "Please enter a valid Loan Amount.";
                if (intTenure.getValue() == null || intTenure.getValue() <= 0) return "Please enter the Tenure in months.";
                if (cmbRepay.getSelectedIndex() == -1) return "Please select a Repayment Type.";
                if (dateEmi.getValue() == null || dateEmi.getValue() < 1 || dateEmi.getValue() > 28) return "EMI Date must be between 1 and 28.";
                break;
            case 3:
                if (rgEmpType.getSelectedItem() == null) return "Please select your Employment Type.";
                String type = rgEmpType.getSelectedItem().getValue();
                if ("Salaried".equals(type) && txtEmployerName.getValue().trim().isEmpty()) return "Please enter your Employer Name.";
                if ("Business".equals(type) && cmbBusinessType.getSelectedIndex() == -1) return "Please select your Business Type.";
                if (decMonthlyIncome.getValue() == null || decMonthlyIncome.getValue().doubleValue() <= 0) return "Please enter valid Monthly Income.";
                break;
            case 4:
                if (txtBankName.getValue().trim().isEmpty()) return "Bank Name cannot be empty.";
                if (txtIfscCode.getValue().trim().length() != 11) return "IFSC Code must be exactly 11 characters.";
                if (txtAccountNo.getValue().trim().isEmpty()) return "Account Number is required.";
                break;
            case 5:
                if (pathPhoto == null || pathSalary == null || pathAadhar == null || pathItr == null || pathBank == null || pathPan == null) {
                    return "All documents are mandatory. Please upload missing files.";
                }
                break;
        }
        return null;
    }
	
	private boolean validateStep(int step) {
        try {
            switch (step) {
                case 1:
                    if (cmbLoanType.getSelectedIndex() == -1) throw new WrongValueException(cmbLoanType, "Please select a loan type");
                    if (decAmount.getValue() == null || decAmount.getValue().doubleValue() <= 0) throw new WrongValueException(decAmount, "Enter a valid loan amount");
                    if (intTenure.getValue() == null || intTenure.getValue() <= 0) throw new WrongValueException(intTenure, "Enter tenure in months");
                    if (cmbRepay.getSelectedIndex() == -1) throw new WrongValueException(cmbRepay, "Select repayment type");
                    if (dateEmi.getValue() == null || dateEmi.getValue() < 1 || dateEmi.getValue() > 28) throw new WrongValueException(dateEmi, "Enter a date between 1 and 28");
                    break;
                case 3:
                    if (rgEmpType.getSelectedItem() == null) throw new WrongValueException(rgEmpType, "Select employment type");
                    if (txtEmployerName.getValue().isEmpty()) throw new WrongValueException(txtEmployerName, "Employer name is required");
                    if (decMonthlyIncome.getValue() == null || decMonthlyIncome.getValue().doubleValue() <= 0) throw new WrongValueException(decMonthlyIncome, "Enter valid monthly income");
                    break;
                case 4:
                    if (txtBankName.getValue().isEmpty()) throw new WrongValueException(txtBankName, "Bank name is required");
                    if (txtIfscCode.getValue().length() < 11) throw new WrongValueException(txtIfscCode, "Enter valid 11-digit IFSC code");
                    if (txtAccountNo.getValue().isEmpty()) throw new WrongValueException(txtAccountNo, "Account number is required");
                    break;
            }
            return true;
        } catch (WrongValueException e) {
            return false;
        }
    }

	@Listen("onClick = #btnBack")
	public void backStep() {
		if (currentStep > 1) {
			currentStep--;
			updateUI();
		}
	}

	private void updateUI() {
		step1.setVisible(currentStep == 1);
		step2.setVisible(currentStep == 2);
		step3.setVisible(currentStep == 3);
		step4.setVisible(currentStep == 4);
		step5.setVisible(currentStep == 5);
		step6.setVisible(currentStep == 6);

		updateIndicator(step1Indicator, 1);
		updateIndicator(step2Indicator, 2);
		updateIndicator(step3Indicator, 3);
		updateIndicator(step4Indicator, 4);
		updateIndicator(step5Indicator, 5);
		updateIndicator(step6Indicator, 6);

		btnBack.setDisabled(currentStep == 1);
		btnNext.setVisible(currentStep < MAX_STEP);
		btnSubmit.setVisible(currentStep == MAX_STEP);
	}

	private void updateIndicator(Div indicator, int stepNum) {
		indicator.setSclass(stepNum <= currentStep ? "step-item active" : "step-item");
	}
	
    /* ==========================================================================
       FINAL SUBMISSION
       ========================================================================== */

	@Listen("onClick = #btnSubmit")
    public void submitApplication() {
        logger.info("Submit triggered. Final validation starting.");
		if (!validateStep(1) || !validateStep(3) || !validateStep(4)) {
            logger.warn("Submission blocked due to invalid step data.");
			Clients.showNotification("Some details are missing. Please go back and check.", "error", null, "middle_center", 3000);
            return;
        }
		
        if (!chkConfirm.isChecked()) {
            Messagebox.show("Please check the confirmation box before submitting.", 
                "Final Review Required", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        try {
            Loan loan = new Loan();
            
            // Step 1 Mapping
            loan.setLoanType(LoanType.valueOf(cmbLoanType.getSelectedItem().getValue().toString().replace(" ", "_").toUpperCase()));
            loan.setLoanAmount(decAmount.getValue());
            loan.setTenureMonths(intTenure.getValue());
            loan.setInterestRate(decInterest.getValue().doubleValue());
            loan.setRepaymentType(RepaymentType.valueOf(cmbRepay.getSelectedItem().getValue().toString().replace(" ", "_").toUpperCase()));
            loan.setPreferredEmiDate(dateEmi.getValue());

            User currentUser = (User) Executions.getCurrent().getSession().getAttribute("user");
            loan.setUser(currentUser);

            // Step 3 Mapping
            EmploymentDetails emp = new EmploymentDetails();
            emp.setEmploymentType(rgEmpType.getSelectedItem().getValue());
            emp.setEmployerName(txtEmployerName.getValue());
            emp.setBusinessType(cmbBusinessType.getValue());
            emp.setMonthlyIncome(decMonthlyIncome.getValue());
            emp.setUser(currentUser);
            loan.setEmploymentDetails(emp);

            // Step 4 Mapping
            AccountInfo acc = new AccountInfo();
            acc.setBankName(txtBankName.getValue());
            acc.setBranchCode(txtBranchCode.getValue());
            acc.setIfscCode(txtIfscCode.getValue());
            acc.setAccountNumber(txtAccountNo.getValue());
            acc.setUser(currentUser);
            loan.setAccountInfo(acc);

            // Step 5 Mapping (File Paths)
            UserLoanDocuments docs = new UserLoanDocuments();
            docs.setUser(currentUser);
            docs.setAadharUploaded(pathAadhar);
            docs.setBankStatementUploaded(pathBank);
            docs.setItrUploaded(pathItr);
            docs.setPanUploaded(pathPan);
            docs.setPhotoUploaded(pathPhoto);
            docs.setSalarySlipUploaded(pathSalary);
            loan.setUserDoc(docs);
            
            loan.setLoanId(loanService.generateDisplayId(loan.getLoanType().name()));
            loan.setApplicationStatus(LoanApplicationStatus.PENDING);
            loan.setSubmissionDate(new Date());

            logger.info("Persisting master loan object for user ID: {}", currentUser.getId());
            loanService.applyLoan(loan);

            Messagebox.show("Application Submitted Successfully! Your Loan ID is: " + loan.getLoanId(), 
                "Success", Messagebox.OK, Messagebox.INFORMATION, event -> {
                    Executions.sendRedirect("/dashboard/dashboard.zul");
                });

        } catch (Exception e) {
            logger.error("Loan submission fatal error: {}", e.getMessage(), e);
            Messagebox.show("Error submitting application: " + e.getMessage(), "System Error", Messagebox.OK, Messagebox.ERROR);
        }
    }
}