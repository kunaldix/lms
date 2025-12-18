package com.lms.controller;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import com.lms.dto.LoanDTO;

public class ApplicationComposer extends SelectorComposer<Window> {

    private static final long serialVersionUID = 1L;

	@Wire
    private Grid applicationGrid;

    private List<LoanDTO> loanList;

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        loadLoans();
        loadGrid();
    }

    private void loadLoans() {
        loanList = new ArrayList<>();

        loanList.add(new LoanDTO(1, "Aashish", "Gautam", "9876543210",
                50000, "Education", "PENDING"));

        loanList.add(new LoanDTO(2, "Rahul", "Sharma", "9123456789",
                80000, "Home Loan", "PENDING"));
    }

    private void loadGrid() {

        Rows rows = new Rows();
        applicationGrid.appendChild(rows);

        for (LoanDTO loan : loanList) {

            Row row = new Row();

            row.appendChild(new Label(loan.getFirstName()));
            row.appendChild(new Label(loan.getLastName()));
            row.appendChild(new Label(loan.getPhone()));
            row.appendChild(new Label(String.valueOf(loan.getAmount())));
            row.appendChild(new Label(loan.getPurpose()));

            Label statusLbl = new Label(loan.getStatus());
            row.appendChild(statusLbl);

            // 👉 ACTION BUTTONS
            Hbox actionBox = new Hbox();
            actionBox.setSpacing("10px");

            Button approveBtn = new Button("Approve");
            approveBtn.setSclass("btn-approve");
            approveBtn.addEventListener("onClick", e -> {
                loan.setStatus("APPROVED");
                statusLbl.setValue("APPROVED");
                Clients.showNotification("Loan Approved");
            });

            Button rejectBtn = new Button("Reject");
            rejectBtn.setSclass("btn-reject");
            rejectBtn.addEventListener("onClick", e -> {
                loan.setStatus("REJECTED");
                statusLbl.setValue("REJECTED");
                Clients.showNotification("Loan Rejected");
            });

            actionBox.appendChild(approveBtn);
            actionBox.appendChild(rejectBtn);

            row.appendChild(actionBox);
            rows.appendChild(row);
        }
    }
}
