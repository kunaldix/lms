package com.lms.controller;

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

public class EditProfileController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;

    @Wire private Textbox txtName, txtPhone, txtAddress, txtEmail, txtRole;
    @Wire private Textbox txtOldPass, txtNewPass, txtCnfNewPass;
    @Wire private Image imgPreview;
    
    @Wire private Button btnCancel;
    
    private User user;
    private String uploadedImageName;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        user = (User) Sessions.getCurrent().getAttribute("user");

        if (user != null) {
            txtName.setValue(user.getName());
//            txtPhone.setValue(user.getPhone());
//            txtAddress.setValue(user.getAddress());
            txtEmail.setValue(user.getEmail());
            txtRole.setValue(user.getRole().name());

            if (user.getProfileImage() != null) {
                imgPreview.setSrc("/img/" + user.getProfileImage());
            }
        }
        btnCancel.addEventListener("onClick", e->((Window) getSelf()).detach());
    }

    // Image Upload
    @Listen("onUpload = #uploadImage")
    public void uploadImage(UploadEvent event) throws Exception {
        Media media = event.getMedia();

        uploadedImageName = System.currentTimeMillis() + "_" + media.getName();

        File file = new File(
            Executions.getCurrent().getDesktop().getWebApp()
            .getRealPath("/img/" + uploadedImageName)
        );

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(media.getByteData());
        }

        imgPreview.setSrc("/img/" + uploadedImageName);
    }

    @Listen("onClick = #btnSave")
    public void saveProfile() {
        // 1. Basic Info Update
//        user.setPhone(txtPhone.getValue());
//        user.setAddress(txtAddress.getValue());

        if (uploadedImageName != null) {
            user.setProfileImage(uploadedImageName);
        }

        // 2. Password Logic (Optional)
        String oldPass = txtOldPass.getValue();
        String newPass = txtNewPass.getValue();
        
        if (!newPass.isEmpty()) {
            if (oldPass.isEmpty()) {
                Clients.showNotification("Please enter current password to set a new one", "warning", null, null, 2000);
                return;
            }
            // TODO: Validate oldPass against user.getPassword() and then user.setPassword(newPass)
        }

        // Save to Session and DB
        Sessions.getCurrent().setAttribute("user", user);
        Clients.showNotification("Profile updated successfully!", "info", null, null, 2000);
        ((Window) getSelf()).detach();
    }
}
