package com.lms.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.util.media.Media;
import org.zkoss.zul.*;

import com.lms.model.User;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Controller for the Edit Profile modal.
 * Manages user metadata updates, profile image uploads, and password change logic.
 */
public class EditProfileController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    
    // Initialize Log4j Logger for tracking profile changes
    private static final Logger logger = LogManager.getLogger(EditProfileController.class);

    @Wire private Textbox txtName, txtPhone, txtAddress, txtEmail, txtRole;
    @Wire private Textbox txtOldPass, txtNewPass, txtCnfNewPass;
    @Wire private Image imgPreview;
    @Wire private Button btnCancel;
    
    private User user;
    private String uploadedImageName;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        logger.info("EditProfileController initialized.");

        // Retrieve current user from session
        user = (User) Sessions.getCurrent().getAttribute("user");

        if (user != null) {
            logger.debug("Loading profile data for user: {}", user.getEmail());
            txtName.setValue(user.getName());
            
            // Uncomment these when the User model has the respective fields
            // txtPhone.setValue(user.getPhoneNumber());
            // txtAddress.setValue(user.getAddress());
            
            txtEmail.setValue(user.getEmail());
            txtRole.setValue(user.getRole().name());

            if (user.getProfileImage() != null) {
                imgPreview.setSrc("/img/" + user.getProfileImage());
            }
        } else {
            logger.warn("Edit Profile accessed without a valid user session.");
            Executions.sendRedirect("/auth/login.zul");
        }
    }

    /**
     * Handles the profile image upload process.
     * Saves the file to the webapp's /img/ directory.
     */
    @Listen("onUpload = #uploadImage")
    public void uploadImage(UploadEvent event) throws Exception {
        Media media = event.getMedia();
        
        if (media == null) return;

        // Create a unique filename to avoid overwriting existing images
        uploadedImageName = System.currentTimeMillis() + "_" + media.getName();
        
        String realPath = Executions.getCurrent().getDesktop().getWebApp().getRealPath("/img/");
        File file = new File(realPath, uploadedImageName);

        logger.info("Uploading profile image: {} to path: {}", uploadedImageName, file.getAbsolutePath());

        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Check if media provides bytes or a stream
            if (media.isBinary()) {
                fos.write(media.getByteData());
            } else {
                // For larger files, it's better to stream the data
                org.zkoss.io.Files.copy(fos, media.getStreamData());
            }
            logger.debug("Image file successfully written to disk.");
        } catch (Exception e) {
            logger.error("Failed to save profile image: {}", e.getMessage(), e);
            Clients.showNotification("Image upload failed.", "error", null, null, 2000);
            return;
        }

        imgPreview.setSrc("/img/" + uploadedImageName);
    }

    /**
     * Persists the updated profile information.
     */
    @Listen("onClick = #btnSave")
    public void saveProfile() {
        if (user == null) return;

        logger.info("Saving profile updates for user: {}", user.getEmail());

        // 1. Basic Info Update
        // user.setPhoneNumber(txtPhone.getValue());
        // user.setAddress(txtAddress.getValue());

        if (uploadedImageName != null) {
            user.setProfileImage(uploadedImageName);
            logger.debug("Profile image updated to: {}", uploadedImageName);
        }

        // 2. Password Change Logic
        String oldPass = txtOldPass.getValue();
        String newPass = txtNewPass.getValue();
        String cnfPass = txtCnfNewPass.getValue();
        
        if (!newPass.isEmpty()) {
            if (oldPass.isEmpty()) {
                Clients.showNotification("Please enter current password to set a new one", "warning", txtOldPass, "end_center", 3000);
                return;
            }
            
            if (!newPass.equals(cnfPass)) {
                Clients.showNotification("New passwords do not match", "error", txtCnfNewPass, "end_center", 3000);
                return;
            }
            
            /* * TODO: Use BCrypt to validate 'oldPass' against user.getPassword().
             * If valid, user.setPassword(passwordEncoder.encode(newPass)).
             */
            logger.info("Password change requested for user: {}", user.getEmail());
        }

        // 3. Save to Session and Database via UserService
        try {
            Sessions.getCurrent().setAttribute("user", user);
            // userService.updateUser(user); // Call your service layer here
            
            logger.info("Profile successfully updated and saved in session.");
            Clients.showNotification("Profile updated successfully!", "info", null, null, 2000);
            detachModal();
        } catch (Exception e) {
            logger.error("Error saving user profile: {}", e.getMessage(), e);
            Clients.showNotification("Error saving profile.", "error", null, null, 2000);
        }
    }

    @Listen("onClick = #btnCancel")
    public void detachModal() {
        if (getSelf() instanceof Window) {
            ((Window) getSelf()).detach();
        }
    }
}