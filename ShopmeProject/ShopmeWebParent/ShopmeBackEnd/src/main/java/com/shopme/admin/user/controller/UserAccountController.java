package com.shopme.admin.user.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.security.ShopmeUserDetails;
import com.shopme.admin.user.UserNotFoundException;
import com.shopme.admin.user.UserService;
import com.shopme.common.entity.User;

@Controller
public class UserAccountController {
	
	@Autowired
	private UserService service;
	
	private static final Logger LOG = LoggerFactory.getLogger(UserAccountController.class);
	
	// Handler method to show account form
	@GetMapping("/account")
	public String viewDetails(@AuthenticationPrincipal ShopmeUserDetails loggedUser,
			Model model) {
		String email = loggedUser.getUsername();
		User user = service.getByEmail(email);
		model.addAttribute("user", user);
		return "users/account_form";
	}
	
	// Handler method to submit the account form
	@PostMapping("/account/update")
	public String saveDetails(
			@ModelAttribute User user, 
			RedirectAttributes redirectAttributes, 
			@AuthenticationPrincipal ShopmeUserDetails loggedUser,
			@RequestParam("image") MultipartFile multipartFile) throws IOException, UserNotFoundException {
		
		if(!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			user.setPhotos(fileName);
			User savedUser = service.updateAccount(user);
			LOG.info("New user saved.");
			String uploadDir = "user-photos/" + savedUser.getId();
			// Delete any previous user image before saving another one
			FileUploadUtil.removeDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);		
		} else {
			if(user.getPhotos().isEmpty())
				user.setPhotos(null);
			service.updateAccount(user);
		}
		
		loggedUser.setFirstName(user.getFirstName());
		loggedUser.setLastName(user.getLastName());
		
		redirectAttributes.addFlashAttribute("message", "The account details have been updated");
		return "redirect:/account";
	}

}
