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
	
	public List<User> listAll() {
		return (List<User>) userRepository.findAll();
	}
	
	public Page<User> listByPage(int pageNumber, String sortField, String sortDir) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending()
;		Pageable pageable = PageRequest.of(pageNumber - 1, USERS_PER_PAGE, sort);
		return userRepository.findAll(pageable);
	}
	
	public List<Role> listRoles() {
		return (List<Role>) roleRepository.findAll();
	}

	public User save(User user) {
		boolean isUpdating = (user.getId() != null);
		
		if (isUpdating) {
			User existingUser = userRepository.findById(user.getId()).get();
			
			if (user.getPassword().isEmpty()) {
				user.setPassword(existingUser.getPassword());
			}
			else {
				encodePassword(user);
			}
		}
		else {
			encodePassword(user);
		}
		
		return userRepository.save(user);
	}
	
	private void encodePassword(User user) {
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
	}
	
	public boolean isUniqueEmail(Integer id, String email) {
		User userByEmail = userRepository.getUserByEmail(email);
		
		if (userByEmail == null) {
			return true;
		}
		
		boolean isCreatingNew = (id == null);
		
		if (isCreatingNew) {
			if (userByEmail != null) {
				return false;
			}
		}
		else {
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
