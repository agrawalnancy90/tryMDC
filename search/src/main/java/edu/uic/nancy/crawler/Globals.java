package edu.uic.nancy.crawler;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Globals {
	
	public static double N = 100; 
	public static double collectionSize = 1;
	public static Map<String, List<String>> invertedIndex = new HashMap<>();
	public static Map<String, Map<String, Double>> tfMap = new HashMap<>();
	public static Map<String, Double> maxTfMap = new HashMap<>();
	public static Map<String, Double> tokenCountMap = new HashMap<>();
	public static Map<String, Double> docVectorLengthMap = new HashMap<>();
	public static Map<String, List<String>> pageDataMap = new HashMap<>();
	public static Set<URL> urlSet = new HashSet<>();
	public static List<URL> urlQueue = new ArrayList<>();

}
