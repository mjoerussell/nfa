package nfa;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class DFANode {
	
	private String label;
	private HashMap<Character, DFANode> transitions;
	private boolean isAccepting;
	
//	private ArrayList<DFANode> distinguishable;
	
	private NFANode[] enclosed;
	
	public DFANode() {
		this.transitions = new HashMap<>();
//		this.distinguishable = new ArrayList<>();
	}
	
	public DFANode(DFANode toCopy) {
		this.label = toCopy.label;
		this.transitions = new HashMap<>(toCopy.transitions);
		this.isAccepting = toCopy.isAccepting;
		this.enclosed = Arrays.copyOf(toCopy.enclosed, toCopy.enclosed.length);
//		this.distinguishable = new ArrayList<>(toCopy.distinguishable);
	}
	
	public DFANode(NFANode ... enclosed) {
		this.transitions = new HashMap<>();
		this.enclosed = enclosed;
//		this.distinguishable = new ArrayList<>();
	}
	
	/**
	 * Create a new DFANode from the union of several DFANodes.
	 * The new DFANode will be enclosing the union of NFANodes
	 * enclosed by inits
	 * @param inits The DFANodes to be joined
	 * @return A new DFANode
	 */
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
	
	/**
	 * Create a new DFANode enclosing the union of lambda closures of
	 * the NFANodes enclosed by the specified DFANode
	 * @param nfa The DFANode whose enclosed NFANodes will be evaluated
	 * @return A new DFANode
	 */
	public static DFANode fromLambdaClosure(DFANode dfa) {
		return DFANode.fromLambdaClosure(dfa.getEnclosed());
	}
	
	/**
	 * Create a new DFANode enclosing the union of lambda closures of
	 * the specified NFANodes
	 * @param nfa The NFANodes whose lambda closures will be
	 * 		calculated
	 * @return A new DFANode
	 */
	public static DFANode fromLambdaClosure(NFANode ... nfas) {
		DFANode dfaNode = DFANode.fromUnion(
				Arrays.asList(nfas)
				.stream()
				.map(nfa -> DFANode.fromLambdaClosure(nfa))
				.collect(Collectors.toList())
				.toArray(new DFANode[0]));
		return dfaNode;
	}
	
	/**
	 * Create a new DFANode enclosing the lambda closure of
	 * the specified NFANode
	 * @param nfa The NFANode whose lambda closure will be
	 * 		calculated
	 * @return A new DFANode
	 */
	public static DFANode fromLambdaClosure(NFANode nfa) {
		DFANode dfaNode = new DFANode();
		dfaNode.enclosed = nfa.getLambdaClosure();
		return dfaNode;
	}
	
	/**
	 * Computes the DFANode that would be reached by taking the specified transition
	 * from this node.  The computed DFANode is represented by its enclosed NFANodes,
	 * which are found by applying the following transition steps:
	 * 	q -> transition;
	 * 	q -> transition -> lambda;
	 * 	q -> lambda -> transition;
	 * 	q -> lambda -> transition -> lambda;
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
	
	public DFANode applyTransition(char transition) {
		return this.transitions.get(transition);
	}
	
	/**
	 * Add a transition by the specified symbol to the specified
	 * DFANode.  If a transition for the symbol already exists it
	 * will be overwritten.
	 * @param sig The symbol to label the transition
	 * @param nextTransition The node reached by the transition
	 */
	public void addTransition(char sig, DFANode nextTransition) {
		this.transitions.put(sig, nextTransition);
	}
	
	public void setLabel(String l) { this.label = l; }
	public String getLabel() { return this.label; }
	
	public void setAccepting(boolean a) { this.isAccepting = a; }
	public boolean isAccepting() { return this.isAccepting; }
	
	/**
	 * Determine if this DFANode is accepting or not. A DFANode is accepting
	 * if any of its enclosed NFANodes are accepting.
	 * [SIDE EFFECT] This function will set isAccepting to the return value
	 * of this function.
	 * @return True if this node is accepting, false otherwise.
	 */
	public boolean computeIsAccepting() {
		this.isAccepting = Arrays.asList(this.enclosed)
				.stream()
				.anyMatch(n -> n.isAccepting());
		return this.isAccepting;
	}
	
	public NFANode[] getEnclosed() { return this.enclosed; }
	public void setEnclosed(NFANode ... nfas) { this.enclosed = nfas; }
	
	/**
	 * Check DFANode equality.  Two DFANodes are equivalent if all of their
	 * enclosed NFANodes are equivalent.
	 * @param other The DFANode to compare to this
	 * @return True if the two nodes are equivalent, false otherwise.
	 */
	@Override
	public boolean equals(Object other) {
		if(other instanceof DFANode) {
			DFANode otherDFA = (DFANode) other;
			if (this.enclosed.length != otherDFA.getEnclosed().length)
				return false;
			for(NFANode nfa : this.enclosed) {
				boolean otherContainsNFA = false;
				for(NFANode otherNFA : otherDFA.getEnclosed()) {
					if (otherNFA.equals(nfa))
						otherContainsNFA = true;
				}
				if (!otherContainsNFA)
					return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + (this.label == null ? this.label.hashCode() : 0);
		return hash;
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
		return sb.toString();
	}

}
