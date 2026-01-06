package com.lms.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.lms.service.UserService;

/**
 * CustomUserService implementation for Spring Security.
 * This class fetches user data from the database via UserService and 
 * maps it to Spring Security's UserDetails format.
 */
public class CustomUserService implements UserDetailsService {
	
	// Logger instance for this class using Log4j
	private static final Logger logger = LogManager.getLogger(CustomUserService.class);

	private UserService userService;

	/**
	 * Injects the UserService bean via setter injection.
	 * Required for database operations.
	 */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

	/**
	 * Overridden method to load user details by their email (username).
	 * Used by the Authentication Manager during the login process.
	 * * @param username The email entered by the user.
	 * @return UserDetails containing credentials and roles.
	 * @throws UsernameNotFoundException if no user is found with the provided email.
	 */
	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		
		// Log the entry point of authentication
		logger.info("Authentication attempt for email: {}", username);
		
		// Fetch the user object from our database service
		com.lms.model.User user = userService.getUserByEmail(username);
		
		// Validation check if user exists
		if(user == null) {
			logger.error("Authentication failed: User with email [{}] not found.", username);
			throw new UsernameNotFoundException("User Not Found: " + username);
		}
		
		// Prepare the list of granted authorities (Roles)
		List<GrantedAuthority> authorities = new ArrayList<>();
		
		// Check if user has a role assigned to prevent NullPointerExceptions
		if (user.getRole() != null) {
			String roleName = "ROLE_" + user.getRole().name();
			authorities.add(new SimpleGrantedAuthority(roleName));
			
			// Debug logging helps developers see what roles are being applied
			logger.debug("Successfully assigned role: {} to user: {}", roleName, username);
		} else {
			logger.warn("User [{}] logged in but has no assigned role.", username);
		}
		
		logger.info("User [{}] successfully loaded from database.", username);
		
		// Construct and return the Spring Security User object
		return new User(
			user.getEmail(), 
			user.getPassword(), 
			authorities
		);
	}
}