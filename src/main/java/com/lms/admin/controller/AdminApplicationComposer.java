package com.lms.admin.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.*;

import com.lms.model.Loan;
import com.lms.service.AdminLoanService;

/**
 * Composer for the Admin Loan Applications view.
 * Handles filtering, dashboard statistics, and launching the document review modal.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class AdminApplicationComposer extends SelectorComposer<Component> {

	private static final long serialVersionUID = -4579866573238714329L;

	// Log4j Logger for admin activity auditing
	private static final Logger logger = LogManager.getLogger(AdminApplicationComposer.class);

	@Wire private Vlayout mainContainer;
	@Wire private Vlayout loanCardsContainer;
	@Wire private Textbox tApplicant;
	@Wire private Combobox loanType;
	@Wire private Combobox loanStatus;
	@Wire private Label processed, pending;

	@WireVariable
	private AdminLoanService adminLoanService;

	private List<Loan> allLoans;
	private final SimpleDateFormat shorthandDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		logger.info("AdminApplicationComposer initialized.");

		// Sync with sidebar toggle for responsive layout
		EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true).subscribe(event -> {
			if ("onSidebarToggle".equals(event.getName())) {
				resizeContent();
			}
		});

		// Initial Data Load
		loadInitialData();
	}

	/**
	 * Fetches all loans from the service and updates the dashboard summary.
	 */
	private void loadInitialData() {
		try {
			allLoans = adminLoanService.getAllLoans();
			int pendingApp = countPendingApplications(allLoans);
			
			pending.setValue(String.valueOf(pendingApp));
			processed.setValue(String.valueOf(allLoans.size()));
			
			renderLoanCards(allLoans);
			logger.info("Successfully loaded {} total applications.", allLoans.size());
		} catch (Exception e) {
			logger.error("Error loading initial loan data: {}", e.getMessage(), e);
		}
	}
	
	private int countPendingApplications(List<Loan> loanList) {
		int count = 0;
		for(Loan loan : loanList) {
			if(loan.getApplicationStatus().name().equalsIgnoreCase("PENDING")) {
				count++;
			}
		}
		return count;
	}

	private void resizeContent() {
		if (mainContainer.getSclass().contains("enlarge")) {
			mainContainer.setSclass("main-container");
		} else {
			mainContainer.setSclass("main-container enlarge");
		}
	}

	/**
	 * Dynamically generates Loan Cards for the admin review list.
	 */
	private void renderLoanCards(List<Loan> loanList) {
		loanCardsContainer.getChildren().clear();

		for (Loan loan : loanList) {
			Div card = new Div();
			card.setSclass("loan-card");

			// --- Header Section ---
			Div header = new Div();
			header.setSclass("card-header");
			Hlayout hlHeader = new Hlayout();
			hlHeader.setValign("middle");

			Span icon = new Span();
			icon.setSclass("fa-solid fa-file-invoice-dollar");
			icon.setStyle("color:#004b98; font-size:18px; margin-right:10px;");

			Label idLabel = new Label("#" + loan.getLoanId());
			idLabel.setSclass("loan-title");

			Label applicantLabel = new Label("| Applicant: " + loan.getUser().getName());
			applicantLabel.setSclass("loan-id");
			applicantLabel.setStyle("font-weight:600; color:#333;");

			hlHeader.appendChild(icon);
			hlHeader.appendChild(idLabel);
			hlHeader.appendChild(applicantLabel);

			Label statusBadge = new Label(loan.getApplicationStatus().toString());
			statusBadge.setSclass("status-badge status-" + loan.getApplicationStatus().toString().toLowerCase());

			header.appendChild(hlHeader);
			header.appendChild(statusBadge);

			// --- Body Section ---
			Div body = new Div();
			body.setSclass("card-body");
			Hlayout hlBody = new Hlayout();
			hlBody.setValign("middle");

			hlBody.appendChild(createInfoBlock("Requested Amount", "₹ " + loan.getLoanAmount(), "highlight-value"));
			hlBody.appendChild(createInfoBlock("Loan Type", loan.getLoanType().toString(), ""));
			hlBody.appendChild(createInfoBlock("Monthly Income", "₹ " + loan.getEmploymentDetails().getMonthlyIncome(), ""));
			
			String formattedDate = (loan.getSubmissionDate() != null) 
					? shorthandDateFormat.format(loan.getSubmissionDate()) 
					: "N/A";

			hlBody.appendChild(createInfoBlock("Submission Date", formattedDate, ""));

			// --- Action Button ---
			Div btnDiv = new Div();
			btnDiv.setHflex("1");
			btnDiv.setStyle("text-align: right;");
			Button btn = new Button("Review Docs");
			btn.setSclass("review-btn");
			btn.setIconSclass("z-icon-folder-open");

			// Attach data to button for the click event
			btn.setAttribute("loanData", loan);
			btn.addEventListener("onClick", event -> {
				Loan data = (Loan) event.getTarget().getAttribute("loanData");
				openReviewModal(data);
			});

			btnDiv.appendChild(btn);
			hlBody.appendChild(btnDiv);

			body.appendChild(hlBody);
			card.appendChild(header);
			card.appendChild(body);

			loanCardsContainer.appendChild(card);
		}
	}

	private Vlayout createInfoBlock(String label, String value, String extraClass) {
		Vlayout v = new Vlayout();
		v.setHflex("1");
		Label l = new Label(label);
		l.setSclass("info-label");
		Label val = new Label(value);
		val.setSclass("info-value " + extraClass);
		v.appendChild(l);
		v.appendChild(val);
		return v;
	}

	/**
	 * Opens the Document Review window as a modal.
	 */
	public void openReviewModal(Loan loan) {
		logger.debug("Opening review modal for Loan ID: {}", loan.getLoanId());
		Map<String, Object> args = new HashMap<>();
		args.put("loanData", loan);
		Window window = (Window) Executions.createComponents("/admin/review_docs.zul", null, args);
		window.doModal();
	}

	/**
	 * Handles searching and multi-criteria filtering.
	 */
	@Listen("onClick = #search")
	public void searchLoan() {
		String applicantName = (tApplicant.getValue() == null) ? "" : tApplicant.getValue().trim().toLowerCase();
		
		String selectedType = (loanType.getSelectedItem() != null && !loanType.getSelectedItem().getLabel().contains("Loan Type")) 
				? loanType.getSelectedItem().getValue() : null;

		String selectedStatus = (loanStatus.getSelectedItem() != null && !loanStatus.getSelectedItem().getLabel().contains("Loan Status")) 
				? loanStatus.getSelectedItem().getValue() : null;

		logger.info("Searching applications. Criteria -> Name: {}, Type: {}, Status: {}", applicantName, selectedType, selectedStatus);

		List<Loan> filtered = new ArrayList<>();
		for (Loan loan : allLoans) {
			boolean matches = true;
			
			// Applicant Name Filter
			if (!applicantName.isEmpty() && (loan.getUser() == null || !loan.getUser().getName().toLowerCase().contains(applicantName))) {
				matches = false;
			}
			// Type Filter
			if (matches && selectedType != null && !selectedType.equalsIgnoreCase(String.valueOf(loan.getLoanType()))) {
				matches = false;
			}
			// Status Filter
			if (matches && selectedStatus != null && !selectedStatus.equalsIgnoreCase(String.valueOf(loan.getApplicationStatus()))) {
				matches = false;
			}

			if (matches) filtered.add(loan);
		}
		renderLoanCards(filtered);
	}
}