package com.shopme.admin.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.brand.BrandService;
import com.shopme.admin.product.ProductService;
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Product;

@Controller
public class ProductController {
	
	@Autowired
	private ProductService productService;
	@Autowired
	private BrandService brandService;
	
	// Handler method to list products
	@GetMapping("/products")
	public String listAll(Model model) {
		List<Product> listProducts = productService.listAll();
		model.addAttribute("listProducts", listProducts);
		return "products/products";
	}
	
	// Handler method to show product registration form
	@GetMapping("/products/new")
	public String newProduct(Model model) {
		List<Brand> listBrands = brandService.listAll();
		Product product = new Product();
		product.setEnabled(true);
		product.setInStock(true);
		
		model.addAttribute("product", product);
		model.addAttribute("listBrands", listBrands);
		model.addAttribute("pageTitle", "Create New Product");

		return "products/product_form";
	}
	
	// Handler method to save new Product
	@PostMapping("/products/save")
	public String saveProduct(@ModelAttribute Product product,
			RedirectAttributes redirectAttributes) {
		String message;
		if (product.getId() == null) {
	        message = "The new product has been created.";
	    } else {
	        message = "Product information has been updated.";
	    }
		productService.save(product);
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/products";
	}
	
	// Handler method to set enabled status
	@GetMapping("/products/{id}/enabled/{status}")
	public String updateCategoryEnabledStatus(
		@PathVariable("id") Integer id, 
		@PathVariable ("status") boolean enabled,
		RedirectAttributes redirectAttributes
	) {
		productService.updateProductEnabledStatus(id, enabled);
		String status = enabled ? "enabled" : "disabled";
		String message = "The Product ID " + id + " has been " + status;
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/products";
	}
}
