package com.lms.custom_macro;

import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues; // Import this
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen; // Import this
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import com.lms.model.User;

public class Navbar extends HtmlMacroComponent {

    private static final long serialVersionUID = 1L;

    @Wire private Label loginName;
    @Wire private Image userProfileImage;
    @Wire private Label sidebarToggle; 
    
    private String activeUser;

    public Navbar() {
        setMacroURI("/WEB-INF/components/navbar.zul");
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        Selectors.wireComponents(this, this, false);
        User currentUser = (User) Sessions.getCurrent().getAttribute("user");
        if (currentUser == null) {
            Executions.sendRedirect("/auth/login.zul");
            return; 
        }
        if (userProfileImage != null && currentUser.getProfileImage() != null) {
             userProfileImage.setSrc("/img/" + currentUser.getProfileImage());
        }
        if (loginName != null) {
            loginName.setValue(currentUser.getName());
        }
    }
    
    @Listen("onClick = #logo")
    public void redirectToHome() {
    	Executions.sendRedirect(activeUser == "admin" ? "/admin/dashboard.zul" : "/dashboard/dashboard.zul");
    }
    
    // --- NEW TOGGLE LOGIC ---
    @Listen("onClick = #sidebarToggle")
    public void onToggleClick() {
        // Broadcast an event named "onSidebarToggle" to the whole page
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
            .publish(new Event("onSidebarToggle", null, null));
    }
}