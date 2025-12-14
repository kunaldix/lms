package com.lms.controller;

import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;


public class ContactUsComposer extends SelectorComposer<Window>{


	    private static final long serialVersionUID = -338549212691970104L;
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

	        submitBtn.addEventListener("onClick", e -> submitForm());
	        resetBtn.addEventListener("onClick", e -> resetForm());
	        
//	        org.zkoss.zk.ui.Session sess = Sessions.getCurrent();
//	        
//	        // Retrieve the user object
//	        User user = (User) sess.getAttribute("user");
//	        
//	        alert(user+"");
	    }

	    private void submitForm() {
	        if (name.getValue().isEmpty() || email.getValue().isEmpty() || message.getValue().isEmpty()) {
	            Messagebox.show("Please fill all the required fields (*)");
	            return;
	        }

	        Messagebox.show("Your inquiry has been submitted successfully!");
	    }

	    private void resetForm() {
	        name.setValue("");
	        email.setValue("");
	        phone.setValue("");
	        contactMethod.setSelectedItem(null);

	        loanType.setSelectedItem(null);
	        existingCustomer.setSelectedIndex(1);
	        loanAccNo.setValue("");
	        
	        subject.setSelectedItem(null);
	        message.setValue("");
	        urgency.setSelectedItem(null);

	        Messagebox.show("Form reset successfully.");
	    }
	}

