package com.shopme.admin.category;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.shopme.common.entity.Category;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class CategoryServiceTest {
	
	@MockBean
	private CategoryRepository repo;
	
	@InjectMocks
	private CategoryService service;
	
	@Test
	public void testCheckUniqueInNewModeReturnDuplicateName() {
		Integer id = null;
		String name = "Computers";
		String alias = "abc";
		Category category = new Category(id, name, alias);
		Mockito.when(repo.findByName(name)).thenReturn(category);
		Mockito.when(repo.findByAlias(alias)).thenReturn(null);
		String result = service.checkUnique(id, name, alias);
		assertThat(result.equals("DuplicatedName"));
	}
	
	@Test
	public void testCheckUniqueInNewModeReturnDuplicateAlias() {
		Integer id = null;
		String alias = "Computers";
		String name = "abc";
		Category category = new Category(id, name, alias);
		Mockito.when(repo.findByName(name)).thenReturn(null);
		Mockito.when(repo.findByAlias(alias)).thenReturn(category);
		String result = service.checkUnique(id, name, alias);
		assertThat(result.equals("DuplicatedAlias"));
	}
	
	@Test
	public void testCheckUniqueInNewModeReturnOk() {
		Integer id = 1;
		String alias = "Computers";
		String name = "Computers";
		Category category = new Category(id, name, alias);
		Mockito.when(repo.findByName(name)).thenReturn(category);
		Mockito.when(repo.findByAlias(alias)).thenReturn(category);
		String result = service.checkUnique(id, name, alias);
		assertThat(result.equals("OK"));
	}
	
	@Test
	public void testCheckUniqueInEditModeReturnDuplicateName() {
		Integer id = 1;
		String name = "Computers";
		String alias = "abc";
		Category category = new Category(2, name, alias);
		Mockito.when(repo.findByName(name)).thenReturn(category);
		Mockito.when(repo.findByAlias(alias)).thenReturn(null);
		String result = service.checkUnique(id, name, alias);
		assertThat(result.equals("DuplicatedName"));
	}
	
	@Test
	public void testCheckUniqueInEditModeReturnDuplicateAlias() {
		Integer id = 1;
		String alias = "abc";
		String name = "Computers";
		Category category = new Category(2, name, alias);
		Mockito.when(repo.findByName(name)).thenReturn(category);
		Mockito.when(repo.findByAlias(alias)).thenReturn(null);
		String result = service.checkUnique(id, name, alias);
		assertThat(result.equals("DuplicatedName"));
	}
	
	@Test
	public void testCheckUniqueInEditModeReturnOk() {
		Integer id = 1;
		String alias = "Computers";
		String name = "Computers";
		Category category = new Category(id, name, alias);
		Mockito.when(repo.findByName(name)).thenReturn(category);
		Mockito.when(repo.findByAlias(alias)).thenReturn(category);
		String result = service.checkUnique(id, name, alias);
		assertThat(result.equals("OK"));
	}
}
