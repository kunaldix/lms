package com.lms.controller;

import java.util.*;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.*;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.*;

import com.lms.model.User;
import com.lms.repository.EmiTransactionRepository;

@VariableResolver(DelegatingVariableResolver.class)
public class HistoryComposer extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    
    @Wire
    private Listbox historyList;
    
    @Wire
    private HtmlMacroComponent sidebarMacro; 
    @Wire
    private Div mainContainer; 
    
    @WireVariable
    private EmiTransactionRepository emiTransactionRepo;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
            .subscribe(event -> {
                if ("onSidebarToggle".equals(event.getName())) {
                    toggleLayout();
                }
            });

        if (historyList != null) {
        	loadDynamicData();
        }
    }

    private void toggleLayout() {
        // Toggle Sidebar
        if (sidebarMacro != null) {
            String sclass = sidebarMacro.getSclass();
            // Default to 'sidebar' if null, then toggle
            if (sclass != null && sclass.contains("collapsed")) {
                sidebarMacro.setSclass("sidebar");
            } else {
                sidebarMacro.setSclass("sidebar collapsed");
            }
        }

        // Toggle Main Container
        if (mainContainer != null) {
            String current = mainContainer.getSclass();
            if (current != null && current.contains("enlarge")) {
                mainContainer.setSclass("main-container");
            } else {
                mainContainer.setSclass("main-container enlarge");
            }
        }
    }

    @Listen("onClick = #sidebarToggle")
    public void onToggleClick() {
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
                   .publish(new Event("onSidebarToggle"));
    }

   private void loadDynamicData() {
        // 1. Get current user from session
        User user = (User) Sessions.getCurrent().getAttribute("user");
        if (user == null) {
            Executions.sendRedirect("/auth/login.zul");
            return;
        }

        // 2. Fetch real data from DB
        List<Map<String, Object>> payments = emiTransactionRepo.getTransactionsByUserId(user.getId());
        
        historyList.getItems().clear();
        
        if (payments.isEmpty()) {
            historyList.setEmptyMessage("No payment transactions found.");
            return;
        }

        for (Map<String, Object> p : payments) {
            Listitem item = new Listitem();
            item.appendChild(new Listcell(String.valueOf(p.get("date"))));
            item.appendChild(new Listcell(String.valueOf(p.get("loanType"))));
            item.appendChild(new Listcell("₹" + p.get("amount")));
            item.appendChild(new Listcell(String.valueOf(p.get("mode"))));
            item.appendChild(new Listcell(String.valueOf(p.get("txnId")).substring(0, 16)));
            
            // Status Cell with CSS class
            Listcell statusCell = new Listcell();
            String status = String.valueOf(p.get("status"));
            Label statusLabel = new Label(status);
            statusLabel.setSclass("SUCCESS".equalsIgnoreCase(status) ? "status-success" : "status-failed");
            statusCell.appendChild(statusLabel);
            item.appendChild(statusCell);

            // Action Cell
            Button viewBtn = new Button("View");
            viewBtn.setSclass("btn-view-style");
            final String path = String.valueOf(p.get("receipt"));
            viewBtn.addEventListener("onClick", e -> openInvoiceModal(path));

            Listcell actionCell = new Listcell();
            actionCell.appendChild(viewBtn);
            item.appendChild(actionCell);
            
            historyList.appendChild(item);
        }
    }

    private void openInvoiceModal(String path) {
        if (path == null || path.isEmpty() || path.endsWith(".zul")) {
            Messagebox.show("Invalid invoice path.", "Error", Messagebox.OK, Messagebox.ERROR);
            return;
        }
        Window window = new Window("Payment Invoice", "normal", true);
        window.setWidth("800px"); window.setHeight("600px");
        window.setClosable(true);
        Iframe iframe = new Iframe(path);
        iframe.setHflex("1"); iframe.setVflex("1");
        window.appendChild(iframe);
        window.setPage(historyList.getPage());
        window.doModal();
    }
}