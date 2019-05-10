package edu.uic.nancy.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextProcessor {
	
	public static List<String> preprocessLine(String line) {
		String[] tokens = line.split(" ");
		List<String> processedTokens = new ArrayList<>();
		for(String token: tokens) {
			token = removePunctuation(token);
			token = removeNumbers(token);
			if(!token.isEmpty() && !Constants.__STOP_WORDS.contains(token.trim().toLowerCase())) {
				token = token.trim().toLowerCase();
				if(token.length() > 2) {
					token = getPorterStem(token);
					if(!Constants.__STOP_WORDS.contains(token.trim()))
						processedTokens.add(token);
				}
			}
		}
		
		//System.out.println(processedTokens.toString());
		return processedTokens;
	}
	
	
	private static String removeNumbers(String token) {
		String s = "";
		if(token != null && !token.trim().isEmpty()) {
			char[] letters = token.toCharArray();
			for(char c: letters) {
				if(!Character.isDigit(c))
					s += c;
				}
		}
		return s;
	}
	
	private static String getPorterStem(String token) {
		PorterStemmer stemmer = new PorterStemmer();
		return stemmer.stripAffixes(token);
	}
	
	public static String removePunctuation(String token) {
		StringBuilder sb = new StringBuilder();
		if(token != null && !token.trim().isEmpty()) {
			char[] letters = token.toCharArray();
			for(char c: letters) {
				int x = c;
				if(Character.isDigit(c) || (!Pattern.matches("\\p{Punct}", String.valueOf(c)) &&
						isAscii(x))) {
					sb.append(c);
				}
			}
		}
		//System.out.println(sb);			
		return sb.toString();
	}

	private static boolean isAscii(int c) {
		return c <= 0x7F;
	}
	
}
