package com.lms.service.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import com.lms.model.User;
import com.lms.repository.UserRepository;
import com.lms.service.UserService;

public class UserServiceImpl implements UserService {

	private UserRepository userRepository;

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public String passwordDigest(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("md5");
		byte[] digest = md.digest(input.getBytes("UTF-8"));
		StringBuilder sb = new StringBuilder();
		for (byte b : digest) {
			sb.append(String.format("%02x", b));
		}

		return sb.toString();
	}

	@Override
	public void saveUser(User user) {
		// TODO Auto-generated method stub
		userRepository.saveUser(user);
	}

	@Override
	public Optional<User> getUserById(int userId) {
		// TODO Auto-generated method stub
		return userRepository.getUserById(userId);
	}

	@Override
	public List<User> getAllUsers() {
		// TODO Auto-generated method stub
		return userRepository.getAllUsers();
	}

	@Override
	public List<User> getAllAdmins() {
		// TODO Auto-generated method stub
		return userRepository.getAllAdmins();
	}

	@Override
	public User getUserByEmail(String email) {
		// TODO Auto-generated method stub
		return userRepository.getUserByEmail(email);
	}

	@Override
	public void updatePassword(int id, String newPassword) {
		// TODO Auto-generated method stub
		userRepository.updatePassword(id, newPassword);
	}

	

//	@Override
//	public void createAccount(Account acc) {
//		acc.setAccountNo(generateAccountNumber());
//		userRepository.createAccount(acc);
//	}

//	public String generateAccountNumber() {
//		long number = 1000000000L + (long) (random.nextDouble() * 9000000000L);
//		return String.valueOf(number);
//	}
}
