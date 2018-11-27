package nfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MinimizedDFA {
	
	private char[] sigma;
	private DFANode[] states;
	private DFANode[] acceptingStates;
	private DFANode initialState;
	
	/**
	 * Creates a minimized DFA using Hopcroft's algorithm.
	 * @param toMinimize The DFA to minimize
	 */
	@SuppressWarnings("unchecked")
	public MinimizedDFA(DFA toMinimize) {
		
		// Initialize partitions and the initial set to check against
		// partitions = 'P'
		ArrayList<ArrayList<DFANode>> partitions = this.initializePartitionSets(toMinimize.getStates());
		// wSets = 'W'
		ArrayList<ArrayList<DFANode>> wSets = new ArrayList<>();
		ArrayList<DFANode> finalStates = new ArrayList<>(); 
		finalStates.addAll(Arrays.asList(toMinimize.getAcceptingStates()));
		wSets.add(finalStates);
		
		// Begin Hopcroft's Algorithm
		while(!wSets.isEmpty()) {
			// Choose one set from W
			ArrayList<DFANode> chosenW = wSets.remove(0);
			for(char c : toMinimize.getSigma()) {
				// Get any state 'q' in toMinimize such that delta(q, c) is in chosen
				ArrayList<DFANode> canReach = this.filterCanReach(
						Arrays.asList(toMinimize.getStates()), 
						chosenW, 
						c);
				// Make a temporary array because we'll be modifying partitions
				ArrayList<ArrayList<DFANode>> tempPartitions = (ArrayList<ArrayList<DFANode>>) partitions.clone();
				for(ArrayList<DFANode> partition : tempPartitions) {
					// Get the the intersection of canReach and partition
					ArrayList<DFANode> intersection = this.intersect(canReach, partition);
					// Get partition - canReach
					ArrayList<DFANode> subtraction = this.subtractSet(partition, canReach);
					// If intersection and subtraction are non-empty, then some states in partition
					// can reach chosenW, and some can't
					// This means that they are distinguishable and we should make a new partition
					if(!intersection.isEmpty() && !subtraction.isEmpty()) {
						// Remove partition from partitions and add intersection and subtraction to partitions
						this.replaceWith(partitions, partition, intersection, subtraction);
						// If wSets contains partition then do the same as above
						if(wSets.contains(partition)) {
							this.replaceWith(wSets, partition, intersection, subtraction);
						} else {
							// Otherwise add the smaller of the two new partitions to wSets
							if (intersection.size() <= subtraction.size()) {
								wSets.add(intersection);
							} else {
								wSets.add(subtraction);
							}
						}
					}
				}
			}
		}
		
		this.sigma = toMinimize.getSigma();
		this.states = new DFANode[partitions.size()];
		
		for(int i = 0; i < this.states.length; i++) {
			this.states[i] = new DFANode();
		}
		
		// Initialize the states of the DFA based on the computed partitions
		for(int i = 0; i < this.states.length; i++) {
			ArrayList<DFANode> currentPartition = partitions.get(i);
			// Find the new initial state
			if(currentPartition.stream().anyMatch(s -> s.equals(toMinimize.getInitialState()))) {
				this.initialState = this.states[i];
			}
			// Find the new accepting state(s)
			this.states[i].setAccepting(currentPartition.stream().anyMatch(s -> s.isAccepting()));
			// Set the label of the current state
			this.states[i].setLabel(i + "");
			// Set all the transitions for the current state
			for(char c : this.sigma) {
				// Get delta(q, c) for all q in currentPartition
				ArrayList<DFANode> transitionStates = (ArrayList<DFANode>) currentPartition.stream()
						.map(s -> s.applyTransition(c))
						.collect(Collectors.toList());
				// Find the partition that contains all the states in transitionStates
				// This is the state that the new transition on c will target
				ArrayList<DFANode> matchingPartition = partitions.stream()
						.filter(p -> p.containsAll(transitionStates))
						.collect(Collectors.toList()).get(0);
				// Get the index of matchinPartition
				int minTransitionState = partitions.indexOf(matchingPartition);
				// Get the DFANode at minTransitionState and create the transition
				this.states[i].addTransition(c, this.states[minTransitionState]);
			}
			
		}
		
		this.acceptingStates = Arrays.asList(this.states).stream()
				.filter(s -> s.isAccepting())
				.collect(Collectors.toList())
				.toArray(new DFANode[0]);
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
	
	/**
	 * Remove an item from a list, and then add a multitude of items to the same list
	 * @param list The list to modify
	 * @param toRemove The item to be removed
	 * @param replacements The items to add to the list
	 */
	private <T> void replaceWith(ArrayList<T> list, T toRemove, T ... replacements) {
		list.remove(toRemove);
		list.addAll(Arrays.asList(replacements));
	}
	
	/**
	 * Initialize the 'P' set of Hopcroft's algorithm using a list of states. States are
	 * partitioned based on whether or not they are accepting.
	 * @param states The states to partition
	 * @return A new list of lists of DFANodes which have been partitioned
	 */
	private ArrayList<ArrayList<DFANode>> initializePartitionSets(DFANode[] states) {
		ArrayList<DFANode> acceptingPartition = new ArrayList<>();
		ArrayList<DFANode> nonacceptingPartition = new ArrayList<>();
		
		acceptingPartition.addAll(Arrays.asList(states).stream()
				.filter(s -> s.isAccepting())
				.collect(Collectors.toList()));
		nonacceptingPartition.addAll(Arrays.asList(states).stream()
				.filter(s -> !s.isAccepting())
				.collect(Collectors.toList()));
		
		ArrayList<ArrayList<DFANode>> partitions = new ArrayList<>();
		partitions.add(nonacceptingPartition);
		partitions.add(acceptingPartition);
		
		return partitions;
	}
	
	/**
	 * Filter a list of DFANodes based on if their transition on 'c' reaches any
	 * of the nodes in 'toReach'
	 * @param filterFrom The list of DFANodes to filter
	 * @param toReach The goal states to be reached by the transition on 'c'
	 * @param c The transition character
	 * @return A new list of filtered DFANodes
	 */
	private ArrayList<DFANode> filterCanReach(List<DFANode> filterFrom, ArrayList<DFANode> toReach, char c) {
		return (ArrayList<DFANode>) filterFrom.stream()
				.filter(s -> toReach.contains(s.applyTransition(c)))
				.collect(Collectors.toList());
	}
	
	/**
	 * Find the intersection of two lists of DFANodes
	 * @param a The first list of DFANodes
	 * @param b The second list of DFANodes
	 * @return A new list of DFANodes
	 */
	private ArrayList<DFANode> intersect(ArrayList<DFANode> a, ArrayList<DFANode> b) {
		ArrayList<DFANode> intersection = new ArrayList<>();
		for(DFANode check : a) {
			if(b.contains(check)) {
				intersection.add(check);
			}
		}
		return intersection;
	}
	
	/**
	 * Find 'from'\'toSubtract' (aka 'from'-'toSubtract')
	 * @param from The list to subtract from
	 * @param toSubtract The list to be subtracted
	 * @return A new list of DFANodes
	 */
	private ArrayList<DFANode> subtractSet(ArrayList<DFANode> from, ArrayList<DFANode> toSubtract) {
		ArrayList<DFANode> subtraction = new ArrayList<>();
		for(DFANode check : from) {
			if(!toSubtract.contains(check)) {
				subtraction.add(check);
			}
		}
		return subtraction;
	}
	
}












