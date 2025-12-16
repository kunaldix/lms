package com.lms.custom_macro;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;

import com.lms.model.User;

public class Navbar extends HtmlMacroComponent{

	private static final long serialVersionUID = -6217607895134331963L;
	
	@Wire
	private Label loginName;
	@Wire
	private Image userProfileImage;
	
	public Navbar() {
        setMacroURI("/WEB-INF/components/navbar.zul");
    }
	
	@Override
	public void afterCompose() {
		// TODO Auto-generated method stub
		super.afterCompose();
		
		User currentUser = (User) Sessions.getCurrent().getAttribute("user");
		
		if(currentUser == null) {
			Executions.sendRedirect("/auth/login.zul");
		}
		userProfileImage.setSrc("/img/"+currentUser.getProfileImage());
		loginName.setValue(currentUser.getName());
	}
}
