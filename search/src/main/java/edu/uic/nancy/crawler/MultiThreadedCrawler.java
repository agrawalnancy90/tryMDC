package edu.uic.nancy.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiThreadedCrawler implements Runnable {
	
	String name;
	Thread t;
	
	int count;
	double maxCount;
	String seedURL;
	String domain;
	//List<URL> urlQueue = new ArrayList<>();
	Map<String, List<String>> pageDataMap = new HashMap<>();
	Map<String, Map<String, Double>> tfMap = new HashMap<>();
	Map<String, List<String>> invertedIndex = new HashMap<>();
	Map<String, Double> tokenCountMap = new HashMap<>();
	Map<String, Double> maxTfMap = new HashMap<>();
	public Map properties = new HashMap();
	
	public MultiThreadedCrawler(String seedURL, String domain, String name) {
		this.name = name;
		this.seedURL = seedURL;
		this.domain = domain;
		
		switch(name) {
			case "main": maxCount = 0.01*Globals.N;
			break;
			/*case "uic": maxCount = 18*Globals.N;
			break;
			case "lib": maxCount = 1.3*Globals.N;
			break;
			case "advance": maxCount = 3*Globals.N;
			break;
			case "cs":
			case "uicarchives": maxCount = 5*Globals.N;*/
			default: maxCount = 5*Globals.N;
		}
		
	}
	
	public Map getResults() {
		System.out.println("Completed thread " + name + " prop: " + properties);
		properties.put("pageDataMap", pageDataMap);
		properties.put("invertedIndex", invertedIndex);
		properties.put("tfMap", tfMap);
		properties.put("maxTfMap", maxTfMap);
		properties.put("tokenCountMap", tokenCountMap);
		return properties;
	}

	public void start() {
		String url = seedURL;
		url = Util.formatURL(url);
		try {
			URL seed = new URL(url);
			//if(!Globals.urlSet.contains(seed)) {
			synchronized (Globals.urlSet) {
				//Globals.urlSet.add(seed);
				Globals.urlQueue.add(seed);
			}
			
			//}
			crawl();
			//updateDocVectLenMap();
		} catch (MalformedURLException e) {
			System.out.println("A malformed URL is present in root list " + url);
			e.printStackTrace();
		}	
	}
	
	private void crawl() {
		while(!Globals.urlQueue.isEmpty() && pageDataMap.size() < maxCount) {
			URL url = null;
			synchronized (Globals.urlQueue) {
				url = Globals.urlQueue.get(0);
				Globals.urlQueue.remove(0);	
				
			}
			boolean visited = false;
			synchronized (Globals.urlSet) {
				visited = Util.containsHttps(Globals.urlSet, url);
				Globals.urlSet.add(url);
			}
			if(!visited && 
					Util.validDomain(url, domain) && Util.robotSafe(url)) {
				Set<String> inPageUrls = fetchUrls(url);
				for(String u: inPageUrls) {
					u = Util.formatURL(u);
					URL nextURL;
					try {
						nextURL = new URL(u);
						synchronized (Globals.urlQueue) {
							//if(!Util.containsAndReplaceWithHttps(Globals.urlSet, urlQueue, nextURL)) {	
								//Globals.urlSet.add(nextURL);
								Globals.urlQueue.add(nextURL);
							//}
						}
					} catch (MalformedURLException e) {
						System.out.println("Found MalformedURL inPage " + u);
						e.printStackTrace();
					}
				}
				System.out.println(name + " : " + pageDataMap.size());
			}
			//synchronized (Globals.urlSet) {
				//Globals.urlSet.add(url);
			//}
		}	
	}

	
	private Set<String> fetchUrls(URL url) {		
		Set<String> allUrls = new HashSet<>();
		Map<String, Double> fMap = new HashMap<>();
		try {
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			int code = connection.getResponseCode();
			if(code > 200) {
				String redirectURLStr = connection.getHeaderField("Location");
				if(redirectURLStr != null && !redirectURLStr.isEmpty())
					allUrls.add(redirectURLStr);
				connection.disconnect();
				return allUrls;
			}
			connection.disconnect();

			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null)
				Util.processLine(url.toString(), inputLine, allUrls, fMap);
			
			if(fMap.size() > 0) {
				System.out.println(url);
				updateMaps(url, fMap);
			}
			
			in.close();
		} catch(IOException e) {
			System.out.println("Some error occurred while fetching URLs from " + url.toString());
		}
		
		return allUrls;
	}

	
	void updateMaps(URL url, Map<String, Double> fMap) {
		pageDataMap.put(url.toString(), Util.getPageTitleAndExcerptInList(url));
		tfMap.put(url.toString(), fMap);
		double max = 0;
		for(String t: fMap.keySet()) {
			if(invertedIndex.containsKey(t)) {
				invertedIndex.get(t).add(url.toString());
			} else {
				invertedIndex.put(t, new ArrayList<String>());
				invertedIndex.get(t).add(url.toString());
			}
			tokenCountMap.merge(t, fMap.get(t), (a, b) -> a + b);
			if(fMap.get(t) > max)
				max = fMap.get(t);
		}
		maxTfMap.put(url.toString(), max);
	}
	
	@Override
	public void run() {
		crawl();		
	}
	
	
}
