package nfa;
import java.util.Arrays;
import java.util.stream.Collectors;

public class NFA {
	
	private final char LAMBDA = '^';
	
	private NFANode[] states;
	private NFANode[] acceptingStates;
	private NFANode initialState;
	private char[] sigma;
	
	public NFA(String nfaFileContents) {
		
		String[] lines = nfaFileContents.split("\n");
		
		int numStates = Integer.parseInt(lines[0]);

		//Initialize sigma
		String sigmaString = Reader.ignoreWhitespace(lines[1]);
		this.sigma = new char[sigmaString.length() + 1];

		for(int i = 0; i < sigmaString.length(); i++) {
			this.sigma[i] = sigmaString.charAt(i);
		}
		this.sigma[sigmaString.length()] = LAMBDA;
		
		//initialize all of the states in the DFA
		this.states = new NFANode[numStates];
		for(int stateNum = 0; stateNum < numStates; stateNum++) {
			this.states[stateNum] = new NFANode();
			this.states[stateNum].setLabel(stateNum + "");
		}
		
		//Add the transitions to each state
		for(int currentState = 0; currentState < this.states.length; currentState++) {
			String currentStateString = lines[2 + currentState];
			int[][] transitionInts = getTransitionsForNode(currentStateString);
			
			for(int transition = 0; transition < transitionInts.length; transition++) {
				for(int t = 0; t < transitionInts[transition].length; t++) {
					int currentTransition = transitionInts[transition][t];
					if(currentTransition != -1) {
						this.states[currentState].addTransition(this.sigma[transition], this.states[currentTransition]);
					}
				}
			}
		}
		
		//Set the initial state
		int initStateNum = Integer.parseInt(lines[2 + numStates]);
		this.initialState = this.states[initStateNum];
		
		//Set the accepting states
		String[] acceptingStates = Reader.match(lines[2 + numStates + 1], "\\D");
		this.acceptingStates = new NFANode[acceptingStates.length];
		int count = 0;
		for(String state : acceptingStates) {
			if(!state.equals("")) {
				int stateNum = Integer.parseInt(state);
				this.states[stateNum].setAccepting(true);
				this.acceptingStates[count] = this.states[stateNum];
				count++;
			}
		}
	}
	
	private int[][] getTransitionsForNode(String transitionString) {
		String[] transitionStates = Reader.tokenize(
				Reader.ignoreWhitespace(
						transitionString.substring(transitionString.indexOf(":") + 1)
				), "{");
		transitionStates = Arrays.asList(transitionStates)
				.stream()
				.map(st -> st.replace("}", ""))
				.collect(Collectors.toList())
				.toArray(new String[transitionStates.length]);
		
		int[][] transitions = new int[transitionStates.length][];
		
		for (int currentTransition = 0; currentTransition < transitionStates.length; currentTransition++) {
			String transString = transitionStates[currentTransition];
			if (transString.equals("")) {
				transitions[currentTransition] = new int[] { -1 };
			} else {
				String[] npTransitions = Reader.tokenize(transString, ",");
				Integer[] newTransitions = Arrays.asList(npTransitions)
						.stream()
						.map(Integer::parseInt)
						.collect(Collectors.toList())
						.toArray(new Integer[npTransitions.length]);
				
				int[] ntInts = new int[newTransitions.length];
				for(int i = 0; i < ntInts.length; i++)
					ntInts[i] = (int) newTransitions[i];
				
				transitions[currentTransition] = ntInts;
			}
		}
		
		return transitions;
	}
	
	public char[] getSigma() { return this.sigma; }
	public NFANode getInitialState() { return this.initialState; }
	
	public NFANode getState(int num) {
		if (num < 0 || num > this.states.length) 
			return null;
		return this.states[num];
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Sigma:\t");
		
		for(char c : this.sigma)
			sb.append(c + "");
		
		sb.append("\n---------------\n");
		
		for(NFANode n : this.states) {
			sb.append(n.toString() + "\n");
		}
		
		sb.append("---------------\n");
		sb.append(this.initialState.getLabel() + ": Initial State\n[");
		
		int groupCount = 1;
		for(NFANode s : this.acceptingStates) {
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
	
		for(char c : this.sigma)
			sb.append(c + "");
		
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
			
		sb.append("---------------\n");
		sb.append(this.initialState.getLabel() + ": Initial State\n[");
		
		int groupCount = 1;
		for(NFANode s : this.acceptingStates) {
			sb.append(s.getLabel() + " ");
			if (groupCount % 10 == 0)
				sb.append("\n");
			groupCount++;
		}
		
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
		
		NFA nfa = new NFA(contents);
		DFA dfa = new DFA(nfa);
		
		MinimizedDFA minimized = dfa.minimize();
		
		if(args.length >= 3) {
			int limit = Integer.parseInt(args[2]);
			System.out.println(nfa.toPresentationString(limit));
			System.out.println("\nTo DFA:\n");
			System.out.println(dfa.toPresentationString(limit));
			System.out.println("\nMinimized DFA:\n");
			System.out.println(minimized.toPresentationString(limit));
		} else {
			System.out.println(nfa.toString());
			System.out.println("\nTo DFA:\n");
			System.out.println(dfa.toString());
			System.out.println("\nMinimized DFA:\n");
			System.out.println(minimized.toString());
		}
		
		
		
		System.out.println("The following strings are accepted:");
		Arrays.asList(testInputs).stream()
			.filter(i -> dfa.testInput(i))
			.forEach(i -> System.out.println(i));
	}
}














