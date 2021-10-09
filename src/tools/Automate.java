package tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Automate {
	
	public HashMap<Integer, String> 						opCodes 	  = new HashMap<Integer, String>();
	public HashMap<Integer, ArrayList<Transition>> 			nodes 		  = new HashMap<Integer, ArrayList<Transition>>();
	public HashMap<Integer, Integer[]> 						table 		  = new HashMap<Integer, Integer[]>();
	public HashMap<Integer, Integer[]> 						table2 		  = new HashMap<Integer, Integer[]>();
	public HashMap<Integer, ArrayList<Integer>> 			epsilonTable  = new HashMap<Integer, ArrayList<Integer>>();
	public HashMap<Integer, ArrayList<Integer>> 			targetLog     = new HashMap<Integer, ArrayList<Integer>>();
	public ArrayList<Integer> 								startTable 	  = new ArrayList<Integer>();
	public ArrayList<Integer> 								endTable   	  = new ArrayList<Integer>();
	public ArrayList<String> 								letters       = new ArrayList<String>();
	public HashMap<Character, Integer> 						letterIndex   = new HashMap<Character, Integer>();
	public ArrayList<Integer> 								done          = new ArrayList<Integer>();
	public ArrayList<Integer> 								terminals     = new ArrayList<Integer>();
	public Integer                                          initial       = -1;
	public Node data;
	
	public ArrayList<Integer> searchList = new ArrayList<Integer>();
	
	int postProcessStart;
	int postProcessEnd;
	
	int comletingNode;
	
	public String epsilon = "£";
	public String init = "init";
	public String terminal = "terminal";
	public String empty = "empty";
	public static int nodeCount = 0;
	
	
	
	
	
	

	public Automate(RegExTree data) throws Exception {
		if(data == null) {throw new Exception("tree is empty");}
		this.data = readData(data);
		processData(this.data);
		
		for(Entry<Integer, String> n : opCodes.entrySet()) {
			if(!n.getValue().equals("*") && !n.getValue().equals("|") && !n.getValue().equals(".") && !letters.contains(n.getValue())) letters.add(n.getValue());
		}
		
		
		constructTables();
		//printAutomate();
		optimizeTable2();
		//printTables();
		
	}
	
	
	
	
	
	
	
	
	public Node readData(RegExTree data) {
		int cur = nodeCount++;
		Node result = new Node(cur); 
		opCodes.put(cur, data.rootToString()); 
		for(RegExTree subData: data.subTrees) result.children.add(readData(subData));
		return result;
	}
	
	
	
	
	
	
	public Transition processData(Node node) {
		if (opCodes.get(node.label).equals("|")) {
				Transition left =  processData(node.children.get(0));
				Transition right =  processData(node.children.get(1));
				int start = nodeCount++;
				int end = nodeCount++;
				
				nodes.put(start, new ArrayList<Transition>());
				nodes.get(start).add(new Transition(start, epsilon, left.from));
				nodes.get(start).add(new Transition(start, epsilon, right.from));
				
				nodes.put(end, new ArrayList<Transition>());
				if(!nodes.containsKey(left.to)) nodes.put(left.to, new ArrayList<Transition>());
				if(!nodes.containsKey(right.to)) nodes.put(right.to, new ArrayList<Transition>());
				nodes.get(left.to).add(new Transition(left.to, epsilon, end));// problems
				nodes.get(right.to).add(new Transition(right.to, epsilon, end));
				
				postProcessStart = start;
				postProcessEnd = end;
				return new Transition(start, empty, end);
			
			
		}
		
		if (opCodes.get(node.label).equals(".")) {
				Transition left =  processData(node.children.get(0));
				Transition right =  processData(node.children.get(1));
				int start = nodeCount++;
				int end = nodeCount++;
				
				nodes.put(start, new ArrayList<Transition>());
				nodes.get(start).add(new Transition(start, epsilon, left.from));
				
				if(!nodes.containsKey(left.to)) nodes.put(left.to, new ArrayList<Transition>());
				nodes.get(left.to).add(new Transition(left.to, epsilon, right.from));
				
				nodes.put(end, new ArrayList<Transition>());
				if(!nodes.containsKey(right.to)) nodes.put(right.to, new ArrayList<Transition>());
				nodes.get(right.to).add(new Transition(right.to, epsilon, end));
				
				postProcessStart = start;
				postProcessEnd = end;
				return new Transition(start, empty, end);
		}
		
		if (opCodes.get(node.label).equals("*")) {
			
			Transition middle =  processData(node.children.get(0));
			
			int start = nodeCount++;
			int end = nodeCount++;
			
			nodes.put(start, new ArrayList<Transition>());
			nodes.put(end, new ArrayList<Transition>());
			if(!nodes.containsKey(middle.from)) nodes.put(middle.from, new ArrayList<Transition>());
			if(!nodes.containsKey(middle.to)) nodes.put(middle.to, new ArrayList<Transition>());
			
			nodes.get(middle.to).add(new Transition(middle.to, epsilon, middle.from));
			nodes.get(middle.to).add(new Transition(middle.to, epsilon, end));
			
			nodes.get(start).add(new Transition(start, epsilon, end));
			nodes.get(start).add(new Transition(start, epsilon, middle.from));
			
			postProcessStart = start;
			postProcessEnd = end;
			return new Transition(start, empty, end);
		}
		
			int start = nodeCount++;
			int end = nodeCount++;
			
			nodes.put(start, new ArrayList<Transition>());
			nodes.put(end, new ArrayList<Transition>());
			nodes.get(start).add(new Transition(start, opCodes.get(node.label), end));
			
			postProcessStart = start;
			postProcessEnd = end;
			return new Transition(start, empty, end);
		
	}
	
	
	public void constructTables() {
		
		for(Entry<Integer, ArrayList<Transition>> n : nodes.entrySet()) {
			Integer[] temp = new Integer[letters.size()];
			for(int i = 0; i<letters.size(); i++) {
				temp[i] = -1;
			}
			table.put(n.getKey(), temp);
			
			epsilonTable.put(n.getKey(), new ArrayList<Integer>());
			
			for(Transition t : n.getValue()) {
				if(t.value.equals(epsilon)) {
					epsilonTable.get(n.getKey()).add(t.to);
				}
				else {
					table.get(n.getKey())[letters.indexOf(t.value)] = t.to;
				}
			}
		}
		startTable.add(postProcessStart);
		endTable.add(postProcessEnd);
		
	}
	
	
	public void optimizeTable2() {
		
		terminals.add(postProcessEnd);
		initial = nodeCount;
		optimizeTableUtil(postProcessStart);
		
		for(Entry<Integer, Integer[]> n : table2.entrySet()) {
			for(int i = 0; i<letters.size();i++) { 
				for(Entry<Integer, ArrayList<Integer>> m : targetLog.entrySet()) {
					if(m.getValue().contains(n.getValue()[i])) n.getValue()[i] = m.getKey();  
					
				}
			}
		}
		ArrayList<Integer> newTerminals = new ArrayList<Integer>();
		for(Integer i : terminals) {
			if(table2.containsKey(i)) newTerminals.add(i);
		}
		terminals = newTerminals;
		int idx = 0;
		for(String s:letters) {
			letterIndex.put(s.charAt(0), idx);
			idx++;
		}
	}
	
	
	
	
	public void optimizeTableUtil(Integer entry) {
		ArrayList<Integer> targets = searchTargets2(entry);
		done.addAll(targets);
		
		Integer newNode = nodeCount++;
		targetLog.put(newNode, targets);
		Integer[] temp = new Integer[letters.size()];
		for(int i = 0; i<letters.size(); i++) {
			temp[i] = -1;
		}
		table2.put(newNode, temp);
		for(Integer i: targets) {
			for(int j = 0; j<letters.size();j++) {
				if(table.get(i)[j] != -1) { 
					table2.get(newNode)[j] = table.get(i)[j];
					
					
				}
			}
			
		}
		for(Entry<Integer, Integer[]> n : table2.entrySet()) {
			for(int j = 0; j<letters.size();j++) { 
				if(targets.contains(n.getValue()[j]))  n.getValue()[j]=newNode;
			}
			
		}
		
		if(isTerminal(newNode, targets)) terminals.add(newNode);
		ArrayList<Integer> neighbours = searchNeighbours2(targets);
		for(Integer i: neighbours) {
		    if(!done.contains(i))  optimizeTableUtil(i);
		}

		
	}
	
	
	
	
	public boolean isTerminal(Integer node, ArrayList<Integer> targets) {
		if(terminals.contains(node)) return false;
		
		for(Integer i: targets) {
			if(terminals.contains(i)) return true;
			for(Entry<Integer, ArrayList<Integer>> m : targetLog.entrySet()) {
				if(m.getValue().contains(i) && terminals.contains(m.getKey())) return true;  
				
			}
			
		}
		return false;
	}
	
	
	
	public ArrayList<Integer> searchTargets2(Integer target) {
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		//if(done.contains(target)) return list;
		list.add(target);
		//done.add(target);
		
		if(epsilonTable.containsKey(target) && epsilonTable.get(target).size()>0 ) {
			for(Integer i: epsilonTable.get(target)) {
			    list.addAll(searchTargets2(i));
			}
		} 
		return list;
	}
	
	
	public ArrayList<Integer> searchNeighbours2(ArrayList<Integer> targets){
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(Integer i : targets) {
			for(Integer j : table.get(i)) {
				if(j != -1 && !list.contains(j)) list.add(j);
			}
		}
		return list;
	}
	
	
	

	public ArrayList<Integer> search(String sentence) {
		char[] sent = sentence.toCharArray();
		ArrayList<Integer> results = new ArrayList<Integer>();
		int i = 0;
		int ln = sent.length;
		int currentNode = initial;
        while(i < ln) {
            currentNode = initial;
            if(!letterIndex.containsKey(sent[i])) {i++; continue;}
            for(int j = i; j < ln; j++) {

                char s = sent[j];
                if(!letterIndex.containsKey(s)) break;
                int next = table2.get(currentNode)[letterIndex.get(s)];
                if(next == -1) break;
                currentNode = next;
                if(terminals.contains(currentNode)) {results.add(i);  i=j;  break;}
                
            }
            i++;
        }
        return results;
    }
	
	/*
	public ArrayList<Integer> search(String sentence) {
		char[] sent = sentence.toCharArray();
		int idx = 0;
		for(String s:letters) {
			letterIndex.put(s.charAt(0), idx);
			idx++;
		}
		ArrayList<Integer> results = new ArrayList<Integer>();
		int i = 0;
        while(i < sentence.length()) {
            int currentNode = initial;
            if(!letterIndex.containsKey(sentence.charAt(i))) {i++; continue;}
            for(int j = i; j < sentence.length(); j++) {

                char s = sentence.charAt(j);
                if(!letterIndex.containsKey(s)) break;
                int next = table2.get(currentNode)[letterIndex.get(s)];
                if(next == -1) break;
                currentNode = next;
                if(terminals.contains(currentNode)) {results.add(i);  i=j;  break;}
                
            }
            i++;
        }
        return results;
    }
	*/
	
	
	
	public void printTables() {
		System.out.println("table/////////////////////////////////////////////////");
		String s = "   ";
		for(String ss : letters) s+= ss+"  ";
		System.out.println(s);
		
		
		for(Entry<Integer, Integer[]> n : table2.entrySet()) {
			s = "";
			s += n.getKey() + ": ";
			for(Integer i: n.getValue()) {
				s += i+"  "; 
			}
			System.out.println(s);
		}
		System.out.println("//////////////////////////////////////////////////////");
		
		System.out.println("starts and ends///////////////////////////////////////");
		
		System.out.println("start: " + initial);
		
		s = "";
		s+= "end: ";
		for(Integer i: terminals) {
			s += i+"  "; 
		}
		System.out.println(s);
		System.out.println("//////////////////////////////////////////////////////");
	}
	

}




