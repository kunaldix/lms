package com.lms.model;



import java.sql.Date;

import com.lms.constant.Gender;
import com.lms.constant.GovtIdType;
import com.lms.constant.Occupation;

public class Account {
	
	private String accountNo;
	
	private User user;
	
	private String image;
	
	private GovtIdType govtIdType;
	
	private String govtIdNumber;
	
	private String govtIdUrl;
	
	private Date dob;
	
	private Gender gender;
	
	private String nationality;
	
	private Occupation occupation;
	
	private String monthlyIncome;
	
	private Branch branch;
	
	private String address;
	
	private String country;
	
	private String state;
	
	private String city;
	
	private String pinCode;

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public GovtIdType getGovtIdType() {
		return govtIdType;
	}

	public void setGovtIdType(GovtIdType govtIdType) {
		this.govtIdType = govtIdType;
	}

	public String getGovtIdNumber() {
		return govtIdNumber;
	}

	public void setGovtIdNumber(String govtIdNumber) {
		this.govtIdNumber = govtIdNumber;
	}

	public String getGovtIdUrl() {
		return govtIdUrl;
	}

	public void setGovtIdUrl(String govtIdUrl) {
		this.govtIdUrl = govtIdUrl;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public Occupation getOccupation() {
		return occupation;
	}

	public void setOccupation(Occupation occupation) {
		this.occupation = occupation;
	}

	public String getMonthlyIncome() {
		return monthlyIncome;
	}

	public void setMonthlyIncome(String monthlyIncome) {
		this.monthlyIncome = monthlyIncome;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPinCode() {
		return pinCode;
	}

	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}

	@Override
	public String toString() {
		return "Account [accountNo=" + accountNo + ", user=" + user + ", image=" + image + ", govtIdType=" + govtIdType
				+ ", govtIdNumber=" + govtIdNumber + ", govtIdUrl=" + govtIdUrl + ", dob=" + dob + ", gender=" + gender
				+ ", nationality=" + nationality + ", occupation=" + occupation + ", monthlyIncome=" + monthlyIncome
				+ ", branch=" + branch + ", address=" + address + ", country=" + country + ", state=" + state
				+ ", city=" + city + ", pinCode=" + pinCode + "]";
	}
	
	
}
