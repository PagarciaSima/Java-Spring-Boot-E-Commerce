package com.shopme.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@DataJpaTest
// @AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class UserRepositoryTests {

	@Autowired
	UserRepository repo;
	@Autowired
	private TestEntityManager entityManager;

	@Test
	public void testCreateUserWithOneRole() {
		Role roleAdmin = entityManager.find(Role.class, 1);
		User pablo = new User("pablo@hotmail.com", "pablo1988", "Pablo", "García Simavilla");
		pablo.addRole(roleAdmin);
		User savedUser = repo.save(pablo);
		assertThat(savedUser.getId()).isGreaterThan(0);
	}

	@Test
	public void testCreateUserWithTwoRoles() {
		User angela = new User("angela@hotmail.com", "angela1988", "angela", "Llamas Delgado");
		Role editor = new Role(3);
		Role assistant = new Role(5);
		angela.addRole(editor);
		angela.addRole(assistant);
		User savedUser = repo.save(angela);
		assertThat(savedUser.getId()).isGreaterThan(0);
	}

	@Test
	public void testListAllUsers() {
		List<User> listUsers = (List<User>) repo.findAll();
		listUsers.forEach(user -> System.out.println("User: " + user));
	}

	@Test
	public void testGetUserById() {
		User pablo = repo.findById(4).orElseThrow(() -> new EntityNotFoundException("User not found for given id"));
		System.out.println(pablo);
		assertThat(pablo).isNotNull();
	}
	
	@Test
	public void testUpdateUserDetails() {
		User pablo = repo.findById(4).orElseThrow(() -> new EntityNotFoundException("User not found for given id"));
		pablo.setEnabled(true);
		pablo.setEmail("pablogs@hotmail.com");
		repo.save(pablo);
	}
	
	@Test
	public void testUpdateUserRoles() {
		User angela = repo.findById(5).get();
		Role assistant = new Role(5);
		Role salesPerson = new Role(2);
		angela.getRoles().remove(assistant);
		angela.getRoles().add(salesPerson);
		repo.save(angela);
	}
	
	@Test
	public void testDeleteUser() {
		Integer userId = 5;
		repo.deleteById(userId);
		repo.findById(userId);
	}
	
	@Test
	public void testGetUserByEmail() {
		String email = "pablogs@hotmail.com";
		User user = repo.getUserByEmail(email);
		assertThat(user).isNotNull();
	}
	
	@Test
	public void tesCountById() {
		Integer id = 6;
		Long count = repo.countById(id);
		assertThat(count).isNotNull().isGreaterThan(0);
	}
	
	@Test
	public void testDisableUser() {
		Integer id = 4;
		repo.updataEnabledStatus(id, false);
	}
	
	@Test
	public void testEnableUser() {
		Integer id = 4;
		repo.updataEnabledStatus(id, true);
	}
	
	@Test
	public void testListFirstPage() {
		int pageNumber = 0;
		int pageSize = 4;
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<User> page = repo.findAll(pageable);
		List<User> listUsers = page.getContent();
		listUsers.forEach(user -> System.out.println(user));
		assertThat(listUsers.size()).isEqualTo(pageSize);
	}
	
	@Test
	public void testSearchUsers() {
		String keyword = "Bruce";
		int pageNumber = 0;
		int pageSize = 4;
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<User> page = repo.findAll(keyword, pageable);
		List<User> listUsers = page.getContent();
		listUsers.forEach(user -> System.out.println(user));
		assertThat(listUsers.size()).isGreaterThan(0);
	}
}
