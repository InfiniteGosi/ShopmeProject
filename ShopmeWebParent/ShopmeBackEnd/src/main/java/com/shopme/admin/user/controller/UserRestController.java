package com.shopme.admin.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopme.admin.user.UserService;

// In this case we only check if the user email is unique, therefore we use @RestController
// to return data as JSON. While @Controller returns web pages

@RestController
public class UserRestController {
	@Autowired
	private UserService userService;
	
	@PostMapping("/users/check_email")
	public String checkDuplicateEmail(@Param("id") Integer id, @Param("email") String email) {
		return userService.isUniqueEmail(id, email) ? "OK" : "Duplicated";
	}
}
