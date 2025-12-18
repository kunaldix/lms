package com.lms.dto;

import java.util.Date;

public class LoanDTO {

    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private double salary;
    private double amount;
    private String purpose;
    private String status;
    private Date appliedDate;
    private int userId;

    public LoanDTO(int id, String firstName, String lastName,
                   String phone, double salary, double amount,
                   String purpose, String status,
                   Date appliedDate, int userId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.salary = salary;
        this.amount = amount;
        this.purpose = purpose;
        this.status = status;
        this.appliedDate = appliedDate;
        this.userId = userId;
    }

    /* ===== GETTERS & SETTERS ===== */

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public double getSalary() { return salary; }
    public double getAmount() { return amount; }
    public String getPurpose() { return purpose; }
    public String getStatus() { return status; }
    public Date getAppliedDate() { return appliedDate; }
    public int getUserId() { return userId; }

    public void setStatus(String status) { this.status = status; }
}
