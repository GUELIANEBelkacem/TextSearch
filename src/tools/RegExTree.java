package tools;

import java.util.ArrayList;

public class RegExTree {
	  public int root;
	  public ArrayList<RegExTree> subTrees;
	  
	  
	  public RegExTree(int root, ArrayList<RegExTree> subTrees) {
	    this.root = root;
	    this.subTrees = subTrees;
	  }
	  
	  
	  public String printTree(String space, int num, String d) {
		   String s  = space + rootToString()+"("+num+d+") ";
		   if(this.subTrees.size() == 1) s+="\n|" + this.subTrees.get(0).printTree(space, num+1,"M");
		   if(this.subTrees.size() == 2) s+="\n"+space+"/   \\ \n" + this.subTrees.get(0).printTree(space+"   ", num+1,"L") + "   "+ this.subTrees.get(1).printTree(space+"   ",num+1,"R")+"\n";
		  
		   return s;
		  
	  }
	  //FROM TREE TO PARENTHESIS
	  public String toString() {
		  
	    if (subTrees.isEmpty()) return rootToString();
	    String result = rootToString()+"("+subTrees.get(0).toString();
	    for (int i=1;i<subTrees.size();i++) result+=","+subTrees.get(i).toString();
	    return result+")";
	  }
	  
	  
	  
	  public String rootToString() {
	    if (root==RegEx.CONCAT) return ".";
	    if (root==RegEx.ETOILE) return "*";
	    if (root==RegEx.ALTERN) return "|";
	    if (root==RegEx.DOT) return ".";
	    return Character.toString((char)root);
	  }
}