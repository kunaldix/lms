package com.lms.security;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.lms.model.User;
import com.lms.service.UserService;

/**
 * Custom Login Success Handler.
 * This class determines where the user should be redirected after a successful login
 * based on their role (Admin or Customer).
 */
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    // Logger instance for tracking login activity
    private static final Logger logger = LogManager.getLogger(LoginSuccessHandler.class);

    private UserService userService;

    /**
     * Injected via XML configuration.
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Triggered immediately after successful authentication.
     * It sets up the session and redirects to the appropriate ZK dashboard.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        
        // 1. Get the authenticated username (email)
        String email = authentication.getName();
        logger.info("Login successful for user: {}", email);

        // 2. Fetch the full User object from Database to put it in session
        User user = userService.getUserByEmail(email);
        
        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            logger.debug("User object for {} added to HttpSession.", email);
        } else {
            logger.warn("User record not found in database for email: {}", email);
        }

        // 3. Determine target URL based on user roles
        String targetUrl = request.getContextPath();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean isAdmin = false;
        boolean isCustomer = false;

        // Iterate through roles to decide redirection logic
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            logger.debug("Checking authority: {}", role);
            
            if (role.equals("ROLE_ADMIN")) {
                isAdmin = true;
                break;
            } else if (role.equals("ROLE_CUSTOMER")) {
                isCustomer = true;
                break;
            }
        }

        // 4. Role-based redirection to specific ZK pages
        if (isAdmin) {
            targetUrl += "/admin/dashboard.zul";
            logger.info("Redirecting Admin [{}] to admin dashboard.", email);
        } else if (isCustomer) {
            targetUrl += "/dashboard/dashboard.zul";
            logger.info("Redirecting Customer [{}] to customer dashboard.", email);
        } else {
            // Default fallback if no specific role matches
            targetUrl += "/index.zul";
            logger.warn("User [{}] has no recognized roles. Redirecting to home.", email);
        }

        // Final redirect
        response.sendRedirect(targetUrl);
    }
}