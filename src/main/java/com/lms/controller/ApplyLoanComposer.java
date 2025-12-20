package com.lms.controller;

import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
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

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ApplyLoanComposer extends SelectorComposer<Component> {
	
	private static final long serialVersionUID = -5630649158860218775L;

    // STEP 1 Wiring
    @Wire private Combobox cmbLoanType, cmbRepay;
    @Wire private Decimalbox decAmount, decInterest;
    @Wire private Intbox intTenure, dateEmi;

    // STEP 3 Wiring
    @Wire private Radiogroup rgEmpType;
    @Wire private Row rowEmployer, rowBusiness;
    @Wire private Textbox txtEmployerName; 
    @Wire private Combobox cmbBusinessType; 
    @Wire private Decimalbox decMonthlyIncome; 

    // STEP 4 Wiring
    @Wire private Textbox txtBankName, txtBranchCode, txtIfscCode, txtAccountNo;

    // STEP 6 Wiring
    @Wire private Checkbox chkConfirm;

    // Navigation Wiring
    @Wire Div step1, step2, step3, step4, step5, step6;
    @Wire Div step1Indicator, step2Indicator, step3Indicator, step4Indicator, step5Indicator, step6Indicator;
    @Wire Button btnBack, btnNext, btnSubmit;
    @Wire Label revLoanType, revAmount, revTenure, revEmployer, revIncome, revBank, revAccount;

    private int currentStep = 1;
    private final int MAX_STEP = 6;
    
    @WireVariable
    private LoanService loanService;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		updateUI();
	}
	
	@Listen("onCheck = #rgEmpType")
	public void empTypeChange() {
		Radio selectedItem = rgEmpType.getSelectedItem();
		String value = (String)selectedItem.getValue();
		
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
		switch (value) {
			case "Personal Loan": {
				decInterest.setValue("12.0");
				break;
			}
			case "Home Loan": {
				decInterest.setValue("8.5");
				break;
			}
			case "Car Loan": {
				decInterest.setValue("9.0");
				break;
			}
		}
	}
	
	@Listen("onClick = #btnNext")
	public void nextStep() {
        String errorMessage = getValidationError(currentStep);

        if (errorMessage == null) {
            // No error, proceed to next step
            if (currentStep < MAX_STEP) {
                currentStep++;
                updateUI();
                if (currentStep == 6) prepareReviewStep();
            }
        } else {
            // Show error notification to the user
            // Arguments: message, type (error/warning), target component, position, duration
            Clients.showNotification(errorMessage, Clients.NOTIFICATION_TYPE_ERROR, btnNext, "middle_center", 3000);
        }
    }
	
	private void prepareReviewStep() {
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
                
                // Validation based on selection
                String type = rgEmpType.getSelectedItem().getValue();
                if ("Salaried".equals(type) && txtEmployerName.getValue().trim().isEmpty()) {
                    return "Please enter your Employer Name.";
                }
                if ("Business".equals(type) && cmbBusinessType.getSelectedIndex() == -1) {
                    return "Please select your Business Type.";
                }
                
                if (decMonthlyIncome.getValue() == null || decMonthlyIncome.getValue().doubleValue() <= 0) {
                    return "Please enter a valid Monthly Income.";
                }
                break;

            case 4:
                if (txtBankName.getValue().trim().isEmpty()) return "Bank Name cannot be empty.";
                if (txtBranchCode.getValue().trim().length() < 3) return "Please enter a valid Branch Code.";
                if (txtIfscCode.getValue().trim().length() != 11) return "IFSC Code must be exactly 11 characters.";
                if (txtAccountNo.getValue().trim().isEmpty()) return "Account Number is required.";
                break;

            case 5:
                // If you haven't implemented file uploads yet, you can skip this 
                // or check if labels are still "Pending"
                break;
        }
        return null; // All valid
    }
	
	private boolean validateStep(int step) {
        try {
            switch (step) {
                case 1:
                    if (cmbLoanType.getSelectedIndex() == -1) throw new WrongValueException(cmbLoanType, "Please select a loan type");
                    if (decAmount.getValue() == null || decAmount.getValue().doubleValue() <= 0) throw new WrongValueException(decAmount, "Enter a valid loan amount");
                    if (intTenure.getValue() == null || intTenure.getValue() <= 0) throw new WrongValueException(intTenure, "Enter tenure in months");
                    if (cmbRepay.getSelectedIndex() == -1) throw new WrongValueException(cmbRepay, "Select repayment type");
                    if (dateEmi.getValue() == null || dateEmi.getValue() < 1 || dateEmi.getValue() > 28) 
                        throw new WrongValueException(dateEmi, "Enter a date between 1 and 28");
                    break;

                case 3:
                    if (rgEmpType.getSelectedItem() == null) throw new WrongValueException(rgEmpType, "Select employment type");
                    if (txtEmployerName.getValue().isEmpty()) throw new WrongValueException(txtEmployerName, "Employer name is required");
                    if (decMonthlyIncome.getValue() == null || decMonthlyIncome.getValue().doubleValue() <= 0) 
                        throw new WrongValueException(decMonthlyIncome, "Enter valid monthly income");
                    break;

                case 4:
                    if (txtBankName.getValue().isEmpty()) throw new WrongValueException(txtBankName, "Bank name is required");
                    if (txtIfscCode.getValue().length() < 11) throw new WrongValueException(txtIfscCode, "Enter a valid 11-digit IFSC code");
                    if (txtAccountNo.getValue().isEmpty()) throw new WrongValueException(txtAccountNo, "Account number is required");
                    break;
                
                case 5:
                    // Example: check if labels still say "Pending"
                    // In a real app, you'd check if the file object is null
                    break;
            }
            return true;
        } catch (WrongValueException e) {
            // This automatically highlights the UI component in ZK
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
		// Toggle Step Visibility
		step1.setVisible(currentStep == 1);
		step2.setVisible(currentStep == 2);
		step3.setVisible(currentStep == 3);
		step4.setVisible(currentStep == 4);
		step5.setVisible(currentStep == 5);
		step6.setVisible(currentStep == 6);

		// Update Stepper Visuals
		updateIndicator(step1Indicator, 1);
		updateIndicator(step2Indicator, 2);
		updateIndicator(step3Indicator, 3);
		updateIndicator(step4Indicator, 4);
		updateIndicator(step5Indicator, 5);
		updateIndicator(step6Indicator, 6);

		// Update Navigation Buttons
		btnBack.setDisabled(currentStep == 1);
		btnNext.setVisible(currentStep < MAX_STEP);
		btnSubmit.setVisible(currentStep == MAX_STEP);
	}

	private void updateIndicator(Div indicator, int stepNum) {
		if (stepNum <= currentStep) {
			indicator.setSclass("step-item active");
		} else {
			indicator.setSclass("step-item");
		}
	}
	
	@Listen("onClick = #btnSubmit")
    public void submitApplication() {
		if (!validateStep(1) || !validateStep(3) || !validateStep(4)) {
            Clients.showNotification("Some details are missing. Please go back and check.", "error", null, "middle_center", 3000);
            return;
        }
		
        if (!chkConfirm.isChecked()) {
            Messagebox.show("Please check the confirmation box before submitting.", 
                "Final Review Required", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        try {
            // 1. Create the Master Loan Object
            Loan loan = new Loan();
            
            // Step 1 Data
            loan.setLoanType(LoanType.valueOf(cmbLoanType.getSelectedItem().getValue().toString().replace(" ", "_").toUpperCase()));
            loan.setLoanAmount(decAmount.getValue());
            loan.setTenureMonths(intTenure.getValue());
            loan.setInterestRate(decInterest.getValue().doubleValue());
            loan.setRepaymentType(RepaymentType.valueOf(cmbRepay.getSelectedItem().getValue().toString().replace(" ", "_").toUpperCase()));
            loan.setPreferredEmiDate(dateEmi.getValue());

            // Step 2 Data (Assume User is in Session)
            User currentUser = (User) Executions.getCurrent().getSession().getAttribute("user");
            loan.setUser(currentUser);

            // Step 3 Data: Employment Details
            EmploymentDetails emp = new EmploymentDetails();
            emp.setEmploymentType(rgEmpType.getSelectedItem().getValue());
            emp.setEmployerName(txtEmployerName.getValue());
            emp.setBusinessType(cmbBusinessType.getValue());
            emp.setMonthlyIncome(decMonthlyIncome.getValue());
            emp.setUser(currentUser);
            loan.setEmploymentDetails(emp);

            // Step 4 Data: Bank Account Info
            AccountInfo acc = new AccountInfo();
            acc.setBankName(txtBankName.getValue());
            acc.setBranchCode(txtBranchCode.getValue());
            acc.setIfscCode(txtIfscCode.getValue());
            acc.setAccountNumber(txtAccountNo.getValue());
            acc.setUser(currentUser);
            loan.setAccountInfo(acc);

            // Step 5 Data: Documents (Set paths/status as per your upload logic)
            UserLoanDocuments docs = new UserLoanDocuments();
            docs.setUser(currentUser);
            docs.setAadharUploaded("aadhar");
            docs.setBankStatementUploaded("yes");
            docs.setItrUploaded("yes");
            docs.setPanUploaded("yes");
            docs.setPhotoUploaded("yes");
            docs.setSalarySlipUploaded("yes");
            // Example paths - populate these from your upload event handlers
            //docs.setPhotoUploaded("uploads/photo_" + currentUser.getId() + ".jpg"); 
            loan.setUserDoc(docs);
            
            loan.setLoanId(loanService.generateDisplayId(loan.getLoanType().name()));

            // System Fields
            loan.setApplicationStatus(LoanApplicationStatus.PENDING);
            loan.setSubmissionDate(new Date());

            // 2. Call Repository to persist
            loanService.applyLoan(loan);

            // 3. Success Notification
            Messagebox.show("Application Submitted Successfully! Your Loan ID is: " + loan.getLoanId(), 
                "Success", Messagebox.OK, Messagebox.INFORMATION, event -> {
                    Executions.sendRedirect("/dashboard/dashboard.zul");
                });

        } catch (Exception e) {
            e.printStackTrace();
            Messagebox.show("Error submitting application: " + e.getMessage(), "System Error", Messagebox.OK, Messagebox.ERROR);
        }
    }
}