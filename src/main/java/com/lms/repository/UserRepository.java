package com.lms.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.lms.dbutils.DBConnection;
import com.lms.constant.Gender;
import com.lms.constant.GovtIdType;
import com.lms.constant.Occupation;
import com.lms.constant.Role;
import com.lms.model.Account;
import com.lms.model.Branch;
import com.lms.model.User;

public class UserRepository {

	public void saveUser(User user) {
		try (Connection conn = DBConnection.getConnection()){
			String name = user.getName();
			String email = user.getEmail();
			String password = user.getPassword();
			String phoneNumber = user.getPhoneNumber();
			Role role = user.getRole();

			String query = "Insert into users(name, email, password, phone_number, role) values(" + "?, ?, ?, ?, ?)";

			PreparedStatement stmt = conn.prepareStatement(query);

			stmt.setString(1, name);
			stmt.setString(2, email);
			stmt.setString(3, password);
			stmt.setString(4, phoneNumber);
			stmt.setString(5, role.name());

			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public Optional<User> getUserById(int userId) {
		User user = null;
		try (Connection conn = DBConnection.getConnection()){

			String query = "select * from users where id = ?";

			PreparedStatement stmt = conn.prepareStatement(query);

			stmt.setInt(1, userId);

			ResultSet rs = stmt.executeQuery();

			if (!rs.next())
				return Optional.of(null);

			user = new User();

			user.setName(rs.getString("name"));
			user.setEmail(rs.getString("email"));
			user.setId(rs.getInt("id"));
			user.setPassword(null);
			user.setPhoneNumber(rs.getString("phone_number"));
			user.setRole(rs.getString("role").equals("CUSTOMER") ? Role.CUSTOMER : Role.ADMIN);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return Optional.of(user);
	}

	public List<User> getAllUsers() {

		List<User> list = new ArrayList<>();

		try (Connection conn = DBConnection.getConnection()){

			String query = "select * from users";

			PreparedStatement stmt = conn.prepareStatement(query);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				User user = new User();

				user.setName(rs.getString("name"));
				user.setEmail(rs.getString("email"));
				user.setId(rs.getInt("id"));
				user.setPassword(null);
				user.setPhoneNumber(rs.getString("phone_number"));
				user.setRole(rs.getString("role").equals("CUSTOMER") ? Role.CUSTOMER : Role.ADMIN);

				list.add(user);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		return list;
	}

	public List<User> getAllAdmins() {
		List<User> list = new ArrayList<>();

		try (Connection conn = DBConnection.getConnection()){

			String query = "select * from users";

			PreparedStatement stmt = conn.prepareStatement(query);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				if (rs.getString("role").equals("ADMIN")) {
					User user = new User();

					user.setName(rs.getString("name"));
					user.setEmail(rs.getString("email"));
					user.setId(rs.getInt("id"));
					user.setPassword(null);
					user.setPhoneNumber(rs.getString("phone_number"));
					user.setRole(rs.getString("role").equals("CUSTOMER") ? Role.CUSTOMER : Role.ADMIN);

					list.add(user);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		return list;
	}

	public User getUserByEmail(String email) {

		User user = null;
		try (Connection conn = DBConnection.getConnection()){

			String query = "select * from users where email = ?";

			PreparedStatement stmt = conn.prepareStatement(query);

			stmt.setString(1, email);

			ResultSet rs = stmt.executeQuery();

			if (!rs.next())
				return null;

			user = new User();

			user.setName(rs.getString("name"));
			user.setEmail(rs.getString("email"));
			user.setId(rs.getInt("id"));
			user.setPassword(rs.getString("password"));
			user.setPhoneNumber(rs.getString("phone_number"));
			user.setRole(rs.getString("role").equals("CUSTOMER") ? Role.CUSTOMER : Role.ADMIN);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		return user;
	}

	public void createAccount(Account acc) {
		// TODO Auto-generated method stub

		String accNo = acc.getAccountNo();
		int userId = acc.getUser().getId();
		String image = acc.getImage();
		String govtIdType = acc.getGovtIdType().name();
		String govtIdNumber = acc.getGovtIdNumber();
		String govtIdUrl = acc.getGovtIdUrl();
		Date dob = acc.getDob();
		String gender = acc.getGender().name();
		String nationality = acc.getNationality();
		String occupation = acc.getOccupation().name();
		String monthlyIncome = acc.getMonthlyIncome();
		int branchId = acc.getBranch().getId();
		String address = acc.getAddress();
		String country = acc.getCountry();
		String state = acc.getState();
		String city = acc.getCity();
		String pinCode = acc.getPinCode();

		try (Connection conn = DBConnection.getConnection()){

			String query1 = "INSERT INTO account (account_no, user_id, image,govt_id_type, govt_id_number, govt_id_url, dob, gender, nationality, occupation, monthly_income, branch_id)"
					+ "values(?,?,?,?,?,?,?,?,?,?,?,?)";

			PreparedStatement stmt = conn.prepareStatement(query1);
			stmt.setString(1, accNo);
			stmt.setInt(2, userId);
			stmt.setString(3, image);
			stmt.setString(4, govtIdType);
			stmt.setString(5, govtIdNumber);
			stmt.setString(6, govtIdUrl);
			stmt.setDate(7, dob);
			stmt.setString(8, gender);
			stmt.setString(9, nationality);
			stmt.setString(10, occupation);
			stmt.setString(11, monthlyIncome);
			stmt.setInt(12, branchId);

			stmt.executeUpdate();

			String query2 = "INSERT INTO address(user_id, address_line, country, state, city, pincode) VALUES(?,?,?,?,?,?)";

			PreparedStatement stmt2 = conn.prepareStatement(query2);

			stmt2.setInt(1, userId);
			stmt2.setString(2, address);
			stmt2.setString(3, country);
			stmt2.setString(4, state);
			stmt2.setString(5, city);
			stmt2.setString(6, pinCode);

			stmt2.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public Account getAccountByUserId(int userId) {

		Account acc = null;

		try (Connection conn = DBConnection.getConnection()){

			String query = "SELECT a.account_no, a.image, a.govt_id_type, a.govt_id_number, a.govt_id_url, a.dob, a.gender, a.nationality, a.occupation, a.monthly_income, a.branch_id, "
					+ "ad.address_line, ad.country, ad.state, ad.city, ad.pincode "
					+ "FROM account a JOIN address ad ON a.user_id = ad.user_id WHERE a.user_id = ?";

			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setInt(1, userId);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				acc = new Account();

				acc.setAccountNo(rs.getString("account_no"));
				acc.setImage(rs.getString("image"));
				acc.setGovtIdType(GovtIdType.valueOf(rs.getString("govt_id_type")));
				acc.setGovtIdNumber(rs.getString("govt_id_number"));
				acc.setGovtIdUrl(rs.getString("govt_id_url"));
				acc.setDob(rs.getDate("dob"));
				acc.setGender(Gender.valueOf(rs.getString("gender")));
				acc.setNationality(rs.getString("nationality"));
				acc.setOccupation(Occupation.valueOf(rs.getString("occupation")));
				acc.setMonthlyIncome(rs.getString("monthly_income"));

				// Address
				acc.setAddress(rs.getString("address_line"));
				acc.setCountry(rs.getString("country"));
				acc.setState(rs.getString("state"));
				acc.setCity(rs.getString("city"));
				acc.setPinCode(rs.getString("pincode"));

				// Branch
				Branch br = new Branch();
				br.setId(rs.getInt("branch_id"));
				acc.setBranch(br);

				// User
				User u = new User();
				u.setId(userId);
				acc.setUser(u);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return acc;
	}
}
