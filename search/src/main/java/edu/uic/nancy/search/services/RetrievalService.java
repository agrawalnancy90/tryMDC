package edu.uic.nancy.search.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import edu.uic.nancy.crawler.Globals;
import edu.uic.nancy.crawler.TextProcessor;
import edu.uic.nancy.search.models.SearchQuery;
import edu.uic.nancy.search.models.SearchResult;


@Service
public class RetrievalService {
	
	public List<SearchResult> getResultsByQuery(SearchQuery query) {	
		Map<String, Double> resultWeightsMap = retrieve(query.getQuery());
		List<SearchResult> results = new ArrayList<>();
		int c = 0;
		
		for(String url: resultWeightsMap.keySet()) {
			++c;
			/*SearchResult r = new SearchResult(Globals.pageDataMap.get(url).get(0), 
					Globals.pageDataMap.get(url).get(1),
					url);*/
			if(c > 100)
				return results;
			SearchResult r = new SearchResult(url," ", url);
			results.add(r);
		}
				
		return results;
	}
	
	
	private Map<String, Double> retrieve(String query) {
		List<String> tokens = TextProcessor.preprocessLine(query);
		
		double queryVectorLength = 0;
		Map<String, Double> queryMap = getQueryMapWithWeights(tokens);
		for(double w: queryMap.values()) {
			queryVectorLength += (w*w);
		}
		
		Map<String, Double> resultWeightsMap = new HashMap<>();
		
		for(String token: tokens) {
			if(Globals.invertedIndex.containsKey(token)) {
				List<String> docs = Globals.invertedIndex.get(token);
				for(String doc: docs) {
					Map<String, Double> fMap = Globals.tfMap.get(doc);
					double tf = fMap.get(token) / Globals.maxTfMap.get(doc);
					double idf = Math.log(Globals.maxTfMap.size()/Globals.invertedIndex.get(token).size())/ Math.log(2);
					double w = tf*idf;
					resultWeightsMap.merge(doc, w * queryMap.get(token), (a, b) -> a + b);
				}
			}
		}
		
		//# Now divide the scores by corresponding lengths
		for(String doc : resultWeightsMap.keySet()) {
			resultWeightsMap.merge(doc, Math.sqrt(Globals.docVectorLengthMap.get(doc) * queryVectorLength), (a, b) -> a / b);
		
		}
		//sort by values, and reserve it, 10,9,8,7,6...
        Map<String, Double> resultMap = resultWeightsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		return resultMap;	
	}
	
	private Map<String, Double> getQueryMapWithWeights(List<String> tokens) {
		Map<String, Double> queryMap = new HashMap<>();
		for(String token: tokens)
			queryMap.merge(token, 1.0, (a, b) -> a + b);

		double maxFreq = 1;
		for(double v: queryMap.values())
			maxFreq = Math.max(v, maxFreq);
		
		double tf = 0, idf = 0, w;
		for(String token: queryMap.keySet()) {
			tf = queryMap.get(token) / maxFreq;
			if(Globals.invertedIndex.containsKey(token)) {
				//System.out.println("collectionSize: " + Globals.collectionSize);
				idf = Math.log(Globals.maxTfMap.size()/Globals.invertedIndex.get(token).size())/ Math.log(2);
			}
			w = tf * idf;
			queryMap.put(token, w);
		}
		return queryMap;
	}

}
