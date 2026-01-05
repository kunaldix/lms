package com.lms.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.lms.dbutils.DBConnection;
import com.lms.constant.Role;
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
			user.setProfileImage(rs.getString("profile_image"));

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		return user;
	}

	public void updatePassword(int id, String newPassword) {
		
		try (Connection conn = DBConnection.getConnection()){
			
			String query = "UPDATE users SET PASSWORD = ? WHERE id = ?";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, newPassword);
			stmt.setInt(2, id);

			stmt.executeUpdate();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public List<User> getAllCustomers() {

        List<User> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            String query = "SELECT * FROM users";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                if ("CUSTOMER".equals(rs.getString("role"))) {

                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhoneNumber(rs.getString("phone_number"));
                    user.setPassword(null);
                    user.setRole(
                        "CUSTOMER".equals(rs.getString("role"))
                            ? Role.CUSTOMER
                            : Role.ADMIN
                    );

                    list.add(user);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    } 
}
