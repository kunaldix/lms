package com.lms.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.lms.model.User;
import com.lms.service.UserService;

public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	
	private UserService userService;
	
	public void setUserService(UserService userService) {
        this.userService = userService;
    }

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// 1. Get the logged-in username (email)
        String email = authentication.getName();

        // 2. Fetch the full User object from your Database
        User user = userService.getUserByEmail(email);

        // 3. Store the user in the Session
        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        // 4. Continue with the standard redirect (to home page or where they clicked)
        super.onAuthenticationSuccess(request, response, authentication);
	}

}
