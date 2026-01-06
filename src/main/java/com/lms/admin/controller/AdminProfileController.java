package com.lms.admin.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import com.lms.model.User;

/**
 * Controller for the Admin Profile view.
 * This class manages the display of admin-specific account details and 
 * ensures the view remains responsive to sidebar toggle events.
 */
public class AdminProfileController extends SelectorComposer<Vlayout> {

    private static final long serialVersionUID = 1L;
    
    // Initialize Apache Log4j logger for profile-related activity tracking
    private static final Logger logger = LogManager.getLogger(AdminProfileController.class);
    
    @Wire private Vlayout mainContainer;
    @Wire private Image adminProfileImage;

    /**
     * Initializes the controller, sets up event subscriptions, and loads admin data.
     * @param comp The Vlayout component being composed.
     */
    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        
        logger.info("AdminProfileController initialized.");

        // Subscribe to global desktop queue for sidebar responsive behavior
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                logger.debug("Received sidebar toggle event in Admin Profile.");
                resizeContent();
            }
        });
        
        // Fetch current admin user from session
        User currentUser = (User) Sessions.getCurrent().getAttribute("user");
        
        if (currentUser == null) {
            logger.warn("Unauthorized access attempt or session expired. Redirecting to login.");
            Executions.sendRedirect("/auth/login.zul");
            return; 
        }
        
        logger.info("Loading profile details for Admin: {}", currentUser.getEmail());

        // Handle Profile Image loading
        if (adminProfileImage != null) {
            String img = currentUser.getProfileImage();
            if (img != null && !img.isEmpty()) {
                adminProfileImage.setSrc("/img/" + img);
                logger.debug("Admin profile image set: /img/{}", img);
            } else {
                logger.debug("No profile image found for admin, using default placeholder.");
            }
        }
    }
    
    /**
     * Toggles the CSS class for the main container to adjust margins 
     * based on sidebar state.
     */
    private void resizeContent() {
        if (mainContainer != null) {
            String currentSclass = mainContainer.getSclass();
            if (currentSclass != null && currentSclass.contains("enlarge")) {
                mainContainer.setSclass("main-container");
            } else {
                mainContainer.setSclass("main-container enlarge");
            }
        }
    }
}