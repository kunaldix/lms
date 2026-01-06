package com.lms.controller;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

// Apache Log4j for auditing
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.*;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.*;	

import com.lms.model.Loan;
import com.lms.model.User;
import com.lms.service.LoanService; 

/**
 * Composer for the My Loans page.
 * Synchronized with EmiComposer UI for a consistent "CreditHub" experience.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class MyLoanComposer extends SelectorComposer<Div> {
    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = LogManager.getLogger(MyLoanComposer.class);

    @Wire private Vlayout mainContainer;
    @Wire private Vlayout loanCardsContainer;
    @Wire private Div myLoanPage, viewDetails;
    @Wire private Label lblActiveLoans, lblTotalLoans;
    
    // Detailed View Wires
    @Wire private Label detUserName, detLoanInfo, detStatus, detSanctioned, detInterest, detOutstanding, detEmi, detRemaining, detNextDue;

    @WireVariable
    private LoanService loanService;

    // Standardized Formatters for the team
    @SuppressWarnings("deprecation")
	private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Override
    public void doAfterCompose(Div comp) throws Exception {
        super.doAfterCompose(comp);
        
        logger.info("Initializing MyLoanComposer...");

        // Subscribe to sidebar toggle for responsive resizing
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true).subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });

        refreshData();
    }

    /**
     * Fetches loans for the logged-in user and updates the UI stats and cards.
     */
    private void refreshData() {
        User currentUser = (User) Sessions.getCurrent().getAttribute("user");
        if (currentUser == null) {
            logger.warn("No user found in session. Redirecting to login might be needed.");
            return;
        }

        List<Loan> userLoans = loanService.getLoansByUserId(currentUser.getId());
        logger.info("Retrieved {} loans for user ID: {}", userLoans.size(), currentUser.getId());
        
        // Update Dashboard Stats
        long activeCount = userLoans.stream()
                .filter(l -> l.getApplicationStatus().name().equalsIgnoreCase("ACCEPTED"))
                .count();
        
        lblActiveLoans.setValue(String.format("%02d", activeCount));
        lblTotalLoans.setValue(String.format("%02d", userLoans.size()));

        renderLoanCards(userLoans);
    }

    /**
     * Builds the Loan Cards dynamically using classes from loans.css
     */
    private void renderLoanCards(List<Loan> loans) {
        loanCardsContainer.getChildren().clear();

        for (Loan loan : loans) {
            Div card = new Div();
            card.setSclass("loan-card");

            // --- 1. Header Section ---
            Div header = new Div();
            header.setSclass("card-header");
            Hlayout hlHeader = new Hlayout();
            
            
            Span icon = new Span();
            icon.setSclass(getIconForType(loan.getLoanType().name()) + " fa-fw");
            icon.setStyle("color:#004b98; font-size:16px; margin-right:10px;");

            Label title = new Label(loan.getLoanType().toString());
            title.setSclass("loan-title");
            
            Label idLabel = new Label("#" + loan.getLoanId());
            idLabel.setSclass("loan-id");

            hlHeader.appendChild(icon);
            hlHeader.appendChild(title);
            hlHeader.appendChild(idLabel);

            // Mapping Loan Status to CSS Badge classes
            String statusStr = loan.getApplicationStatus().toString().toLowerCase();
            Label statusBadge = new Label(loan.getApplicationStatus().toString());
            // Logic: Accepted -> Paid (Green), Rejected -> Overdue (Red), Pending -> Pending (Blue)
            String badgeClass = statusStr.contains("accepted") ? "paid" : (statusStr.contains("rejected") ? "overdue" : "pending");
            statusBadge.setSclass("status-badge status-" + badgeClass);

            header.appendChild(hlHeader);
            header.appendChild(statusBadge);

            // --- 2. Body Section ---
            Div body = new Div();
            body.setSclass("card-body");
            Hlayout hlBody = new Hlayout();
            
            BigDecimal paid = (loan.getAmountPaid() != null) ? loan.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal outstanding = loan.getLoanAmount().subtract(paid);

            // Using currencyFormat for ₹ symbol and proper commas
            hlBody.appendChild(createInfoBlock("Loan Amount", currencyFormat.format(loan.getLoanAmount()), "highlight-value"));
            
            // Due date logic (matching EmiComposer style)
            String nextDue = loan.getPreferredEmiDate() + " " + new SimpleDateFormat("MMM yyyy").format(new java.util.Date());
            hlBody.appendChild(createInfoBlock("Next Due Date", nextDue, ""));
            
            hlBody.appendChild(createInfoBlock("Outstanding", currencyFormat.format(outstanding), ""));
            hlBody.appendChild(createInfoBlock("Tenure", loan.getTenureMonths() + " Months", ""));

            // --- 3. Action Section ---
            Button btn = new Button("View Details");
            btn.setSclass("pay-btn"); // Matches the "Pay Now" button style from CSS
            btn.setIconSclass("fa-solid fa-eye");
            btn.addEventListener("onClick", e -> {
                logger.debug("Viewing details for Loan ID: {}", loan.getLoanId());
                showLoanDetails(loan);
            });

            Div btnDiv = new Div();
            btnDiv.setHflex("1");
            btnDiv.setStyle("text-align: right;");
            btnDiv.appendChild(btn);

            hlBody.appendChild(btnDiv);
            body.appendChild(hlBody);
            
            card.appendChild(header);
            card.appendChild(body);
            loanCardsContainer.appendChild(card);
        }
    }

    private void showLoanDetails(Loan loan) {
        myLoanPage.setVisible(false);
        viewDetails.setVisible(true);

        detUserName.setValue(loan.getUser().getName().toUpperCase());
        detLoanInfo.setValue("Customer ID: #USR-" + loan.getUser().getId() + " | Loan Account: #" + loan.getLoanId());
        
        detStatus.setValue("LOAN STATUS: " + loan.getApplicationStatus());
        
        // Re-using the logic for status styling in details view
        boolean isAccepted = loan.getApplicationStatus().toString().equalsIgnoreCase("ACCEPTED");
        String statusTheme = isAccepted ? "color: #15803d; background: #dcfce7;" : "color: #b91c1c; background: #fee2e2;"; 
        detStatus.setStyle(statusTheme + " font-weight: bold; font-size: 14px; padding: 5px 10px; border-radius: 4px;");

        detSanctioned.setValue(currencyFormat.format(loan.getLoanAmount()));
        detInterest.setValue(loan.getInterestRate() + "% p.a.");
        
        BigDecimal paid = (loan.getAmountPaid() != null) ? loan.getAmountPaid() : BigDecimal.ZERO;
        detOutstanding.setValue(currencyFormat.format(loan.getLoanAmount().subtract(paid)));
        
        detRemaining.setValue(loan.getTenureMonths() + " Months");
        double principal = loan.getLoanAmount().doubleValue(); 
        double annualRate = loan.getInterestRate();
        double monthlyRate = annualRate / 12 / 100;
        int tenure = loan.getTenureMonths();

        // Standard Amortization Formula: [P x R x (1+R)^N]/[(1+R)^N-1]
        double emiAmount = (principal * monthlyRate * Math.pow(1 + monthlyRate, tenure)) 
                           / (Math.pow(1 + monthlyRate, tenure) - 1);
        detEmi.setValue(currencyFormat.format(emiAmount));
        detNextDue.setValue(loan.getPreferredEmiDate() + " " + new SimpleDateFormat("MMM yyyy").format(new java.util.Date()));
    }

    @Listen("onClick = #backbtn")
    public void onBack() {
        myLoanPage.setVisible(true);
        viewDetails.setVisible(false);
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

    private String getIconForType(String type) {
        String t = type.toUpperCase();
        if (t.contains("HOME")) return "fa-solid fa-house-chimney";
        if (t.contains("CAR")) return "fa-solid fa-car";
        return "fa-solid fa-user-tag";
    }

    private void resizeContent() {
        if (mainContainer != null) {
            mainContainer.setSclass(mainContainer.getSclass().contains("enlarge") ? "main-container" : "main-container enlarge");
        }
    }
}