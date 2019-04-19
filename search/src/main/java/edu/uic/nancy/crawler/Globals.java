package edu.uic.nancy.crawler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Globals {
	
	public static int N = 10; 
	public static double collectionSize = 1;
	public static Map<String, List<String>> invertedIndex = new HashMap<>();
	public static Map<String, Map<String, Double>> tfMap = new HashMap<>();
	public static Map<String, Double> maxTfMap = new HashMap<>();
	public static Map<String, Double> tokenCountMap = new HashMap<>();
	public static Map<String, Double> docVectorLengthMap = new HashMap<>();
	public static Map<String, List<String>> pageDataMap = new HashMap<>();


}
