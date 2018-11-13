package nfa;

public class DFA {
	
	private final char LAMBDA = '^';
	
	private DFANode[] states;
	private DFANode initialState;
	private char[] sigma;
	
	
	public DFA(NFA nfa) {
		
		this.sigma = nfa.getSigma();
		
		
		
	}
	
	public String toString() {
		
		return null;
	}
	
	
}
