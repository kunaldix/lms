package com.lms.model;

public class UserLoanDocuments {
	
	private int id;
	private User user;
	private String PhotoUploaded;
    private String SalarySlipUploaded;
    private String ItrUploaded;
    private String BankStatementUploaded;
    private String AadharUploaded;
    private String PanUploaded;
    
	public UserLoanDocuments() {
		super();
		// TODO Auto-generated constructor stub
	}
	public UserLoanDocuments(int id, User user, String photoUploaded, String salarySlipUploaded, String itrUploaded,
			String bankStatementUploaded, String aadharUploaded, String panUploaded) {
		super();
		this.id = id;
		this.user = user;
		PhotoUploaded = photoUploaded;
		SalarySlipUploaded = salarySlipUploaded;
		ItrUploaded = itrUploaded;
		BankStatementUploaded = bankStatementUploaded;
		AadharUploaded = aadharUploaded;
		PanUploaded = panUploaded;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getPhotoUploaded() {
		return PhotoUploaded;
	}
	public void setPhotoUploaded(String photoUploaded) {
		PhotoUploaded = photoUploaded;
	}
	public String getSalarySlipUploaded() {
		return SalarySlipUploaded;
	}
	public void setSalarySlipUploaded(String salarySlipUploaded) {
		SalarySlipUploaded = salarySlipUploaded;
	}
	public String getItrUploaded() {
		return ItrUploaded;
	}
	public void setItrUploaded(String itrUploaded) {
		ItrUploaded = itrUploaded;
	}
	public String getBankStatementUploaded() {
		return BankStatementUploaded;
	}
	public void setBankStatementUploaded(String bankStatementUploaded) {
		BankStatementUploaded = bankStatementUploaded;
	}
	public String getAadharUploaded() {
		return AadharUploaded;
	}
	public void setAadharUploaded(String aadharUploaded) {
		AadharUploaded = aadharUploaded;
	}
	public String getPanUploaded() {
		return PanUploaded;
	}
	public void setPanUploaded(String panUploaded) {
		PanUploaded = panUploaded;
	}
    
    
	
}
