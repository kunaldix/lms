package com.lms.security;

import java.util.ArrayList;
import java.util.List;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.lms.service.UserService;


public class CustomUserService implements UserDetailsService {
	
	private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		
		com.lms.model.User user = userService.getUserByEmail(username);
		
		if(user == null) {
			throw new UsernameNotFoundException("User Not Found");
		}
		
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
		
		return new User(username, user.getPassword(), authorities);
	}
}