/*
 public void optimizeTable() {
		
		while(existEpsilon()) {
			//find targets
			Integer target = findEpsilon();
			searchList = new ArrayList<Integer>();
			searchTargets(target);
			
			
			// creating and initializing the new node
			Integer newNode = nodeCount++;
			//done.add(newNode);
			Integer[] temp = new Integer[letters.size()];
			for(int i = 0; i<letters.size(); i++) {
				temp[i] = -1;
			}
			table.put(newNode, temp);
			for(Integer i: searchList) {
				for(int j = 0; j<letters.size();j++) {
					if(table.get(i)[j] != -1) table.get(newNode)[j] = table.get(i)[j];
				}
			}
			
			
			//destroying targets
			for(Integer d : searchList) {
				table.remove(d);
				epsilonTable.remove(d);
			}
			for(Entry<Integer, Integer[]> n : table.entrySet()) {
				for(int j = 0; j<letters.size();j++) { 
					if(searchList.contains(n.getValue()[j]))  n.getValue()[j]=newNode;
				}
				
			}
			for(Entry<Integer, ArrayList<Integer>> n : epsilonTable.entrySet()) {
				ArrayList<Integer> toGo = new ArrayList<Integer>();
				for(Integer i: n.getValue()) { 
					if(searchList.contains(i)) {  toGo.add(i);}
				}
				for(Integer i: toGo) {
					n.getValue().remove(i); if(!n.getValue().contains(newNode)) { n.getValue().add(newNode);}
				}
			}
			
			
			
		}
	}
	
	public void searchTargets(Integer target) {
		//if(done.contains(target)) return;
		addSearchList(target);
		
		if(epsilonTable.containsKey(target) && epsilonTable.get(target).size()>0 ) {
			for(Integer i: epsilonTable.get(target)) {
				searchTargets(i);
			}
		} 
	}
	
	public void addSearchList(Integer x) {
		if(!searchList.contains(x) && x!=-1) {
			
			searchList.add(x);
		}
	}
	
	
		public boolean existEpsilon() {
		for(Entry<Integer, ArrayList<Integer>> n : epsilonTable.entrySet()) {
			
			if(n.getValue().size()>0) return true;
		}
		return false;
	}
	
	
	
	public Integer findEpsilon() {
		for(Entry<Integer, ArrayList<Integer>> n : epsilonTable.entrySet()) {
			if(n.getValue().size()>0) return n.getKey();
		}
		return -1;
	}
	
	
	public void printAutomate() {
		for(Entry<Integer, ArrayList<Transition>> n : nodes.entrySet()) {
			System.out.println("-------------------------------------------------------");
			System.out.println(n.getKey()+":");
			if(n.getValue()!=null)
			for(Transition t : n.getValue()) {
				System.out.println(t);
			}
			System.out.println("-------------------------------------------------------");
		}
		
	}
	
 */
