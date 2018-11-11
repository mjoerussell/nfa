package nfa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class NFA {
	
	private final char LAMBDA = '^';
	
	private NFANode[] states;
	private char[] sigma;
	
	/* Format of NFA files
	 * 
	 * 3							<-- Number of states
	 * 		a b c d					<-- Sigma
	 * 0:	{0} {0,1} {} {} {1,2,0}	<-- Transitions (correspond to inputs, last = lambda)
	 * 1:	{} {1} {1} {1} {1}
	 * 2: 	{} {1,2} {1} {1} {2}
	 * 0							<-- End of states
	 * {1,2}						<-- Accepting states
	 */
	public NFA(String nfaFileContents) {
		
		String[] lines = nfaFileContents.split("\n");
		
		int numStates = Integer.parseInt(lines[0]);

		//Initialize sigma
		String[] sigmaStrings = lines[1].split(" ");
		this.sigma = new char[sigmaStrings.length + 1];
		for(int i = 0; i < this.sigma.length; i++) {
			this.sigma[i] = sigmaStrings[i].charAt(0);
		}
		this.sigma[this.sigma.length] = LAMBDA;
		
		//initialize all of the states in the DFA
		this.states = new NFANode[numStates];
		Arrays.fill(this.states, new NFANode());
		
		//Add the transitions to each state
		for(int i = 0; i < this.states.length; i++) {
			int[][] transitionInts = getTransitionsForNode(lines[2 + i]);
			
			NFANode[][] transitionNodes = new NFANode[transitionInts.length][];
			
			for(int transition = 0; transition < transitionInts.length; transition++) {
				
				for(int t = 0; t < transitionInts[transition].length; t++) {
					int currentTransition = transitionInts[transition][t];
					transitionNodes[transition][t] = this.states[currentTransition];
				}
				
			}
			
			this.states[i].setTransitions(this.sigma, transitionNodes);
			this.states[i].setLabel(i + "");
		}
		
		//Set the accepting states
		String[] acceptingStates = lines[2 + numStates + 1].split("{},");
		for(String state : acceptingStates) {
			int stateNum = Integer.parseInt(state);
			this.states[stateNum].setAccepting(true);
		}
		
		
	}
	
	private int[][] getTransitionsForNode(String transitionString) {
		
		String[] transitionStates = transitionString.substring(1).split(" {}");
		
		int[][] transitions = new int[transitionStates.length][];
		
		for(int i = 0; i < transitionStates.length; i++) {
			
			String[] multiTransitions = transitionStates[i].split(",");
			transitions[i] = new int[multiTransitions.length];
			
			for(int j = 0; j < multiTransitions.length; j++) {
				transitions[i][j] = Integer.parseInt(multiTransitions[j]);
			}
				
		}
		
		return transitions;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Sigma: " + this.sigma.toString() + "\n");
		sb.append("--------\n");
		for(NFANode state : this.states) {
			sb.append(state.toString() + "\n");
		}
		sb.append("--------\n");
		sb.append("0: Initial State\n");
		
		NFANode[] acceptingStates = Arrays.asList(this.states)
				.stream()
				.filter(state -> state.isAccepting())
				.collect(Collectors.toList())
				.toArray(new NFANode[0]);
		sb.append(acceptingStates.toString() + ": Accepting State(s)");
		
		return sb.toString();
	}
	
}














