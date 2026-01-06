package com.lms.custom_macro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen; 
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import com.lms.model.User;

/**
 * Navbar macro component for CreditHub.
 * Handles user profile display, role-based home redirection, and sidebar toggle broadcasting.
 */
public class Navbar extends HtmlMacroComponent {

    private static final long serialVersionUID = 1L;
    
    // Log4j Logger for tracking navigation and session status
    private static final Logger logger = LogManager.getLogger(Navbar.class);

    @Wire private Label loginName;
    @Wire private Image userProfileImage;
    @Wire private Label sidebarToggle; 
    
    private String activeUserRole;

    public Navbar() {
        setMacroURI("/WEB-INF/components/navbar.zul");
    }

    /**
     * Initializes the navbar after the component tree is composed.
     * Checks for a valid session and sets up user-specific UI elements.
     */
    @Override
    public void afterCompose() {
        super.afterCompose();
        
        // Manual wiring required for Macro Components
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        User currentUser = (User) Sessions.getCurrent().getAttribute("user");
        
        if (currentUser == null) {
            logger.warn("No user found in session. Redirecting to login page.");
            Executions.sendRedirect("/auth/login.zul");
            return; 
        }

        // Store role for navigation logic
        this.activeUserRole = currentUser.getRole().name().toLowerCase(); 
        logger.info("Navbar loaded for user: {} with role: {}", currentUser.getName(), activeUserRole);

        // Load profile image if available
        if (userProfileImage != null && currentUser.getProfileImage() != null) {
             userProfileImage.setSrc("/img/" + currentUser.getProfileImage());
        }
        
        // Display user name in navbar
        if (loginName != null) {
            loginName.setValue(currentUser.getName());
        }
    }
    
    /**
     * Redirects to the appropriate profile page based on the user's role.
     */
    @Listen("onClick = #popProfile")
    public void redirectToProfile() {
        User currentUser = (User) Sessions.getCurrent().getAttribute("user");
        if (currentUser != null) {
            String role = currentUser.getRole().name();
            logger.debug("Redirecting {} to profile page.", role);
            
            if ("ADMIN".equalsIgnoreCase(role)) {
                Executions.sendRedirect("/admin/profile.zul");
            } else {
                Executions.sendRedirect("/profile/user_profile.zul");
            }
        }
    }
    
    /**
     * Redirects to the respective dashboard when the logo is clicked.
     */
    @Listen("onClick = #logo")
    public void redirectToHome() {
        logger.info("Logo clicked. Redirecting user with role [{}] to home.", activeUserRole);
        
        if ("admin".equalsIgnoreCase(activeUserRole)) {
            Executions.sendRedirect("/admin/dashboard.zul");
        } else {
            Executions.sendRedirect("/dashboard/dashboard.zul");
        }
    }
    
    /**
     * Publishes a toggle event to the 'dashboardQueue'.
     * The Sidebar and Content Area subscribe to this event to adjust their layouts.
     */
    @Listen("onClick = #sidebarToggle")
    public void onToggleClick() {
        logger.debug("Sidebar toggle clicked. Publishing 'onSidebarToggle' event.");
        
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
            .publish(new Event("onSidebarToggle", null, null));
    }
}