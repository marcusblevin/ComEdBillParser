/**
 * This program parses ComEd files and splits out the interval data based on customer account/premise/meter map
 * 
 * Author:		Marcus Levin
 * Date:		July 27, 2018
 * Dependencies:
 * 		Gson-2.6.2
 * 		Apache Commons Email 1.5
 * 		Apache Commons Lang 3.7
 * 		Apache POI 3.17
 * 		JavaxMail
 */
package graphet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.mail.HtmlEmail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class mainRun {
	
	private static String[] interval_48 = {"0:30","1:00","1:30","2:00","2:30","3:00","3:30","4:00","4:30","5:00","5:30","6:00","6:30","7:00","7:30","8:00","8:30","9:00","9:30",
										  "10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30","14:00","14:30","15:00","15:30","16:00","16:30","17:00","17:30","18:00",
										  "18:30","19:00","19:30","20:00","20:30","21:00","21:30","22:00","22:30","23:00","23:30","24:00"};
	private static String[] interval_96 = {"0:15","0:30","0:45","1:00","1:15","1:30","1:45","2:00","2:15","2:30","2:45","3:00","3:15","3:30","3:45","4:00","4:15","4:30","4:45",
										  "5:00","5:15","5:30","5:45","6:00","6:15","6:30","6:45","7:00","7:15","7:30","7:45","8:00","8:15","8:30","8:45","9:00","9:15","9:30",
										  "9:45","10:00","10:15","10:30","10:45","11:00","11:15","11:30","11:45","12:00","12:15","12:30","12:45","13:00","13:15","13:30","13:45",
										  "14:00","14:15","14:30","14:45","15:00","15:15","15:30","15:45","16:00","16:15","16:30","16:45","17:00","17:15","17:30","17:45","18:00",
										  "18:15","18:30","18:45","19:00","19:15","19:30","19:45","20:00","20:15","20:30","20:45","21:00","21:15","21:30","21:45","22:00","22:15",
										  "22:30","22:45","23:00","23:15","23:30","23:45","24:00"};
	
	public static void main(String[] args) {
		// get list of all files to process
		String curDir 								= System.getProperty("user.dir");
		File folder 								= new File(curDir);
		List<File> files							= listFilesInFolder(folder);
		BufferedReader br							= null;
		String line									= "";
		List<String> l								= null;
		HashMap<String, List<List<String>>> map 	= new HashMap<String, List<List<String>>>();
		Date lmDate									= null;
		String lmDateFormat							= "";
		SimpleDateFormat sdf						= new SimpleDateFormat("yyyy-MM-dd");
		
		Integer intervals		= 0;
		Long lm 				= null;
		String account 			= "";
		String premise 			= "";
		String meter 			= "";
		String key 				= "";

		// track all files and folders processed for each customer for later emailing
		HashMap<String, List<String>> filesProcessed 	= new HashMap<String, List<String>>();
		HashMap<String, List<String>> foldersProcessed 	= new HashMap<String, List<String>>();
		
		// get mapping of premise/account/meter numbers to customer
		Customer[] customerMap = getAccountMap("customerMap.json");
		System.out.println("Customer Meter Map: "+customerMap.length);
		
		// step through files and process
		for (File file : files) {
			System.out.println("Processing: "+file.getName());
			
			lm 				= file.lastModified();
			lmDate			= new Date(lm);
			lmDateFormat	= sdf.format(lmDate);
			
			
			try {
				br = new BufferedReader(new FileReader(file.getPath()));
				
				/* 
				 * Need to know how many intervals there are for saving to a file
				 * First line of file is the column headers in order
				 * 		Account, Premise, Meter, Date, Interal_01, Interval_02, etc
				 * Last column in row would be Interval_N where N is number of intervals
				 */
				if ((line = br.readLine()) != null) {
					l = new LinkedList<String>(Arrays.asList(line.split(",")));
					intervals = Integer.parseInt(l.get(l.size()-1).split("_")[1]);
				}
				System.out.println("Intervals: "+intervals);

				
				while ((line = br.readLine()) != null) {
					l = new LinkedList<String>(Arrays.asList(line.split(",")));
					
					account = l.get(0);
					premise = l.get(1);
					meter	= l.get(2);
					
					key 	= account + "_" + premise + "_" + meter;
					
					l.remove(0); // remove account
					l.remove(0); // remove premise
					l.remove(0); // remove meter
					
					if (!map.containsKey(key)) {
						List<List<String>> nl = new ArrayList<List<String>>();
						map.put(key, nl);
					}
					map.get(key).add(l);
				}
				
				System.out.println("Mapped Meters: "+map.size());
				
				// reset variables for reuse
				account = premise = meter = "";
				
				
				// iterate through map of meters, matching up with customer and write data to files 
				for (Map.Entry<String, List<List<String>>> entry : map.entrySet()) {
					String k 				= entry.getKey(); 		// [account]_[premise]_[meter]
					List<List<String>> val 	= entry.getValue();		// [date], [Interval_01], ..., etc
					
					String[] tmp 	= k.split("_");
					account 		= tmp[0];
					premise 		= tmp[1];
					meter 			= tmp[2];


					List<String> columns = new ArrayList<String>();
					columns.add("Date");
					
					if (intervals == 48) {
						// 30 minute interval data
						columns.addAll(Arrays.asList(interval_48));
					} else if (intervals == 96) {
						// 15 minute interval data
						columns.addAll(Arrays.asList(interval_96));
					}
					
					Customer cus 		= getCustomerFolder(customerMap, account, premise);
					String cusName		= "";
					String fileName 	= k+"_"+lmDateFormat;
					String fileLocation	= ""; // would dump files in current run directory
					
					if (cus == null) {
						// didn't match to customer mapping
						System.out.println("Could not find matching map for Account: "+account+", Premise: "+premise+", Meter: "+meter+". Placing in Unmatched directory.");
						
						fileLocation 	= "Unmatched\\"+lmDateFormat+"\\";
						cusName 		= "Unmatched";
					} else {
						// found matching customer and the folder name
						// write values to files in customer folders
						fileLocation 	= "\\\\phoenix\\ComedCustomers\\"+cus.getFolder()+"\\Data\\Utilities\\Electric Interval\\"+lmDateFormat+"\\";
						cusName 		= cus.getName();
						
						// check if the meter is mapped to the customer
						// if not, add the Unmatched string to the end of the filename
						Account[] acc = cus.getMap();
						for (Account a : acc) {
							if (!a.getMeters().contains(meter)) {
								fileName += " Unmatched";
							}
						}
					}
					checkFolderExists(fileLocation); // check if the directory exists already
					//fileName += ".xlsx"; // add file extension
					fileName += ".csv"; // add file extension

					// map all files and folders created for each client
					if (!filesProcessed.containsKey(cusName))  						{  filesProcessed.put(cusName, new ArrayList<String>());  }
					if (!foldersProcessed.containsKey(cusName)) 					{  foldersProcessed.put(cusName, new ArrayList<String>());	}
					if (!filesProcessed.get(cusName).contains(file.getName())) 		{  filesProcessed.get(cusName).add(file.getName());	}
					if (!foldersProcessed.get(cusName).contains(fileLocation)) 		{  foldersProcessed.get(cusName).add(fileLocation);	}
					
					
					fileLocation = checkFileExists(fileLocation+fileName);
					
					// write file to network location or sub-folder					
					//new ExcelWriter(fileLocation, columns, val);
					new CSVWriter(fileLocation, columns, val);
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close(); // close file 
						
						moveFile(file); // move file to Processed folder
						map.clear(); // clear hashmap for next file
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		} // end stepping through files
		
		System.out.println("Done processing files.");
		
		
		System.out.println("Send emails for RT tickets");
		
		for (Map.Entry<String, List<String>> entry : foldersProcessed.entrySet()) {
			String cusName				= entry.getKey(); 		
			List<String> folderNames	= entry.getValue();
			String fileList				= "";
			String folderList			= "";
			List<String> fileNames 		= filesProcessed.get(cusName); // keys should match
			
			for (String f : fileNames) {
				fileList += "<li>"+f+"</li>";
			}
			
			for (String f : folderNames) {
				folderList += "<li>"+f+"</li>";
			}
			
			try {
				
				HtmlEmail email = new HtmlEmail();
				email.setHostName("192.168.84.25");
				email.setSmtpPort(25);
				email.addTo("mlevin@graphet.com", "Marcus Levin");
				email.addTo("data@graphet.com", "RT");
				email.addTo("rrao@graphet.com", "Ramesh Rao");
				email.addTo("ruthann@graphet.com", "Ruthann Rao");
				email.setFrom("noreply@graphet.com", "Graphet, Inc");
				email.setSubject(cusName+" Electric Interval Data");
				
				email.setHtmlMsg("<html>Customer "+cusName+" has new Electric Interval data waiting to be imported. Files are located at: <br><ul>"+folderList+"</ul><br>"+
								 "Raw data files used:<ul>"+fileList+"</ul></html>");

				email.send();
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		System.out.println("Done sending emails");
	}
	
	/**
	 * List all CSV files in a folder
	 * @param File folder
	 * @return List<File>
	 */
	public static List<File> listFilesInFolder(final File folder) {
		List<File> files = new ArrayList<File>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				// listFilesInFolder(fileEntry); // recursively list files in sub-folders
			} else if (fileEntry.getName().contains(".csv")) {
				files.add(fileEntry);
			}
		}
		return files;
	}
	
	/**
	 * read in JSON file of what Account/Premise/Meter numbers apply to each customer
	 * @param fname
	 * @return
	 */
	public static Customer[] getAccountMap(String fname) {
		Customer[] m = null;
		
		try {
			Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ArrayAdapterFactory()).create();
			BufferedReader br 	= new BufferedReader(new FileReader(fname));
			String line 		= "";
			String json			= "";
			
			while ((line = br.readLine()) != null) {
				json += line.trim();
			}
			br.close();
			
			// convert JSON string to array of objects
			m = gson.fromJson(json, Customer[].class);
			
		} catch (FileNotFoundException e) {
			System.out.println("Cannot find "+fname+". Please provide a valid file name/location:");
			
			Scanner s 		= new Scanner(System.in);
			String newFName = s.next();
			s.close();
			
			return getAccountMap(newFName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return m;
	}
	
	/**
	 * Search the Customer Map to find the matching index based on Account/Premise combination
	 * @param cMap
	 * @param premise
	 * @param account
	 * @return
	 */
	public static Customer getCustomerFolder(Customer[] cMap, String account, String premise) {
		
		for (Customer c : cMap) {
			Account[] accounts = c.getMap();
			
			for (Account a : accounts) {
				if (a.getAccount().equals(account) && a.getPremise().equals(premise)) {
					return c;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * move file into the sub-directory "Processed" and check directory exists
	 * @param file
	 */
	public static void moveFile(File file) {
		checkFolderExists("Processed");
		file.renameTo(new File("Processed/"+file.getName()));
	}
	
	/**
	 * check if directory exists already, create if not
	 * @param folderName
	 */
	public static void checkFolderExists(String folderName) {
		File dir = new File(folderName);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}
	
	/**
	 * Check if file exists, if not increment file count and check using recursion 
	 * @param fileLocation
	 * @return String
	 */
	public static String checkFileExists(String fileLocation) {
		File fc = new File(fileLocation);
		if (!fc.exists()) {
			return fileLocation;
		} else {
			System.out.println("File "+fileLocation+" already exists");
			return checkFileExists(fileLocation, 1);
		}
	}
	
	/**
	 * Check if file exists, if not increment file count and check using recursion
	 * @param fileLocation
	 * @param i
	 * @return String
	 */
	public static String checkFileExists(String fileLocation, Integer i) {
		// check to see if the file exists already
		String fname 	= fileLocation.replace(".", " ("+i+").");
		File fc 		= new File(fname);
		if (fc.exists()) {
			System.out.println("File "+fname+" already exists");
			i++;
			return checkFileExists(fileLocation, i);
		} else {
			return fname;
		}
	}
}