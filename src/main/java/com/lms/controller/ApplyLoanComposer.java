package com.lms.controller;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import java.math.BigDecimal;

public class ApplyLoanComposer extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;

    // --- Wire Components ---
    @Wire private Div step1;
    @Wire private Div step2;
    @Wire private Div step3;
    @Wire private Div step4;
    @Wire private Div step5;
    @Wire private Div step6;

    @Wire private Label lblProgress;
    @Wire private Button btnBack, btnNext, btnSubmit;

    @Wire private Combobox cmbLoanType;
    @Wire private Decimalbox decInterest;
    @Wire private Textbox txtRepay;

    @Wire private Radiogroup rgEmpType;
    @Wire private Row rowEmployer, rowSalarySlip, rowWorkExperience;
    @Wire private Row rowBusiness, rowGST, rowITR;

    @Wire private Checkbox chkTerm1, chkTerm2;
    
    @Wire
	private Vlayout mainContainer;
    
    private int currentStep = 1;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });
        
        updateWizardVisibility();
    }
    
    private void resizeContent() {
		if (mainContainer != null) {
            if (mainContainer.getSclass().contains("enlarge")) {
                mainContainer.setSclass("main-container");
            } else {
                mainContainer.setSclass("main-container enlarge");
            }
        }
    }

    // NEXT BUTTON
    @Listen("onClick = #btnNext")
    public void onNext() {
        currentStep++;
        updateWizardVisibility();
    }

    // BACK BUTTON
    @Listen("onClick = #btnBack")
    public void onBack() {
        if (currentStep > 1) {
            currentStep--;
            updateWizardVisibility();
        }
    }

    // LOAN TYPE CHANGE
    @Listen("onSelect = #cmbLoanType")
    public void onLoanTypeChange() {
        String type = cmbLoanType.getValue();

        if ("Personal Loan".equals(type)) {
            decInterest.setValue(new BigDecimal("11.5"));
            txtRepay.setValue("EMI (Standard)");
        } else if ("Home Loan".equals(type)) {
            decInterest.setValue(new BigDecimal("8.4"));
            txtRepay.setValue("EMI (Reducing Balance)");
        } else if ("Car Loan".equals(type)) {
            decInterest.setValue(new BigDecimal("9.2"));
            txtRepay.setValue("EMI (Fixed)");
        }
    }

    // EMPLOYMENT CHANGE
    @Listen("onCheck = #rgEmpType")
    public void onEmploymentChange() {
        Radio selected = rgEmpType.getSelectedItem();
        boolean isSalaried = "Salaried".equals(selected.getValue());

        rowEmployer.setVisible(isSalaried);
        rowSalarySlip.setVisible(isSalaried);
        rowWorkExperience.setVisible(isSalaried);
        
        rowBusiness.setVisible(!isSalaried);
        rowGST.setVisible(!isSalaried);
        rowITR.setVisible(!isSalaried);
    }

    // SUBMIT
    @Listen("onClick = #btnSubmit")
    public void onSubmit() {
        Messagebox.show("Application Submitted Successfully via MVC!");
    }

    // STEP SWITCHING
    private void updateWizardVisibility() {
        step1.setVisible(currentStep == 1);
        step2.setVisible(currentStep == 2);
        step3.setVisible(currentStep == 3);
        step4.setVisible(currentStep == 4);
        step5.setVisible(currentStep == 5);
        step6.setVisible(currentStep == 6);

        lblProgress.setValue("Step " + currentStep + " of 6");

        btnBack.setDisabled(currentStep == 1);
        btnNext.setVisible(currentStep != 6);
        btnSubmit.setVisible(currentStep == 6);
    }
}
