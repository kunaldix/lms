package com.lms.controller;

import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import com.lms.constant.LoanApplicationStatus;
import com.lms.constant.LoanType;
import com.lms.constant.RepaymentType;
import com.lms.model.AccountInfo;
import com.lms.model.EmploymentDetails;
import com.lms.model.Loan;
import com.lms.model.User;
import com.lms.model.UserLoanDocuments;
import com.lms.repository.LoanRepository;

public class ApplyLoanComposer extends SelectorComposer<Component> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5630649158860218775L;

    // STEP 1 Wiring
    @Wire private Combobox cmbLoanType, cmbRepay;
    @Wire private Decimalbox decAmount, decInterest;
    @Wire private Intbox intTenure, dateEmi;

    // STEP 3 Wiring
    @Wire private Radiogroup rgEmpType;
    @Wire private Row rowEmployer, rowBusiness;
    @Wire private Textbox txtEmployerName; // Add this ID in ZUL
    @Wire private Combobox cmbBusinessType; // Add this ID in ZUL
    @Wire private Decimalbox decMonthlyIncome; // Add this ID in ZUL

    // STEP 4 Wiring
    @Wire private Textbox txtBankName, txtBranchCode, txtIfscCode, txtAccountNo;

    // STEP 6 Wiring
    @Wire private Checkbox chkConfirm;

    // Navigation Wiring
    @Wire Div step1, step2, step3, step4, step5, step6;
    @Wire Div step1Indicator, step2Indicator, step3Indicator, step4Indicator, step5Indicator, step6Indicator;
    @Wire Button btnBack, btnNext, btnSubmit;

    private int currentStep = 1;
    private final int MAX_STEP = 6;
    private LoanRepository loanRepo = new LoanRepository();

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
		if (currentStep < MAX_STEP) {
			currentStep++;
			updateUI();
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
            // Example paths - populate these from your upload event handlers
            docs.setPhotoUploaded("uploads/photo_" + currentUser.getId() + ".jpg"); 
            loan.setUserDoc(docs);

            // System Fields
            loan.setApplicationStatus(LoanApplicationStatus.PENDING);
            loan.setSubmissionDate(new Date());

            // 2. Call Repository to persist
            //loanRepo.applyLoan(loan);
            System.out.println(loan);

            // 3. Success Notification
            Messagebox.show("Application Submitted Successfully! Your Loan ID is: " + loan.getLoanId(), 
                "Success", Messagebox.OK, Messagebox.INFORMATION, event -> {
                    Executions.sendRedirect("/dashboard.zul");
                });

        } catch (Exception e) {
            e.printStackTrace();
            Messagebox.show("Error submitting application: " + e.getMessage(), "System Error", Messagebox.OK, Messagebox.ERROR);
        }
    }

	private boolean saveLoanToDatabase() {
	    
	    return true; 
	}
}