package com.shopme.admin.brand;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.shopme.admin.category.CategoryService;
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;

@Controller
public class BrandController {
	
	@Autowired
	private BrandService service;
	
	@Autowired
	private CategoryService categoryService;
	
	// Handler method to list all brands
	@GetMapping("/brands")
	public String listAll(Model model) {
		List<Brand> listBrands = service.listAll();
		model.addAttribute("listBrands" ,listBrands);
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
}