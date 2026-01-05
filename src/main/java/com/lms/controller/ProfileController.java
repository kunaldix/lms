package com.lms.controller;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Component; 
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vlayout;
import com.lms.model.User;

public class ProfileController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	// Only Wire components that actually exist in profile.zul with IDs
	@Wire
	private Vlayout mainContainer;
	
	@Wire
	private Label userName,role,email,lblFullName,lblEmail,lblPhone,lblRole;
	
	@Wire
	private Image userProfileImage;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		// 1. Subscribe to Sidebar Toggle
		EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });
		
		// 2. Get User
		User currentUser = (User) Sessions.getCurrent().getAttribute("user");
        if (currentUser == null) {
            Executions.sendRedirect("/auth/login.zul");
            return; 
        }
        
        // 3. Null-Safe Data Loading
        // We check if 'userName' is not null before setting value to prevent crashes
        if (userName != null) {
            userName.setValue(currentUser.getName());
            lblFullName.setValue(currentUser.getName());
        }
        
        if (userProfileImage != null) {
            String img = currentUser.getProfileImage();
            if (img != null && !img.isEmpty()) {
            	userProfileImage.setSrc("/img/" + img);
            }
        }
        role.setValue(currentUser.getRole().name());
        email.setValue(currentUser.getEmail());
        
        lblPhone.setValue(currentUser.getPhoneNumber());
        lblRole.setValue(currentUser.getRole().name());
        lblEmail.setValue(currentUser.getEmail());
	}
	
	@Listen("onClick = #btnEditProfile")
	public void openEditProfile() {
	    Executions.createComponents(
	        "/profile/edit-profile.zul",
	        null,
	        null
	    );
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