package nfa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * NFANodes are nodes whose transitions can reach multiple nodes.  Their lambda transitions
 * can also reach new nodes.
 * @author mjoer
 *
 */
public class NFANode implements Node {
	
	private HashMap<Character, Node[]> transitions;
	private String label;
	private boolean isAccepting;
	
	public NFANode() {
		this.transitions = new HashMap<>();
		this.label = "";
		this.isAccepting = false;
	}
	
	public void setTransitions(char[] sigma, NFANode[][] transitionNodes) {
		for(int i = 0; i < sigma.length; i++) {
			transitions.put(sigma[i], transitionNodes[i]);
		}
	}
	
	@Override
	public Node getClosure(char transition) {
		
		Node[] ts = this.applyTransition(transition);
//		Node tsLambda = Arrays.asList(ts).stream()		// start with transition char then apply lambda
//				.map(t -> t.getLambdaClosure())
//				.reduce(new DFANode(), (a, b) -> ((DFANode) a).combine(b));
//				.collect(Collectors.toList())
//				.toArray(new Node[ts.length]);
		
		
		Node ls = this.getLambdaClosure();
		Node lsTransition = ls.getClosure(transition);		// start with lambda then apply transition char
		
		
		
		return null; // RETURN COMBINED NODES tsLambda + lsTransition
		
	}

	@Override
	public Node getLambdaClosure() {
		Node[] lClosure = this.applyTransition(Node.LAMBDA);
		
		Node[] lClosureComplete = Arrays.asList(lClosure).stream()
				.map(l -> l.getLambdaClosure())
				.collect(Collectors.toList())
				.toArray(new Node[lClosure.length]);
		
		return null; // RETURN COMBINED NODES foldl (\cns n -> combine(n, cns)) (new Node) lClosureComplete
		
	}
	
	public String getLabel() { return this.label; }
	public void setLabel(String l) { this.label = l; }
	
	public boolean isAccepting() { return this.isAccepting; }
	public void setAccepting(boolean a) { this.isAccepting = a; }
	
	@Override
	public String toString() { 
		StringBuilder sb = new StringBuilder();
		for(Character c : this.transitions.keySet()) {
			sb.append("(" + c + "," + this.transitions.get(c).toString() + ") ");
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Node other) {
		return false;
	}

	@Override
	public Node[] applyTransition(char transition) {
		return this.transitions.get(transition);
	}

	@Override
	public Character[] getKeys() {
		return this.transitions.keySet().toArray(new Character[0]);
	}

}
