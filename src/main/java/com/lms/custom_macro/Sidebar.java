package com.lms.custom_macro;

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

public class Sidebar extends HtmlMacroComponent {

    private static final long serialVersionUID = 7780960820375450823L;
    
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

    public Sidebar() {
        this.setMacroURI("/WEB-INF/components/sidebar.zul");
    }

    // This allows you to use <sidebar activePage="loans" /> in ZUL
    public void setActivePage(String page) {
        this.activePage = page;
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        
        Selectors.wireComponents(this, this, false);
        
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(new EventListener<Event>() {
            public void onEvent(Event event) {
                if ("onSidebarToggle".equals(event.getName())) {
                    toggleSelf();
                }
            }
        });
        
        highlightMenu();
        
        // Highlight the correct menu item based on the parameter
        
    }
    
    private void toggleSelf() {
        if (sidebar.getSclass().contains("collapsed")) {
            sidebar.setSclass("sidebar");
        } else {
            sidebar.setSclass("sidebar collapsed");
        }
    }
    
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
    
    @Listen("onClick = #menuDashboard")
    public void goDashboard() { Executions.sendRedirect("/dashboard/dashboard.zul"); }

    @Listen("onClick = #menuLoans")
    public void goLoans() { Executions.sendRedirect("/user/my_loans.zul"); }
    
    @Listen("onClick = #menuApply")
    public void goApply() { Executions.sendRedirect("/applyloan/apply_loan.zul"); }
    
    @Listen("onClick = #menuEmi")
    public void goEmi() { Executions.sendRedirect("/emi/emi.zul"); }

    @Listen("onClick = #menuHistory")
    public void goHistory() { Executions.sendRedirect("/user/history.zul"); }
    
    @Listen("onClick = #menuProfile")
    public void goProfile() { Executions.sendRedirect("/profile/user_profile.zul"); }

    @Listen("onClick = #menuSupport")
    public void goSupport() { Executions.sendRedirect("/user/support.zul"); }
}
