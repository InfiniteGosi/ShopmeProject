package com.shopme.admin.user;

import java.util.List;
import java.io.IOException;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.shopme.common.entity.User;

import jakarta.servlet.http.HttpServletResponse;

public class UserCsvExporter extends AbstractExporter {
	public void export(List<User> listUser, 
			// The HTTP response to write the CSV file as an attachment.
			HttpServletResponse response) throws IOException {
		super.setResponseHeader(response, "text/csv", ".csv");
		
		// STANDARD_PREFERENCE specifies standard formatting for the CSV
		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
		
		// The column names that will appear in the CSV file.
		String[] csvHeader = {"User Id", "E-mail", "First Name", "Last Name", "Roles", "Enabled"};
		// Maps the fields of the User class to the corresponding columns in the CSV file.
		String[] fieldMapping = {"id", "email", "firstName", "lastName", "roles", "enabled"};
		
		csvWriter.writeHeader(csvHeader);
		
		for (User user : listUser) {
			csvWriter.write(user, fieldMapping);
		}
		
		csvWriter.close();
	}
}
