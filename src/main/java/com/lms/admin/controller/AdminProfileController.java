package com.lms.admin.controller;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import com.lms.model.User;

public class AdminProfileController extends SelectorComposer<Vlayout> {

    private static final long serialVersionUID = 1L;
    
    @Wire
	private Vlayout mainContainer;
    @Wire private Image adminProfileImage;

    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
    	// TODO Auto-generated method stub
    	super.doAfterCompose(comp);
    	
    	EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });
    	
    	User currentUser = (User) Sessions.getCurrent().getAttribute("user");
        if (currentUser == null) {
            Executions.sendRedirect("/auth/login.zul");
            return; 
        }
        
        if (adminProfileImage != null) {
            String img = currentUser.getProfileImage();
            if (img != null && !img.isEmpty()) {
            	adminProfileImage.setSrc("/img/" + img);
            }
        }
    }
    
    private void resizeContent() {
		if (mainContainer != null) {
            if (mainContainer.getSclass().contains("enlarge")) {
                mainContainer.setSclass("main-container");
            } else {
                mainContainer.setSclass("main-container enlarge");
            }
        }
    }
}
