package com.lms.admin.controller;


import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;


public class AdminApplicationComposer extends SelectorComposer<Component> {

	private static final long serialVersionUID = -4579866573238714329L;
	
	@Wire
	private Vlayout mainContainer;


    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });
       
    }
    
    private void resizeContent() {
        // Toggle the Main Container Margin
        if (mainContainer.getSclass().contains("enlarge")) {
            // Sidebar is opening (Back to Normal)
            mainContainer.setSclass("main-container");
        } else {
            // Sidebar is collapsing (Expand content)
            mainContainer.setSclass("main-container enlarge");
        }
    }
    
    @Listen("onClick = #btnReviewDocs") 
    public void openReviewModal() {
        // 1. Create the window from the ZUL file
        Window window = (Window) Executions.createComponents("/admin/review_docs.zul", null, null);
        
        // 2. Open it as a modal popup
        window.doModal();
    }
}
