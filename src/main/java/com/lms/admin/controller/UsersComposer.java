package com.lms.admin.controller;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Listcell;

import org.zkoss.zul.Vlayout;

import com.lms.model.User;
import com.lms.service.LoanService;
import com.lms.service.UserService;

@VariableResolver(DelegatingVariableResolver.class)
public class UsersComposer extends SelectorComposer<Div> {

    private static final long serialVersionUID = -6735214827680029146L;

    @Wire
    private Vlayout mainContainer;

    @Wire
    private Listbox userListbox;

    @WireVariable("realUserService")
    private UserService userService;
    
    @WireVariable
    private LoanService loanService;
    
    @Wire
    private Label totalUsers;

    @Wire
    private Textbox txtSearch;
    
    private List<User> users = new ArrayList<>(); 
    
    @Override
    public void doAfterCompose(Div comp) throws Exception {
        super.doAfterCompose(comp);


        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
            .subscribe(event -> {
                if ("onSidebarToggle".equals(event.getName())) {
                    resizeContent();
                }
            });

        users = userService.getAllCustomers();
        renderUserList(users);
       
        totalUsers.setValue(String.valueOf(users.size()));
    }

    private void resizeContent() {
        if (mainContainer != null) {
            if (mainContainer.getSclass().contains("enlarge")) {
                mainContainer.setSclass("main-container");
            } else {
                mainContainer.setSclass("main-container enlarge");
            }
        }
    }
  
    private void renderUserList(List<User> users) {
        userListbox.getItems().clear();

        for (User user : users) {
        	
            Listitem item = new Listitem();
            item.setValue(user);
            item.appendChild(new Listcell(user.getName()));
            item.appendChild(new Listcell(user.getEmail()));
            
            item.appendChild(
                new Listcell("₹ " + loanService.getTotalDebt(user.getId()))
            );
            
            item.appendChild(
                new Listcell(String.valueOf(
                    loanService.getActiveLoans(user.getId())
                ))
            );

            Button detailsBtn = new Button("User Details");
            detailsBtn.setSclass("z-button");
            detailsBtn.addEventListener("onClick", e -> {
                Executions.sendRedirect(
                    "userdetails.zul?userId=" + user.getId()
                );
            });

            Listcell actionCell = new Listcell();
            actionCell.appendChild(detailsBtn);

            item.appendChild(actionCell);

            userListbox.appendChild(item);   
        }
    }

    @Listen("onClick = #btnSearch")
    public void searchUsers() {

        String keyword = txtSearch.getValue();

        if (keyword == null || keyword.trim().isEmpty()) {
            // Reset list
            renderUserList(users);
            totalUsers.setValue(String.valueOf(users.size()));
            return;
        }

        String search = keyword.trim().toLowerCase();

        List<User> filteredUsers = users.stream()
            .filter(u ->
                (u.getName() != null && u.getName().toLowerCase().contains(search))).toList(); 

        renderUserList(filteredUsers);
        totalUsers.setValue(String.valueOf(filteredUsers.size()));
    } 
}