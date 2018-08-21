package graphet;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class CSVWriter {
	private String				fileName		= null;
	private List<String>		columns 		= null; // column headers
	private List<List<String>> 	rows 			= null;
	
	public CSVWriter(String f, List<String> c, List<List<String>> r) {
		fileName 	= f;
		columns 	= c;
		rows 		= r;
		
		try {
			PrintWriter pw 		= new PrintWriter(new File(fileName));
			StringBuilder sb	= new StringBuilder();
			
			for (int i=0; i<columns.size(); i++) {
				sb.append(columns.get(i));
				if (i < columns.size()-1)  	{  sb.append(',');  }
				else  						{  sb.append('\n');  }
			}
			
			for (List<String> list : rows) {
				for (int i=0; i<list.size(); i++) {
					sb.append(list.get(i));
					if (i < list.size()-1)  {  sb.append(',');  }
				}
				sb.append('\n');
			}
			
			pw.write(sb.toString());
			pw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}