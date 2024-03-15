package com.shopme.admin.user.util.exporter;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shopme.common.entity.User;

public class UserPdfExporter extends AbstractExporter{
	
	public void export(List<User> listUsers, HttpServletResponse response) throws IOException {
		
		super.setResponseHeader(response, "application/pdf", ".pdf");		
		Document document = new Document(PageSize.A4);
		PdfWriter.getInstance(document, response.getOutputStream());
		document.open();
	
	    addLogoToPdf(document);
        
		Font font = setTitleFont();
		Paragraph title = new Paragraph("List of users", font);
		title.setAlignment(Paragraph.ALIGN_CENTER);		
		document.add(title);
		
		PdfPTable table = new PdfPTable(6);
		table.setWidthPercentage(100f);
		table.setSpacingBefore(25);
		table.setWidths(new float[] {1.2f, 3.5f, 3.0f, 3.0f, 3.0f, 1.7f});
		writeTableHeader(table);
		writeTableData(table, listUsers);
		document.add(table);
		document.close();
	}

	private void writeTableData(PdfPTable table, List<User> listUsers) {
		for(User user: listUsers) {
			table.addCell(String.valueOf(user.getId()));
			table.addCell(user.getEmail());
			table.addCell(user.getFirstName());
			table.addCell(user.getLastName());
			table.addCell(user.getRoles().toString());
			table.addCell(String.valueOf(user.isEnabled()));
		}
	}

	private void writeTableHeader(PdfPTable table) {
		PdfPCell cell = new PdfPCell();
		cell.setBackgroundColor(new Color(96, 143, 186));
		cell.setPadding(5);
		Font font = setHeaderRowFont();
		
		cell.setPhrase(new Phrase("User ID", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("E-mail", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("First Name", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("Last Name", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("Roles", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("Enabled", font));
		table.addCell(cell);
	}

	private Font setHeaderRowFont() {
		Font font = FontFactory.getFont(FontFactory.HELVETICA);
		font.setSize(14);
		font.setColor(Color.WHITE);
		return font;
	}
	
	private Font setTitleFont() {
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(18);
		font.setColor(96, 143, 186);
		return font;
	}
	
	private void addLogoToPdf(Document document) throws IOException {
		// 1. Classpath Resource with Relative Path:
	    ClassPathResource imgFile = new ClassPathResource("static/images/ShopmeAdminSmall.png");
	    byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
	    Image image = Image.getInstance(bytes);
	    image.setAbsolutePosition(50, 760);
	    image.scaleAbsolute(80, 50);
	    document.add(image);
	}
}
