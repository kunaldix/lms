package com.lms.controller;

import java.util.Random;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import com.lms.constant.Role;
import com.lms.model.User;
import com.lms.service.UserService;
import com.lms.utils.EmailUtility;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SignupComposer extends SelectorComposer<Window> {

	private static final long serialVersionUID = 5281834708167657577L;

	@Wire
	private Textbox tname, temail, tphone, tpassword, tconfirmPwd, tEmailOtp;
	@Wire
	private Combobox crole;
	@Wire
	private Button btnVerifyEmail, btnConfirmEmail, signupBtn;
	@Wire
	private Hlayout rowEmailOtp;

	private String generatedEmailOtp;
	private boolean isEmailVerified = false;

	@WireVariable("realUserService")
	private UserService userService;

	// Real-time button visibility logic
	@Listen("onChanging = #temail")
	public void onEmailChanging(InputEvent event) {
		String value = event.getValue();
		// Regex to check if input looks like an email
		boolean isValidFormat = value != null && value.matches("^[A-Za-z0-9+_.-]+@(.+)$");
		btnVerifyEmail.setVisible(isValidFormat);
	}

	@Listen("onClick = #btnVerifyEmail")
	public void onSendEmailOtp() {
		String email = temail.getValue();
		// Generate 6-digit OTP
		generatedEmailOtp = String.format("%06d", new Random().nextInt(999999));

		EmailUtility.sendVerificationEmail(email, generatedEmailOtp);

		rowEmailOtp.setVisible(true);
		Clients.showNotification("OTP sent to " + email + " "+ generatedEmailOtp);
	}

	@Listen("onClick = #btnConfirmEmail")
	public void onConfirmEmail() {
		if (tEmailOtp.getValue().equals(generatedEmailOtp)) {
			isEmailVerified = true;
			temail.setReadonly(true);
			btnVerifyEmail.setDisabled(true);
			btnVerifyEmail.setVisible(false);
			rowEmailOtp.setVisible(false);
			Clients.showNotification("Email Verified!", "info", null, null, 2000);
		} else {
			Clients.showNotification("Invalid OTP", "error", null, null, 2000);
		}
	}

	@Listen("onClick = #signupBtn")
	public void signupAction() {
	    String name = tname.getValue().trim();
	    String email = temail.getValue().trim();
	    String phone = tphone.getValue().trim();
	    String password = tpassword.getValue();
	    String confirmPwd = tconfirmPwd.getValue();

	    // 1. Check if Email is verified
	    if (!isEmailVerified) {
	        Clients.showNotification("Please verify your email first.", "warning", null, "middle_center", 3000);
	        return;
	    }

	    // 2. Mobile Number Validation (Exactly 10 digits)
	    if (!phone.matches("^[0-9]{10}$")) {
	        Clients.showNotification("Mobile number must be exactly 10 digits.", "error", tphone, "end_after", 3000);
	        tphone.focus();
	        return;
	    }

	    // 3. Password Strength Validation
	    // Regex: One uppercase, one lowercase, one number, one special character, min 8 chars
	    String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
	    if (!password.matches(passwordRegex)) {
	        Clients.showNotification("Password must contain: one uppercase, one lowercase, one number, and one special character and at least 8 characters.", 
	                "error", tpassword, "end_after", 5000);
	        return;
	    }

	    // 4. Password Match Validation
	    if (!password.equals(confirmPwd)) {
	        Clients.showNotification("Passwords do not match.", "error", tconfirmPwd, "end_after", 3000);
	        return;
	    }

	    // 5. Check empty fields (Basic)
	    if (name.isEmpty() || crole.getSelectedItem() == null) {
	        Clients.showNotification("All fields are required.", "warning", null, "middle_center", 3000);
	        return;
	    }

	    // Proceed with Saving
	    try {
	        String passdig = userService.passwordDigest(password);
	        Role role = crole.getSelectedItem().getValue().equals("ADMIN") ? Role.ADMIN : Role.CUSTOMER;

	        User u = new User(name, email, passdig, phone, role);
	        userService.saveUser(u);

	        Messagebox.show("Account created successfully!", "Success", Messagebox.OK, Messagebox.INFORMATION, event -> {
	            Executions.sendRedirect("/auth/login.zul");
	        });
	    } catch (Exception e) {
	        Clients.showNotification("Signup failed: " + e.getMessage(), "error", null, "middle_center", 3000);
	    }
	}
}