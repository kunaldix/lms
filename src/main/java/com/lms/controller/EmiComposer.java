package com.lms.controller;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

/**
 * Controller for the EMI management and Payment initiation page.
 * Handles display of user installments and integration with PayU gateway.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class EmiComposer extends SelectorComposer<Div> {

    private static final long serialVersionUID = -6735214827680029146L;
    
    // Initialize Log4j Logger
    private static final Logger logger = LogManager.getLogger(EmiComposer.class);

    // PayU Test Credentials - Keep these secure in production
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
        
        logger.info("EmiComposer initialized.");
        
        // Handle Sidebar Resize Events
        EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true).subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName())) {
                resizeContent();
            }
        });
        
        // Check for payment callback status in URL parameters
        String status = Executions.getCurrent().getParameter("status");
        if ("success".equals(status)) {
            logger.info("Payment success callback detected in URL.");
            Clients.showNotification("Payment Completed Successfully!", "info", null, "middle_center", 3000);
        } else if ("failed".equals(status)) {
            logger.warn("Payment failure callback detected in URL.");
            Clients.showNotification("Payment Failed. Please try again.", "error", null, "middle_center", 3000);
        }

        refreshData();
    }

    /**
     * Fetches fresh EMI data for the logged-in user.
     */
    private void refreshData() {
        User currentUser = (User) Sessions.getCurrent().getAttribute("user");
        if (currentUser == null) {
            logger.warn("No user in session. Redirecting to login.");
            Executions.sendRedirect("/auth/login.zul");
            return;
        }

        List<Emi> userEmis = emiService.getEmisForCurrentUser(currentUser.getId()); 
        logger.debug("Loaded {} EMI records for user ID: {}", userEmis.size(), currentUser.getId());
        
        updateStats(userEmis);
        renderEmiCards(userEmis);
    }

    /**
     * Prepares the parameters and hash for PayU payment initiation.
     */
    private void handlePaymentInitiation(Emi emi) {
        User user = (User) Sessions.getCurrent().getAttribute("user");
        
        // Generate unique transaction ID for tracking
        String txnid = "TXN" + System.currentTimeMillis() + emi.getEmiId();
        String amount = String.format("%.2f", emi.getEmiAmount());
        String productInfo = "EMI_PAY_" + emi.getLoan().getLoanId();
        
        logger.info("Initiating payment for EMI ID: {}. Generated TXN_ID: {}", emi.getEmiId(), txnid);
        
        // Redirect URLs
        String surl = "http://localhost:8080/lms/paymentResponse"; 
        String furl = "http://localhost:8080/lms/paymentResponse";

        // PayU Hash Sequence: key|txnid|amount|productinfo|firstname|email|udf1|udf2|udf3|udf4|udf5||||||SALT
        String hashSequence = MERCHANT_KEY + "|" + txnid + "|" + amount + "|" + productInfo + "|" 
                + user.getName() + "|" + user.getEmail() + "|||||||||||" + MERCHANT_SALT;
        
        String hash = generateHash(hashSequence);

        if (hash == null) {
            logger.error("Hash generation failed. Aborting payment for TXN_ID: {}", txnid);
            Clients.showNotification("System error: Unable to process payment hash.", "error", null, "middle_center", 3000);
            return;
        }

        JSONObject payuParams = new JSONObject();
        payuParams.put("key", MERCHANT_KEY);
        payuParams.put("hash", hash);
        payuParams.put("txnid", txnid);
        payuParams.put("amount", amount);
        payuParams.put("firstname", user.getName());
        payuParams.put("email", user.getEmail());
        payuParams.put("phone", user.getPhoneNumber());
        payuParams.put("productinfo", productInfo);
        payuParams.put("surl", surl);
        payuParams.put("furl", furl);

        // Hand off to JavaScript for form submission
        logger.debug("Payload ready. Handing off to client-side launchPayU.");
        Clients.evalJavaScript("launchPayU('" + payuParams.toString() + "')");
    }

    /**
     * Generates SHA-512 hash required by PayU security protocols.
     */
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
            logger.error("SHA-512 algorithm not found: {}", e.getMessage(), e);
            return null;
        }
    }

    /* ==========================================================================
       UI HELPER METHODS
       ========================================================================== */

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

            // Header Section
            Div header = new Div();
            header.setSclass("card-header");
            Hlayout hlHeader = new Hlayout();

            Span icon = new Span();
            icon.setSclass(getIconByLoanType(emi.getLoan().getLoanType().toString()) + " fa-fw");
            icon.setStyle("color:#004b98; font-size:16px; margin-right:10px;");

            Label title = new Label(emi.getLoan().getLoanType().toString());
            title.setSclass("loan-title");

            Label idLabel = new Label("#" + emi.getLoan().getLoanId());
            idLabel.setSclass("loan-id");
            idLabel.setStyle("margin-left: 10px; color: #888; font-size: 12px;");

            hlHeader.appendChild(icon);
            hlHeader.appendChild(title);
            hlHeader.appendChild(idLabel);

            Label statusBadge = new Label(emi.getStatus().name());
            statusBadge.setSclass("status-badge status-" + emi.getStatus().name().toLowerCase());

            header.appendChild(hlHeader);
            header.appendChild(statusBadge);

            // Body Section
            Div body = new Div();
            body.setSclass("card-body");
            Hlayout hlBody = new Hlayout();

            BigDecimal outstanding = emi.getLoan().getLoanAmount().subtract(emi.getLoan().getAmountPaid());

            hlBody.appendChild(createInfoBlock("EMI Amount", currencyFormat.format(emi.getEmiAmount()), "highlight-value"));
            hlBody.appendChild(createInfoBlock("Next Due Date", dateFormat.format(emi.getDueDate()), ""));
            hlBody.appendChild(createInfoBlock("Outstanding Balance", currencyFormat.format(outstanding), ""));
            hlBody.appendChild(createInfoBlock("Installment", emi.getInstallmentNumber() + " of " + emi.getLoan().getTenureMonths(), ""));

            // Action Section
            Div btnDiv = new Div();
            btnDiv.setHflex("1");
            btnDiv.setStyle("text-align: right;");
            
            if (!"PAID".equalsIgnoreCase(emi.getStatus().name())) {
                Button btn = new Button("Pay Now");
                btn.setSclass("pay-btn");
                btn.setIconSclass("fa-solid fa-credit-card");
                btn.addEventListener("onClick", e -> handlePaymentInitiation(emi));
                btnDiv.appendChild(btn);
            }
            
            hlBody.appendChild(btnDiv);
            body.appendChild(hlBody);

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
        String t = type.toLowerCase();
        if (t.contains("home")) return "fa-solid fa-house-chimney";
        if (t.contains("car")) return "fa-solid fa-car";
        return "fa-solid fa-user-tag";
    }

    private void resizeContent() {
        if (mainContainer != null) {
            String sclass = mainContainer.getSclass();
            mainContainer.setSclass(sclass != null && sclass.contains("enlarge") ? "main-container" : "main-container enlarge");
        }
    }
}