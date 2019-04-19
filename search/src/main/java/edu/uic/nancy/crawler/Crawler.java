package edu.uic.nancy.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;


public class Crawler {
	
	public Crawler() {
		Set<URL> urls = new HashSet<>();
		Queue<URL> urlQueue = new LinkedList<>();
		URL seed = null;
		try {
			seed = new URL("https://www.cs.uic.edu/");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		urls.add(seed);
		urlQueue.offer(seed);
		
		System.out.println("Crawling and creating index for " + Globals.N + " pages . . .");
		Instant start = Instant.now();
		
		try {
			crawl(urls, urlQueue);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n\nInverted Index\n");
		//System.out.println(invertedIndex.toString());
		
		System.out.println("\n\nMaxtfmap\n");
		//System.out.println(maxTfMap.toString());
		
		System.out.println("\n\tokenCountMap\n");
		System.out.println(Globals.tokenCountMap.toString());
		
		System.out.println("\n\tfMap\n");
		System.out.println(Globals.tfMap.toString());
		
		
		System.out.println("Updating document vector lengths . . .");
		updateDocVectLenMap();
		System.out.println("\ndocVectorLengthMap\n");
		System.out.println(Globals.docVectorLengthMap.toString());
				
		Instant finish = Instant.now();
		
		long timeElapsed = Duration.between(start, finish).toMinutes();  //in minutes
		System.out.println("Time taken to prepare the search engine for " + Globals.N + " documents: " 
				+ timeElapsed + " minutes");

	}
	
	private static void updateDocVectLenMap() {
		for(String term : Globals.invertedIndex.keySet()) {
			double idf = Math.log(Globals.collectionSize/Globals.invertedIndex.get(term).size())/ Math.log(2);
			List<String> docs = Globals.invertedIndex.get(term);
			for(String doc : docs) {
				double normalizedTF = Globals.tfMap.get(doc).get(term) / Globals.maxTfMap.get(doc);
				double w = normalizedTF * idf;
				Globals.docVectorLengthMap.merge(doc, w*w, (a,b) -> a + b);
			}
		}
	}
	
	
	private static void crawl(Set<URL> urls, Queue<URL> urlQueue) throws IOException {
		
		while(!urlQueue.isEmpty()) {
			System.out.println(urls.size());
			URL url = urlQueue.poll();
			if(uicDomain(url)) {
				Set<URL> inPageUrls = fetchUrls(url, urls);
				if(inPageUrls != null) {
					for(URL u: inPageUrls) {
						if(!urls.contains(u)  && urls.size() < Globals.N) {
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
		Map<String, Double> fMap = new HashMap<>();
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream()));

			String resourceName = url.toString();
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				processLine(resourceName, inputLine, allUrls, fMap);
			
			if(fMap.size() > 0) {
				++Globals.collectionSize;
				System.out.println("******************* SIZE " + Globals.collectionSize);
				Globals.pageDataMap.put(url.toString(), getPageTitleAndExcerptInList(url));
				Globals.tfMap.put(url.toString(), fMap);
				double max = 0;
				for(String t: fMap.keySet()) {
					if(Globals.invertedIndex.containsKey(t)) {
						Globals.invertedIndex.get(t).add(url.toString());
					} else {
						Globals.invertedIndex.put(t, new ArrayList<String>());
						Globals.invertedIndex.get(t).add(url.toString());
					}
					Globals.tokenCountMap.merge(t, fMap.get(t), (a, b) -> a + b);
					if(fMap.get(t) > max)
						max = fMap.get(t);
				}
				Globals.maxTfMap.put(url.toString(), max);
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
		Map<String, Double> fMap = new HashMap<>();
		try {
			
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream()));

			String resourceName = url.toString();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				processLine(resourceName, inputLine, allUrls, fMap);
			}
			if(fMap.size() > 0) {
				++Globals.collectionSize;
				Globals.tfMap.put(url.toString(), fMap);
				Globals.pageDataMap.put(url.toString(), getPageTitleAndExcerptInList(url));
				double max = 0;
				for(String t: fMap.keySet()) {
					if(Globals.invertedIndex.containsKey(t)) {
						Globals.invertedIndex.get(t).add(url.toString());
					} else {
						Globals.invertedIndex.put(t, new ArrayList<String>());
						Globals.invertedIndex.get(t).add(url.toString());
					}
					Globals.tokenCountMap.merge(t, fMap.get(t), (a, b) -> a + b);
					if(fMap.get(t) > max)
						max = fMap.get(t);
				}
				Globals.maxTfMap.put(url.toString(), max);
			}
						
			in.close();
		} catch(IOException e) {
			//System.out.println("Retry Error in URL " + url);
		}
		
		return allUrls;
	}
	
	
	private static void processLine(String url, String line, Set<String> allUrls,
			Map<String, Double> fMap) {
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
		if(Pattern.matches("<[^>]*>", line) || line.matches("<[^>]*></[^>]*>")
				|| line.matches(".*[\\[\\]].*") || line.matches("href=\"[^\"]*\"") ||
				line.matches(".*[\\(\\)].*") || line.matches(".*[\\{\\}].*") ||
				line.contains("!important;") || line.matches("class=\".*\"") ||
				line.matches(".*[\"\'];") || line.matches("target=\".*\">") ||
				line.matches(".*;"))
			return;
				
		line = line.replaceAll("<[^>]*>", "");
		line = line.replaceAll("[.]", " ");
		line = line.replaceAll("nbsp", "");
		if(line.isEmpty() || line.trim().isEmpty())
			return;
		
		List<String> processedTokens = TextProcessor.preprocessLine(line);
		for(String t : processedTokens)
			fMap.merge(t, 1.0, (a, b) -> a + b);
	}
	
	
	private static boolean isScript(String line) {
		if(line.contains("script>"))
			return true;
		return false;
	}
	
	
	private static Set<String> getURLsInString(String str){
		Set<String> urls = new HashSet<>();
		String url = "";
		if(str == null)
			return urls;
		if(str.contains("http") && str.contains("uic.edu")) {
			int index = str.indexOf("http");
			url = cleanupURL(str.substring(index));
			urls.add(url);
		} else if(str.contains("www") && str.contains("uic.edu")) {
			int index = str.indexOf("www");
			url = cleanupURL(str.substring(index));
			urls.add(url);
		}
		//ignore images
		if(url.matches(".*(.jpg|.png|.gif|.bmp|.jpeg)$"))
			urls.remove(url);
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

	private static List<String> getPageTitleAndExcerptInList(URL url) {
		List<String> res = new ArrayList<>();
		String title = "";
		String excerpt = "";
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream()));

			String inputLine;
			//String titleStart = "";
			while ((inputLine = in.readLine()) != null) {
				if(inputLine.toLowerCase().contains("<title>")) {
					title += inputLine;
				} else if(!title.isEmpty()) {
					title += inputLine;
				}
				if(inputLine.toLowerCase().contains("</title>")) {
					break;
				}
			}
			
			in.close();
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			while ((inputLine = in.readLine()) != null) {
				if(inputLine.toLowerCase().contains("<p>")) {
					excerpt += inputLine;
				} else if(!excerpt.isEmpty()) {
					excerpt += inputLine;
				}
				if(inputLine.toLowerCase().contains("</p>")) {
					if(!(excerpt.replace("<p>", "").replace("</p>", "").trim().isEmpty())) {
						System.out.println("breaking*******");
						break;
					} else {
						excerpt = "";
					}	
				}
			}
			
			
			if(!title.isEmpty()) {
				int startIndex = title.toLowerCase().indexOf("<title>");
				int endIndex = title.toLowerCase().indexOf("</title>");
				title = title.substring(startIndex + "<title>".length(), endIndex).trim();
			}
			
			if(!excerpt.isEmpty()) {
				int startIndex = excerpt.toLowerCase().indexOf("<p>");
				int endIndex = excerpt.toLowerCase().indexOf("</p>");
				excerpt = excerpt.substring(startIndex + "<p>".length(), endIndex).trim();
			}
			
			System.out.println(title);
			System.out.println("text: " + excerpt);
			
			res.add(title);
			res.add(excerpt);
			
			in.close();
		} catch(IOException e) {
			//System.out.println("Error in URL" + url);
		}
		return res;
	}
	
}
