package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopme.common.entity.Category;

@Service
@Transactional
public class CategoryService {
	
	@Autowired
	private CategoryRepository categoryRepo;
	public static final int ROOT_CATEGORIES_PER_PAGE = 4;

	public List<Category> listByPage(CategoryPageInfo pageInfo, int pageNum, String sortDir,
			String keyword) {
		Sort sort = Sort.by("name");
		if(sortDir.equals("asc"))
			sort = sort.ascending();
		else
			sort = sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum -1, ROOT_CATEGORIES_PER_PAGE, sort);
		
		Page<Category> pageCategories = null;
		if(keyword != null && !keyword.isEmpty()) {
			pageCategories = categoryRepo.search(keyword, pageable);
		}else {
			pageCategories = categoryRepo.findRootCategories(pageable);
		}
		
		List<Category> rootCategories = pageCategories.getContent();
		
		pageInfo.setTotalElements(pageCategories.getTotalElements());
		pageInfo.setTotalPages(pageCategories.getTotalPages());
		
		if(keyword != null && !keyword.isEmpty()) {
			List<Category> searchResult = pageCategories.getContent();
			for(Category category : searchResult) {
				category.setHasChildren(category.getChildren().size() > 0);
			}
			return searchResult;
		}else {
			return listHierarchicalCategories(rootCategories, sortDir);
		}
		
	}
	
	public List<Category> listHierarchicalCategories(List<Category> rootCategories, String sortDir){
		List<Category> hierarchicalCategories = new ArrayList<> ();
		for (Category rootCategory : rootCategories) {
			hierarchicalCategories.add(Category.copyFull(rootCategory));
			Set<Category> children = sortSubcategories(rootCategory.getChildren(), sortDir);
			for(Category subcategory : children) {
				String name = "-- " + subcategory.getName();
				hierarchicalCategories.add(Category.copyFull(subcategory, name));
				listSubHierarchicalCategories(hierarchicalCategories, subcategory, 1, sortDir);
			}
		}
		return hierarchicalCategories;
	}
	
	private void listSubHierarchicalCategories(
			List<Category> hierarchicalCategories, Category parent, int subLevel, String sortDir
			
	) {
		Set<Category> children = sortSubcategories (parent.getChildren(), sortDir);
		int newSubLevel = subLevel + 1;
		for(Category subcategory : children) {
			String name = "";
			for(int i = 0; i < newSubLevel; i ++) {
				name += "--";
			}
			name += " " + subcategory.getName();
			hierarchicalCategories.add(Category.copyFull(subcategory, name));
			listSubHierarchicalCategories(hierarchicalCategories, subcategory, newSubLevel, sortDir);
		}
	}
	
	
	public Category save(Category category) {
		Category parent = category.getParent();
		if(parent != null) {
			String allParentIds = parent.getAllParentIDs() == null ? "-" : parent.getAllParentIDs();
			allParentIds += String.valueOf(parent.getId()) + "-";
			category.setAllParentIDs(allParentIds);
		}
		return categoryRepo.save(category);
	}
	
	public Category getCategoryById(Integer id) throws CategoryNotFoundException {
		return categoryRepo.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException ("Could not find any category with ID " + id));
	}
	
	public List<Category> listCategoriesUsedInForm(){
		List<Category> categoriesUSedInForm = new ArrayList<Category>();
		Iterable<Category> categoriesInDb = categoryRepo.findAll(Sort.by("name").ascending());
		for(Category category : categoriesInDb) {
			if(category.getParent() == null) {
				categoriesUSedInForm.add(Category.copyIdAndName(category));
				Set<Category> children = sortSubcategories(category.getChildren());
				for(Category subcategory: children) {
					String name = "-- " + subcategory.getName();
					categoriesUSedInForm.add(Category.copyIdAndName(subcategory.getId(), name));
					listSubCategoriesUsedInForm(categoriesUSedInForm, subcategory, 1);
				}
			}
		}
			return categoriesUSedInForm;
	}
	
	private void listSubCategoriesUsedInForm(List<Category> categoriesUSedInForm, Category parent, int subLevel) {
		int newSubLevel = subLevel + 1;
		Set<Category> children = sortSubcategories(parent.getChildren());
		
		for(Category subcategory: children) {
			String name = "";
			for(int i = 0; i < newSubLevel; i ++) {
				name += "--";
			}
			name += " " +  subcategory.getName();
			categoriesUSedInForm.add(Category.copyIdAndName(subcategory.getId(), name));
			listSubCategoriesUsedInForm(categoriesUSedInForm, subcategory, newSubLevel);
		}
	}
	
	public String checkUnique(Integer id, String name, String alias) {
		boolean isCreatingNew = (id == null || id == 0);
		Category categoryByName = categoryRepo.findByName(name);
		if(isCreatingNew) {
			if(categoryByName != null) {
				return "DuplicatedName";
			}else {
				Category categoryByAlias = categoryRepo.findByAlias(alias);
				if(categoryByAlias != null) {
					return "DuplicatedAlias";
				}
			}
		} else {
			if (categoryByName != null && categoryByName.getId() != id) {
				return "DuplicatedName";
			}
			Category categoryByAlias = categoryRepo.findByAlias(alias);
			if (categoryByAlias != null && categoryByAlias.getId() != id) {
				return "DuplicatedAlias";
			}
		}
		return "OK";
	}
	private SortedSet<Category> sortSubcategories(Set<Category> children) {
		return sortSubcategories (children, "asc");
	}
	
	private SortedSet<Category> sortSubcategories(Set<Category> children, String sortDir) {
	    SortedSet<Category> sortedChildren = new TreeSet<> (new Comparator<Category> () {

			@Override
			public int compare(Category cat1, Category cat2) {
				 if(sortDir.equals("asc")) {
			    	return cat1.getName().compareTo(cat2.getName());
			    } else {
			    	return cat2.getName().compareTo(cat1.getName());
			    }	       
			}
	    	
	    });	  
	    sortedChildren.addAll(children);
	    return sortedChildren;
	}
	
	public void updateCategoryEnabledStatus(Integer id, boolean enabled) {
		categoryRepo.updateEnabledStatus(id, enabled);
	}
	
	public void delete (Integer id) throws CategoryNotFoundException{
		Long countById = categoryRepo.countById(id);
		if(countById == null || countById == 0) {
			throw new CategoryNotFoundException("Could not find any category with ID " + id);
		}
		categoryRepo.deleteById(id);
	}
}
