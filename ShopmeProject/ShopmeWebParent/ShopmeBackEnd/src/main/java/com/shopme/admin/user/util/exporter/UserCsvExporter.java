package com.shopme.admin.user.util.exporter;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.shopme.common.entity.User;

public class UserCsvExporter extends AbstractExporter{
	
	public void export(List<User> listUsers, HttpServletResponse response) throws IOException {
		super.setResponseHeader(response,"text/csv" , ".csv");
		
		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter()
				, CsvPreference.STANDARD_PREFERENCE);
		String [] csvHeader = {"User ID", "E-mail", "First Name", "Last Name", "Roles", "Enabled"};
		writeCsvBody(listUsers, csvWriter);
		csvWriter.writeHeader(csvHeader);
		csvWriter.close();
			
	}

	private void writeCsvBody(List<User> listUsers, ICsvBeanWriter csvWriter) throws IOException {
		String[] fieldMapping = {"id", "email", "firstName", "lastName", "roles", "enabled"};
		
		for(User user : listUsers) {
			csvWriter.write(user, fieldMapping);
		}
		csvWriter.close();
	}
}
