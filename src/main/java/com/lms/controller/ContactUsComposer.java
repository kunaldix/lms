package com.lms.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for the Contact Us / Support page.
 * Handles customer inquiries, feedback, and support requests.
 */
public class ContactUsComposer extends SelectorComposer<Window> {

    private static final long serialVersionUID = -338549212691970104L;

    // Initialize Log4j Logger for auditing support traffic
    private static final Logger logger = LogManager.getLogger(ContactUsComposer.class);

    /* --- WIRE UI COMPONENTS --- */
    @Wire private Textbox name;
    @Wire private Textbox email;
    @Wire private Textbox phone;
    @Wire private Combobox contactMethod;

    @Wire private Combobox loanType;
    @Wire private Radiogroup existingCustomer;
    @Wire private Textbox loanAccNo;

    @Wire private Combobox subject;
    @Wire private Textbox message;
    @Wire private Combobox urgency;
    @Wire private Fileupload fileUpload;

    @Wire private Button submitBtn;
    @Wire private Button resetBtn;
    
    @Wire private Groupbox loanInfo;
    @Wire private Groupbox inquiryDetails;
    
    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        logger.info("ContactUsComposer initialized.");
    }

    /**
     * Validates and processes the support inquiry submission.
     */
    @Listen("onClick = #submitBtn")
    public void submitForm() {
        String userName = name.getValue();
        String userEmail = email.getValue();

        // Basic validation
        if (userName.isEmpty() || userEmail.isEmpty() || message.getValue().isEmpty()) {
            logger.warn("Support form submission blocked: Missing required fields.");
            Messagebox.show("Please fill all the required fields (*)", "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        logger.info("New support inquiry submitted by: {} ({})", userName, userEmail);
        
        /* * TODO: Integrate with EmailUtility or a SupportService 
         * to persist the inquiry to the database.
         */

        Messagebox.show("Your inquiry has been submitted successfully!", "Success", Messagebox.OK, Messagebox.INFORMATION);
        
        // Optionally reset the form after success
        resetFormLogic();
    }

    /**
     * Resets the form fields to their default state.
     */
    @Listen("onClick = #resetBtn")
    public void resetForm() {
        logger.debug("User triggered form reset on Contact Us page.");
        resetFormLogic();
        Messagebox.show("Form reset successfully.", "Reset", Messagebox.OK, Messagebox.INFORMATION);
    }

    /**
     * Internal logic for clearing form components.
     */
    private void resetFormLogic() {
        name.setValue("");
        email.setValue("");
        phone.setValue("");
        if (contactMethod != null) contactMethod.setSelectedItem(null);

        if (loanType != null) loanType.setSelectedItem(null);
        if (existingCustomer != null) existingCustomer.setSelectedIndex(1);
        loanAccNo.setValue("");
        
        if (subject != null) subject.setSelectedItem(null);
        message.setValue("");
        if (urgency != null) urgency.setSelectedItem(null);
    }
}