package com.lms.admin.controller;

import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;

import org.zkoss.chart.Charts;
import org.zkoss.chart.model.DefaultCategoryModel;
import org.zkoss.chart.model.DefaultPieModel;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Vlayout;

import com.lms.model.Loan;
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
        lblOverdue.setValue("2");
    }

    private void loadTrendChart() {

        DefaultCategoryModel model = new DefaultCategoryModel();

        Map<YearMonth, Integer> data =
                loanService.getLast5MonthsLoanCount();

        for (Map.Entry<YearMonth, Integer> entry : data.entrySet()) {

            String label =
                entry.getKey().getMonth().toString().substring(0, 3)
                + " " + entry.getKey().getYear();

            model.setValue("Loans", label, entry.getValue());
        }

        loanTrendChart.setModel(model);
    }


    private void loadLoanTypeChart() {

        DefaultPieModel model = new DefaultPieModel();

        loanService.getALlApprovedLoans().stream()
            .collect(Collectors.groupingBy(
                Loan::getLoanType,
                Collectors.counting()
            ))
            .forEach((loanType, count) ->
                model.setValue(loanType.name(), count)
            );

        loanTypeChart.setModel(model);
    }

    
}