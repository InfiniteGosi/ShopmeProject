package com.shopme.admin.user.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.user.UserNotFoundException;
import com.shopme.admin.user.UserService;
import com.shopme.admin.user.export.UserCsvExporter;
import com.shopme.admin.user.export.UserExcelExporter;
import com.shopme.admin.user.export.UserPdfExporter;
import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UserController {
	@Autowired
	private UserService userService;
	
	@GetMapping("/users")
	public String listFirstPage(Model model) {
		return listByPage(1, model, "firstName", "asc", null);
	}
	
	@GetMapping("/users/page/{pageNum}")
	public String listByPage(
			@PathVariable(name = "pageNum") // Captures the page number from the URL (e.g., /users/page/2 means pageNum = 2).
			int pageNum, 
			Model model, 
			@Param("sortField") String sortField, // The field to sort by (e.g., firstName, email). This value is optional and can come from query parameters (e.g., ?sortField=firstName).
			@Param("sortDir") String sortDir,
			@Param("keyword") String keyword) {
		Page<User> page = userService.listByPage(pageNum, sortField, sortDir, keyword);
		List<User> listUser = page.getContent(); // Extracts the user list for the current page
		
		// If pageNum = 1 --> startCount = (1 - 1) * 4 + 1 = 1 (The first item on the current page.)
		//					  endCount = 1 + 4 - 1 = 4 (The last item on the current page.)
		// So we show the item 1 to 4 in the first page
		long startCount = (pageNum - 1) * UserService.USERS_PER_PAGE + 1;
		long endCount = startCount + UserService.USERS_PER_PAGE - 1;
		
		// Ensures endCount doesn’t exceed the total number of users in the database.
		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		
		// This is used to toggle sorting when the user clicks a column header in the view.
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItem", page.getTotalElements());
		model.addAttribute("listUser", listUser);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("keyword", keyword);
		
		return "users/users";
	}
	
	@GetMapping("/users/new")
	public String newUser(Model model) {
		List<Role> listRole = userService.listRoles();
		
		User user = new User();
		user.setEnabled(true);
		
		model.addAttribute("user", user);
		model.addAttribute("listRole", listRole);
		model.addAttribute("pageTitle", "Create new user");
		
		return "users/user_form";
	}
	
	@PostMapping("/users/save")
	public String saveUser(User user, 
			RedirectAttributes redirectAttributes, // Used to pass a flash message (temporary feedback) to the redirected page after the user is saved.
			// Captures the uploaded profile photo as a file. MultipartFile allows reading the file's content.
			@RequestParam("image") MultipartFile multipartFile ) throws IOException {
		// Ensures that the user uploaded a photo.
		if (!multipartFile.isEmpty()) {
			// Extracts the original file name and cleans it with to remove invalid characters.
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename()); 
			user.setPhoto(fileName);
			User savedUser = userService.save(user);
			
			String uploadDir = "user-photos/" + savedUser.getId();
			// Removes any existing files in the directory. This ensures only the new photo is stored.
			FileUploadUtil.cleanDir(uploadDir);
			// Save the uploaded photo to the server.
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}
		else { // If no photo was uploaded
			// If the photo field is empty, it sets photo to null (indicating no photo is stored)
			if (user.getPhoto().isEmpty()) {
				user.setPhoto(null);
			}
			userService.save(user);
		}
		// Adds a flash message to indicate success. Flash attributes are temporary and will be displayed on the redirected page.
		redirectAttributes.addFlashAttribute("message", "The user has been saved successfully!");
		
		return getRedirectURLtoAffectedUser(user);
	}

	private String getRedirectURLtoAffectedUser(User user) {
		String firstPartOfEmail = user.getEmail().split("@")[0];
		return "redirect:/users/page/1?sortField=id&sortDir=asc&keyword=" + firstPartOfEmail;
	}
	
	
	@GetMapping("/users/edit/{id}")
	public String editUser(
			@PathVariable(name = "id") Integer id, 
			Model model,
			RedirectAttributes redirectAttributes) {
		try {
			User user = userService.get(id);
			List<Role> listRole = userService.listRoles();
			
			model.addAttribute("user", user);
			model.addAttribute("listRole", listRole);
			model.addAttribute("pageTitle", "Edit user (ID: " + id + ")");
			
			return "users/user_form";
		}
		catch (UserNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return "redirect:/users";
		}
	}
	
	@GetMapping("/users/delete/{id}")
	public String deleteUser(@PathVariable(name = "id") Integer id, 
			Model model,
			RedirectAttributes redirectAttributes) {
		try {
			userService.delete(id);
			redirectAttributes.addFlashAttribute("message", "The user ID " + id + " has been deleted");
		}
		catch (UserNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
		}
		return "redirect:/users";
	}
	
	@GetMapping("/users/{id}/enabled/{status}")
	public String updateUserEnableStatus(@PathVariable("id") Integer id, 
			@PathVariable("status") boolean enabled,
			RedirectAttributes redirectAttributes) {
		userService.updateUserEnabledStatus(id, enabled);
		
		String status = enabled ? "enabled" : "disabled";
		String message = "The user ID " + id + " has been " + status;
		redirectAttributes.addFlashAttribute("message", message);
		
		return "redirect:/users";
	}
	
	@GetMapping("/users/export/csv")
	public void exportToCsv(HttpServletResponse response) throws IOException {
		List<User> listUser = userService.listAll();
		UserCsvExporter exporter = new UserCsvExporter();
		exporter.export(listUser, response);
	}
	
	@GetMapping("/users/export/excel") 
	public void exportToExcel(HttpServletResponse response) throws IOException {
		List<User> listUser = userService.listAll();
		UserExcelExporter exporter = new UserExcelExporter();
		exporter.export(listUser, response);
	}
	
	@GetMapping("/users/export/pdf") 
	public void exportToPDF(HttpServletResponse response) throws IOException {
		List<User> listUser = userService.listAll();
		UserPdfExporter exporter = new UserPdfExporter();
		exporter.export(listUser, response);
	}
}
