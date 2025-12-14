package com.lms.model;

public class Branch {
	
	private int id;
	
	private String branchCode;
	
	private String branchName;
	
	private String address;
	
	private String city;
	
	private String state;
	
	private String country;
	
	private String ifscCode;
	
	

	public Branch() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Branch(int id, String branchCode, String branchName, String address, String city, String state,
			String country, String ifscCode) {
		super();
		this.id = id;
		this.branchCode = branchCode;
		this.branchName = branchName;
		this.address = address;
		this.city = city;
		this.state = state;
		this.country = country;
		this.ifscCode = ifscCode;
	}



	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	@Override
	public String toString() {
		return "Branch [id=" + id + ", branchCode=" + branchCode + ", branchName=" + branchName + ", address=" + address
				+ ", city=" + city + ", state=" + state + ", country=" + country + ", ifscCode=" + ifscCode + "]";
	}
	
	
}
