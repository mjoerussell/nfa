package nfa;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Reader {

	
	public static String readEntireFile(String path) {
		StringBuilder sb = new StringBuilder();
		try {
			Scanner in = new Scanner(new File(path));
			
			while(in.hasNextLine()) {
				sb.append(in.nextLine() + "\n");
			}
			
			in.close();
		} catch (FileNotFoundException e) {
			return null;
		} 
		
		return sb.toString();
	}
	
	public static String[] readIntoLines(String path) {
		return Reader.tokenize(Reader.readEntireFile(path), "\n");
	}
	
	public static String[] match(String base, String regex) {
		String[] tokens = base.split(regex);
		ArrayList<String> nonEmpty = new ArrayList<>();
		
		for(String token : tokens) {
//			System.out.println("match() token: " + token);
			if (!token.equals("")) {
				nonEmpty.add(token);
			}
		}
		
		return nonEmpty.toArray(new String[nonEmpty.size()]);
	}
	
	public static String[] tokenize(String base, String delims) {
		
		ArrayList<String> tokens = new ArrayList<>();
		
		int nextIndex = 0;
		
		int[] delimIndices = Reader.indexOfAll(base, delims);
		
		if(delimIndices[0] == -1)
			return new String[] { base };
		
		for (int i = 0; i < delimIndices.length; i++) {
			String token = base.substring(nextIndex, delimIndices[i]);
//			System.out.println("Token: |" + token + "|, found in substring [" + nextIndex + ", " + delimIndices[i] + "]");
			if (!token.equals("")) {
				tokens.add(token);
			}
			nextIndex = delimIndices[i] + 1;
		}
		
		int indexOfLastToken = delimIndices[delimIndices.length - 1] + 1;
		if (indexOfLastToken < base.length()) {
			String endToken = base.substring(indexOfLastToken);
			if (!endToken.equals(""))
				tokens.add(endToken);
		}
		
		return tokens.toArray(new String[tokens.size()]);
	}
	
	public static String ignoreWhitespace(String base) {
		StringBuilder sb = new StringBuilder();
		for(char c : base.toCharArray()) {
			if (!Character.isWhitespace(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static int[] indexOfAll(String base, String checks, int from, int to) {
		ArrayList<Integer> indices = new ArrayList<>();

		for (int i = from; i < to && i < base.length(); i++) {
			if (checks.contains(base.charAt(i) + "")) {
				indices.add(i);
			}
		}
		if (indices.isEmpty()) 
			return new int[] { -1 };
		
		int[] result = new int[indices.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = indices.get(i);
		
		return result;
	}
	
	public static int[] indexOfAll(String base, String checks, int from) {
		return Reader.indexOfAll(base, checks, from, base.length());
	}
	
	public static int[] indexOfAll(String base, String checks) {
		return Reader.indexOfAll(base, checks, 0, base.length());
	}
	
	public static int indexOfAny(String base, String checks, int from, int to) {
		if (from < 0 || to > base.length())
			return -1;
		
		for (int i = from; i < to; i++) {
			if (checks.contains(base.charAt(i) + ""))
				return i;
		}
		
		return -1;
	}
	
	public static int indexOfAny(String base, String checks, int from) {
		return indexOfAny(base, checks, from, base.length());
	}
	
	public static int indexOfAny(String base, String checks) {
		return indexOfAny(base, checks, 0, base.length());
	}
	
}
