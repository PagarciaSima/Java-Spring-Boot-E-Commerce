package com.shopme.admin.brand.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopme.admin.brand.BrandNotFoundException;
import com.shopme.admin.brand.BrandNotFoundRestException;
import com.shopme.admin.brand.BrandService;
import com.shopme.admin.brand.CategoryDto;
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;

@RestController
public class BrandRestController {
	
	@Autowired
	private BrandService service;
	
	@PostMapping("/brands/check_unique")
	public String checkUnique(@RequestParam (name = "id", required = false) Integer id
			, @RequestParam("name") String name) {
		return service.checkUnique(id, name);
	}
	
	@GetMapping("/brands/{id}/categories")
	public List<CategoryDto> listCategoriesByBrand(@PathVariable(name = "id") Integer brandId) 
			throws BrandNotFoundRestException{
		
		List <CategoryDto> listCategories = new ArrayList<>();
		try {
			Brand brand = service.get(brandId);
			Set<Category> categories = brand.getCategories();
			for(Category category : categories) {
				CategoryDto dto = new CategoryDto(category.getId(), category.getName());
				listCategories.add(dto);
			}
			return listCategories;
		} catch (BrandNotFoundException e) {
			throw new BrandNotFoundRestException();
		}
	}
}
