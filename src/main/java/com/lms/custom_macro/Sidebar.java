package com.lms.custom_macro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Vlayout;

/**
 * Sidebar macro component for the Customer role in CreditHub.
 * Manages navigation, menu state highlighting, and responsiveness via EventQueues.
 */
public class Sidebar extends HtmlMacroComponent {

    private static final long serialVersionUID = 7780960820375450823L;
    
    // Log4j Logger for navigation tracking
    private static final Logger logger = LogManager.getLogger(Sidebar.class);
    
    // Wiring the menu items from sidebar.zul
    @Wire private Hlayout menuDashboard;
    @Wire private Hlayout menuLoans;
    @Wire private Hlayout menuApply;
    @Wire private Hlayout menuEmi;
    @Wire private Hlayout menuHistory;
    @Wire private Hlayout menuProfile;
    @Wire private Hlayout menuSupport;    
    @Wire private Vlayout sidebar;

    private String activePage;

    /**
     * Constructor links the Java class to the ZUL template.
     */
    public Sidebar() {
        this.setMacroURI("/WEB-INF/components/sidebar.zul");
    }

    /**
     * Allows setting the active page from the parent ZUL.
     * Example: <sidebar activePage="emi" />
     * @param page The key for the menu item to highlight.
     */
    public void setActivePage(String page) {
        this.activePage = page;
    }

    /**
     * Lifecycle method called after the component is created.
     * Subscribes to the global dashboardQueue to handle UI toggles.
     */
    @Override
    public void afterCompose() {
        super.afterCompose();
        
        // Manual wiring for component references and @Listen annotations
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        // Subscribe to the Sidebar Toggle event broadcasted by the Navbar
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(new EventListener<Event>() {
            public void onEvent(Event event) {
                if ("onSidebarToggle".equals(event.getName())) {
                    logger.debug("Sidebar received toggle command.");
                    toggleSelf();
                }
            }
        });
        
        // Highlight the menu item corresponding to the current page
        highlightMenu();
        logger.info("Customer Sidebar initialized for page: {}", activePage);
    }
    
    /**
     * Switches between expanded and collapsed states using CSS classes.
     */
    private void toggleSelf() {
        String currentClass = sidebar.getSclass();
        if (currentClass != null && currentClass.contains("collapsed")) {
            sidebar.setSclass("sidebar");
        } else {
            sidebar.setSclass("sidebar collapsed");
        }
    }
    
    /**
     * Applies the 'active' CSS class to the layout identified by the activePage property.
     */
    private void highlightMenu() {
        if ("dashboard".equals(activePage) && menuDashboard != null) {
            menuDashboard.setSclass(menuDashboard.getSclass() + " active");
        } else if ("loans".equals(activePage) && menuLoans != null) {
            menuLoans.setSclass(menuLoans.getSclass() + " active");
        } else if ("apply".equals(activePage) && menuApply != null) {
            menuApply.setSclass(menuApply.getSclass() + " active");
        } else if ("history".equals(activePage) && menuHistory != null) {
            menuHistory.setSclass(menuHistory.getSclass() + " active");
        } else if ("profile".equals(activePage) && menuProfile != null) {
            menuProfile.setSclass(menuProfile.getSclass() + " active");
        } else if ("emi".equals(activePage) && menuEmi != null) {
            menuEmi.setSclass(menuEmi.getSclass() + " active");
        }
    }
    
    /* ==========================================================================
       Customer Navigation Listeners
       ========================================================================== */

    @Listen("onClick = #menuDashboard")
    public void goDashboard() { 
        logger.info("User navigating to Customer Dashboard.");
        Executions.sendRedirect("/dashboard/dashboard.zul"); 
    }

    @Listen("onClick = #menuLoans")
    public void goLoans() { 
        logger.info("User navigating to My Loans.");
        Executions.sendRedirect("/my_loans.zul"); 
    }
    
    @Listen("onClick = #menuApply")
    public void goApply() { 
        logger.info("User navigating to Loan Application.");
        Executions.sendRedirect("/applyloan/apply_loan.zul"); 
    }
    
    @Listen("onClick = #menuEmi")
    public void goEmi() { 
        logger.info("User navigating to EMI Schedule.");
        Executions.sendRedirect("/emi/emi.zul"); 
    }

    @Listen("onClick = #menuHistory")
    public void goHistory() { 
        logger.info("User navigating to Transaction History.");
        Executions.sendRedirect("/history.zul"); 
    }
    
    @Listen("onClick = #menuProfile")
    public void goProfile() { 
        logger.info("User navigating to Profile.");
        Executions.sendRedirect("/profile/user_profile.zul"); 
    }

    @Listen("onClick = #menuSupport")
    public void goSupport() { 
        logger.info("User navigating to Support.");
        Executions.sendRedirect("/support.zul"); 
    }
}