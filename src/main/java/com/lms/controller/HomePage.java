package com.lms.controller;

import java.io.InputStream;

import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class HomePage extends SelectorComposer<Window> {

	private static final long serialVersionUID = 5912531274209602396L;

	@Wire
	private Label welcomeMsg;
	
	@Wire
	private Image img;

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		InputStream is = Sessions.getCurrent()
	            .getWebApp()
	            .getResourceAsStream("/WEB-INF/img/Loan-Management-System.jpg");

	    AImage aimg = new AImage("logo", is);
	    img.setContent(aimg);

	}

	// NAVBAR BUTTONS
	@Listen("onClick = #signBtn")
	public void onSignUpClick() {
		Executions.sendRedirect("auth/signup.zul");
	}

	@Listen("onClick = #logBtn")
	public void onLoginClick() {
		Executions.sendRedirect("auth/login.zul");
	}

	// HERO BUTTON
	@Listen("onClick = #knowMBtn")
	public void onKnowMoreClick() {
		Executions.sendRedirect("about/AboutUs.zul");
	}

	// OPTIONAL NAVBAR LINKS
	@Listen("onClick = #homeNav")
	public void onHomeClick() {
		Executions.sendRedirect(null);
	}

	@Listen("onClick = #aboutNav")
	public void onAboutClick() {
		Executions.sendRedirect("about/AboutUs.zul");
	}

	@Listen("onClick = #serviceNav")
	public void onServicesClick() {
		Executions.sendRedirect("services.zul");
	}

	@Listen("onClick = #contactNav")
	public void onContactClick() {
		Executions.sendRedirect("contact/ContactUs.zul");
	}
}
