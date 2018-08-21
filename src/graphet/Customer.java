package graphet;

public class Customer {
	
	private String 		name 		= "";
	private String 		folder		= "";
	private Account[] 	map 		= null;
	
	
	public Customer(String n, String f, Account[] m) {
		name 	= n;
		folder 	= f;
		map 	= m;
	}
	
	public String getName() {
		return name;
	}
	public String getFolder() {
		return folder;
	}
	public Account[] getMap() {
		return map;
	}
}