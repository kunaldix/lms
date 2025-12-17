package com.lms.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import com.lms.model.User;

public interface UserService {
	
	public String passwordDigest(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException;
	
	public void saveUser(User user);
	
	public Optional<User> getUserById(int userId);
	
	public User getUserByEmail(String email);
	
	public List<User> getAllUsers();
	
	public List<User> getAllAdmins();
	
	public void updatePassword(int id, String newPassword);
	
//	public void createAccount(Account acc);
	
}
