package com.shopme.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class UserRepositoryTests {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Test
	public void testCreateUserWithOneRole() {
		User user1 = new User("test@gmail.com", "123", "Khang", "Ho");
		Role roleAdmin = entityManager.find(Role.class, 1);
		user1.addRole(roleAdmin);
		User savedUser = userRepository.save(user1);
		
		assertThat(savedUser.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testCreateUserWithTwoRoles() {
		User user2 = new User("test1@gmail.com", "123", "Khanh", "Ho");
		Role roleEditor = new Role(3);
		Role roleAssistant = new Role(5);
		
		user2.addRole(roleEditor);
		user2.addRole(roleAssistant);
		
		User savedUser = userRepository.save(user2);
		
		assertThat(savedUser.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testListAllUsers() {
		Iterable<User> listUser = userRepository.findAll();
		listUser.forEach(user -> System.out.println(user));
	}
	
	@Test
	public void testGetUserById() {
		User user = userRepository.findById(1).get();
		System.out.println(user);
		assertThat(user).isNotNull();
	}
	
	@Test
	public void testUpdateUserDetails() {
		User user = userRepository.findById(1).get();
		user.setEnabled(true);
		user.setEmail("khangho@gmail.com");
		
		userRepository.save(user);
	}
	
	@Test
	public void testUpdateUserRoles() {
		User user = userRepository.findById(2).get();
		Role roleEditor = new Role(3);
		Role roleSalesperson = new Role(2);
		
		user.getRoles().remove(roleEditor);
		user.addRole(roleSalesperson);
		
		userRepository.save(user);
	}
	
	@Test
	public void testDeleteUser() {
		userRepository.deleteById(2);
	}
	
	@Test
	public void testGetUserByEmail() {
		String email = "johndoe@gmail.com";
		User user = userRepository.getUserByEmail(email);
		
		assertThat(user).isNotNull();
	}
	
	@Test
	public void testCountById() {
		Integer id = 1;
		Long count = userRepository.countById(id);
		
		assertThat(count).isNotNull().isGreaterThan(0);
	}
	
	@Test
	public void testDisableUser() {
		Integer id = 3;
		userRepository.updateEnabledStatus(id, false);
	}
	
	@Test
	public void testEnableUser() {
		Integer id = 3;
		userRepository.updateEnabledStatus(id, true);
	}
	
	@Test
	public void testListFirstPage() {
		int pageNumber = 1;
		int pageSize = 4;
		
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<User> page = userRepository.findAll(pageable);
		List<User> listUser = page.getContent();
		
		listUser.forEach(user -> System.out.println(user));
		
		assertThat(listUser.size()).isEqualTo(pageSize);
	}
}
