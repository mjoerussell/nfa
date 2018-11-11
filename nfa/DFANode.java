package nfa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class DFANode implements Node {

	private NFANode[] subnodes;
	private String label;
	
	public DFANode() {
		this.label = "";
		this.subnodes = new NFANode[0];
	}
	
	public DFANode(NFANode ... subnodes) {
		this.subnodes = subnodes;
		this.label = Arrays.asList(subnodes).stream()
				.map(node -> node.getLabel())
				.reduce("", (l1, l2) -> l1 + l2);
	}
	
	@Override
	public Node getClosure(char transition) {
		NFANode[] resultNodes = Arrays.asList(this.subnodes).stream()
				.map(n -> n.getClosure(transition))
				.collect(Collectors.toList())
				.toArray(new NFANode[this.subnodes.length]);
		
		return new DFANode(resultNodes);
	}

	@Override
	public Node getLambdaClosure() {
		return this;
	}

	@Override
	public Node[] applyTransition(char transition) {
//		return new Node[] { this.transitions.get(transition) };
		return null;
	}
	
	public DFANode combine(NFANode other) {
		NFANode[] newSubnodes = Arrays.copyOf(this.subnodes, this.subnodes.length + 1);
		newSubnodes[this.subnodes.length] = other;
		
		return new DFANode(newSubnodes);
	}
	
	public String getLabel() { return this.label; }
	
	public Character[] getKeys() {
		Character[] ks = this.transitions.keySet().toArray(new Character[0]);
		return ks;
	}
	
	@Override
	public boolean equals(Node other) {
		boolean labelEq = this.label.equals(other.getLabel());
		boolean keysEq = Arrays.deepEquals(this.getKeys(), other.getKeys());
		boolean transitionsEq = true;
		
		for(Character k : this.transitions.keySet()) {
			char key = (char) k;
			transitionsEq = transitionsEq && this.applyTransition(key).equals(other.applyTransition(key));
		}
		
		return labelEq && keysEq && transitionsEq;
		
	}
	
	@Override
	public String toString() {
		
		return null;
	}

}
