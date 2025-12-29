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
			rowEmailOtp.setVisible(false);
			Clients.showNotification("Email Verified!", "info", null, null, 2000);
		} else {
			Clients.showNotification("Invalid OTP", "error", null, null, 2000);
		}
	}

	@Listen("onClick = #signupBtn")
	public void signupAction() {
		if (!isEmailVerified) {
			Clients.showNotification("Please verify your email first.", "warning", null, null, 3000);
			return;
		}
		if (!tpassword.getValue().equals(tconfirmPwd.getValue())) {
			alert("Passwords do not match");
			return;
		}

		String passdig = userService.passwordDigest(tpassword.getValue());
		Role role = crole.getSelectedItem().getValue().equals("ADMIN") ? Role.ADMIN : Role.CUSTOMER;

		User u = new User(tname.getValue(), temail.getValue(), passdig, tphone.getValue(), role);
		userService.saveUser(u);

		Messagebox.show("Account created successfully!", "Success", Messagebox.OK, Messagebox.INFORMATION, event -> {
			Executions.sendRedirect("/auth/login.zul");
		});

	}
}