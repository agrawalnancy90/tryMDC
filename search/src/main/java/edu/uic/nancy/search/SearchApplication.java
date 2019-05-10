package edu.uic.nancy.search;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import edu.uic.nancy.crawler.Globals;
import edu.uic.nancy.crawler.MultiThreadedCrawler;
import edu.uic.nancy.crawler.Util;


@SpringBootApplication
public class SearchApplication {
	
	//private static String rootURL;
	
	public static void main(String[] args) {
		
		/*MultiThreadedCrawler crawler = new MultiThreadedCrawler("https://cs.uic.edu", "uic.edu", "main");
		crawler.start();
		List<Map> p = new ArrayList<>();
		p.add(crawler.getResults());
		
		try {
			Globals.urlSet.add(new URL("https://disabilityresources.uic.edu"));
			Globals.urlSet.add(new URL("http://disabilityresources.uic.edu"));
			Globals.urlSet.add(new URL("http://cmhsrp.uic.edu/nrtc")); 

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		MultiThreadedCrawler c1 = new MultiThreadedCrawler("", "uic.edu", "c1");
		MultiThreadedCrawler c2 = new MultiThreadedCrawler("", "uic.edu", "c2");
		MultiThreadedCrawler c3 = new MultiThreadedCrawler("", "uic.edu", "c3");
		MultiThreadedCrawler c4 = new MultiThreadedCrawler("", "uic.edu", "c4");
		MultiThreadedCrawler c5 = new MultiThreadedCrawler("", "uic.edu", "c5");
		MultiThreadedCrawler c6 = new MultiThreadedCrawler("", "uic.edu", "c6");
		//MultiThreadedCrawler c7 = new MultiThreadedCrawler("https://housing.uic.edu", "uic.edu", "housing");
		//MultiThreadedCrawler c8 = new MultiThreadedCrawler("https://grad.uic.edu", "uic.edu", "grad");
		//MultiThreadedCrawler c9 = new MultiThreadedCrawler("https://today.uic.edu", "uic.edu", "today");
		
		
		
		List<Thread> crawlers = new ArrayList<>();
		crawlers.add(new Thread(c1, "c1"));
		crawlers.add(new Thread(c2, "c2"));
		crawlers.add(new Thread(c3, "c3"));
		crawlers.add(new Thread(c4, "c4"));
		crawlers.add(new Thread(c5, "c5"));
		crawlers.add(new Thread(c6, "c6"));
		crawlers.add(new Thread(c7, "housing"));
		crawlers.add(new Thread(c8, "grad"));
		crawlers.add(new Thread(c9, "today"));
		
		
		for(Thread t: crawlers) {
			t.start();
		}
		
		for(Thread t: crawlers) {
			try {
				t.join();
			}catch(InterruptedException e) {
				
			}
		}
		
		
		p.add(c1.getResults());
		p.add(c2.getResults());
		p.add(c3.getResults());
		p.add(c4.getResults());
		p.add(c5.getResults());
		p.add(c6.getResults());
		//p.add(c7.getResults());
		//p.add(c8.getResults());
		//p.add(c9.getResults());
		
		
		while(true) {
			for(Map m: p) {
				if(m == null)
					continue;
			}
			break;
		}
		
		System.out.println("All done");
		mergeResults(p);
		Util.updateDocVectLenMap();
		System.out.println("Total in collection: " + Globals.pageDataMap.size());
		Util.writeInvertedIndexTfMapAndMaxFreqMapToFile();*/
		
		
		Util.loadDataFromFile();
		Util.updateDocVectLenMap();
		SpringApplication.run(SearchApplication.class, args);
	}
	
	
	private static void mergeResults(List<Map> allProps) {
		int i = 0;
		for(Map p : allProps) {
			Map<String, List<String>> invertedIndex = (Map<String, List<String>>) p.get("invertedIndex");
			if(invertedIndex != null) {
				for(String s: invertedIndex.keySet()) {
					if(!Globals.invertedIndex.containsKey(s)) {
						Globals.invertedIndex.put(s, invertedIndex.get(s));
					} else {
						List<String> docs = Globals.invertedIndex.get(s);
						for(String u: invertedIndex.get(s)) {
							if(!docs.contains(u)) {
								Globals.invertedIndex.get(s).add(u);
							}
						}
					}
				}
			} else {
				System.out.println("****** InvertedIndex null for " + i);
			}

			
			Map<String, Map<String, Double>> tfMap = (Map<String, Map<String, Double>>) p.get("tfMap");
			if(tfMap != null) {
				for(String s: tfMap.keySet()) {
					if(!Globals.tfMap.containsKey(s)) {
						Map<String, Double> fMap = tfMap.get(s);
						Globals.tfMap.put(s, fMap);
						for(String token: fMap.keySet()) {
							Globals.tokenCountMap.merge(token, fMap.get(token), (a, b) -> a + b);
						}
					}
				}
			} else {
				System.out.println("****** tfMap null for " + i);
			}
			
			
			Map<String, Double> maxTfMap = (Map<String, Double>) p.get("maxTfMap");
			if(maxTfMap != null) {
				for(String s: maxTfMap.keySet()) {
					if(!Globals.maxTfMap.containsKey(s)) {
						Globals.maxTfMap.put(s, maxTfMap.get(s));
					}
				}
			} else {
				System.out.println("****** maxTfMap null for " + i);
			}
			
			/*Map<String, Double> tokenCountMap = (Map<String, Double>) p.get("tokenCountMap");
			if(tokenCountMap != null) {
				for(String s: tokenCountMap.keySet()) {
					if(!Globals.tokenCountMap.containsKey(s)) {
						Globals.tokenCountMap.put(s, tokenCountMap.get(s));
					}
				}
			} else {
				System.out.println("****** tokenCount null for " + i);
			}*/
			
			Map<String, List<String>> pageDataMap = (Map<String, List<String>>) p.get("pageDataMap");
			if(pageDataMap != null) {
				for(String s: pageDataMap.keySet()) {
					if(!Globals.pageDataMap.containsKey(s)) {
						Globals.pageDataMap.put(s, pageDataMap.get(s));
					}
				}	
			} else {
				System.out.println("****** pageDataMap null for " + i);
			}
			++i;
		}
	}
	
	
}

