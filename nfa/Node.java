package nfa;

public interface Node {

	public final char LAMBDA = '^';
	
	/**
	 * Get the closure of this node when applying a specified transition character
	 * @param transition The transition character to apply
	 * @return The closure on the given transition
	 */
	public Node getClosure(char transition);
	
	/**
	 * Get the lambda closure of this Node.
	 * @return A Node representing the lambda closure of this node.
	 */
	public Node getLambdaClosure();
	
	/**
	 * Perform a transition and get an array of Nodes representing the possible outcomes
	 * of the transition.  Will always be of length 1 for a DFA Node
	 * @param transition The transition string
	 * @return The possible array of nodes that can be reached with this transition
	 */
	public Node[] applyTransition(char transition);
	
	public String getLabel();
	public Character[] getKeys();
	
	public boolean equals(Node other);
	public String toString();
	
}
