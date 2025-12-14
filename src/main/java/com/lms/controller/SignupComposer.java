package com.lms.controller;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.zkoss.zkplus.spring.DelegatingVariableResolver; 
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.lms.constant.Role;
import com.lms.model.User;
import com.lms.service.UserService;

@VariableResolver(DelegatingVariableResolver.class)
public class SignupComposer extends SelectorComposer<Window>{

	private static final long serialVersionUID = 5281834708167657577L;
	
	@Wire
	private Textbox tname, temail, tphone, tpassword, tconfirmPwd;
	
	@Wire
	private Combobox crole;
	
	@WireVariable("realUserService")
	private UserService userService;
	
	@Listen("onClick = #signupBtn")
	public void signupAction() {
		
		String name = tname.getValue();
		String email = temail.getValue();
		String phone = tphone.getValue();
		String password = tpassword.getValue();
		String confirmPassword = tconfirmPwd.getValue();
		Role role = crole.getSelectedItem().getValue().equals("ADMIN") ? Role.ADMIN : Role.CUSTOMER;
		
		if(!password.equals(confirmPassword)) {
			alert("Password and Confirm password doesn't match");
			return;
		}
		
		//Encoding password
		String passdig = null;
		try {
			passdig = userService.passwordDigest(password);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//registering user
		User u = new User(name, email, passdig, phone, role);
		userService.saveUser(u);
		
		//redirect to home page
		
		Executions.sendRedirect("/auth/login.zul");
	}
}
