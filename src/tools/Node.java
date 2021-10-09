package tools;

import java.util.ArrayList;

public class Node {

	
	
	public int label;
	public ArrayList<Node> children;
	
	
	public Node(int label) {
		this.label = label;
		children = new ArrayList<Node>();
		
		
	}
	
	
	
}

