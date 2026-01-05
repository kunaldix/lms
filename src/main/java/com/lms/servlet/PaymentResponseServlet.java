package com.lms.servlet;

import java.io.IOException;
import java.security.MessageDigest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.lms.service.EmiService;

public class PaymentResponseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // MUST match your Composer Salt
    private final String MERCHANT_SALT = "scblTubDlAM2aZf4S0TvocI4ux4AjmRq"; 

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String status = request.getParameter("status");
        String txnid = request.getParameter("txnid");
        String amount = request.getParameter("amount");
        String hashReceived = request.getParameter("hash");
        String key = request.getParameter("key");
        String productinfo = request.getParameter("productinfo");
        String firstname = request.getParameter("firstname");
        String email = request.getParameter("email");
        String mihpayid = request.getParameter("mihpayid");
        String additionalCharges = request.getParameter("additional_charges");
        String modeFromPayU = request.getParameter("mode");

        // PayU expects 10 UDF placeholders. Even if empty, they need 11 pipes to reach email from status.
        // Logic: SALT|status|||||||||||email|firstname|productinfo|amount|txnid|key
        String hashSeq = MERCHANT_SALT + "|" + status + "|||||||||||" 
                       + email + "|" + firstname + "|" + productinfo + "|" + amount + "|" + txnid + "|" + key;

        // If additional_charges are present, PayU prepends them to the hash string
        if (additionalCharges != null && !additionalCharges.isEmpty()) {
            hashSeq = additionalCharges + "|" + hashSeq;
        }

        String calculatedHash = generateHash(hashSeq);

        if (calculatedHash.equalsIgnoreCase(hashReceived) && "success".equals(status)) {
            WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            EmiService emiService = ctx.getBean(EmiService.class);
            
            if (emiService.processPayment(txnid, mihpayid, modeFromPayU)) {
                response.sendRedirect(request.getContextPath() + "/emi/emi.zul?status=success");
            } else {
                response.sendRedirect(request.getContextPath() + "/emi/emi.zul?status=error");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/emi/emi.zul?status=failed");
        }
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
        } catch (Exception e) {
            return null;
        }
    }
}