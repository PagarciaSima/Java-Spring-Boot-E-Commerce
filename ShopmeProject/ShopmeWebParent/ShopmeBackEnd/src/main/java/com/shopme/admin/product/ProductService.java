package com.shopme.admin.product;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopme.common.entity.Product;

@Service
@Transactional
public class ProductService {
	
	@Autowired
	private ProductRepository repo;
	
	public List<Product> listAll(){
		return (List<Product>)repo.findAll();
	}
	
	public Product save(Product product) {
		if (product.getId() == null) {
			product.setCreatedTime(new Date());
		}
		if(product.getAlias() == null || product.getAlias().isEmpty()) {
			String defaultAlias = product.getName().replace(" ", "-");
			product.setAlias(defaultAlias);
		}else {
			product.setAlias(product.getAlias().replace(" ", "-"));
		}
		product.setUpdatedTime(new Date());
		return repo.save(product);
	}
	
	public String checkUnique(Integer id, String name) {
		boolean isCreatingNew = (id == null || id == 0);
		Product productByName = repo.findByName(name);
		
		if(isCreatingNew) {
			if(productByName != null)
				return "Duplicated";
		} else {
			if(productByName != null && productByName.getId() != id)
				return "Duplicated";
		}
		return "OK";
	}
	
	public void updateProductEnabledStatus(Integer id, boolean enabled) {
		repo.updateEnabledStatus(id, enabled);
	}
}
