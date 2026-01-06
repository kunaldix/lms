package com.lms.controller;

import java.math.BigDecimal;
import java.util.List;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.*;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.*;	
import com.lms.model.Loan;
import com.lms.model.User;
import com.lms.service.LoanService; 

@VariableResolver(DelegatingVariableResolver.class)
public class MyLoanComposer extends SelectorComposer<Div> {
    private static final long serialVersionUID = 1L;

    @Wire private Vlayout mainContainer;
    @Wire private Vlayout loanCardsContainer;
    @Wire private Div myLoanPage, viewDetails;
    @Wire private Label lblActiveLoans, lblTotalLoans;
    
    // Detailed View Wires
    @Wire private Label detUserName, detLoanInfo, detStatus, detSanctioned, detInterest, detOutstanding, detEmi, detRemaining, detNextDue;

    @WireVariable
    private LoanService loanService;

    @Override
    public void doAfterCompose(Div comp) throws Exception {
        super.doAfterCompose(comp);
        
        // Handle Sidebar Toggle
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true).subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });

        refreshData();
    }

    private void refreshData() {
        User currentUser = (User) Sessions.getCurrent().getAttribute("user");
        if (currentUser == null) return;

        List<Loan> userLoans = loanService.getLoansByUserId(currentUser.getId());
        
        // Update Stats
        long activeCount = userLoans.stream().filter(l -> l.getApplicationStatus().name().equalsIgnoreCase("ACCEPTED")).count();
        lblActiveLoans.setValue(String.format("%02d", activeCount));
        lblTotalLoans.setValue(String.format("%02d", userLoans.size()));

        renderLoanCards(userLoans);
    }

    private void renderLoanCards(List<Loan> loans) {
        loanCardsContainer.getChildren().clear();

        for (Loan loan : loans) {
            Div card = new Div();
            card.setSclass("loan-card");

            // Header
            Div header = new Div();
            header.setSclass("card-header");
            Hlayout hlHeader = new Hlayout();
            
            Span icon = new Span();
            icon.setSclass(getIconForType(loan.getLoanType().name()));
            icon.setStyle("color:#004b98; font-size:18px; margin-right:10px;");

            Label title = new Label(loan.getLoanType().toString());
            title.setSclass("loan-title");
            
            Label idLabel = new Label("#" + loan.getLoanId());
            idLabel.setSclass("loan-id");

            hlHeader.appendChild(icon);
            hlHeader.appendChild(title);
            hlHeader.appendChild(idLabel);

            Label statusBadge = new Label(loan.getApplicationStatus().toString());
            statusBadge.setSclass("status-badge status-" + loan.getApplicationStatus().toString().toLowerCase());

            header.appendChild(hlHeader);
            header.appendChild(statusBadge);

            // Body
            Div body = new Div();
            body.setSclass("card-body");
            Hlayout hlBody = new Hlayout();
            BigDecimal outstanding = loan.getLoanAmount().subtract(loan.getAmountPaid());

            hlBody.appendChild(createInfoBlock("Loan Amount", "₹ " + loan.getLoanAmount(), "highlight-value"));
            hlBody.appendChild(createInfoBlock("Next Due Date", loan.getPreferredEmiDate() + " " + new java.text.SimpleDateFormat("MMM yyyy").format(new java.util.Date()), "")); // Dynamic logic here
            hlBody.appendChild(createInfoBlock("Outstanding", "₹ " + outstanding.toString(), ""));
            hlBody.appendChild(createInfoBlock("Tenure", loan.getTenureMonths() + " Months", ""));

            Button btn = new Button("View Details");
            btn.setSclass("pay-btn");
            btn.addEventListener("onClick", e -> showLoanDetails(loan));

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
        String statusTheme = loan.getApplicationStatus().toString().equalsIgnoreCase("ACCEPTED") 
                             ? "color: #28A745; background: #E8F5E9;"
                             : "color: #E65100; background: #FFF3E0;"; 
        detStatus.setStyle(statusTheme + " font-weight: bold; font-size: 14px; padding: 5px 10px; border-radius: 4px;");

        // 3. Financial Grid Data
        detSanctioned.setValue("₹ " + loan.getLoanAmount().toString());
        detInterest.setValue(loan.getInterestRate() + "% p.a.");
        
        // Calculation for Outstanding
        BigDecimal paid = (loan.getAmountPaid() != null) ? loan.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal outstanding = loan.getLoanAmount().subtract(paid);
        detOutstanding.setValue("₹ " + outstanding.toString());
        
        // Tenure and Monthly EMI
        detRemaining.setValue(loan.getTenureMonths() + " Months");
        
        // Placeholder for EMI (You can replace this with your actual EMI calculation logic)
        detEmi.setValue("₹ " + String.format("%.2f", (loan.getLoanAmount().doubleValue() / loan.getTenureMonths())));
        
        // Next Due Date - Logic to show the preferred date for the current month
        detNextDue.setValue(loan.getPreferredEmiDate() + " " + new java.text.SimpleDateFormat("MMM yyyy").format(new java.util.Date()));
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
        if (type.contains("HOME")) return "fa-solid fa-house-chimney";
        if (type.contains("CAR")) return "fa-solid fa-car";
        return "fa-solid fa-user-tag";
    }

    private void resizeContent() {
        if (mainContainer != null) {
            mainContainer.setSclass(mainContainer.getSclass().contains("enlarge") ? "main-container" : "main-container enlarge");
        }
    }
}