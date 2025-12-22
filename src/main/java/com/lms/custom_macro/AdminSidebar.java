package com.lms.custom_macro;

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

public class AdminSidebar extends HtmlMacroComponent{

	private static final long serialVersionUID = 5647598979621519995L;
	
	// Wiring the menu items from sidebar.zul
    @Wire private Hlayout menuDashboard;
    @Wire private Hlayout menuManageUsers;
    @Wire private Hlayout menuProfile;
    @Wire private Hlayout menuLoanApplications;	
	@Wire private Vlayout sidebar;

    private String activePage;

    public AdminSidebar() {
        this.setMacroURI("/WEB-INF/components/adminsidebar.zul");
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
        } else if ("users".equals(activePage) && menuManageUsers != null) {
        	menuManageUsers.setSclass(menuManageUsers.getSclass() + " active");
        } else if ("applications".equals(activePage) && menuLoanApplications != null) {
        	menuLoanApplications.setSclass(menuLoanApplications.getSclass() + " active");
        }else if ("profile".equals(activePage) && menuProfile != null) {
            menuProfile.setSclass(menuProfile.getSclass() + " active");
        } 
    }
    
    @Listen("onClick = #menuDashboard")
    public void goDashboard() { Executions.sendRedirect("/admin/dashboard.zul"); }

    @Listen("onClick = #menuLoanApplications")
    public void goLoans() { Executions.sendRedirect("/admin/loan_application.zul"); }
    
    @Listen("onClick = #menuManageUsers")
    public void goApply() { Executions.sendRedirect("/admin/users.zul"); }
    
    @Listen("onClick = #menuProfile")
    public void goProfile() { Executions.sendRedirect("/admin/profile.zul"); }
}
