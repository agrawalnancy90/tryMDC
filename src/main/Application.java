package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;


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
				processText(resourceName, inputLine, allUrls);
			}
			in.close();
			System.out.println(allUrls.toString());
			System.out.println(allUrls.size());
		} catch(IOException e) {
			System.out.println("Error in URL" + url);
			int index = url.toString().indexOf(";");
			if(index != -1) {
				urls.remove(url);
				String cleaned = url.toString().substring(0, index);
				URL newUrl;
				try {
					newUrl = new URL(cleaned);
					allUrls = retryCleanedUp(newUrl);
				} catch (MalformedURLException e1) {
					System.out.println("Malformed after ; removal " + cleaned);
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
				processText(resourceName, inputLine, allUrls);
			}
			in.close();
		} catch(IOException e) {
			System.out.println("Retry Error in URL " + url);
		}
		
		return allUrls;
	}
	
	
	private static void processText(String url, String text, Set<String> allUrls) {
		
		if(text.startsWith("<script>") || text.endsWith("</script>"))
				return;
		String[] tokens = text.split(" ");
		for(String token: tokens) {
			if(token != null && !token.trim().isEmpty()) {
				Set<String> urls = getURLsInString(token);
				allUrls.addAll(urls);
				//System.out.println(token);
			}
		}
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
		System.out.println("URL: " + url);
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
