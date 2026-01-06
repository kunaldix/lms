package com.lms.admin.controller;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vlayout;

import com.lms.model.User;
import com.lms.service.LoanService;
import com.lms.service.UserService;

/**
 * Controller for the User Details view in the Admin panel.
 * Displays a comprehensive profile and loan summary for a specific customer
 * identified by the 'userId' URL parameter.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class Userdetails extends SelectorComposer<Div> {
	
	private static final long serialVersionUID = 615498041154939607L;

	// Initialize Log4j Logger for tracking admin access to user data
	private static final Logger logger = LogManager.getLogger(Userdetails.class);

	/* ---------- LAYOUT ---------- */
    @Wire private Vlayout mainContainer;

    /* ---------- PROFILE LABELS ---------- */
    @Wire private Label lblName;
    @Wire private Label lblUserId;
    @Wire private Label lblEmail;
    @Wire private Label lblPhone;

    /* ---------- LOAN SUMMARY ---------- */
    @Wire private Label lblTotalLoan;
    @Wire private Label lblOutstanding;
    @Wire private Label lblActiveLoans;

    /* ---------- SERVICES ---------- */
    @WireVariable("realUserService")
    private UserService userService;

    @WireVariable
    private LoanService loanService;

    @Override
    public void doAfterCompose(Div comp) throws Exception {
        super.doAfterCompose(comp);
        
        logger.info("Initializing UserDetails view.");

        subscribeSidebarEvents();
        loadUserDetails();
    }

    /**
     * Extracts the userId from the execution parameters and fetches data from the service.
     */
    private void loadUserDetails() {
        String userIdParam = Executions.getCurrent().getParameter("userId");

        if (userIdParam == null || userIdParam.isEmpty()) {
            logger.warn("Access attempt to UserDetails without a valid userId parameter.");
            redirectToUsers();
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdParam);
            logger.debug("Fetching details for User ID: {}", userId);
        } catch (NumberFormatException e) {
            logger.error("Invalid User ID format received: {}", userIdParam);
            redirectToUsers();
            return;
        }

        // Fetch user from DB using the UserService
        Optional<User> optionalUser = userService.getUserById(userId);

        optionalUser.ifPresentOrElse(
            user -> {
                logger.info("Successfully loaded data for user: {} ({})", user.getName(), user.getEmail());
                populateUserDetails(user);
            },
            () -> {
                logger.warn("User with ID {} not found in database.", userId);
                redirectToUsers();
            }
        );
    }

    /**
     * Updates the UI labels with data from the User and LoanService.
     */
    private void populateUserDetails(User user) {
        // Profile information
        lblName.setValue(user.getName());
        lblUserId.setValue("ID: #EMP" + user.getId());
        lblEmail.setValue(user.getEmail());
        lblPhone.setValue(user.getPhoneNumber());

        // Financial summary (Currency formatted manually or via a helper could be added here)
        lblTotalLoan.setValue("₹ " + loanService.getTotalLoanOfUser(user.getId()));
        lblOutstanding.setValue("₹ " + loanService.getTotalDebt(user.getId()));
        lblActiveLoans.setValue(String.valueOf(loanService.getActiveLoans(user.getId())));
        
        logger.debug("UI populated for user ID: {}", user.getId());
    }

    /**
     * Listens for sidebar toggle events to adjust the layout dynamically.
     */
    private void subscribeSidebarEvents() {
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
            .subscribe(event -> {
                if ("onSidebarToggle".equals(event.getName())) {
                    resizeContent();
                }
            });
    }

    /**
     * Helper to adjust the CSS class of the main container based on sidebar state.
     */
    private void resizeContent() {
        if (mainContainer == null) return;

        String base = "main-container";
        boolean isEnlarged = mainContainer.getSclass().contains("enlarge");
        
        mainContainer.setSclass(isEnlarged ? base : base + " enlarge");
        logger.debug("Layout resized. Enlarged: {}", !isEnlarged);
    }

    /**
     * Redirects the admin back to the user list page if data loading fails.
     */
    private void redirectToUsers() {
        Executions.sendRedirect("users.zul");
    }
}