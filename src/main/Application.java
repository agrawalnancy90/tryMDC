package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import utils.Constants;


public class Application {
	
	
	public static void main(String[] args) throws IOException {
		Set<URL> urls = new HashSet<>();
		Queue<URL> urlQueue = new LinkedList<>();
		URL seed = new URL("https://www.cs.uic.edu/");
		urls.add(seed);
		urlQueue.offer(seed);
		crawl(urls, urlQueue);
	}
	
	private static void crawl(Set<URL> urls, Queue<URL> urlQueue) throws IOException {
		
		while(!urlQueue.isEmpty()) {
			URL url = urlQueue.poll();
			if(uicDomain(url)) {
				Set<URL> inPageUrls = fetchUrls(url, urls);
				if(inPageUrls != null) {
					for(URL u: inPageUrls) {
						if(!urls.contains(u)) {
							urls.add(u);
							urlQueue.offer(u);
						}
					}
				}
				break;
				//System.out.println(urls.size());
			}
		}
	}
	
	
	private static Set<URL> fetchUrls(URL url, Set<URL> urls) {	
		Set<String> allUrls = new HashSet<>();
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream()));

			String resourceName = url.toString();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				processLine(resourceName, inputLine, allUrls);
			}
			in.close();
		} catch(IOException e) {
			//System.out.println("Error in URL" + url);
			int index = url.toString().indexOf(";");
			if(index != -1) {
				urls.remove(url);
				String cleaned = url.toString().substring(0, index);
				URL newUrl;
				try {
					newUrl = new URL(cleaned);
					allUrls = retryCleanedUp(newUrl);
				} catch (MalformedURLException e1) {
					//System.out.println("Malformed after ; removal " + cleaned);
				}
			}
		}
		
		Set<URL> result = new HashSet<>();
		for(String x: allUrls) {
			try {
				result.add(new URL(x));
			} catch (MalformedURLException e) {
				continue;
			}
		}
		return result;
	}
	
	
	private static Set<String> retryCleanedUp(URL url){
		Set<String> allUrls = new HashSet<>();
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream()));

			String resourceName = url.toString();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				processLine(resourceName, inputLine, allUrls);
			}
			in.close();
		} catch(IOException e) {
			//System.out.println("Retry Error in URL " + url);
		}
		
		return allUrls;
	}
	
	
	private static void processLine(String url, String line, Set<String> allUrls) {
		if(line == null || line.isEmpty())
			return;
		line = line.trim();
		if(line == null || line.isEmpty())
			return;
		
		if(isScript(line))
			return;
		
		
		String[] tokens = line.split(" ");
		for(String token: tokens) {
			if(token != null && !token.trim().isEmpty()) {
				Set<String> urls = getURLsInString(token);
				allUrls.addAll(urls);
			}
		}
		
		/* Remove SGML Tags and script stuff*/
		if(Pattern.matches("<[^>]*>", line) || line.matches(".*[\\[\\]].*") || 
				line.matches(".*[\\(\\)].*") || line.matches(".*[\\{\\}].*") ||
				line.contains("!important;") || line.matches("class=\".*\""))
			return;
		
		System.out.println("line: " +  line);
		
		line = line.replaceAll("<[^>]*>", "");
		line = line.replaceAll("[.]", " ");
		line = line.replaceAll("nbsp", "");
		tokens = line.split(" ");
		//int i = 0;
		for(String token: tokens) {
			if(token != null && !token.trim().isEmpty()) {
				token = removePunctuation(token);
				if(!token.isEmpty() && !Constants.__STOP_WORDS.contains(token.trim().toLowerCase())) {
					token = token.trim().toLowerCase();
					System.out.println(token);
					//++i;
				}
			}
		}
		//System.out.println("Count: " + i);
	}	
	
	private static boolean isScript(String line) {
		if(line.contains("script>"))
			return true;
		return false;
	}
	
	private static String removePunctuation(String token) {
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
	
	private static Set<String> getURLsInString(String str){
		Set<String> urls = new HashSet<>();;
		if(str == null)
			return urls;
		if(str.contains("http") && str.contains("uic.edu")) {
			int index = str.indexOf("http");
			String url = cleanupURL(str.substring(index));
			urls.add(url);
		} else if(str.contains("www") && str.contains("uic.edu")) {
			int index = str.indexOf("www");
			String url = cleanupURL(str.substring(index));
			urls.add(url);
		} 
		return urls;
	}
	
	
	private static String cleanupURL(String url) {
		int index = url.indexOf("\"");
		if(index != -1) {
			url = url.substring(0, index);
		}
		index = url.indexOf("\'");
		if(index != -1) {
			url = url.substring(0, index);
		}
		url = url.replace("\\", "");
		if(url.startsWith("www")) {
			url = "http://" + url;
		}
		return url;
	}
	
	
	private static boolean uicDomain(URL checkUrl) {
		String url = checkUrl.toString();
		//System.out.println("URL: " + url);
		String[] p = url.split("/");
		if(p[0].startsWith("http")) {
			if(p[2].endsWith("uic.edu"))
				return true;
		} else {
			if(p[0].endsWith("uic.edu"))
				return true;
		}
		return false;
	}

}
