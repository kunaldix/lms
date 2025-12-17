package com.lms.controller;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients; // Import Clients
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.lms.model.User;
import com.lms.service.UserService;
import com.lms.utils.EmailUtility;

@VariableResolver(DelegatingVariableResolver.class)
public class LoginPage extends SelectorComposer<Window> {

	private static final long serialVersionUID = -6654346293830676291L;
	
	@Wire
	private Div div1, div2, div3;
	
	@Wire private Textbox newPwd, confirmPwd;
	
	@Wire private Button resetPwdBtn;
	
	@Wire
	private A signup, fpassword;
	
    @Wire private Textbox emailBox, otpBox;
    
    @Wire private Button sendOtpBtn, validateOtpBtn;
    
    @Wire private Label otpLabel;

    private String generatedOtp;
    
    private User user;
    
    @WireVariable("realUserService")
    private UserService userService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
	}

	@Listen("onClick = #sendOtpBtn")
    public void sendOtp() {
        String email = emailBox.getValue();
        
        user = userService.getUserByEmail(email);
        
        if(user == null) {
        		Clients.showNotification("Account doesn't exists", "warning", emailBox, "end_center", 2000);
            return;
        }

        if (email == null || email.isBlank()) {
            // Show warning directly next to the email box
            Clients.showNotification("Please enter email", "warning", emailBox, "end_center", 2000);
            return;
        }

        generatedOtp = String.valueOf(100000 + new Random().nextInt(900000));

        System.out.println("OTP sent to email: " + generatedOtp);
        
         try {
             EmailUtility.sendOtpEmail(email, generatedOtp);
             // Show success message at top of screen
             Clients.showNotification("OTP sent successfully", "info", null, "top_center", 3000);
         } catch (Exception e) {
             e.printStackTrace();
             Clients.showNotification("Error sending email. Check logs.", "error", null, "top_center", 3000);
             return;
         }

        // UI changes
        sendOtpBtn.setVisible(false);
        otpLabel.setVisible(true);
        otpBox.setVisible(true);
        validateOtpBtn.setVisible(true);
    }
	
	@Listen("onClick = #validateOtpBtn")
	public void validateOtp() {
	    if (otpBox.getValue().equals(generatedOtp)) {
	        Clients.showNotification("OTP Verified Successfully", "info", null, "top_center", 2000);

	        div2.setVisible(false);
	        div3.setVisible(true); 
	    } else {
            // Show error pointing to the OTP box
	        Clients.showNotification("Invalid OTP", "error", otpBox, "end_center", 2000);
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
            // Warning on the new password field
            Clients.showNotification("All fields required", "warning", newPwd, "end_center", 2000);
	        return;
	    }

	    if (!newPwd.getValue().equals(confirmPwd.getValue())) {
            // Error pointing to confirm password field
            Clients.showNotification("Passwords do not match", "error", confirmPwd, "end_center", 2000);
	        return;
	    }
	    
	    String passdig = null;
		try {
			passdig = userService.passwordDigest(newPwd.getValue());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    userService.updatePassword(user.getId(), passdig);
		
        Clients.showNotification("Password changed successfully", "info", null, "top_center", 3000);

	    div3.setVisible(false);
	    div1.setVisible(true);   
	}


	@Listen("onClick = #signup")
	public void goSignUp() {
		Executions.sendRedirect("signup.zul");
	}

}