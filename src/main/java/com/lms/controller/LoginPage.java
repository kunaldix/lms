package com.lms.controller;


import java.util.Random;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Span;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class LoginPage extends SelectorComposer<Window> {

	private static final long serialVersionUID = -6654346293830676291L;
	
	@Wire
	private Div div1, div2, div3;
	
	@Wire private Textbox newPwd, confirmPwd;
	
	@Wire private Button resetPwdBtn;

	@Wire
	private Textbox password;

	@Wire
	private Button hidePwdBtn;

	@Wire
	private A signup, fpassword;
	
    @Wire private Textbox emailBox, otpBox;
    
    @Wire private Button sendOtpBtn, validateOtpBtn;
    
    @Wire private Label otpLabel;

    private String generatedOtp;

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
	}

	@Listen("onClick = #sendOtpBtn")
    public void sendOtp() {
        String email = emailBox.getValue();

        if (email == null || email.isBlank()) {
            Messagebox.show("Please enter email");
            return;
        }

        generatedOtp = String.valueOf(100000 + new Random().nextInt(900000));

        // TODO: Send OTP via email
        System.out.println("OTP sent to email: " + generatedOtp);

        // UI changes
        sendOtpBtn.setVisible(false);
        otpLabel.setVisible(true);
        otpBox.setVisible(true);
        validateOtpBtn.setVisible(true);
    }
	
	@Listen("onClick = #validateOtpBtn")
	public void validateOtp() {
	    if (otpBox.getValue().equals(generatedOtp)) {
	        Messagebox.show("OTP Verified Successfully");

	        div2.setVisible(false);
	        div3.setVisible(true); 
	    } else {
	        Messagebox.show("Invalid OTP");
	    }
	}

	
	@Listen("onClick = #hidePwdBtn")
	public void onToggleConfirmPasswordVisibility() {
		if (password.getValue() == null)
			return;

		if ("password".equalsIgnoreCase(password.getType())) {
			password.setType("text");
			hidePwdBtn.setIconSclass("z-icon-eye");
		} else {
			password.setType("password");
			hidePwdBtn.setIconSclass("z-icon-eye-slash");
		}
	}

	@Listen("onClick = #fpassword")
	public void doForgotPWD() {
		div1.setVisible(false);
		div2.setVisible(true);
	}
	
	@Listen("onClick = #resetPwdBtn")
	public void resetPassword() {

	    if (newPwd.getValue().isBlank() || confirmPwd.getValue().isBlank()) {
	        Messagebox.show("All fields required");
	        return;
	    }

	    if (!newPwd.getValue().equals(confirmPwd.getValue())) {
	        Messagebox.show("Passwords do not match");
	        return;
	    }

	    // TODO: update password in DB
	    Messagebox.show("Password changed successfully");

	    div3.setVisible(false);
	    div1.setVisible(true);   
	}


	@Listen("onClick = #signup")
	public void goSignUp() {
		Executions.sendRedirect("signup.zul");
	}

}