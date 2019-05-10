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


public class Crawler {
	
	String rootURL;
	
	Set<URL> urlSet = new HashSet<>();
	List<URL> urlQueue = new ArrayList<>();
	
	public Crawler(String rootURL) {
		this.rootURL = rootURL;
	}
	
	public void start() {
		String url = rootURL;
		url = Util.formatURL(url);
		try {
			URL seed = new URL(url);
			if(!urlSet.contains(seed)) {
				urlSet.add(seed);
				urlQueue.add(seed);
			}
			crawl();
			updateDocVectLenMap();				
		} catch (MalformedURLException e) {
			System.out.println("A malformed URL is present in root list " + url);
			e.printStackTrace();
		}		
	}
	
	private void crawl() {
		//List<URL> urlQueue = new LinkedList<>();
		//urlQueue.add(seed);
		
		while(!urlQueue.isEmpty() 
				&& Globals.pageDataMap.size() < Globals.N) {
			URL url = urlQueue.get(0);
			if(Util.validDomain(url, "uic.edu") && Util.robotSafe(url)) {
	
				Set<String> inPageUrls = fetchUrls(url);
				for(String u: inPageUrls) {
					u = Util.formatURL(u);
					URL nextURL;
					try {
						nextURL = new URL(u);
						if(!Util.containsHttps(urlSet, nextURL)) {
							//System.out.println(nextURL.toString());							
							urlSet.add(nextURL);
							urlQueue.add(nextURL);
						}
					} catch (MalformedURLException e) {
						System.out.println("Found MalformedURL inPage " + u);
						e.printStackTrace();
					}
				}
			}
			System.out.println(Globals.pageDataMap.size());
			urlQueue.remove(0);	
		}
		
	}

	public void updateDocVectLenMap() {
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

			String resourceName = url.toString();
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				Util.processLine(resourceName, inputLine, allUrls, fMap);
			
			if(fMap.size() > 0) {
				updateMaps(url, fMap);
			}
			
			in.close();
		} catch(IOException e) {
			System.out.println("Some error occurred while fetching URLs from " + url.toString());
		}
		
		return allUrls;
	}
	
	void updateMaps(URL url, Map<String, Double> fMap) {
		++Globals.collectionSize;
		Globals.pageDataMap.put(url.toString(), Util.getPageTitleAndExcerptInList(url));
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
			
}
