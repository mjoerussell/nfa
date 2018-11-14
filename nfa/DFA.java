package nfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DFA {
	
	private DFANode[] states;
	private DFANode[] acceptingStates;
	private DFANode initialState;
	private char[] sigma;
	
	public DFA(NFA nfa) {
		
		ArrayList<DFANode> stateAcc = new ArrayList<>();
		
		// Get sigma from nfa, minus lambda
		this.sigma = Arrays.copyOfRange(nfa.getSigma(), 0, nfa.getSigma().length - 1);
		this.initialState = DFANode.fromLambdaClosure(nfa.getInitialState());
		
		stateAcc.add(this.initialState);
		
		boolean stateCreated = true;
		while(stateCreated) {
			stateCreated = false;
			ArrayList<DFANode> computedNodes = new ArrayList<>();
			for (DFANode dfa : stateAcc) {
				for(char s : this.sigma) {
					DFANode computed = dfa.computeTransition(s);
					
					// Get all currently known nodes from stateAcc + computedNodes
					ArrayList<DFANode> currentNodes = new ArrayList<>(stateAcc);
					currentNodes.addAll(computedNodes);
					DFANode[] accSnapshot = currentNodes.toArray(new DFANode[currentNodes.size()]);
					
					// If the computed state already exists, then replace it
					// with the existing one (so that every node with a transition
					// to it references the same one and not a copy)
					if (this.stateExists(computed, accSnapshot)) {
						computed = this.getEquivalentState(computed, accSnapshot);
					} else {
						stateCreated = true;
						computedNodes.add(computed);
					}
					dfa.addTransition(s, computed);
				}
			}
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
	}
	
	public boolean testInput(String input) {
		DFANode currentState = this.initialState;
		for(char c : input.toCharArray()) {
			currentState = currentState.applyTransition(c);
			if(currentState == null)
				return false;
		}
		return currentState.isAccepting();
	}
	
	private boolean stateExists(DFANode state, DFANode ... checks) {
		for(DFANode d : checks) {
			if (d.equals(state)) {
				return true;
			}
		}

		return false;
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
		int groupCount = 1;
		for(DFANode s : this.acceptingStates) {
			sb.append(s.getLabel() + " ");
			if (groupCount % 10 == 0)
				sb.append("\n");
			groupCount++;
		}
		sb.append("]: Accepting state(s)");
		
		return sb.toString();
	}
	
	public String toPresentationString(int limit) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("Sigma:\t");
		for(char c : this.sigma) {
			sb.append(c + "\t");
		}
		sb.append("\n---------------\n");
		
		limit = this.states.length - limit > 5 ? limit : this.states.length;
		
		for(int i = 0; i < limit; i++) {
			sb.append(this.states[i].toString() + "\n");
		}
		
		if(limit != this.states.length) {
			sb.append("....\n");
			for(int i = this.states.length - 6; i < this.states.length; i++) {
				sb.append(this.states[i].toString() + "\n");
			}
		}
		
		sb.append(this.initialState.getLabel() + ": Initial State\n");
		sb.append("[");
		int groupCount = 1;
		for(DFANode s : this.acceptingStates) {
			sb.append(s.getLabel() + " ");
			if (groupCount % 10 == 0)
				sb.append("\n");
			groupCount++;
		}
		sb.append("]: Accepting state(s)");
		
		return sb.toString();
	}
	
	
}
