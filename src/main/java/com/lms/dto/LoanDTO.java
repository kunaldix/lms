package com.lms.dto;

public class LoanDTO {

	private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private double amount;
    private String purpose;
    private String status;

    public LoanDTO(int id, String firstName, String lastName,
                   String phone, double amount,
                   String purpose, String status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.amount = amount;
        this.purpose = purpose;
        this.status = status;
    }

    // Getters & Setters
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public double getAmount() { return amount; }
    public String getPurpose() { return purpose; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
	
}
