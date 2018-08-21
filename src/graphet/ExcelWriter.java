/**
 * Uses Apache POI library to write Excel file
 * Each cell is formatted as a string
 * 
 * Taken from: https://www.callicoder.com/java-write-excel-file-apache-poi/
 */
package graphet;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;


public class ExcelWriter {
	private String				fileName		= null;
	private List<String>		columns 		= null; // column headers
	private List<List<String>> 	rows 			= null;
	private SimpleDateFormat 	sdf				= new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
	
	public ExcelWriter(String f, List<String> c, List<List<String>> r) {
		fileName 	= f;
		columns 	= c;
		rows 		= r;
		
		try {
			// create a workbook
			Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for gerating '.xls' file
			
			// create a sheet
			Sheet sheet = workbook.createSheet("Sheet 1");
			
			// create header row
			Row headerRow = sheet.createRow(0);
			
			for (int i=0; i<columns.size(); i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns.get(i));
				
				if (i > 0) {
					CellStyle cs 		= workbook.createCellStyle();
					CreationHelper ch	= workbook.getCreationHelper();
					cs.setDataFormat(ch.createDataFormat().getFormat("h:mm"));
					cell.setCellStyle(cs);
				}
			}
			
			// write rows of values
			int rowNum = 1;
			for (List<String> list : rows) {
				Row row = sheet.createRow(rowNum++);
				
				for (int i=0; i<list.size(); i++) {
					
					if (NumberUtils.isCreatable(list.get(i))) {									// check if string is a number
						row.createCell(i).setCellValue(Double.parseDouble(list.get(i)));
					/*
					} else if (isValidDate(list.get(i))) {										// check if string is a date
						CellStyle cellStyle 		= workbook.createCellStyle();
						CreationHelper createHelper = workbook.getCreationHelper();
						cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("MM/dd/yyyy HH:mm:ss"));
						
						Cell cell = row.createCell(i);
						cell.setCellValue(sdf.parse(list.get(i)));
						cell.setCellStyle(cellStyle);
					*/
					} else {																	// default, uses Excel formulas to determine cell data type
						row.createCell(i).setCellValue(list.get(i));
					}
				}
			}
			
			
			// resize all columsn to fit the content size
			for (int i=0; i<columns.size(); i++) {
				sheet.autoSizeColumn(i);
			}
			
			
			// write output to a file
			FileOutputStream fos = new FileOutputStream(fileName);
			workbook.write(fos);
			fos.close();
			
			// close the workbook
			workbook.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * check if a string is actually a date in a known format
	 * taken from: https://stackoverflow.com/questions/33968333/how-to-check-if-a-string-is-date
	 * @param inDate
	 * @return
	 */
	private boolean isValidDate(String inDate) {
		//sdf.setLenient(false);
		try {
			sdf.parse(inDate.trim());
		} catch (ParseException pe) {
			return false;
		}
		return true;
	}
}