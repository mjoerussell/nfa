package nfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * NFANodes are nodes whose transitions can reach multiple nodes.  Their lambda transitions
 * can also reach new nodes.
 * @author mjoer
 *
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
	
	public void setTransitions(char[] sigma, NFANode[][] transitionNodes) {
		for(int i = 0; i < sigma.length; i++) {
			NFANode[] transitionsS = Arrays.copyOf(transitionNodes[i], transitionNodes[i].length);
			transitions.put(sigma[i], transitionsS);
		}
	}
	
	public void addTransition(char sig, NFANode nextTransition) {
		if(this.transitions.containsKey(sig)) {
			NFANode[] newTransitionList = Arrays.copyOf(this.transitions.get(sig), this.transitions.get(sig).length + 1);
			newTransitionList[newTransitionList.length - 1] = nextTransition;
			this.transitions.put(sig, newTransitionList);
		} else {
			this.transitions.put(sig, new NFANode[] { nextTransition });
		}
	}
	
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
		for(Character c : this.transitions.keySet()) {
			sb.append("(" + c + ",{");
			Arrays.asList(this.transitions.get(c))
				.stream()
				.map(n -> n.getLabel())
				.forEach(label -> sb.append(label + " "));
			sb.append("}) ");
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
