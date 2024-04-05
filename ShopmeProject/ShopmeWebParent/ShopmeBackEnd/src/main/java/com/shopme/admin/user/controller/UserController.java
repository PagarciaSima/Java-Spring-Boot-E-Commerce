package com.shopme.admin.user.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.user.UserNotFoundException;
import com.shopme.admin.user.UserService;
import com.shopme.admin.user.util.exporter.UserCsvExporter;
import com.shopme.admin.user.util.exporter.UserExcelExporter;
import com.shopme.admin.user.util.exporter.UserPdfExporter;
import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;	

@Controller
public class UserController {
	
	private final static Logger LOG = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	private UserService service;
	
	// Handler method to show the list of users
	@GetMapping("/users")
	public String listAll(Model model) {
		return listByPage(1, model, "firstName", "asc", null);
	}
	
	// Handler method to show the list of users with pagination
	@GetMapping("/users/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum,
			Model model,
			@RequestParam ("sortField") String sortField,
			@RequestParam ("sortDir") String sortDir,
			@RequestParam(name = "keyword", required = false) String keyword
	) {
		Page<User> page = service.listByPage(pageNum, sortField, sortDir, keyword);
		long startCount = (pageNum -1) * UserService.USERS_PER_PAGE + 1;
		long endCount = startCount + UserService.USERS_PER_PAGE - 1;
		if(endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
		List<User> listUsers = page.getContent();
		model.addAttribute("listUsers", listUsers);
		model.addAttribute("totalItems",  page.getTotalElements());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("keyword", keyword);
		return "users/users";
	}
	
	// Handler method to show the new user register form
	@GetMapping("/users/new")
	public String newUser(Model model){
		List<Role> listRoles = service.listRoles();
		User user = new User();
		user.setEnabled(true);
		model.addAttribute("user", user);
		model.addAttribute("listRoles", listRoles);
		model.addAttribute("pageTitle", "Create New User");
		return "users/user_form";
	}
	
	// Handler method to submit the register form
	@PostMapping("/users/save")
	public String saveUser(@ModelAttribute User user, 
			RedirectAttributes redirectAttributes, 
			@RequestParam("image") MultipartFile multipartFile) throws IOException {
		
		String message;
	    
	    if (user.getId() == null) {
	        message = "The new user has been created.";
	    } else {
	        message = "User information has been updated.";
	    }
		
		if(!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			user.setPhotos(fileName);
			User savedUser = service.save(user);
			LOG.info("New user saved.");
			String uploadDir = "user-photos/" + savedUser.getId();
			// Delete any previous user image before saving another one
			FileUploadUtil.removeDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);		
		} else {
			if(user.getPhotos().isEmpty())
				user.setPhotos(null);
			service.save(user);
		}
		
		redirectAttributes.addFlashAttribute("message", message);
		return redirectUrlToAffectedUser(user);
	}

	private String redirectUrlToAffectedUser(User user) {
		String email = user.getEmail();
		return "redirect:/users/page/1?sortField=id&sortDir=asc&keyword=" + email;
	}
	
	// Handler method to show edit form
	@GetMapping("/users/edit/{id}")
	public String editUser(@PathVariable (name = "id") Integer id, 
			Model model,
			RedirectAttributes redirectAttributes) {
		try {
			User user = service.getUser(id);
			List<Role> listRoles = service.listRoles();
			model.addAttribute("user", user);
			model.addAttribute("listRoles", listRoles);
			model.addAttribute("pageTitle", "Edit User (ID " + id + ")");
			return "users/user_form";
		} catch (UserNotFoundException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
			return "redirect:/users";
		}
	}
	
	// Handler method to delete user
	@GetMapping("/users/delete/{id}")
	public String deleteUser(@PathVariable (name = "id") Integer id,
			Model model,
			RedirectAttributes redirectAttributes) {
		try {
			service.delete(id);
			FileUploadUtil.removeDir("user-photos/" + id);
			redirectAttributes.addFlashAttribute("message", "The user ID " + id + " has been deleted successfully.");
		}catch(UserNotFoundException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/users";
	}
	
	// Handler method to enable / disable user
	@GetMapping("/users/{id}/enabled/{status}")
	public String updateUserEnabledStatus(@PathVariable (name = "id") Integer id, 
			@PathVariable ("status") boolean  enabled,
			RedirectAttributes redirectAttributes) {
		service.updateUserEnabledStatus(id, enabled);
		String status = enabled ? "enabled" : "disabled";
		String message = "The user with ID " + id + " has been " + status + ".";
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/users";
	}
	
	// Handler method to export csv
	@GetMapping("/users/export/csv")
	public void exportToCSV(HttpServletResponse response) throws IOException {
		List<User> listUsers = service.listAll();
		UserCsvExporter exporter = new UserCsvExporter();
		exporter.export(listUsers, response);
	}
	
	// Handler method to export Excel
	@GetMapping("/users/export/excel")
	public void exportToExcel(HttpServletResponse response) throws IOException {
		List<User> listUsers = service.listAll();
		UserExcelExporter exporter = new UserExcelExporter ();
		exporter.export(listUsers, response);
	}
	
	// Handler method to export Excel
	@GetMapping("/users/export/pdf")
	public void exportToPdf(HttpServletResponse response) throws IOException {
		List<User> listUsers = service.listAll();
		UserPdfExporter exporter = new UserPdfExporter ();
		exporter.export(listUsers, response);
	}
}
