package nfa;

import java.util.HashMap;
import java.util.stream.Collectors;

public class NFA {
	
	private class NFANode {
		
		private HashMap<Character, NFANode[]> transitions;
		
		public NFANode(char[] transitionChars, NFANode[][] transitionNodes) {
			for(int i = 0; i < transitionChars.length; i++) {
				transitions.put(transitionChars[i], transitionNodes[i]);
			}
		}
		
		public NFANode[] applyTransition(char input) {
			return transitions.get(input);
		}
		
		/** Return an array of all inputs that will return a valid node
		 * @return The subset of inputs that represent valid transitions from this
		 * node, as a char array.
		 */
		public char[] validInputs() {
			Character[] tChars = transitions.keySet()
					.stream()
					.filter(t -> transitions.get(t) != null && transitions.get(t).length != 0)
					.collect(Collectors.toList())
					.toArray(new Character[0]);
			
			char[] result = new char[tChars.length];
			for(int i = 0; i < tChars.length; i++) {
				result[i] = (char) tChars[i];
			}
			
			return result;
		}
		
		
	}
	
	
	
}
