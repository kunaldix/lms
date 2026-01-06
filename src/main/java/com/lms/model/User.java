package com.lms.model;

import com.lms.constant.Role;

import java.io.Serializable;

/**
 * Core model representing a user within the CreditHub system.
 * This entity supports both Admin and Customer roles and maintains 
 * essential profile details including contact info and security credentials.
 */
public class User implements  Serializable{

    private static final long serialVersionUID = -8277245406903677811L;
    
	private int id;
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String profileImage; 
    private Account account;
    private Role role;
    
    /**
     * Default constructor required for framework serialization 
     * and ZK data binding components.
     */
    public User() {
        super();
    }

    /**
     * Complete constructor for creating a user with a specific profile image.
     */
    public User(String name, String email, String password, String phoneNumber, Role role, String profileImage) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.profileImage = profileImage;
    }
    
    /**
     * Standard constructor for registration where a profile image might not yet be provided.
     */
    public User(String name, String email, String password, String phoneNumber, Role role) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    /* --- Standard Getters and Setters --- */

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Account getAccount() { 
        return account; 
    }
    
    public void setAccount(Account account) { 
        this.account = account; 
    }
    
    public String getPhoneNumber() { 
        return phoneNumber; 
    }
    
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber; 
    }

    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }

    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }

    public Role getRole() { 
        return role; 
    }
    
    public void setRole(Role role) { 
        this.role = role; 
    }

    /**
     * Provides a summarized string of the user object.
     * We exclude the password here to prevent accidental exposure in logs.
     */
    @Override
    public String toString() {
        return "User [ID=" + id + ", Name=" + name + ", Role=" + role + ", ProfileImage=" + profileImage + "]";
    }
}