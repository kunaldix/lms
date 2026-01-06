package com.lms.controller;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.zkoss.chart.Charts;
import org.zkoss.chart.model.DefaultCategoryModel;
import org.zkoss.chart.model.DefaultPieModel;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Vlayout;

import com.lms.model.User;
import com.lms.service.EmiService;
import com.lms.service.LoanService;

@VariableResolver(DelegatingVariableResolver.class)
public class UserDashboardComposer extends SelectorComposer<Vlayout> {

	private static final long serialVersionUID = 1L;

	@Wire
	private Label lblTotalBorrowed, lblActiveLoans, lblNextEmi, lblOutstanding;
	@Wire
	private Hlayout menuMyLoans, menuApply;
	@Wire
	private Charts repaymentChart; 
	@Wire
	private Charts emiHistoryChart; 
	@Wire
	private Listbox emiListbox;
	
	@Wire
	private Vlayout mainContainer;

	@WireVariable
	private EmiService emiService;
	
    @WireVariable
    private LoanService loanService;

    private User currentUser;
	
	@Override
	public void doAfterCompose(Vlayout comp) throws Exception {
		super.doAfterCompose(comp);
		
		EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });
		
		// 2. Get User
				currentUser = (User) Sessions.getCurrent().getAttribute("user");
		        if (currentUser == null) {
		            Executions.sendRedirect("/auth/login.zul");
		            return; 
		        }
		       
		
		// 3. Setup Charts dimensions
		repaymentChart.setWidth(450);
		repaymentChart.setHeight(300);
		emiHistoryChart.setWidth(450);
		emiHistoryChart.setHeight(300);

		// 4. Load Data
		loadUserStats();
		loadRepaymentChart();
		loadEmiHistoryChart();
	}
	
	private void resizeContent() {
        // Toggle the Main Container Margin
        if (mainContainer.getSclass().contains("enlarge")) {
            // Sidebar is opening (Back to Normal)
            mainContainer.setSclass("main-container");
            repaymentChart.setWidth(450);
            emiHistoryChart.setWidth(450);
        } else {
            // Sidebar is collapsing (Expand content)
            mainContainer.setSclass("main-container enlarge");
            repaymentChart.setWidth(500); // Make charts bigger
            emiHistoryChart.setWidth(500);
        }
        
        // Redraw charts to fit new width
        repaymentChart.invalidate();
        emiHistoryChart.invalidate();
    }

	private void loadUserStats() {
		// Mock Data - In real app, fetch from userService.getUserStats(userId)
		lblTotalBorrowed.setValue(loanService.getTotalLoanOfUser(currentUser.getId()));
		lblActiveLoans.setValue(String.valueOf(loanService.getActiveLoans(currentUser.getId())));
		lblNextEmi.setValue(String.valueOf(emiService.getNextEmiAmountDueForUser(currentUser.getId())));
		lblOutstanding.setValue(loanService.getTotalDebt(currentUser.getId()));
	}

	private void loadRepaymentChart() {
		DefaultPieModel model = new DefaultPieModel();
		model.setValue("Paid Principal", Double.parseDouble(loanService.getTotalLoanOfUser(currentUser.getId())) - 
				Double.parseDouble(loanService.getTotalDebt(currentUser.getId())));
		model.setValue("Outstanding Balance", Double.parseDouble(loanService.getTotalDebt(currentUser.getId())));
		repaymentChart.setModel(model);
		repaymentChart.getTitle().setText("Loan Balance Overview");
	}

	private void loadEmiHistoryChart() {

	    DefaultCategoryModel model = new DefaultCategoryModel();

	    User user = (User) Sessions.getCurrent().getAttribute("user");
	    if (user == null) return;

	    Map<YearMonth, BigDecimal> paidData =
	        emiService.getLast5PaidMonthsEmi(user.getId());

	    YearMonth current = YearMonth.now();
	    YearMonth latestPaid = emiService.getLatestPaidEmiMonth(user.getId());


	    YearMonth endMonth = current;
	    if (latestPaid != null && latestPaid.isAfter(current)) {
	        endMonth = latestPaid;
	    }

	    
	    for (int i = 4; i >= 0; i--) {

	        YearMonth ym = endMonth.minusMonths(i);

	        BigDecimal amount =
	            paidData.getOrDefault(ym, BigDecimal.ZERO);

	        String label = ym.format(
	            DateTimeFormatter.ofPattern("MMM yyyy")
	        );

	        model.setValue("EMI Paid", label, amount);
	    }

	    emiHistoryChart.setModel(model);
	    emiHistoryChart.getXAxis().setTitle("Month");
	    emiHistoryChart.getYAxis().setTitle("Amount (₹)");
	}	
	
}