package nfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * NFANodes are nodes whose transitions can reach multiple nodes.  Their lambda transitions
 * can also reach new nodes.
 * @author mjoer
 */
public class NFANode {
	
	private HashMap<Character, NFANode[]> transitions;
	private String label;
	private boolean isAccepting;
	
	public NFANode() {
		this.transitions = new HashMap<>();
		this.transitions.put('^', new NFANode[] { this });
		this.label = "";
		this.isAccepting = false;
	}
	
	/**
	 * Add a transition to this node.  If a transition already exists for 
	 * the given symbol, then the new node is added to the list of nodes
	 * reachable via the symbol.
	 * @param sig The symbol of this transition
	 * @param nextTransition The node being added to the transition.
	 */
	public void addTransition(char sig, NFANode nextTransition) {
		if(this.transitions.containsKey(sig)) {
			NFANode[] newTransitionList = Arrays.copyOf(this.transitions.get(sig), this.transitions.get(sig).length + 1);
			newTransitionList[newTransitionList.length - 1] = nextTransition;
			this.transitions.put(sig, newTransitionList);
		} else {
			this.transitions.put(sig, new NFANode[] { nextTransition });
		}
	}
	
	/**
	 * Computes the lambda closure of this node.  The lambda closure is the set of
	 * nodes that can be reached with only lambda transitions.  The process is recursive,
	 * so for all nodes that can be reached with a lambda closure from this node, their 
	 * lambda closure must also be found.  The base case is when no new nodes are added
	 * to the result set.
	 * @return An array of NFANodes which can be reached by lambda transitions.
	 */
	public NFANode[] getLambdaClosure() {
		ArrayList<NFANode> enclosedNodes = new ArrayList<>();
		ArrayList<NFANode> candidateNodes = new ArrayList<>();
		
		enclosedNodes.add(this);
		boolean nodeWasAdded = true;
		
		while(nodeWasAdded) {
			nodeWasAdded = false;
			for(NFANode n : enclosedNodes) {
				candidateNodes.addAll(Arrays.asList(n.applyTransition('^')));
			}
			for(NFANode c : candidateNodes) {
				if (!enclosedNodes.contains(c)) {
					enclosedNodes.add(c);
					nodeWasAdded = true;
				}
			}
		}
		
		return enclosedNodes.toArray(new NFANode[enclosedNodes.size()]);
	}
	
	public String getLabel() { return this.label; }
	public void setLabel(String l) { this.label = l; }
	
	public boolean isAccepting() { return this.isAccepting; }
	public void setAccepting(boolean a) { this.isAccepting = a; }
	
	@Override
	public String toString() { 
		StringBuilder sb = new StringBuilder();
		sb.append(this.label + ":\t");
		for(Character c : this.transitions.keySet()) {
			sb.append("(" + c + ",{");
			Arrays.asList(this.transitions.get(c))
				.stream()
				.map(n -> n.getLabel())
				.forEach(label -> sb.append(label + " "));
			sb.deleteCharAt(sb.length() - 1);
			sb.append("})  ");
		}
		return sb.toString();
	}
	
	public boolean equals(NFANode other) {
		return this.label.equals(other.getLabel());
	}

	public NFANode[] applyTransition(char transition) {
		if (this.transitions.containsKey(transition)) {
			return this.transitions.get(transition);
		}
		return new NFANode[0];
	}

}
