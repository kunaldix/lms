package com.lms.servlet;

import java.io.IOException;
import java.security.MessageDigest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.lms.service.EmiService;

/**
 * Servlet to handle the response (callback) from the PayU Payment Gateway.
 * It verifies the hash integrity and updates the EMI status in CreditHub.
 */
public class PaymentResponseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Logger for financial transaction tracking
    private static final Logger logger = LogManager.getLogger(PaymentResponseServlet.class);
    
    // MUST match your Composer Salt - Keep this secure!
    private final String MERCHANT_SALT = "scblTubDlAM2aZf4S0TvocI4ux4AjmRq"; 

    /**
     * PayU sends a POST request to this servlet after user completes payment.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Extract parameters sent by PayU
        String status = request.getParameter("status");
        String txnid = request.getParameter("txnid");
        String amount = request.getParameter("amount");
        String hashReceived = request.getParameter("hash");
        String key = request.getParameter("key");
        String productinfo = request.getParameter("productinfo");
        String firstname = request.getParameter("firstname");
        String email = request.getParameter("email");
        String mihpayid = request.getParameter("mihpayid"); // PayU's internal transaction ID
        String additionalCharges = request.getParameter("additional_charges");
        String modeFromPayU = request.getParameter("mode"); // e.g., CC, DC, NB

        logger.info("Payment callback received. TXN_ID: {}, Status: {}, Amount: {}", txnid, status, amount);

        /* * Reverse Hash Verification Logic:
         * PayU expects 10 UDF placeholders (empty here). 
         * Formula: SALT|status|||||||||||email|firstname|productinfo|amount|txnid|key
         */
        String hashSeq = MERCHANT_SALT + "|" + status + "|||||||||||" 
                       + email + "|" + firstname + "|" + productinfo + "|" + amount + "|" + txnid + "|" + key;

        // If PayU applied additional charges (like convenience fees), they are prepended to the hash
        if (additionalCharges != null && !additionalCharges.isEmpty()) {
            hashSeq = additionalCharges + "|" + hashSeq;
            logger.debug("Additional charges detected: {}. Prepended to hash sequence.", additionalCharges);
        }

        String calculatedHash = generateHash(hashSeq);

        // Security: Check if the hash matches and status is 'success'
        if (calculatedHash != null && calculatedHash.equalsIgnoreCase(hashReceived) && "success".equals(status)) {
            
            logger.info("Hash verification successful for TXN_ID: {}", txnid);
            
            // Get Spring Context to access Service layer in a standard Servlet
            WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            EmiService emiService = ctx.getBean(EmiService.class);
            
            // Process business logic: Update DB, mark EMI as paid
            if (emiService.processPayment(txnid, mihpayid, modeFromPayU)) {
                logger.info("Payment processed successfully in database for TXN_ID: {}", txnid);
                response.sendRedirect(request.getContextPath() + "/emi/emi.zul?status=success");
            } else {
                logger.error("Hash verified but EmiService failed to update DB for TXN_ID: {}", txnid);
                response.sendRedirect(request.getContextPath() + "/emi/emi.zul?status=error");
            }
        } else {
            // Log security warning if hashes don't match (could be a tampering attempt)
            logger.warn("Payment Failed or Hash Mismatch! Received: {}, Calculated: {}, Status: {}", 
                         hashReceived, calculatedHash, status);
            response.sendRedirect(request.getContextPath() + "/emi/emi.zul?status=failed");
        }
    }
    
    /**
     * Generates SHA-512 Hash for PayU verification.
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
        } catch (Exception e) {
            logger.error("Error generating SHA-512 hash: ", e);
            return null;
        }
    }
}