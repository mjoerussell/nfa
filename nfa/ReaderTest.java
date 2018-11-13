package nfa;

import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.Ignore;

public class ReaderTest {

	@Test
	public void tokenize() {
		String base = ":hello;/world";
		String delims = ":;/";
		
		String[] actualTokens = Reader.tokenize(base, delims);
		String[] expectedTokens = new String[] { "hello", "world" };
		
		assertArrayEquals(expectedTokens, actualTokens);
	}

	@Test @Ignore
	public void matchWithTokenPrefix() {
		String base = "1,2,3";
		String regex = "\\D";
		
		String[] actualTokens = Reader.tokenize(base, regex);
		String[] expectedTokens = new String[] { "1", "2", "3" };
		
		assertArrayEquals(expectedTokens, actualTokens);
	}
	
	@Test
	public void tokenizeWithManyDelimsInBase() {
		String base = "{hello}{world}{bye}";
		String delims = "{}";
		
		String[] actualTokens = Reader.tokenize(base, delims);
		String[] expectedTokens = new String[] { "hello", "world", "bye" };
		
		assertArrayEquals(expectedTokens, actualTokens);
		
	}
	
	@Test
	public void ignoreWhitespace() {
		String base = "  	a  	b";
		String actualResult = Reader.ignoreWhitespace(base);
		
		assertEquals("ab", actualResult);
	}
	
	@Test
	public void indexOfAnyTest() {
		String base = "abcdefg";
		String checks = "bdf";
		
		int index = Reader.indexOfAny(base, checks);
		
		assertEquals(1, index);
	}
	
	@Test
	public void indexOfAnyNoneFoundTest() {
		String base = "abcdefg";
		String checks = "h";
		
		int index = Reader.indexOfAny(base, checks);
		
		assertEquals(-1, index);
	}
	
	@Test
	public void indexOfAnyFromAndToTest() {
		String base = "abcdefg";
		String checks = "bdf";
		
		int index = Reader.indexOfAny(base, checks, 2, 5);
		
		assertEquals(3, index);
		
	}
	
	@Test
	public void indexOfAll() {
		String base = ":hello;/world";
		String checks = ":;/";
		
		int[] actualIndices = Reader.indexOfAll(base, checks);
		int[] expectedIndices = new int[] { 0, 6, 7 };
		
		assertArrayEquals(expectedIndices, actualIndices);
	}
	
	@Test
	public void indexOfAllMultiples() {
		String base = "{hello}{world}{bye}";
		String checks = "{}";
		
		int[] actualIndices = Reader.indexOfAll(base, checks);
		int[] expectedIndices = new int[] { 0, 6, 7, 13, 14, 18 };
		
		assertArrayEquals(expectedIndices, actualIndices);
	}
	
	@Test
	public void indexOfAllNoneFound() {
		String base = "abcdefg";
		String checks = "hij";
		
		int[] actualIndices = Reader.indexOfAll(base, checks);
		int[] expectedIndices = new int[] { -1 };
		
		assertArrayEquals(expectedIndices, actualIndices);
	}
	
}
