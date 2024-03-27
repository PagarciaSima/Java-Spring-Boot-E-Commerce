package com.shopme.admin.brand.controller;

import java.io.IOException;
import java.util.List;

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
import com.shopme.admin.brand.BrandNotFoundException;
import com.shopme.admin.brand.BrandService;
import com.shopme.admin.category.CategoryService;
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;

@Controller
public class BrandController {
	
	@Autowired
	private BrandService brandService;
	
	@Autowired
	private CategoryService categoryService;
	
	// Handler method to list all brands
	@GetMapping("/brands")
	public String listAll(Model model) {
		return listByPage(1, model, "name", "asc", null);
	}
	
	// Handler method to list by page
	@GetMapping("/brands/page/{pageNum}")
	public String listByPage(
			@PathVariable (name = "pageNum") int pageNum,
			Model model,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@RequestParam(name = "keyword" , required = false) String keyword) {
		
		Page<Brand> page = brandService.listByPage(pageNum, sortField, sortDir, keyword);
		List<Brand> listBrands = page.getContent();
		
		long startCount = (pageNum - 1) * BrandService.BRANDS_PER_PAGE + 1;
		long endCount = startCount + BrandService.BRANDS_PER_PAGE - 1;
		if(endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("keyword", keyword);
		model.addAttribute("listBrands", listBrands);
		return "brands/brands";
	}
	
	// Handler method to show new brand form
	@GetMapping("/brands/new")
	public String newBrand(Model model) {
		List<Category> listCategories = categoryService.listCategoriesUsedInForm();
		model.addAttribute("listCategories", listCategories);
		model.addAttribute("brand", new Brand());
		model.addAttribute("pageTitle", "Create new brand");

		return "brands/brand_form";
	}
	
	// Handler method to save (create / edit) brand
	@PostMapping("/brands/save")
	public String saveBrand(@ModelAttribute Brand brand, 
			@RequestParam ("fileImage") MultipartFile multipartFile, 
			RedirectAttributes redirectAttributes ) throws IOException {
		
		String message;
		if (brand.getId() == null) {
	        message = "The new brand has been created.";
	    } else {
	        message = "Brand information has been updated.";
	    }
		if( !multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			brand.setLogo(fileName);
			
			Brand savedBrand = brandService.save(brand);
			String uploadDir = "brand-logos/" + savedBrand.getId();
			FileUploadUtil.removeDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}else {
			brandService.save(brand);
		}
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/brands";
	}
	
	// Handler method to show edit brand form
	@GetMapping("/brands/edit/{id}")
	public String editBrand(@PathVariable(name = "id") Integer id, 
			Model model, 
			RedirectAttributes redirectAttributes) {
		try {
			Brand brand = brandService.get(id);
			List<Category> listCategories = categoryService.listCategoriesUsedInForm();
			model.addAttribute("brand", brand);
			model.addAttribute("listCategories", listCategories);
			model.addAttribute("pageTitle", "Edit Brand (ID: " + id + ")");
			return "brands/brand_form";
		} catch (BrandNotFoundException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/brands";
	}
	
	// Handler method to delete brand
	@GetMapping("/brands/delete/{id}")
	public String deleteBrand (@PathVariable (name = "id") Integer id,
		Model model,
		RedirectAttributes redirectAttributes){
		try {
			brandService.delete(id);
			String brandDir = "/brand-logos/" + id;
			FileUploadUtil.removeDir(brandDir);
			redirectAttributes.addFlashAttribute("message", "The brand ID " + id + " has been deleted successfully");
		}catch (BrandNotFoundException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/brands";
	}
}
