package com.lms.admin.controller;

import java.util.Optional;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vlayout;

import com.lms.model.User;
import com.lms.service.LoanService;
import com.lms.service.UserService;

@VariableResolver(DelegatingVariableResolver.class)
public class Userdetails extends SelectorComposer<Div> {

    private static final long serialVersionUID = 1L;

    /* ---------- LAYOUT ---------- */
    @Wire
    private Vlayout mainContainer;

    /* ---------- PROFILE LABELS ---------- */
    @Wire private Label lblName;
    @Wire private Label lblUserId;
    @Wire private Label lblEmail;
    @Wire private Label lblPhone;
    @Wire private Label lblAddress;

    /* ---------- LOAN SUMMARY ---------- */
    @Wire private Label lblTotalLoan;
    @Wire private Label lblOutstanding;
    @Wire private Label lblActiveLoans;

    /* ---------- SERVICES ---------- */
    @WireVariable("realUserService")
    private UserService userService;

    @WireVariable
    private LoanService loanService;

    @Override
    public void doAfterCompose(Div comp) throws Exception {
        super.doAfterCompose(comp);

        subscribeSidebarEvents();
        loadUserDetails();
    }

    /* ---------- LOAD USER ---------- */
    private void loadUserDetails() {

        String userIdParam = Executions.getCurrent().getParameter("userId");

        if (userIdParam == null) {
            redirectToUsers();
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdParam);
        } catch (NumberFormatException e) {
            redirectToUsers();
            return;
        }

        Optional<User> optionalUser = userService.getUserById(userId);

        optionalUser.ifPresentOrElse(
            this::populateUserDetails,
            this::redirectToUsers
        );
    }

    /* ---------- POPULATE UI ---------- */
    private void populateUserDetails(User user) {

        // Profile information
        lblName.setValue(user.getName());
        lblUserId.setValue("ID: #" + user.getId());
        lblEmail.setValue(user.getEmail());
        lblPhone.setValue(user.getPhoneNumber());
//        lblAddress.setValue(user.getAddress());

        // Loan summary
        lblTotalLoan.setValue("₹ " + loanService.getTotalLoanOfUser(user.getId()));
        lblOutstanding.setValue("₹ " + loanService.getTotalDebt(user.getId()));
        lblActiveLoans.setValue(
            String.valueOf(loanService.getActiveLoans(user.getId()))
        );
    }

    /* ---------- SIDEBAR EVENTS ---------- */
    private void subscribeSidebarEvents() {
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
            .subscribe(event -> {
                if ("onSidebarToggle".equals(event.getName())) {
                    resizeContent();
                }
            });
    }

    /* ---------- UI HELPERS ---------- */
    private void resizeContent() {
        if (mainContainer == null) return;

        String base = "main-container";
        mainContainer.setSclass(
            mainContainer.getSclass().contains("enlarge")
                ? base
                : base + " enlarge"
        );
    }

    private void redirectToUsers() {
        Executions.sendRedirect("users.zul");
    }
}