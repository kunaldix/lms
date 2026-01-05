package com.lms.controller;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Vlayout;

public class MyLoanComposer extends SelectorComposer<Div>
{
	private static final long serialVersionUID = -6735214827680029146L;
	
	@Wire
	private Vlayout mainContainer;
	
	@Wire
	private Div myLoanPage,viewDetails;
	
	@Wire
	private Button vdetailbtn1,vdetailbtn2,vdetailbtn3,backbtn;

	@Override
	public void doAfterCompose(Div comp) throws Exception 
	{
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });
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
	
	@Listen("onClick = #vdetailbtn1")
	 public void onVdetailbtn1(Event event) 
	{
		myLoanPage.setVisible(false);
		viewDetails.setVisible(true);
	}
	
	@Listen("onClick = #vdetailbtn2")
	 public void onVdetailbtn2(Event event) 
	{
		myLoanPage.setVisible(false);
		viewDetails.setVisible(true);
	}
	
	@Listen("onClick = #vdetailbtn3")
	 public void onVdetailbtn3(Event event) 
	{
		myLoanPage.setVisible(false);
		viewDetails.setVisible(true);
	}
	
	@Listen("onClick = #backbtn")
	 public void onBackbtn(Event event) 
	{
		myLoanPage.setVisible(true);
		viewDetails.setVisible(false);
	}
}