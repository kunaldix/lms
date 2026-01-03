package com.lms.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.lms.model.User;
import com.lms.repository.UserRepository;
import com.lms.service.UserService;

public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    
    private BCryptPasswordEncoder passwordEncoder;

    public void setPasswordEncoder(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public String passwordDigest(String input) {
        return passwordEncoder.encode(input);
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

    @Override
    public List<User> getAllCustomers() {
        // TODO Auto-generated method stub
        return userRepository.getAllCustomers();
    }
}
