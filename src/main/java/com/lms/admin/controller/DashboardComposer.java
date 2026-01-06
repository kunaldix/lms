package com.lms.admin.controller;

import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

/**
 * DashboardComposer handles the primary Admin data overview.
 * It manages statistical labels, loan trend charts (Category Model),
 * and loan distribution charts (Pie Model).
 */
@VariableResolver(DelegatingVariableResolver.class)
public class DashboardComposer extends SelectorComposer<Vlayout> {

    private static final long serialVersionUID = -6735214827680029146L;

    // Initialize Log4j Logger for dashboard metrics and session tracking
    private static final Logger logger = LogManager.getLogger(DashboardComposer.class);

    @Wire private Label lblTotalLoans;
    @Wire private Label lblPendingApps;
    @Wire private Label lblActiveLoans;
    @Wire private Label lblOverdue;
    @Wire private Label lblAdminName;
    @Wire private Label lblAvatarText;
    @Wire private Charts loanTrendChart;
    @Wire private Charts loanTypeChart;
    @Wire private Listbox loanListbox;
    @Wire private Vlayout sidebar;
    @Wire private Vlayout mainContainer;
    
    @WireVariable
    private LoanService loanService;

    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        
        logger.info("Initializing Admin Dashboard...");

        // Retrieve admin session data
        String adminName = (String) Sessions.getCurrent().getAttribute("loggedAdmin");
        
        // Subscribe to global event queue for responsive chart resizing
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                logger.debug("Sidebar toggle detected, resizing charts.");
                resizeContent();
            }
        });
        
        // Personalize dashboard greeting
        if (adminName != null) {
            lblAdminName.setValue("Welcome Admin " + adminName);
            lblAvatarText.setValue(adminName.substring(0, 1).toUpperCase());
            logger.info("Admin [{}] logged in successfully.", adminName);
        } else {
            logger.warn("Admin dashboard accessed without 'loggedAdmin' session attribute.");
        }
        
        // Initial Chart Sizing
        setInitialChartDimensions();
        
        // Load Business Data
        loadStats();
        loadTrendChart();
        loadLoanTypeChart();
    }

    /**
     * Sets the default width/height for charts upon initial load.
     */
    private void setInitialChartDimensions() {
        loanTrendChart.setWidth(450);
        loanTrendChart.setHeight(300);
        loanTypeChart.setWidth(450);
        loanTypeChart.setHeight(300);
    }
    
    /**
     * Dynamically adjusts chart dimensions when the sidebar is toggled.
     * Uses .invalidate() to force a re-draw of the ZK Charts.
     */
    private void resizeContent() {
        if (mainContainer != null) {
            boolean isEnlarged = mainContainer.getSclass().contains("enlarge");
            
            if (isEnlarged) {
                mainContainer.setSclass("main-container");
                updateChartSizes(450, 300);
            } else {
                mainContainer.setSclass("main-container enlarge");
                updateChartSizes(550, 300);
            }
            
            loanTrendChart.invalidate();
            loanTypeChart.invalidate();
            logger.debug("Charts invalidated and redrawn. Enlarged state: {}", !isEnlarged);
        }
    }

    private void updateChartSizes(int width, int height) {
        loanTrendChart.setWidth(width);
        loanTrendChart.setHeight(height);
        loanTypeChart.setWidth(width);
        loanTypeChart.setHeight(height);
    }

    /**
     * Loads summary statistics from the LoanService.
     */
    private void loadStats() {
        try {
            lblTotalLoans.setValue(String.valueOf(loanService.getTotalLoans()));
            lblPendingApps.setValue(String.valueOf(loanService.getTotalPendingLoans()));
            lblActiveLoans.setValue(String.valueOf(loanService.getTotalActiveLoans()));
            lblOverdue.setValue("2"); // Static for now, can be linked to service later
            logger.debug("Dashboard statistics loaded successfully.");
        } catch (Exception e) {
            logger.error("Error loading dashboard statistics: {}", e.getMessage(), e);
        }
    }

    /**
     * populates the Line/Bar chart with loan counts for the last 5 months.
     */
    private void loadTrendChart() {
        try {
            DefaultCategoryModel model = new DefaultCategoryModel();
            Map<YearMonth, Integer> data = loanService.getLast5MonthsLoanCount();

            for (Map.Entry<YearMonth, Integer> entry : data.entrySet()) {
                String label = entry.getKey().getMonth().toString().substring(0, 3)
                             + " " + entry.getKey().getYear();
                model.setValue("Loans", label, entry.getValue());
            }

            loanTrendChart.setModel(model);
            logger.debug("Trend chart data mapped for {} months.", data.size());
        } catch (Exception e) {
            logger.error("Failed to load Trend Chart: {}", e.getMessage(), e);
        }
    }

    /**
     * populates the Pie chart showing the distribution of approved loans by type.
     */
    private void loadLoanTypeChart() {
        try {
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
            logger.debug("Loan distribution chart populated.");
        } catch (Exception e) {
            logger.error("Failed to load Loan Type Distribution Chart: {}", e.getMessage(), e);
        }
    }
}