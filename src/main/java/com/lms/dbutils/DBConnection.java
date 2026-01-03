package com.lms.dbutils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	private static Connection con = null;
	
	private static final String URL = "jdbc:mysql://localhost:3306/lms";
	
	private static final String USERNAME = "root";
	
	private static final String PASSWORD = "root123";
	
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
	
	private DBConnection() {
		
		try {
			Class.forName(DRIVER);
			con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		}
		catch (SQLException | ClassNotFoundException e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static Connection getConnection() throws SQLException {
		if(con == null || con.isClosed()) {
			new DBConnection();
		}
		return con;
	}
}
