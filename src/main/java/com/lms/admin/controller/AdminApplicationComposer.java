package com.lms.admin.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import com.lms.constant.LoanApplicationStatus;
import com.lms.constant.LoanType;
import com.lms.constant.RepaymentType;
import com.lms.model.*;

public class AdminApplicationComposer extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;

    @Wire private Listbox applicationsList;
    @Wire private Window detailsWin;
    @Wire
    private Listbox documentsList;

    @Wire private Label dLoanType, dAmount, dTenure, dInterest, dRepayment, dEmiDate;
    @Wire private Label dName, dEmail;
    @Wire private Label dEmployment, dEmployer, dIncome;
    @Wire private Label dBank, dIfsc, dAccount;
    
    @Wire
	private Vlayout mainContainer;

    private List<Loan> loanList;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });
        
        loanList = createDummyLoans();
        populateList();
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

    // ================= POPULATE TABLE =================
    private void populateList() {

        applicationsList.getItems().clear();

        for (Loan loan : loanList) {

            Listitem item = new Listitem();
            item.setValue(loan);

            item.appendChild(new Listcell(loan.getLoanId()));
            item.appendChild(new Listcell(
                    loan.getUser().getName() + "\n" + loan.getUser().getEmail()
            ));
            item.appendChild(new Listcell(loan.getLoanType().name()));
            item.appendChild(new Listcell("₹ " + loan.getLoanAmount()));
            item.appendChild(new Listcell(loan.getTenureMonths() + " Months"));
            item.appendChild(new Listcell(loan.getApplicationStatus().name()));

         // ===== ACTION DROPDOWN =====
            Menubar menubar = new Menubar();

            Menu actionMenu = new Menu("Actions");
            actionMenu.setSclass("nav-btn");
            menubar.appendChild(actionMenu);

            Menupopup popup = new Menupopup();
            actionMenu.appendChild(popup);

            /* 🔵 VIEW DETAILS */
            Menuitem viewItem = new Menuitem("View Details");
            viewItem.setSclass("menu-view");
            viewItem.addEventListener("onClick", e -> openDetails(loan));
            popup.appendChild(viewItem);

            /* 🟢 APPROVE */
            Menuitem approveItem = new Menuitem("Approve");
            approveItem.setSclass("menu-approve");
            approveItem.addEventListener("onClick", e -> {
                loan.setApplicationStatus(LoanApplicationStatus.ACCEPTED);
                Messagebox.show("Loan Approved");
                populateList();
            });
            popup.appendChild(approveItem);

            /* 🔴 REJECT */
            Menuitem rejectItem = new Menuitem("Reject");
            rejectItem.setSclass("menu-reject");
            rejectItem.addEventListener("onClick", e -> {
                loan.setApplicationStatus(LoanApplicationStatus.REJECTED);
                Messagebox.show("Loan Rejected");
                populateList();
            });
            popup.appendChild(rejectItem);

            /* STATUS BASED VISIBILITY */
            if (loan.getApplicationStatus() == LoanApplicationStatus.ACCEPTED) {
                approveItem.setVisible(false);
            }
            if (loan.getApplicationStatus() == LoanApplicationStatus.REJECTED) {
                rejectItem.setVisible(false);
            }

            /* ADD TO ROW */
            Listcell actionCell = new Listcell();
            actionCell.appendChild(menubar);
            item.appendChild(actionCell);



            applicationsList.appendChild(item);
        }
    }

    // ================= VIEW DETAILS =================
    private void openDetails(Loan loan) {

        if (!detailsWin.isVisible()) {
            detailsWin.setVisible(true);
            Selectors.wireComponents(detailsWin, this, false);
        }

        dLoanType.setValue(loan.getLoanType().name());
        dAmount.setValue("₹ " + loan.getLoanAmount());
        dTenure.setValue(String.valueOf(loan.getTenureMonths()));
        dInterest.setValue(loan.getInterestRate() + "%");
        dRepayment.setValue(loan.getRepaymentType().name());
        dEmiDate.setValue(String.valueOf(loan.getPreferredEmiDate()));

        dName.setValue(loan.getUser().getName());
        dEmail.setValue(loan.getUser().getEmail());

        dEmployment.setValue(loan.getEmploymentDetails().getEmploymentType());
        dEmployer.setValue(loan.getEmploymentDetails().getEmployerName());
        dIncome.setValue("₹ " + loan.getEmploymentDetails().getMonthlyIncome());

        dBank.setValue(loan.getAccountInfo().getBankName());
        dIfsc.setValue(loan.getAccountInfo().getIfscCode());
        dAccount.setValue(loan.getAccountInfo().getAccountNumber());
        loadDocuments();
    }
    
    private void loadDocuments() {

        documentsList.getItems().clear();

        // TEMP STATIC DOCUMENTS (replace later with DB)
        addDoc("Aadhaar Card", "PDF");
        addDoc("PAN Card", "PDF");
        addDoc("Salary Slip", "PDF");
        addDoc("Bank Statement", "PDF");
    }

    
    private void addDoc(String name, String type) {

        Listitem item = new Listitem();

        item.appendChild(new Listcell(name));
        item.appendChild(new Listcell(type));

        Button viewBtn = new Button("View");
        viewBtn.setSclass("nav-btn");

        viewBtn.addEventListener("onClick", e ->
            Messagebox.show(name + " clicked (dummy document)")
        );

        Listcell actionCell = new Listcell();
        actionCell.appendChild(viewBtn);

        item.appendChild(actionCell);
        documentsList.appendChild(item);
    }


    // ================= TEMP DUMMY DATA =================
    private List<Loan> createDummyLoans() {

        List<Loan> list = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {

            User user = new User();
            user.setName("User " + i);
            user.setEmail("user" + i + "@gmail.com");

            EmploymentDetails emp = new EmploymentDetails();
            emp.setEmploymentType("Salaried");
            emp.setEmployerName("Company " + i);
            emp.setMonthlyIncome(new BigDecimal(40000 + (i * 5000)));

            AccountInfo acc = new AccountInfo();
            acc.setBankName("HDFC Bank");
            acc.setIfscCode("HDFC00012" + i);
            acc.setAccountNumber("12345678" + i);

            Loan loan = new Loan(
                    "LN00" + i,
                    LoanType.PERSONAL_LOAN,
                    new BigDecimal(100000 * i),
                    12 + i,
                    10.5,
                    RepaymentType.MONTHLY_EMI,
                    5 + i,
                    user,
                    emp,
                    acc,
                    null,
                    LoanApplicationStatus.PENDING,
                    new Date()
            );

            list.add(loan);
        }
        return list;
    }
}
