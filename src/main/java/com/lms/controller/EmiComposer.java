package com.lms.controller;


import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Vlayout;

public class EmiComposer extends SelectorComposer<Div>
{
	private static final long serialVersionUID = -6735214827680029146L;
	
	@Wire
	private Vlayout mainContainer;

	@Override
	public void doAfterCompose(Div comp) throws Exception {
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

}