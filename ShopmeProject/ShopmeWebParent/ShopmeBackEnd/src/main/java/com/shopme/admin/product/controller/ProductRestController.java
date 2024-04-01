package com.shopme.admin.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopme.admin.product.ProductService;

@RestController
public class ProductRestController {
	
	@Autowired 
	private ProductService service;
	
	@PostMapping("/products/check_unique")
	public String checkUnique(@RequestParam (name = "id", required = false) Integer id,
			@RequestParam(name = "name" , required = false) String name) {
		return service.checkUnique(id, name);
	}
}
