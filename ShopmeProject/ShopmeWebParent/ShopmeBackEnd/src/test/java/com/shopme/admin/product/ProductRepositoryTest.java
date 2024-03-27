package com.shopme.admin.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;
import com.shopme.common.entity.Product;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class ProductRepositoryTest {
	
	@Autowired
	private ProductRepository repo;
	@Autowired
	private TestEntityManager entityManager;
	
	@Test
	public void testCreateProduct() {
		Brand brand = entityManager.find(Brand.class, 1);
		Category category = entityManager.find(Category.class, 5);
		Product product = new Product();
		product.setName("Acer Aspire Desktop");
		product.setAlias("Acer_Aspire_Desktop");
		product.setShortDescription("Short description for Acer Aspire Desktop");
		product.setFullDescription("Full description Acer Aspire Desktop");
		product.setBrand(brand);
		product.setCategory(category);
		product.setPrice(678);
		product.setCost(600);
		product.setEnabled(true);
		product.setInStock(true);
		product.setCreatedTime(new Date());
		product.setCreatedTime(new Date());

		Product savedProduct = repo.save(product);
		assertThat(savedProduct).isNotNull();
		assertThat(savedProduct.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testListAllProducts() {
		Iterable<Product> iterableProducts= repo.findAll();
		iterableProducts.forEach(System.out :: println);
	}
	
	@Test
	public void testGetProduct() {
		Integer id = 2;
		Optional<Product> findById = repo.findById(id);
		System.out.println(findById.get());
		assertThat(findById).isNotNull();
	}
	
	@Test
	public void testUpdateProduct() {
		Integer id = 1;
		Product product = repo.findById(id).get();
		product.setPrice(499);
		repo.save(product);
		Product updatedProduct = entityManager.find(Product.class, id);
		assertThat(updatedProduct.getPrice()).isEqualTo(499);
	}
	
	@Test
	public void testDeleteProduct() {
		Integer id = 3;
		repo.deleteById(id);
		Optional<Product> result = repo.findById(id);
		assertThat(!result.isPresent());
		
	}
}
