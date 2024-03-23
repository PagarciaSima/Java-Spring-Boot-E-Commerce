package com.shopme.admin.category;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.mysql.cj.log.LogFactory;
import com.shopme.admin.AbstractExporter;
import com.shopme.common.entity.Category;

public class CategoryCsvExporter extends AbstractExporter{
	
	private static final Logger log = LoggerFactory.getLogger(CategoryCsvExporter.class);
	
	public void export(List<Category> listCategories, HttpServletResponse response) throws IOException {
		super.setResponseHeader(response,"text/csv" , ".csv", "Categories_");
		
		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter()
				, CsvPreference.STANDARD_PREFERENCE);
		String [] csvHeader = {"Category ID", "Category name"};
		String [] fieldMapping = {"id", "name"};
		csvWriter.writeHeader(csvHeader);
		
		 listCategories.forEach(category -> {
	            category.setName(category.getName().replace("--", "  "));
	            try {
					csvWriter.write(category, fieldMapping);
				} catch (IOException e) {
					log.error("Error while mapping csv fields");
				}
	        });
		csvWriter.close();
			
	}
}
