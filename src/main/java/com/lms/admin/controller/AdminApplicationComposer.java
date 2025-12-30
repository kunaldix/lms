package com.lms.admin.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import com.lms.model.Loan;
import com.lms.service.AdminLoanService;
import com.lms.service.impl.AdminLoanServiceImpl;


public class AdminApplicationComposer extends SelectorComposer<Component> {

	private static final long serialVersionUID = -4579866573238714329L;
	
	@Wire private Vlayout mainContainer;
	@Wire private Vlayout loanCardsContainer;
	@Wire private Combobox loanType;
	
	private AdminLoanService loanService = new AdminLoanServiceImpl();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });
        
        loanType.setSelectedIndex(0);
        renderLoanCards();
    }
    
    private void resizeContent() {
        // Toggle the Main Container Margin
        if (mainContainer.getSclass().contains("enlarge")) {
            // Sidebar is opening (Back to Normal)
            mainContainer.setSclass("main-container");
        } else {
            // Sidebar is collapsing (Expand content)
            mainContainer.setSclass("main-container enlarge");
        }
    }
    
    private void renderLoanCards() {
        // Clear previous cards
        loanCardsContainer.getChildren().clear();
        
        List<Loan> loans = loanService.getAllLoans();
        
        for (Loan loan : loans) {
            Div card = new Div();
            card.setSclass("loan-card");
            
            // 1. Header Section
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
            
            // 2. Body Section
            Div body = new Div();
            body.setSclass("card-body");
            Hlayout hlBody = new Hlayout();
            hlBody.setValign("middle");
            
            hlBody.appendChild(createInfoBlock("Requested Amount", "₹ " + loan.getLoanAmount(), "highlight-value"));
            hlBody.appendChild(createInfoBlock("Loan Type", loan.getLoanType().toString(), ""));
            hlBody.appendChild(createInfoBlock("Monthly Income", "₹ " + loan.getEmploymentDetails().getMonthlyIncome(), ""));
            hlBody.appendChild(createInfoBlock("Submission Date", loan.getSubmissionDate().toString(), ""));
            
            // 3. Action Button
            Div btnDiv = new Div();
            btnDiv.setHflex("1");
            btnDiv.setStyle("text-align: right;");
            Button btn = new Button("Review Docs");
            btn.setSclass("review-btn");
            btn.setIconSclass("z-icon-folder-open");
            
            // Attach the specific loan data to the button for the modal
            btn.setAttribute("loanData", loan);
            btn.addEventListener("onClick", event -> {
                openReviewModal((Loan) btn.getAttribute("loanData"));
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
    
    public void openReviewModal(Loan loan) {
        Map<String, Object> args = new HashMap<>();
        args.put("loanData", loan);
        Window window = (Window) Executions.createComponents("/admin/review_docs.zul", null, args);
        window.doModal();
    }
}
