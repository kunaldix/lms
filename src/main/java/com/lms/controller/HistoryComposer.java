package com.lms.controller;

import java.util.*;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.*;
import org.zkoss.zul.*;

public class HistoryComposer extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;

    
    @Wire
    private Listbox historyList;
    
    
    @Wire
    private HtmlMacroComponent sidebarMacro; 

   
    @Wire
    private Div mainContainer; 

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
            loadStaticData();
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

    private void loadStaticData() {
        historyList.getItems().clear();
        for (Map<String, Object> p : getStaticPayments()) {
            Listitem item = new Listitem();
            item.appendChild(new Listcell(String.valueOf(p.get("date"))));
            item.appendChild(new Listcell(String.valueOf(p.get("loanType"))));
            item.appendChild(new Listcell("₹" + p.get("amount")));
            item.appendChild(new Listcell(String.valueOf(p.get("mode"))));
            item.appendChild(new Listcell(String.valueOf(p.get("txnId"))));
            
            Listcell statusCell = new Listcell();
            Label statusLabel = new Label(String.valueOf(p.get("status")));
            statusLabel.setSclass("status-success"); 
            statusCell.appendChild(statusLabel);
            item.appendChild(statusCell);

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

    private List<Map<String, Object>> getStaticPayments() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(createPayment("10-Jan-2026", "Home Loan", "8,500.00", "UPI", "TXN123456", "SUCCESS", "/receipts/sample.pdf"));
        list.add(createPayment("12-Feb-2026", "Personal Loan", "4,200.00", "Credit Card", "TXN789012", "SUCCESS", "/receipts/sample.pdf"));
        list.add(createPayment("15-Mar-2026", "Car Loan", "12,750.00", "Net Banking", "TXN456789", "SUCCESS", "/receipts/sample.pdf"));
        list.add(createPayment("05-Apr-2026", "Personal Loan", "5,500.00", "Debit Card", "TXN112233", "SUCCESS", "/receipts/sample.pdf"));
        list.add(createPayment("20-May-2026", "Car Loan", "2,300.00", "UPI", "TXN998877", "SUCCESS", "/receipts/sample.pdf"));
        list.add(createPayment("10-Jun-2026", "Home Loan", "8,500.00", "UPI", "TXN334455", "SUCCESS", "/receipts/sample.pdf"));
        return list;
    }

    private Map<String, Object> createPayment(String date, String type, String amt, String mode, String id, String status, String path) {
        Map<String, Object> p = new HashMap<>();
        p.put("date", date); p.put("loanType", type); p.put("amount", amt);
        p.put("mode", mode); p.put("txnId", id); p.put("status", status); p.put("receipt", path);
        return p;
    }
}