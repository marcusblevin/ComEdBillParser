package graphet;

import java.util.ArrayList;
import java.util.List;

public class Account {
	
	private String 			account 	= "";
	private String 			premise 	= "";
	private List<String> 	meters 		= new ArrayList<String>();
	
	public Account(String a, String p, List<String> m) {
		account = a;
		premise	= p;
		meters	= m;
	}
	
	public String getAccount() {
		return account;
	}
	public String getPremise() {
		return premise;
	}
	public List<String> getMeters() {
		return meters;
	}
}