package com.lms.servlet;

import java.io.IOException;
import java.security.MessageDigest;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.lms.service.EmiService;

@WebServlet("/paymentResponse")
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
        String mihpayid = request.getParameter("mihpayid"); // PayU's internal ID

        // 1. Verify Reverse Hash: sha512(SALT|status||||||udf5|udf4|udf3|udf2|udf1|email|firstname|productinfo|amount|txnid|key)
        // Note: The empty pipes |||||| are for UDF (User Defined Fields) which we aren't using.
        String hashSeq = MERCHANT_SALT + "|" + status + "||||||||||" + email + "|" + firstname + "|" + productinfo + "|" + amount + "|" + txnid + "|" + key;
        String calculatedHash = generateHash(hashSeq);

        if (calculatedHash.equalsIgnoreCase(hashReceived) && "success".equals(status)) {
            // 2. Success Path: Update Database
            // Get the Spring Service manually since Servlets aren't Spring-managed by default
            WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            EmiService emiService = ctx.getBean(EmiService.class);
            
            // Logic: productinfo contains "EMI_PAY_LOANID", parse or use txnid to find the EMI
//            boolean isUpdated = emiService.processPayment(txnid, mihpayid); 
//
//            if (isUpdated) {
//                response.sendRedirect("emi.zul?status=success");
//            } else {
//                response.sendRedirect("emi.zul?status=error");
//            }
        } else {
            // 3. Failure Path
            response.sendRedirect("emi.zul?status=failed");
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