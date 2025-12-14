package com.lms.controller;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.lms.model.User;
import com.lms.service.UserService;
import com.lms.service.impl.UserServiceImpl;

public class LoginPage extends SelectorComposer<Window> {

	private static final long serialVersionUID = -6654346293830676291L;
	@Wire
	private Div div1;
	@Wire
	private Div div2, div3;

	@Wire
	private Hbox linkA;

	@Wire
	private Textbox userid;

	@Wire
	private Textbox password;

	@Wire
	private Textbox phoneno;

	@Wire
	private Textbox otp;

	@Wire
	private Button loginBtn;

	@Wire
	private Button otpBtn;

	@Wire
	private Button backBtn;

	@Wire
	private Button loginpage;

	@Wire
	private Button hidePwdBtn;

	@Wire
	private A signup, fpassword;

	private UserService userService = new UserServiceImpl();

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
	}

	@Listen("onClick = #loginBtn")
	public void doLogin() {

		if (div1.isVisible()) {
			String user = userid.getValue().trim();
			String pass = password.getValue().trim();

			if (user.isEmpty() || pass.isEmpty()) {
				Clients.showNotification("Please enter both Username and Password!", "warning", null, "middle_center",
						2000);
				return;
			}

			// checking if user exists
			User currUser = userService.getUserByEmail(user);
			if (currUser == null) {
				Clients.showNotification("User doesn't exists, Create Account!", "warning", null, "middle_center",
						2000);
				return;
			}

			// Logging User
			try {
				if (currUser.getPassword().equals(userService.passwordDigest(pass))) {
					Executions.sendRedirect("/index.zul");
					Sessions.getCurrent().setAttribute("user", user);
				} else {
					Clients.showNotification("Invalid Credentials!", "warning", null, "middle_center", 2000);
					return;
				}
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			String phone = phoneno.getValue().trim();
			String otpstr = otp.getValue().trim();

			loginWithPhone(phone, otpstr);
		}

	}

	public void loginWithPhone(String phone, String otpstr) {
		if (phone.isEmpty()) {
			Clients.showNotification("Please enter Phone No.!", "warning", null, "middle_center", 2000);
			return;
		}
		if (otpstr.isEmpty()) {
			Clients.showNotification("Please enter OTP.!", "warning", null, "middle_center", 2000);
			return;
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

	@Listen("onClick = #otpBtn")
	public void doLoginWithOtp() {
		div1.setVisible(false);
		div2.setVisible(true);
		otpBtn.setVisible(false);
		backBtn.setVisible(true);
		linkA.setVisible(false);
	}

	@Listen("onClick = #backBtn")
	public void doBackLogin() {
		div1.setVisible(true);
		div2.setVisible(false);
		div3.setVisible(false);
		otpBtn.setVisible(true);
		loginBtn.setVisible(true);
		backBtn.setVisible(false);
		linkA.setVisible(true);
	}

	@Listen("onClick = #fpassword")
	public void doForgotPWD() {
		div1.setVisible(false);
		div3.setVisible(true);
		backBtn.setVisible(true);
		otpBtn.setVisible(false);
		loginBtn.setVisible(false);
		linkA.setVisible(false);
	}

	@Listen("onClick = #signup")
	public void goSignUp() {
		Executions.sendRedirect("signup.zul");
	}

}