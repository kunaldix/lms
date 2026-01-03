package com.lms.controller;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.*;

import com.lms.model.Emi;
import com.lms.model.User;
import com.lms.service.EmiService;

@VariableResolver(DelegatingVariableResolver.class)
public class EmiComposer extends SelectorComposer<Div> {

    private static final long serialVersionUID = -6735214827680029146L;

    // PayU Test Credentials
    private final String MERCHANT_KEY = "qx9uC2"; 
    private final String MERCHANT_SALT = "scblTubDlAM2aZf4S0TvocI4ux4AjmRq"; 
    private final String PAYU_URL = "https://test.payu.in/_payment";

    @Wire private Vlayout mainContainer;
    @Wire private Vlayout loanListContainer;
    @Wire private Label lblTotalPayable;
    @Wire private Label lblNextDueDate;

    @WireVariable private EmiService emiService;

    @SuppressWarnings("deprecation")
	private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    @Override
    public void doAfterCompose(Div comp) throws Exception {
        super.doAfterCompose(comp);
        
        // Handle Sidebar Resize
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true).subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });
        
        String status = Executions.getCurrent().getParameter("status");
        if ("success".equals(status)) {
            Clients.showNotification("Payment Completed Successfully!", "info", null, "middle_center", 3000);
        } else if ("failed".equals(status)) {
            Clients.showNotification("Payment Failed. Please try again.", "error", null, "middle_center", 3000);
        }

        refreshData();
    }

    private void refreshData() {
        User currentUser = (User) Sessions.getCurrent().getAttribute("user");
        if (currentUser == null) {
            Executions.sendRedirect("/auth/login.zul");
            return;
        }

        List<Emi> userEmis = emiService.getEmisForCurrentUser(currentUser.getId()); 
        updateStats(userEmis);
        renderEmiCards(userEmis);
    }

    private void handlePaymentInitiation(Emi emi) {
        User user = (User) Sessions.getCurrent().getAttribute("user");
        
        // Generate unique transaction ID
        String txnid = "TXN" + System.currentTimeMillis() + emi.getEmiId();
        String amount = String.valueOf(emi.getEmiAmount());
        String productInfo = "EMI_PAY_" + emi.getLoan().getLoanId();
        
        // Success and Failure URLs (Point to your Response Servlet)
        String surl = "http://localhost:8080/CreditHub/paymentResponse"; 
        String furl = "http://localhost:8080/CreditHub/paymentResponse";

        // PayU Hash Sequence: key|txnid|amount|productinfo|firstname|email|udf1|udf2|udf3|udf4|udf5||||||SALT
        String hashSequence = MERCHANT_KEY + "|" + txnid + "|" + amount + "|" + productInfo + "|" 
                            + user.getName() + "|" + user.getEmail() + "|||||||||||" + MERCHANT_SALT;
        
        String hash = generateHash(hashSequence);

        JSONObject payuParams = new JSONObject();
        payuParams.put("key", MERCHANT_KEY);
        payuParams.put("hash", hash);
        payuParams.put("txnid", txnid);
        payuParams.put("amount", amount);
        payuParams.put("firstname", user.getName());
        payuParams.put("email", user.getEmail());
        payuParams.put("phone", user.getPhoneNumber()); // Placeholder
        payuParams.put("productinfo", productInfo);
        payuParams.put("surl", surl);
        payuParams.put("furl", furl);

        // Call JS function in emi.zul to submit hidden form
        Clients.evalJavaScript("launchPayU('" + payuParams.toString() + "')");
    }

    private String generateHash(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] digest = md.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    // --- Helper UI Methods ---
    private void updateStats(List<Emi> emis) {
        double total = 0;
        for (Emi e : emis) {
            if (!"PAID".equalsIgnoreCase(e.getStatus().name())) {
                total += e.getEmiAmount();
            }
        }
        lblTotalPayable.setValue(currencyFormat.format(total));
        if (!emis.isEmpty()) {
            lblNextDueDate.setValue(dateFormat.format(emis.get(0).getDueDate()));
        }
    }

    private void renderEmiCards(List<Emi> emis) {
        loanListContainer.getChildren().clear();

        for (Emi emi : emis) {
            Div card = new Div();
            card.setSclass("loan-card");

            // --- 1. Header Section ---
            Div header = new Div();
            header.setSclass("card-header");
            
            Hlayout hlHeader = new Hlayout();

            // Loan Type Icon
            Span icon = new Span();
            icon.setSclass(getIconByLoanType(emi.getLoan().getLoanType().toString()) + " fa-fw");
            icon.setStyle("color:#004b98; font-size:16px; margin-right:10px;");

            // Title (Loan Type)
            Label title = new Label(emi.getLoan().getLoanType().toString());
            title.setSclass("loan-title");

            // Loan ID (#)
            Label idLabel = new Label("#" + emi.getLoan().getLoanId());
            idLabel.setSclass("loan-id");
            idLabel.setStyle("margin-left: 10px; color: #888; font-size: 12px;");

            hlHeader.appendChild(icon);
            hlHeader.appendChild(title);
            hlHeader.appendChild(idLabel);

            // Status Badge (PAID / PENDING / OVERDUE)
            Label statusBadge = new Label(emi.getStatus().name());
            statusBadge.setSclass("status-badge status-" + emi.getStatus().name().toLowerCase());

            header.appendChild(hlHeader);
            header.appendChild(statusBadge);

            // --- 2. Body Section ---
            Div body = new Div();
            body.setSclass("card-body");
            Hlayout hlBody = new Hlayout();

            // Calculate Outstanding Balance
            BigDecimal outstanding = emi.getLoan().getLoanAmount().subtract(emi.getLoan().getAmountPaid());

            // Info Blocks
            hlBody.appendChild(createInfoBlock("EMI Amount", currencyFormat.format(emi.getEmiAmount()), "highlight-value"));
            hlBody.appendChild(createInfoBlock("Next Due Date", dateFormat.format(emi.getDueDate()), ""));
            hlBody.appendChild(createInfoBlock("Outstanding Balance", currencyFormat.format(outstanding), ""));
            hlBody.appendChild(createInfoBlock("Installment", emi.getInstallmentNumber() + " of " + emi.getLoan().getTenureMonths(), ""));

            // --- 3. Action Section ---
            Div btnDiv = new Div();
            btnDiv.setHflex("1");
            btnDiv.setStyle("text-align: right;");
            
            if (!"PAID".equalsIgnoreCase(emi.getStatus().name())) {
                Button btn = new Button("Pay Now");
                btn.setSclass("pay-btn");
                btn.setIconSclass("fa-solid fa-credit-card");
                // This now triggers the PayU flow we set up earlier
                btn.addEventListener("onClick", e -> handlePaymentInitiation(emi));
                btnDiv.appendChild(btn);
            }
            
            hlBody.appendChild(btnDiv);
            body.appendChild(hlBody);

            // Assemble the Card
            card.appendChild(header);
            card.appendChild(body);
            loanListContainer.appendChild(card);
        }
    }

    
    private Vlayout createInfoBlock(String label, String value, String extraClass) {
        Vlayout v = new Vlayout(); v.setHflex("1");
        Label l = new Label(label); l.setSclass("info-label");
        Label val = new Label(value); val.setSclass("info-value " + extraClass);
        v.appendChild(l); v.appendChild(val);
        return v;
    }

    private String getIconByLoanType(String type) {
        if (type.toLowerCase().contains("home")) return "fa-solid fa-house-chimney";
        if (type.toLowerCase().contains("car")) return "fa-solid fa-car";
        return "fa-solid fa-user-tag";
    }

    private void resizeContent() {
        mainContainer.setSclass(mainContainer.getSclass().contains("enlarge") ? "main-container" : "main-container enlarge");
    }
}