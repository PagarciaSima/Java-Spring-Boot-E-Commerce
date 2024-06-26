package com.shopme.admin.product.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.shopme.admin.brand.BrandService;
import com.shopme.admin.category.CategoryService;
import com.shopme.admin.product.ProductNotFoundException;
import com.shopme.admin.product.ProductService;
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;
import com.shopme.common.entity.Product;
import com.shopme.common.entity.ProductImage;

@Controller
public class ProductController {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);
	
	@Autowired
	private ProductService productService;
	@Autowired
	private BrandService brandService;
	@Autowired
	private CategoryService categoryService;
	
	// Handler method to list products
	@GetMapping("/products")
	public String listFirstPage(Model model) {
		return listByPage(1, model, "name", "asc", null);
	}
	
	// Handler method to list by page
		@GetMapping("/products/page/{pageNum}")
		public String listByPage(
				@PathVariable (name = "pageNum") int pageNum,
				Model model,
				@RequestParam("sortField") String sortField,
				@RequestParam("sortDir") String sortDir,
				@RequestParam(name = "keyword" , required = false) String keyword) {
			
			Page<Product> page = productService.listByPage(pageNum, sortField, sortDir, keyword);
			List<Product> listProducts = page.getContent();
			List<Category> listCategories = categoryService.listCategoriesUsedInForm();
			
			long startCount = (pageNum - 1) * ProductService.PRODUCTS_PER_PAGE + 1;
			long endCount = startCount + ProductService.PRODUCTS_PER_PAGE - 1;
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
			model.addAttribute("listProducts", listProducts);
			model.addAttribute("listCategories", listCategories);
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
		model.addAttribute("numberOfExistingExtraImages", 0);

		return "products/product_form";
	}
	
	// Handler method to save new Product
	@PostMapping("/products/save")
	public String saveProduct(
			@ModelAttribute Product product,
			@RequestParam("fileImage") MultipartFile mainImageMultipartFile,
			@RequestParam("extraImage") MultipartFile[] extraImageMultiparts,
			@RequestParam(name = "detailIDs", required = false) String[] detailIDs,
			@RequestParam(name = "detailNames", required = false) String[] detailNames,
			@RequestParam(name = "detailValues", required = false) String[] detailValues,
			@RequestParam(name = "imageIDs", required = false) String[] imageIDs,
			@RequestParam(name = "imageNames", required = false) String[] imageNames,
			RedirectAttributes redirectAttributes
	) throws IOException {
		String message;
		if (product.getId() == null) {
	        message = "The new product has been created.";
	    } else {
	        message = "Product information has been updated.";
	    }
		setMainImageName(mainImageMultipartFile, product);
		setExistingExtraImageNames(imageIDs, imageNames, product);
		setNewExtraImageNames(extraImageMultiparts, product);	
		setProductDetails(detailNames, detailValues, detailIDs, product);
		
		Product savedProduct = productService.save(product);
		saveUploadedImages(mainImageMultipartFile, extraImageMultiparts, savedProduct);
		deleteExtraImagesWereRemovedOnForm(product);
		
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
	
	// Handler method to delete products
	@GetMapping("/products/delete/{id}")
	public String deleteProduct(@PathVariable(name = "id") Integer id,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		try {
			productService.delete(id);
			String productExtraImagesDir = "product-images/" + id + "/extras";
			String productImagesDir = "product-images/" + id ;
			FileUploadUtil.removeDir(productExtraImagesDir);
			FileUploadUtil.removeDir(productImagesDir);
			
			redirectAttributes.addFlashAttribute("message", 
					"The product ID " + id + " has been deleted successfully");
		}catch(ProductNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
		}
		
		return "redirect:/products";
	}
	
	// Handler method to get edit form
	@GetMapping("/products/edit/{id}")
	public String editProduct(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			Product product = productService.get(id);
			List<Brand> listBrands = brandService.listAll();
			Integer numberOfExistingExtraImages = product.getImages().size();
			
			model.addAttribute("product", product);
			model.addAttribute("pageTitle", "Edit Product (ID: " + id + ")");
			model.addAttribute("listBrands", listBrands);
			model.addAttribute("numberOfExistingExtraImages", numberOfExistingExtraImages);
			
			return "/products/product_form";
		} catch (ProductNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
			return "redirect/:products";
		}
	}
	
	// Handler method to access product details
	@GetMapping("/products/detail/{id}")
	public String viewProductDetails(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			Product product = productService.get(id);			
			model.addAttribute("product", product);
			return "/products/product_detail_modal";
		} catch (ProductNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
			return "redirect/:products";
		}
	}
	
	private void deleteExtraImagesWereRemovedOnForm(Product product) {
		String extraImageDir = "product-images/" + product.getId() + "/extras";
		
		Path dirPath = Paths.get(extraImageDir);
		
		try {
			Files.list(dirPath).forEach(file -> {
				String filename = file.toFile().getName();
				if (!product.containsImageName(filename)) {
					try {
						Files.delete(file);
						LOG.info("Deleted extra image: " + filename);
					}catch(IOException e) {
						LOG.error("Could not delete extra image: " + filename);
					}
				}
			});
		}catch (IOException ex) {
			LOG.error("Could list directory: " + dirPath);
		}
	}

	private void setExistingExtraImageNames(String[] imageIDs, String[] imageNames, Product product) {
		if(imageIDs == null || imageIDs.length == 0)
			return;
		
		Set<ProductImage> images = new HashSet<ProductImage> ();
		
		for (int count = 0; count < imageIDs.length; count++) {
			Integer id = Integer.parseInt(imageIDs[count]);
			String name = imageNames[count];
			images.add(new ProductImage(id, name, product));
		}
			
		product.setImages(images);
	}

	private void setProductDetails(String[] detailNames, String[] detailValues, 
			String[] detailIDs, Product product) {
		if(detailNames == null || detailNames.length == 0)
			return;
		for(int count = 0; count < detailNames.length; count ++) {
			String name = detailNames[count];
			String value = detailValues[count];
			Integer id = Integer.parseInt(detailIDs[count]);
			if (id != 0) {
				product.addDetail(id, name, value);
			}else if(!name.isEmpty() && !value.isEmpty()) {
				product.addDetail(name, value);
			}
		}	
	}

	private void saveUploadedImages(MultipartFile mainImageMultipartFile, MultipartFile[] extraImageMultiparts,
			Product savedProduct) throws IOException {
		if(!mainImageMultipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(mainImageMultipartFile.getOriginalFilename());
			String uploadDir = "product-images/" + savedProduct.getId();
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, mainImageMultipartFile);
		}
		if(extraImageMultiparts.length > 0) {
			for(MultipartFile multipartFile : extraImageMultiparts) {
				String uploadDir = "product-images/" + savedProduct.getId() + "/extras";
				if(multipartFile.isEmpty()) {
					continue;
				}
				String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
				FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
			}
		}
	}

	private void setNewExtraImageNames(MultipartFile[] extraImageMultiparts, Product product) {
		if(extraImageMultiparts.length > 0) {
			for(MultipartFile multipartFile : extraImageMultiparts) {
				if(!multipartFile.isEmpty()) {
					String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
					if(!product.containsImageName(fileName)) {
						product.addExtraImage(fileName);
					}
				}
			}
		}	
	}

	private void setMainImageName(MultipartFile mainImageMultipartFile, Product product) {
		if(!mainImageMultipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(mainImageMultipartFile.getOriginalFilename());
			product.setMainImage(fileName);
		}
	}
}
