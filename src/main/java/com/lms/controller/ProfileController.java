package com.lms.controller;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.lms.model.User;

public class ProfileController extends SelectorComposer<Vlayout> {

	private static final long serialVersionUID = -3206917372193374003L;
	
	@Wire 
	private Textbox txtFullName, txtEmail, txtMobile;
	
    @Wire 
    private Datebox txtDob;
    
    @Wire 
    private Combobox cmbGender;
	
    @Wire
	private Listbox loanListbox;

	@Wire
	private Vlayout sidebar;
	
	@Wire
	private Vlayout mainContainer;
	
	@Wire
	private Label sidebarToggle;

	@Override
	public void doAfterCompose(Vlayout comp) throws Exception {
		super.doAfterCompose(comp);

		sidebarToggle.addEventListener(Events.ON_CLICK, evt -> toggleSidebar());
	}

	private void toggleSidebar() {
		if (sidebar.getSclass().contains("collapsed")) {
			sidebar.setSclass("sidebar");
			mainContainer.setSclass("main-container");
			
		} else {
			sidebar.setSclass("sidebar collapsed");
			mainContainer.setSclass("main-container enlarge");
			
		}
	}
	
	
}
