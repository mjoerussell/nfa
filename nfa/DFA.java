package nfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DFA {
	
	private final char LAMBDA = '^';
	
	private DFANode[] states;
	private DFANode[] acceptingStates;
	private DFANode initialState;
	private char[] sigma;
	
	
	public DFA(NFA nfa) {
		
		ArrayList<DFANode> stateAcc = new ArrayList<>();
		this.sigma = Arrays.copyOfRange(nfa.getSigma(), 0, nfa.getSigma().length - 1);
		this.initialState = DFANode.fromLambdaClosure(nfa.getInitialState());
		
		stateAcc.add(this.initialState);
		this.states = new DFANode[] { this.initialState };
		
//		System.out.println("Initial state = " + this.initialState.debugToString());
		
		// For all sigma
		// Compute newStates <- d(q,sigma) for all q in stateAcc
		// If state q' in newStates == state q in stateAcc then q' <- q
		// Compute q.addTransition(sigma, q')
		// If a new state was created then re-run d(q', sigma)
		// When no new states are created move to next symbol in sigma
		
//		for(char s : this.sigma) {
//			System.out.println("Computing transitions '" + s + "'");
		boolean stateCreated = true;
		while(stateCreated) {
			stateCreated = false;
			ArrayList<DFANode> computedNodes = new ArrayList<>();
			for (DFANode dfa : stateAcc) {
				for(char s : this.sigma) {
//					System.out.println("Computing transition " + s + " for DFA " + dfa.debugToString() + "");
					DFANode computed = dfa.computeTransition(s);
//					System.out.println("Computed transtion = " + computed.debugToString());
					
					ArrayList<DFANode> currentNodes = new ArrayList<>(stateAcc);
					currentNodes.addAll(computedNodes);
					DFANode[] accSnapshot = currentNodes.toArray(new DFANode[currentNodes.size()]);
					
					if (this.stateExists(computed, accSnapshot)) {
//						System.out.println("DFANode " + computed.debugToString() + " already exists.");
						computed = this.getEquivalentState(computed, accSnapshot);
					} else {
//						System.out.println("New node: " + computed.debugToString());
						stateCreated = true;
						computedNodes.add(computed);
					}
					
//					System.out.println("Adding transition d(\n\t" + dfa.debugToString() + ",\n\t" + s + " = " + computed.debugToString());
					dfa.addTransition(s, computed);
				}
			}
//			System.out.println("Computed nodes: " + computedNodes.toString());
			stateAcc.addAll(computedNodes);
		}
		
		this.states = stateAcc.toArray(new DFANode[stateAcc.size()]);
		for(int i = 0; i < this.states.length; i++) {
			this.states[i].setLabel(i + "");
		}
		
		this.acceptingStates = Arrays.asList(this.states).stream()
				.filter(s -> s.computeIsAccepting())
				.collect(Collectors.toList())
				.toArray(new DFANode[0]);
		
//		Arrays.asList(this.states).stream().forEach(s -> System.out.print(s.toString()));
		
		
	}
	
	private boolean stateExists(DFANode state) {
		return this.stateExists(state, this.states);
	}
	
	private boolean stateExists(DFANode state, DFANode ... checks) {
		for(DFANode d : checks) {
			if (d.equals(state)) {
//				System.out.println("" + d.debugToString() + " == " + state.debugToString() + "");
				return true;
			}
//			System.out.println("" + d.debugToString() + " /= " + state.debugToString() + "");
		}

		return false;
	}
	
	
	
	private DFANode getEquivalentState(DFANode state) {
		return getEquivalentState(state, this.states);
	}
	
	private DFANode getEquivalentState(DFANode state, DFANode ... existingStates) {
		for(DFANode d : existingStates) {
			if (d.equals(state)) {
				return d;
			}
		}
		return state;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("Sigma:\t");
		for(char c : this.sigma) {
			sb.append(c + "\t");
		}
		sb.append("\n---------------\n");
		
		Arrays.asList(this.states).stream()
			.forEach(s -> sb.append(s.toString() + "\n"));
		
		sb.append(this.initialState.getLabel() + ": Initial State\n");
		sb.append("[");
		for(DFANode s : this.acceptingStates) {
			sb.append(s.getLabel() + " ");
		}
		sb.append("]: Accepting state(s)");
		
		return sb.toString();
	}
	
	
}
