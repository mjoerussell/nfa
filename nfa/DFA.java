package nfa;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DFA {
	
	private DFANode[] states;
	private DFANode[] acceptingStates;
	private DFANode initialState;
	private char[] sigma;
	
	public DFA(String dfaString) {
		String[] lines = dfaString.split("\n");
		
		int numStates = Integer.parseInt(lines[0]);
		
		String[] sigmaLine = Reader.tokenize(lines[1], " ");
		
		this.sigma = new char[sigmaLine.length - 1];
		this.states = new DFANode[numStates];
		
		for(int i = 0; i < numStates; i++) {
			this.states[i] = new DFANode();
		}
		
		for(int i = 1; i < sigmaLine.length; i++) {
			this.sigma[i - 1] = sigmaLine[i].charAt(0);
		}
		
		for(int lineNum = 3; lineNum < 3 + numStates; lineNum++) {
			String[] transitions = Reader.tokenize(lines[lineNum], " ");
			int currentStateNum = lineNum - 3;
			this.states[currentStateNum].setLabel(currentStateNum + "");
			for(int i = 1; i < transitions.length; i++) {
				int transitionNum = Integer.parseInt(transitions[i]);
				char c = this.sigma[i - 1];
				this.states[currentStateNum].addTransition(c, this.states[transitionNum]);
			}
		}
		
		int initialStateLineNum = 4 + numStates;
		String initialStateLabel = Reader.tokenize(lines[initialStateLineNum], ":")[0];
		this.initialState = this.states[Integer.parseInt(initialStateLabel)];
		
		int acceptingStatesLineNum = initialStateLineNum + 1;
		
		String[] acceptingStates = Reader.tokenize(Reader.tokenize(lines[acceptingStatesLineNum], ":")[0], ",");
		this.acceptingStates = new DFANode[acceptingStates.length];
		
		for(int i = 0; i < acceptingStates.length; i++) {
			String s = acceptingStates[i];
			int stateNum = Integer.parseInt(s);
			this.states[stateNum].setAccepting(true);
			this.acceptingStates[i] = this.states[stateNum];
		}
		
	}
	
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
					if(currentNodes.contains(computed)) {
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
	
	public MinimizedDFA minimize() {
		return new MinimizedDFA(this);
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
	
	private DFANode getEquivalentState(DFANode state, DFANode ... existingStates) {
		for(DFANode d : existingStates) {
			if (d.equals(state)) {
				return d;
			}
		}
		return state;
	}
	
	public DFANode[] getStates() { return this.states; }
	public DFANode[] getAcceptingStates() { return this.acceptingStates; }
	public DFANode getInitialState() { return this.initialState; } 
	public char[] getSigma() { return this.sigma; }
	
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
		int sequenceStart = -2;
		int previousState = -2;
		for(DFANode s : this.acceptingStates) {
			int currentState = Integer.parseInt(s.getLabel());
			if(previousState != currentState - 1) {
				if(sequenceStart >= 0) {
					if(sequenceStart - previousState == 0) {
						 sb.append(sequenceStart + " ");
					} else {
						sb.append(sequenceStart + "-" + previousState + " ");
					}
					sequenceStart = currentState;
					groupCount++;
				}
				sequenceStart = currentState;
			}
			previousState = currentState;
			if (groupCount % 10 == 0)
				sb.append("\n");
		}
		sb.append(sequenceStart + "-" + this.acceptingStates[this.acceptingStates.length - 1].getLabel() + " ");
		sb.append("]: Accepting state(s)");
		
		return sb.toString();
	}
	
	public static void main(String[] args) {
		if(args.length < 2) {
			System.out.println("NFA requires (2) arguments");
			return;
		}
		
		String contents = Reader.readEntireFile(args[0]);
		String[] testInputs = Reader.readIntoLines(args[1]);
		
		DFA dfa = new DFA(contents);
		MinimizedDFA mini = dfa.minimize();
		
		if(args.length >= 3) {
			int limit = Integer.parseInt(args[2]);
			System.out.println("\nDFA:\n");
			System.out.println(dfa.toPresentationString(limit));
			System.out.println("\nMinimized DFA:\n");
			System.out.println(mini.toPresentationString(limit));
		} else {
			System.out.println("\nDFA:\n");
			System.out.println(dfa.toString());
			System.out.println("\nMinimized DFA:\n");
			System.out.println(mini.toString());
		}
		
	}
	
	
}
