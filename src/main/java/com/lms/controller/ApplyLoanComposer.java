package com.lms.controller;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

public class ApplyLoanComposer extends SelectorComposer<Component> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5630649158860218775L;

	@Wire
	private Combobox cmbLoanType;
	@Wire
	private Decimalbox decInterest;
	@Wire
	private Radiogroup rgEmpType;
	@Wire
	private Row rowEmployer, rowBusiness;
	@Wire
	private Checkbox chkConfirm;

	// Wire the Step Divs
	@Wire
	Div step1, step2, step3, step4, step5, step6;

	// Wire the Indicators
	@Wire
	Div step1Indicator, step2Indicator, step3Indicator, step4Indicator, step5Indicator, step6Indicator;

	// Wire the Buttons
	@Wire
	Button btnBack, btnNext, btnSubmit;

	private int currentStep = 1;
	private final int MAX_STEP = 6;

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
	    // 1. Check for Confirmation
	    if (!chkConfirm.isChecked()) {
	        Messagebox.show("Please check the confirmation box before submitting.", 
	            "Final Review Required", Messagebox.OK, Messagebox.EXCLAMATION);
	        return;
	    }

	    // 2. Logic: Create a Loan Application Object
	    // Replace with your actual Model class
	    try {
	        System.out.println("Submitting loan for: " + cmbLoanType.getValue());
	        
	        // Example of saving data (Your DB logic goes here)
	        boolean success = saveLoanToDatabase(); 

	        if (success) {
	            // 3. Show Success and Redirect
	            Messagebox.show("Your loan application has been submitted successfully!", 
	                "Success", Messagebox.OK, Messagebox.INFORMATION, event -> {
	                    // Redirect to another page (e.g., dashboard)
	                    Executions.sendRedirect("/dashboard.zul");
	                });
	        }
	    } catch (Exception e) {
	        Messagebox.show("Error submitting application: " + e.getMessage(), 
	            "System Error", Messagebox.OK, Messagebox.ERROR);
	    }
	}

	private boolean saveLoanToDatabase() {
	    
	    return true; 
	}
}