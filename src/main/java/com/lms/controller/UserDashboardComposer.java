package com.lms.controller;

import org.zkoss.chart.Charts;
import org.zkoss.chart.model.DefaultCategoryModel;
import org.zkoss.chart.model.DefaultPieModel;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Vlayout;

public class UserDashboardComposer extends SelectorComposer<Vlayout> {

	private static final long serialVersionUID = 1L;

	@Wire
	private Label lblTotalBorrowed, lblActiveLoans, lblNextEmi, lblOutstanding;
	@Wire
	private Label loginName, avatarLetter;
	@Wire
	private Hlayout menuMyLoans, menuApply;
	@Wire
	private Charts repaymentChart; 
	@Wire
	private Charts emiHistoryChart; 
	@Wire
	private Listbox emiListbox;
	
	// Sidebar Logic
	@Wire
	private Vlayout sidebar, mainContainer;
	@Wire
	private Label sidebarToggle;

	@Override
	public void doAfterCompose(Vlayout comp) throws Exception {
		super.doAfterCompose(comp);

		// 1. Get Logged in User 
		String user = (String) Sessions.getCurrent().getAttribute("loggedUser");
		if (user == null) user = "User"; // Fallback if session is empty

		loginName.setValue(user);
		avatarLetter.setValue(user.substring(0, 1).toUpperCase());

		// 2. Navigation
		menuMyLoans.addEventListener(Events.ON_CLICK, e -> Executions.sendRedirect("/user/my_loans.zul"));
		menuApply.addEventListener(Events.ON_CLICK, e -> Executions.sendRedirect("/user/apply_loan.zul"));
		
		sidebarToggle.addEventListener(Events.ON_CLICK, evt -> toggleSidebar());
		
		// 3. Setup Charts dimensions
		repaymentChart.setWidth(500);
		repaymentChart.setHeight(300);
		emiHistoryChart.setWidth(500);
		emiHistoryChart.setHeight(300);

		// 4. Load Data
		loadUserStats();
		loadRepaymentChart();
		loadEmiHistoryChart();
		loadUpcomingEmis();
	}

	private void toggleSidebar() {
		if (sidebar.getSclass().contains("collapsed")) {
			sidebar.setSclass("sidebar");
			mainContainer.setSclass("main-container");
			repaymentChart.setWidth(500);
			emiHistoryChart.setWidth(500);
		} else {
			sidebar.setSclass("sidebar collapsed");
			mainContainer.setSclass("main-container enlarge");
			repaymentChart.setWidth(600);
			emiHistoryChart.setWidth(600);
		}
		repaymentChart.invalidate();
		emiHistoryChart.invalidate();
	}

	private void loadUserStats() {
		// Mock Data - In real app, fetch from userService.getUserStats(userId)
		lblTotalBorrowed.setValue("₹ 5,00,000");
		lblActiveLoans.setValue("2");
		lblNextEmi.setValue("₹ 12,500");
		lblOutstanding.setValue("₹ 3,40,000");
	}

	private void loadRepaymentChart() {
		DefaultPieModel model = new DefaultPieModel();
		model.setValue("Paid Principal", 160000);
		model.setValue("Outstanding Balance", 340000);
		repaymentChart.setModel(model);
		repaymentChart.getTitle().setText("Loan Balance Overview");
	}

	private void loadEmiHistoryChart() {
		DefaultCategoryModel model = new DefaultCategoryModel();
		model.setValue("EMI Paid", "Aug", 12500);
		model.setValue("EMI Paid", "Sep", 12500);
		model.setValue("EMI Paid", "Oct", 12500);
		model.setValue("EMI Paid", "Nov", 12500);
		model.setValue("EMI Paid", "Dec", 12500);
		
		emiHistoryChart.setModel(model);
		emiHistoryChart.getXAxis().setTitle("Month");
		emiHistoryChart.getYAxis().setTitle("Amount (₹)");
	}

	private void loadUpcomingEmis() {
		ListModelList<EmiDTO> list = new ListModelList<>();
		// Mock Data
		list.add(new EmiDTO("LN-2023-001", "Home Loan", "05 Jan 2026", "₹ 15,200", "Pending"));
		list.add(new EmiDTO("LN-2024-089", "Personal Loan", "10 Jan 2026", "₹ 4,500", "Pending"));
		
		emiListbox.setModel(list);
	}
	
	// Helper DTO Class for Listbox
	public static class EmiDTO {
		private String loanAccount;
		private String loanType;
		private String dueDate;
		private String amount;
		private String status;
		
		public EmiDTO(String la, String lt, String dd, String amt, String st) {
			this.loanAccount = la; this.loanType = lt; this.dueDate = dd; this.amount = amt; this.status = st;
		}
		public String getLoanAccount() { return loanAccount; }
		public String getLoanType() { return loanType; }
		public String getDueDate() { return dueDate; }
		public String getAmount() { return amount; }
		public String getStatus() { return status; }
	}
}