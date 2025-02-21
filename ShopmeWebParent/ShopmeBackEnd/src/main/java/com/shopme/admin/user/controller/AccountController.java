package com.shopme.admin.user.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.security.ShopmeUserDetails;
import com.shopme.admin.user.UserService;
import com.shopme.common.entity.User;

@Controller
public class AccountController {
	@Autowired
	private UserService userService;
	
	@GetMapping("/account")
	public String viewDetails(@AuthenticationPrincipal ShopmeUserDetails loggedUser,
			Model model) {
		String email = loggedUser.getUsername();
		User user = userService.getByEmail(email);
		
		model.addAttribute("user", user);
		
		return "users/account_form";
	}
	
	@PostMapping("/account/update")
	public String saveDetails(User user, 
			RedirectAttributes redirectAttributes, 
			@AuthenticationPrincipal ShopmeUserDetails loggedUser,
			@RequestParam("image") MultipartFile multipartFile ) throws IOException {
		
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename()); 
			user.setPhoto(fileName);
			User savedUser = userService.updateAccount(user);
			
			String uploadDir = "user-photos/" + savedUser.getId();
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}
		else { // If no photo was uploaded
			// If the photo field is empty, it sets photo to null (indicating no photo is stored)
			if (user.getPhoto().isEmpty()) {
				user.setPhoto(null);
			}
			userService.updateAccount(user);
		}
		loggedUser.setFirstName(user.getFirstName());
		loggedUser.setLastName(user.getLastName());
		
		// Adds a flash message to indicate success. Flash attributes are temporary and will be displayed on the redirected page.
		redirectAttributes.addFlashAttribute("message", "Your account details have been updated");
		
		return "redirect:/account";
	}
}
