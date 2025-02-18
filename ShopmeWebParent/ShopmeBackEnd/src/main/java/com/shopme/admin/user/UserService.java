package com.shopme.admin.user;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService {
	public static final int USERS_PER_PAGE = 4;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public User getByEmail(String email) {
		return userRepository.getUserByEmail(email);
	}
	
	public List<User> listAll() {
		return (List<User>) userRepository.findAll(Sort.by("firstName").ascending());
	}
	
	public Page<User> listByPage(int pageNumber, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();		
		Pageable pageable = PageRequest.of(pageNumber - 1, USERS_PER_PAGE, sort);
		
		if (keyword != null) {
			return userRepository.findAll(keyword, pageable);
		}
		
		return userRepository.findAll(pageable);
	}
	
	public List<Role> listRoles() {
		return (List<Role>) roleRepository.findAll();
	}

	public User save(User user) {
		boolean isUpdating = (user.getId() != null);
		
		if (isUpdating) {
			User existingUser = userRepository.findById(user.getId()).get(); // Fetches the existing user by ID.
			
			if (user.getPassword().isEmpty()) { 
				user.setPassword(existingUser.getPassword()); // Retains the old password if the new password is empty.
			}
			else { 
				encodePassword(user); // Encrypts the password if a new one is provided.
			}
		}
		else { // If creating a new user
			encodePassword(user);
		}
		
		return userRepository.save(user);
	}
	
	public User updateAccount(User userInForm) {
		User userInDB = userRepository.findById(userInForm.getId()).get();
		
		if (!userInForm.getPassword().isEmpty()) {
			userInDB.setPassword(userInForm.getPassword());
			encodePassword(userInDB);
		}
		
		if (userInForm.getPhoto() != null) {
			userInDB.setPhoto(userInForm.getPhoto());
		}
		
		userInDB.setFirstName(userInForm.getFirstName());
		userInDB.setLastName(userInForm.getLastName());
		
		return userRepository.save(userInDB);
	}
	
	private void encodePassword(User user) {
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
	}
	
	public boolean isUniqueEmail(Integer id, String email) {
		User userByEmail = userRepository.getUserByEmail(email);
		
		// If no user exists with the given email, it's unique.
		if (userByEmail == null) {
			return true;
		}
		
		boolean isCreatingNew = (id == null);
		
		if (isCreatingNew) { // If creating a new user, the email must not already exist.
			if (userByEmail != null) {
				return false;
			}
		}
		else { // If updating, the email is unique if it belongs to the same user (id matches).
			if (userByEmail.getId() != id) {
				return false;
			}
		}
		
		return true;
	}

	public User get(Integer id) throws UserNotFoundException {
		try {
			return userRepository.findById(id).get();
		}
		catch (NoSuchElementException ex) {
			throw new UserNotFoundException("Could not find user with ID " + id);
		}
	}
	
	public void delete(Integer id) throws UserNotFoundException {
		Long count = userRepository.countById(id);
		
		if (count == null || count == 0) {
			throw new UserNotFoundException("Could not find user with ID " + id);
		}
		
		userRepository.deleteById(id);
	}
	
	public void updateUserEnabledStatus(Integer id, boolean enabled) {
		userRepository.updateEnabledStatus(id, enabled);
	}
} 
