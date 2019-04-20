package edu.uic.nancy.search.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uic.nancy.search.models.SearchQuery;
import edu.uic.nancy.search.models.SearchResult;
import edu.uic.nancy.search.services.RetrievalService;

@RestController
public class QueryController {
	
	@Autowired
    RetrievalService retrievalService;
	
	@CrossOrigin(origins = "*")
	@GetMapping("/query")
	public List<SearchResult> getResultsByQuery(
			@RequestParam(value = "tokens", required = false) String query){
		System.out.println(query);
		SearchQuery q = new SearchQuery(query);
		List<SearchResult> allResults = retrievalService.getResultsByQuery(q);
		
		return allResults;		
	}

	@CrossOrigin(origins = "*")
	@GetMapping("/test")
	public String getHello(){
		return "HelloWorld!";
				
	}

	
}
