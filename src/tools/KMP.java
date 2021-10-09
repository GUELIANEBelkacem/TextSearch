package tools;

import java.util.ArrayList;

public class KMP {

	public String text;
	public String exp = "";
	public Integer[] CarryOver ;
	public ArrayList<Integer> foundIndex = new ArrayList<Integer>(); 
	
	
	public KMP(String exp) throws Exception {
		//this.text = text;
		this.exp = exp;
		this.CarryOver = new Integer[exp.length()];
		createCO();
		optimizeCO();
	
	}
	
	public void createCO() throws Exception {
		CarryOver[0] = -1;
		for(int i=1; i<exp.length(); i++) {
			ArrayList<String> suffexes = findSuffix(exp, i);
			String s = "";
			for(String suffix : suffexes) {
				if(isPreffix(exp, suffix)) s = suffix;
			}
			CarryOver[i] = s.length();
		}
	}
	
	public void optimizeCO() {
		for(int i=0; i<exp.length(); i++) {
			if(CarryOver[i] != -1 && exp.charAt(i) == exp.charAt(CarryOver[i]))
				CarryOver[i] = CarryOver[CarryOver[i]];
		}
	}
	
	public ArrayList<Integer> search(String text){
		this.text = text;
		foundIndex.clear();
		if(text.isEmpty()) return new ArrayList<Integer>();
		searchUtil(0, 0);
		return foundIndex;
	}
	public boolean searchUtil(int posexp, int postxt) {
		
		if((postxt == (text.length()-1) && (posexp != (exp.length()-1) || exp.charAt(posexp) != text.charAt(postxt)))) 
			return false;
		
		if(exp.charAt(posexp) == text.charAt(postxt)) {
			if(posexp == (exp.length()-1)) {
				foundIndex.add(postxt - posexp);
				if(postxt == (text.length()-1)) return false;
				else return searchUtil(0, postxt+1);
			}
			else return searchUtil(posexp+1, postxt+1);
		}else {
			
			return searchUtil(0, postxt-CarryOver[posexp]);
		}
		
	}
	
	public ArrayList<String> findSuffix(String s, int pos){
		ArrayList<String> list = new ArrayList<String>();
		StringBuilder combin = new StringBuilder();
		for(int i = pos-1; i>0; i--) {
			combin.insert(0,  s.charAt(i));
			list.add(combin.toString());
		}
		return list;
	
	}
	public boolean isPreffix(String s, String suffix) throws Exception {
		if(suffix.length()>s.length()) throw new Exception("suffix is bigger than expression");
		for(int i = 0; i<suffix.length(); i++ ) {
			if(s.charAt(i) != suffix.charAt(i)) return false;
		}
		return true;
	}
	
	
	
	public void printKMP() {
		String s1 = "";
		String s2 = "";
		for(int i =0; i< exp.length(); i++) {
			int j = CarryOver[i];
			s2 += j+" ";
			if(j==-1 || j>9) s1 += exp.charAt(i)+"  ";
			else s1 += exp.charAt(i)+" ";
			
		}
		System.out.println(s1);
		System.out.println(s2);
	}
	
	
	
	
	
	
	
	
	
	
	
	/*
	
	public static void main(String[] args) throws Exception {
		KMP kmp = new KMP("is a");
		kmp.createCO();
		kmp.optimizeCO();
		kmp.printKMP();
		//boolean found = kmp.search();
		System.out.println("line: "+kmp.text);
		System.out.println("word: "+kmp.exp);
		//System.out.println("found: "+found);
		
	}
	*/
}
