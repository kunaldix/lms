package com.lms.custom_macro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Vlayout;

/**
 * AdminSidebar is a custom ZK Macro Component.
 * It manages navigation, menu highlighting, and the sidebar toggle state
 * specifically for the Admin role in CreditHub.
 */
public class AdminSidebar extends HtmlMacroComponent {

    private static final long serialVersionUID = 5647598979621519995L;

    // Initialize Log4j Logger for sidebar navigation auditing
    private static final Logger logger = LogManager.getLogger(AdminSidebar.class);

    // Wiring the menu items defined in adminsidebar.zul
    @Wire private Hlayout menuDashboard;
    @Wire private Hlayout menuManageUsers;
    @Wire private Hlayout menuProfile;
    @Wire private Hlayout menuLoanApplications;
    @Wire private Vlayout sidebar;

    private String activePage;

    /**
     * Default constructor setting the location of the macro template.
     */
    public AdminSidebar() {
        this.setMacroURI("/WEB-INF/components/adminsidebar.zul");
    }

    /**
     * Setter for the activePage property.
     * Used in ZUL files like: <sidebar activePage="dashboard" />
     * @param page String identifier for the page to highlight.
     */
    public void setActivePage(String page) {
        this.activePage = page;
    }

    /**
     * Called after ZK has created the component and its children.
     * Handles manual wiring and EventQueue subscriptions.
     */
    @Override
    public void afterCompose() {
        super.afterCompose();

        // Since this is a Macro and not a standard Composer, we wire manually
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);

        // Listen for sidebar toggle events from the Global Header
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
            .subscribe(new EventListener<Event>() {
                public void onEvent(Event event) {
                    if ("onSidebarToggle".equals(event.getName())) {
                        logger.debug("Sidebar toggle event received.");
                        toggleSelf();
                    }
                }
            });

        // Apply active CSS class to the currently selected menu item
        highlightMenu();
        logger.info("Admin sidebar initialized. Active page: {}", activePage);
    }

    /**
     * Toggles the CSS class for the sidebar to switch between 
     * expanded and collapsed states.
     */
    private void toggleSelf() {
        String currentSclass = sidebar.getSclass();
        if (currentSclass != null && currentSclass.contains("collapsed")) {
            sidebar.setSclass("sidebar");
        } else {
            sidebar.setSclass("sidebar collapsed");
        }
    }

    /**
     * Compares the activePage property against menu items and appends the "active" CSS class.
     */
    private void highlightMenu() {
        if ("dashboard".equals(activePage) && menuDashboard != null) {
            menuDashboard.setSclass(menuDashboard.getSclass() + " active");
        } else if ("users".equals(activePage) && menuManageUsers != null) {
            menuManageUsers.setSclass(menuManageUsers.getSclass() + " active");
        } else if ("applications".equals(activePage) && menuLoanApplications != null) {
            menuLoanApplications.setSclass(menuLoanApplications.getSclass() + " active");
        } else if ("profile".equals(activePage) && menuProfile != null) {
            menuProfile.setSclass(menuProfile.getSclass() + " active");
        }
    }

    /* ==========================================================================
       Navigation Listeners
       ========================================================================== */

    @Listen("onClick = #menuDashboard")
    public void goDashboard() {
        logger.info("Navigating to Admin Dashboard.");
        Executions.sendRedirect("/admin/dashboard.zul");
    }

    @Listen("onClick = #menuLoanApplications")
    public void goLoans() {
        logger.info("Navigating to Loan Applications Management.");
        Executions.sendRedirect("/admin/applications.zul");
    }

    @Listen("onClick = #menuManageUsers")
    public void goUsers() {
        logger.info("Navigating to User Management.");
        Executions.sendRedirect("/admin/users/users.zul");
    }

    @Listen("onClick = #menuProfile")
    public void goProfile() {
        logger.info("Navigating to Admin Profile.");
        Executions.sendRedirect("/admin/profile.zul");
    }
}