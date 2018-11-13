package nfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class DFANode {
	
	private String label;
	private HashMap<Character, DFANode> transitions;
	private boolean isAccepting;
	
	private NFANode[] enclosed;
	
	public DFANode() {
		this.transitions = new HashMap<>();
	}
	
	public DFANode(NFANode ... enclosed) {
		this.transitions = new HashMap<>();
		this.enclosed = enclosed;
	}
	
	public static DFANode fromUnion(DFANode ... inits) {
		ArrayList<NFANode> uniqueEnclosed = new ArrayList<>();

		for(DFANode dfaN : inits) {
			for(NFANode nfaN : dfaN.getEnclosed()) {
				if (!uniqueEnclosed.contains(nfaN)) {
					uniqueEnclosed.add(nfaN);
				}
			}
		}
		return new DFANode(uniqueEnclosed.toArray(new NFANode[uniqueEnclosed.size()]));
	}
	
	public static DFANode fromLambdaClosure(DFANode dfa) {
		return DFANode.fromLambdaClosure(dfa.getEnclosed());
	}
	
	public static DFANode fromLambdaClosure(NFANode ... nfas) {
		DFANode dfaNode = DFANode.fromUnion(
				Arrays.asList(nfas)
				.stream()
				.map(nfa -> DFANode.fromLambdaClosure(nfa))
				.collect(Collectors.toList())
				.toArray(new DFANode[0]));
		return dfaNode;
	}
	
	public static DFANode fromLambdaClosure(NFANode nfa) {
		DFANode dfaNode = new DFANode();
		dfaNode.enclosed = nfa.getLambdaClosure();
		return dfaNode;
	}
	
	/**
	 * Computes the DFANode that would be reached by taking the specified transition
	 * from this node.  The computed DFANode is represented by its enclosed NFANodes,
	 * which are found by applying the following transition steps:
	 * 	q -> transition
	 * 	q -> transition -> lambda
	 * 	q -> lambda -> transition
	 * 	q -> lambda -> transition -> lambda
	 * @param transition The transition character to apply (comes from sigma)
	 * @return A new DFANode represented by enclosed NFANodes
	 */
	public DFANode computeTransition(char transition) {
		// q -> transition
		DFANode fromT = new DFANode(this.computeSingleTransition(transition));
		// q -> transition -> lambda
		DFANode fromTL = DFANode.fromLambdaClosure(fromT);
		// q -> lambda (not used in final union - just for intermediate calculation)
		DFANode fromL = DFANode.fromLambdaClosure(this);
		// q -> lambda -> transition
		DFANode fromLT = new DFANode(fromL.computeSingleTransition(transition));
		// q -> lambda -> transition -> lambda
		DFANode fromLTL = DFANode.fromLambdaClosure(fromLT);
		
		return DFANode.fromUnion(fromT, fromTL, fromLT, fromLTL);
	}
	
	/**
	 * Apply a transition to all of the enclosed nodes and return the distinct
	 * result nodes.  Does not apply the transition recursively, nor does it
	 * take into account additional lambda transitions.
	 * @param transition The transition character to apply (comes from sigma)
	 * @return An array of NFANodes
	 */
	public NFANode[] computeSingleTransition(char transition) {
		ArrayList<NFANode> enclosedNodes = new ArrayList<>();
		ArrayList<NFANode> candidateNodes = new ArrayList<>();
		
		for(NFANode n : this.enclosed) {
			candidateNodes.addAll(Arrays.asList(n.applyTransition(transition)));
		}
		for(NFANode c : candidateNodes) {
			if (!enclosedNodes.contains(c)) {
				enclosedNodes.add(c);
			}
		}
		
		return enclosedNodes.toArray(new NFANode[enclosedNodes.size()]);
	}
	
	public void addTransition(char sig, DFANode nextTransition) {
		this.transitions.put(sig, nextTransition);
	}
	
	public void setLabel(String l) { this.label = l; }
	public String getLabel() { return this.label; }
	
	public void setAccepting(boolean a) { this.isAccepting = a; }
	public boolean isAccepting() { return this.isAccepting; }
	public boolean computeIsAccepting() {
		return Arrays.asList(this.enclosed)
				.stream()
				.anyMatch(n -> n.isAccepting());
	}
	
	
	public NFANode[] getEnclosed() { return this.enclosed; }
	public void setEnclosed(NFANode ... nfas) { this.enclosed = nfas; }
	
	public boolean equals(DFANode other) {
		if (this.enclosed.length != other.getEnclosed().length)
			return false;
		for(NFANode nfa : this.enclosed) {
			boolean otherContainsNFA = false;
			for(NFANode otherNFA : other.getEnclosed()) {
				if (otherNFA.equals(nfa))
					otherContainsNFA = true;
//				else 	// DEBUG ONLY
//					System.out.println("NFA: " + nfa.getLabel() + " /= " + otherNFA.getLabel());
			}
			if (!otherContainsNFA)
				return false;
		}
		return true;
	}
	
	public String debugToString() {
		return "(" + Arrays.asList(this.enclosed).stream()
				.map(n -> n.getLabel())
				.reduce("", (a, b) -> "" + a + ", " + b + "") + ")";
	}
	
	public String toString() { 
		StringBuilder sb = new StringBuilder();
		sb.append(this.label + ": ");
		for (char c : this.transitions.keySet()) {
			sb.append("\t" + this.transitions.get(c).getLabel());
		}
//		sb.append(" | DEBUG: " + this.debugToString() + "\n");
		return sb.toString();
	}

}
