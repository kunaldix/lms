package com.lms.admin.controller;

import org.zkoss.chart.Charts;
import org.zkoss.chart.model.DefaultPieModel;
import org.zkoss.chart.model.DefaultXYModel;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Vlayout;

import com.lms.service.LoanService;

@VariableResolver(DelegatingVariableResolver.class)
public class DashboardComposer extends SelectorComposer<Vlayout> {

    private static final long serialVersionUID = -6735214827680029146L;

    @Wire
    private Label lblTotalLoans;
    @Wire
    private Label lblPendingApps;
    @Wire
    private Label lblActiveLoans;
    @Wire
    private Label lblOverdue;
    @Wire
    private Label lblAdminName;
    @Wire
    private Label lblAvatarText;
    @Wire
    private Charts loanTrendChart;
    @Wire
    private Charts loanTypeChart;
    @Wire
    private Listbox loanListbox;

    @Wire
    private Vlayout sidebar;
    @Wire
    private Vlayout mainContainer;
    
     @WireVariable
     private LoanService loanService;
    

    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);

        String adminName = (String) Sessions.getCurrent().getAttribute("loggedAdmin");
        
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });
        
        if (adminName != null) {
            lblAdminName.setValue("Welcome Admin " + adminName);
            lblAvatarText.setValue(adminName.substring(0, 1).toUpperCase());
        }
        
        loanTrendChart.setWidth(450);
        loanTrendChart.setHeight(300);

        loanTypeChart.setWidth(450);
        loanTypeChart.setHeight(300);
        
        loadStats();
        loadTrendChart();
        loadLoanTypeChart();
        loadRecentApplications();
    }
    
    private void resizeContent() {
        if (mainContainer != null) {
            if (mainContainer.getSclass().contains("enlarge")) {
                mainContainer.setSclass("main-container");
                loanTrendChart.setWidth(450);
                loanTrendChart.setHeight(300);

                loanTypeChart.setWidth(450);
                loanTypeChart.setHeight(300);
            } else {
                mainContainer.setSclass("main-container enlarge");
                loanTrendChart.setWidth(550);
                loanTrendChart.setHeight(300);

                loanTypeChart.setWidth(550);
                loanTypeChart.setHeight(300);
            }
        }
        loanTrendChart.invalidate();
        loanTypeChart.invalidate();
    }

    private void loadStats() {
        lblTotalLoans.setValue(String.valueOf(loanService.getTotalLoans()));
        lblPendingApps.setValue(String.valueOf(loanService.getTotalPendingLoans()));
        lblActiveLoans.setValue(String.valueOf(loanService.getTotalActiveLoans()));
        lblOverdue.setValue("14");
    }

    private void loadTrendChart() {
        DefaultXYModel model = new DefaultXYModel();
        model.addValue("Loans", 1, 20);
        model.addValue("Loans", 2, 35);
        model.addValue("Loans", 3, 40);
        model.addValue("Loans", 4, 55);
        model.addValue("Loans", 5, 70);
        loanTrendChart.setModel(model);
    }

    private void loadLoanTypeChart() {
        DefaultPieModel model = new DefaultPieModel();
        model.setValue("Home Loan", 40);
        model.setValue("Personal Loan", 30);
        model.setValue("Business Loan", 20);
        loanTypeChart.setModel(model);
    }

    private void loadRecentApplications() {
        ListModelList<String[]> list = new ListModelList<>();

        list.add(new String[] { "Aashish", "50,000", "Pending", "18 Nov" });
        list.add(new String[] { "Rohan", "1,20,000", "Approved", "20 Nov" });
        list.add(new String[] { "Sneha", "2,00,000", "Rejected", "21 Nov" });

        loanListbox.setModel(list);
    }
}