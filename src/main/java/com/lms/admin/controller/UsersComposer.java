package com.lms.admin.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

/**
 * Controller for managing the User Management view in the Admin panel.
 * Displays a list of all customers, their debt summary, and provides 
 * search functionality and navigation to detailed user profiles.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class UsersComposer extends SelectorComposer<Div> {

    private static final long serialVersionUID = -6735214827680029146L;

    // Initialize Log4j Logger for auditing user data access and searches
    private static final Logger logger = LogManager.getLogger(UsersComposer.class);

    @Wire private Vlayout mainContainer;
    @Wire private Listbox userListbox;
    @Wire private Label totalUsers;
    @Wire private Textbox txtSearch;

    @WireVariable("realUserService")
    private UserService userService;
    
    @WireVariable
    private LoanService loanService;
    
    private List<User> users = new ArrayList<>(); 
    
    @Override
    public void doAfterCompose(Div comp) throws Exception {
        super.doAfterCompose(comp);
        
        logger.info("UsersComposer initialized. Loading customer list.");

        // Subscribe to sidebar toggle for responsive UI behavior
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
            .subscribe(event -> {
                if ("onSidebarToggle".equals(event.getName())) {
                    resizeContent();
                }
            });

        // Load all customers from the database
        try {
            users = userService.getAllCustomers();
            renderUserList(users);
            totalUsers.setValue(String.valueOf(users.size()));
            logger.info("Successfully loaded {} customers.", users.size());
        } catch (Exception e) {
            logger.error("Failed to load customer list: {}", e.getMessage(), e);
        }
    }

    /**
     * Toggles the main container size based on the sidebar state.
     */
    private void resizeContent() {
        if (mainContainer != null) {
            String currentSclass = mainContainer.getSclass();
            if (currentSclass != null && currentSclass.contains("enlarge")) {
                mainContainer.setSclass("main-container");
            } else {
                mainContainer.setSclass("main-container enlarge");
            }
            logger.debug("Container resized. New Sclass: {}", mainContainer.getSclass());
        }
    }
  
    /**
     * Dynamically builds the listbox items for the user list.
     * @param userList The list of users to display.
     */
    private void renderUserList(List<User> userList) {
        userListbox.getItems().clear();

        for (User user : userList) {
            Listitem item = new Listitem();
            item.setValue(user);
            
            item.appendChild(new Listcell(user.getName()));
            item.appendChild(new Listcell(user.getEmail()));
            
            // Fetch financial summary per user
            item.appendChild(new Listcell("₹ " + loanService.getTotalDebt(user.getId())));
            item.appendChild(new Listcell(String.valueOf(loanService.getActiveLoans(user.getId()))));

            // Navigation Button to Detailed Profile
            Button detailsBtn = new Button("User Details");
            detailsBtn.setSclass("z-button");
            detailsBtn.setIconSclass("z-icon-info-circle");
            detailsBtn.addEventListener("onClick", e -> {
                logger.debug("Admin navigating to details for User ID: {}", user.getId());
                Executions.sendRedirect("userdetails.zul?userId=" + user.getId());
            });

            Listcell actionCell = new Listcell();
            actionCell.appendChild(detailsBtn);
            item.appendChild(actionCell);

            userListbox.appendChild(item);   
        }
    }

    /**
     * Filters the user list based on the search keyword provided in txtSearch.
     */
    @Listen("onClick = #btnSearch")
    public void searchUsers() {
        String keyword = txtSearch.getValue();

        if (keyword == null || keyword.trim().isEmpty()) {
            logger.info("Search reset: Loading full user list.");
            renderUserList(users);
            totalUsers.setValue(String.valueOf(users.size()));
            return;
        }

        String search = keyword.trim().toLowerCase();
        logger.info("Performing user search with keyword: [{}]", search);

        // Filter users by name match
        List<User> filteredUsers = users.stream()
            .filter(u -> (u.getName() != null && u.getName().toLowerCase().contains(search)))
            .toList(); 

        renderUserList(filteredUsers);
        totalUsers.setValue(String.valueOf(filteredUsers.size()));
        logger.debug("Search completed. Found {} matches.", filteredUsers.size());
    } 
}