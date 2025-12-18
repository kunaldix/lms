package com.lms.controller;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

public class ApplyLoanComposer extends SelectorComposer<Component> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5630649158860218775L;

	// Wire the Step Divs
    @Wire Div step1, step2, step3, step4, step5, step6;
    
    // Wire the Indicators
    @Wire Div step1Indicator, step2Indicator, step3Indicator, step4Indicator, step5Indicator, step6Indicator;
    
    // Wire the Buttons
    @Wire Button btnBack, btnNext, btnSubmit;

    private int currentStep = 1;
    private final int MAX_STEP = 6;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        updateUI();
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
}