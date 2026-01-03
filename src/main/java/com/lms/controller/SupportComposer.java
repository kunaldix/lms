package com.lms.controller;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

public class SupportComposer extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;

    @Wire private HtmlMacroComponent sidebarMacro;
    @Wire private Div mainContainer;
    @Wire private Textbox txtSubject;
    @Wire private Textbox txtDesc;
    @Wire private Combobox cmbCategory;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        // Handle Sidebar Toggle via desktop-wide queue
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
            .subscribe(event -> {
                if ("onSidebarToggle".equals(event.getName())) {
                    toggleLayout();
                }
            });
    }

    private void toggleLayout() {
        if (sidebarMacro != null) {
            String s = sidebarMacro.getSclass();
            sidebarMacro.setSclass(s != null && s.contains("collapsed") ? "sidebar" : "sidebar collapsed");
        }

        if (mainContainer != null) {
            String c = mainContainer.getSclass();
            mainContainer.setSclass(c != null && c.contains("enlarge") ? "main-container" : "main-container enlarge");
        }
    }

    @Listen("onClick = #btnSubmitTicket")
    public void onSubmit() {
        if(txtSubject.getValue().trim().isEmpty()) {
            Messagebox.show("Please enter a subject", "CreditHub Support", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }
        
        // Success Logic
        Messagebox.show("Your support ticket has been submitted successfully!", "Success", Messagebox.OK, Messagebox.INFORMATION);
        
        // Reset form
        txtSubject.setValue("");
        txtDesc.setValue("");
        cmbCategory.setValue(null);
    }
}