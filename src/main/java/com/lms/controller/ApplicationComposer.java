package com.lms.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.event.Events;
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

    /* ================= LOAD DUMMY DATA ================= */
    private void loadLoans() {
        loanList = new ArrayList<>();

        loanList.add(new LoanDTO(
                1,
                "Aashish",
                "Gautam",
                "9876543210",
                50000,
                200000,
                "Education Loan",
                "PENDING",
                new Date(),
                101
        ));

        loanList.add(new LoanDTO(
                2,
                "Rahul",
                "Sharma",
                "9123456789",
                80000,
                350000,
                "Home Loan",
                "PENDING",
                new Date(),
                102
        ));
    }

    /* ================= GRID DATA ================= */
    private void loadGrid() {

        Rows rows = new Rows();
        applicationGrid.appendChild(rows);

        SimpleDateFormat sdf =
                new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

        for (LoanDTO loan : loanList) {

            Row row = new Row();

            row.appendChild(new Label(loan.getFirstName()));
            row.appendChild(new Label(loan.getLastName()));
            row.appendChild(new Label(loan.getPhone()));
            row.appendChild(new Label(String.valueOf(loan.getSalary())));
            row.appendChild(new Label(loan.getPurpose()));
            row.appendChild(new Label(String.valueOf(loan.getAmount())));

            Label statusLbl = new Label(loan.getStatus());
            statusLbl.setSclass(
                    loan.getStatus().equals("APPROVED")
                            ? "status-accept"
                            : loan.getStatus().equals("REJECTED")
                            ? "status-reject"
                            : "status-pending"
            );
            row.appendChild(statusLbl);

            row.appendChild(new Label(sdf.format(loan.getAppliedDate())));
            row.appendChild(new Label(String.valueOf(loan.getUserId())));

            /* ===== ACTION BUTTONS ===== */
            Hbox actionBox = new Hbox();
            actionBox.setSpacing("10px");

            Button approveBtn = new Button("Approve");
            approveBtn.setSclass("btn-accept");
            approveBtn.addEventListener(Events.ON_CLICK, e -> {
                loan.setStatus("APPROVED");
                statusLbl.setValue("APPROVED");
                statusLbl.setSclass("status-accept");
                Clients.showNotification("Loan Approved Successfully");
            });

            Button rejectBtn = new Button("Reject");
            rejectBtn.setSclass("btn-reject");
            rejectBtn.addEventListener(Events.ON_CLICK, e -> {
                loan.setStatus("REJECTED");
                statusLbl.setValue("REJECTED");
                statusLbl.setSclass("status-reject");
                Clients.showNotification("Loan Rejected");
            });

            actionBox.appendChild(approveBtn);
            actionBox.appendChild(rejectBtn);

            row.appendChild(actionBox);
            rows.appendChild(row);
        }
    }
}
