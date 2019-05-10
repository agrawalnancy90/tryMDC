package edu.uic.nancy.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.regex.Pattern;

public class Util {
	
	private static final String DISALLOW = "Disallow";

	/**
	 * Returns a single format URL. Helps in avoiding duplication due to different representation.
	 * For example, www.cs.uic.edu = cs.uic.edu = cs.uic.edu/
	 * @param url
	 * @return
	 */
	public static String formatURL(String url) {

		//Remove www.
		String[] parts = url.split("://");
		if(parts.length > 1)
			parts[1] = parts[1].replace("www.", "");
		else
			parts[0] = parts[0].replace("www.", "");
		url = "";
		if(parts.length > 1)
			url += parts[0] + "://";
		for(int i = 1; i<parts.length; i++)
			url += parts[i];
		
		try {
			URL u = new URL(url);
			String reference = u.getRef();
			if(reference != null && !reference.isEmpty()) {
				reference = "#" + reference;
				url = u.toString().replace(reference, "");
				//System.out.println(u.toString() + "\n" + url);
			}
		} catch (MalformedURLException e) {
			//
		}
		
		//Remove trailing /
		if(url.endsWith("/"))
			url = url.substring(0, url.length()-1);

		return url;
	}
	
	/**
	 * Validates domain
	 * @param checkUrl
	 * @return
	 */
	
	public static boolean validDomain(URL checkUrl, String domain) {
		if(checkUrl.getHost().endsWith(domain))
			return true;
		return false;
	}
	
	public static boolean validDomainURLString(String checkUrl, String domain) {	
		try {
			URL u = new URL(checkUrl);
			if(u.getHost().endsWith(domain))
				return true;
				
		} catch(MalformedURLException e) {
			return false;
		}		
		return false;
	}
	public static String getLastPartOfURL(String url) {
		int index = url.lastIndexOf('/');
		if(index == -1 || index == url.length())
			return url;
		return url.substring(index + 1);
	}

	synchronized public static boolean containsHttps(Set<URL> urlSet, URL url) {
		Set<String> urls = new HashSet<>();
		for(URL u: urlSet)
			urls.add(u.toString());
		
		String protocol = url.getProtocol();
		String urlStr = url.toString();
		
		String[] parts = urlStr.split("://");
		String option1 = "https://" + parts[1];
		//String option2 = "http://" + parts[1];
		
		if(protocol.equals("http")) {
			if(urls.contains(urlStr) || urls.contains(option1))
				return true;
		} else {
			if(urls.contains(urlStr))
				return true;
			/*if(urls.contains(option2)) {
				try {
					URL candidate = new URL(option2);
					//urlSet.remove(candidate);
					urlQueue.remove(candidate);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}*/	
		}
		
		return false;
	}

	public static boolean robotSafe(URL url) 
	{
		URL urlRobot = getRobotURL(url.getHost());
	    String strCommands = "";
	    try 
	    {
	    	if(urlRobot == null)
	    		return true;
	    	
			BufferedReader in = new BufferedReader(
					new InputStreamReader(urlRobot.openStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null)
				strCommands += (inputLine + "\n");
			in.close();
	    }
	    catch (IOException e) 
	    {
	        return true; // if there is no robots.txt file, it is OK to search
	    }

	    if (strCommands.contains(DISALLOW)) // if there are no "disallow" values, then they are not blocking anything.
	    {
	        String[] split = strCommands.split("\n");
	        ArrayList<RobotRule> robotRules = new ArrayList<>();
	        String mostRecentUserAgent = null;
	        for (int i = 0; i < split.length; i++) 
	        {
	            String line = split[i].trim();
	            if (line.toLowerCase().startsWith("user-agent")) 
	            {
	                int start = line.indexOf(":") + 1;
	                int end   = line.length();
	                mostRecentUserAgent = line.substring(start, end).trim();
	            }
	            else if (line.startsWith(DISALLOW)) {
	                if (mostRecentUserAgent != null) {
	                    RobotRule r = new RobotRule();
	                    r.userAgent = mostRecentUserAgent;
	                    int start = line.indexOf(":") + 1;
	                    int end   = line.length();
	                    r.rule = line.substring(start, end).trim();
	                    robotRules.add(r);
	                }
	            }
	        }

	        for (RobotRule robotRule : robotRules)
	        {
	        	//System.out.println("*** Robot rule: " + robotRule.toString());
	            String path = url.getPath();
	            if (robotRule.rule.length() == 0) return true; // allows everything if BLANK
	            if (robotRule.rule == "/") {
	            	System.out.println("Not safe URL: " + url);
	            	System.out.println("User Agent: " + robotRule.userAgent);
	            	System.out.println("Rule: " + robotRule.rule);
	            	return false;       // allows nothing if /}
	            }

	            if (robotRule.rule.length() <= path.length())
	            { 
	                String pathCompare = path.substring(0, robotRule.rule.length());
	                if (pathCompare.equals(robotRule.rule)) {
	                	System.out.println("Not safe URL: " + url);
		            	System.out.println("User Agent: " + robotRule.userAgent);
		            	System.out.println("Rule: " + robotRule.rule);

	                	return false;
	                }
	            }
	        }
	    }
	    return true;
	}
	     
	private static URL getRobotURL(String url) {
	    String strRobot = "http://" + url + "/robots.txt";
	    URL urlRobot = null;
	    try {
	    	urlRobot = new URL(strRobot);
			HttpURLConnection connection = (HttpURLConnection)urlRobot.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			int code = connection.getResponseCode();
			if(code > 200) {
				String redirectURLStr = connection.getHeaderField("Location");
				if(redirectURLStr != null && !redirectURLStr.isEmpty() 
						&& redirectURLStr.endsWith("robots.txt"))
					urlRobot = new URL(redirectURLStr);	
			}
			connection.disconnect();
	    } catch (MalformedURLException e) {
	        return null; 
	    } catch (IOException e) {
			e.printStackTrace();
		}
	    return urlRobot;

	}

	public static void processLine(String url, String line, Set<String> allUrls,
			Map<String, Double> fMap) {
		if(line == null || line.trim().isEmpty())
			return;
		line = line.trim();
		
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
		str = str.replaceAll("[\\)]", "");
		str = str.replaceAll("</guid>", "");
		str = str.replaceAll("</link>", "");
		
		if(str.contains("http") && str.contains("uic.edu")) {
			int index = str.indexOf("http");
			url = cleanupURL(str.substring(index));
			urls.add(url);
		} else if(str.contains("www") && str.contains("uic.edu")) {
			int index = str.indexOf("www");
			url = cleanupURL(str.substring(index));
			urls.add(url);
		}
		
		//get last part of url
		String lastPart = Util.getLastPartOfURL(url);
		if(lastPart.contains("?")) {
			lastPart = lastPart.substring(0, lastPart.indexOf('?'));
			if(lastPart.matches(".*(.jpg|.png|.gif|.bmp|.jpeg|.php|.css|.svg|.pdf|.ppt|.doc|.docx|.mp3|.mp4|.xml)$"))
				urls.remove(url);
		}
		
		if(lastPart.contains("#")) {
			lastPart = lastPart.substring(0, lastPart.indexOf('#'));
			if(lastPart.matches(".*(.jpg|.png|.gif|.bmp|.jpeg|.php|.css|.svg|.pdf|.ppt|.doc|.docx|.mp3|.mp4|.xml)$"))
				urls.remove(url);
		}
		
		
		//ignore formats
		if(url.matches(".*(.jpg|.png|.gif|.bmp|.jpeg|.php|.css|.svg|.pdf|.ppt|.doc|.docx|.mp3|.mp4|.xml)$"))
			urls.remove(url);
		
		//ignore non-UIC domain
		if(!Util.validDomainURLString(url, "uic.edu")) {
			urls.remove(url);
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

	public static List<String> getPageTitleAndExcerptInList(URL url) {
		List<String> res = new ArrayList<>();
		String title = "";
		String excerpt = "";
		try {
			String text = "";
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				text += inputLine;
			}
			
			if(!text.isEmpty()) {
				int start = text.indexOf("<title>");
				int endIndex = text.indexOf("</title>");
				if(start >= 0 && endIndex >= 0)
					title = text.substring(start + "<title>".length(), endIndex);
				if(title.isEmpty())
					title = url.toString();
			}
			
			res.add(title);
			res.add(excerpt);
			
			in.close();
		} catch(IOException e) {
			System.out.println("Error in URL" + url);
		}
		return res;
	}
	
	
	public static void updateDocVectLenMap() {
		for(String term : Globals.invertedIndex.keySet()) {
			double idf = Math.log(Globals.maxTfMap.size()/Globals.invertedIndex.get(term).size())/ Math.log(2);
			List<String> docs = Globals.invertedIndex.get(term);
			for(String doc : docs) {
				if(Globals.tfMap.get(doc) != null && Globals.tfMap.get(doc).get(term) != null 
						&& Globals.maxTfMap.get(doc) != null) {
					
					double normalizedTF = Globals.tfMap.get(doc).get(term) / Globals.maxTfMap.get(doc);
					double w = normalizedTF * idf;
					Globals.docVectorLengthMap.merge(doc, w*w, (a,b) -> a + b);
				}
				else
					Globals.docVectorLengthMap.merge(doc, 0.0, (a,b) -> a + b);
			}
		}
	}

	public static void loadDataFromFile() {
	    BufferedReader reader;
	   // StringBuilder sb = new StringBuilder();
	    int separatorCount = 0;
	    
		try {
			reader = new BufferedReader(new FileReader("invertedIndex.txt"));
			String line;
            while ((line = reader.readLine()) != null) {
            	if(line.startsWith("-----")) {
            		++separatorCount;
            		continue;
            	}
            	if(separatorCount == 0) {
            		//Inverted Index
            		String[] tokenSplitter = line.split(":");
            		String token = "";
            		if(!tokenSplitter[0].isEmpty())
            			token = tokenSplitter[0];
            		if(token.isEmpty())
            			continue;
            		int firstColonIndex = line.indexOf(":");
            		String docsStr = line.substring(firstColonIndex + 1);
            		String[] docSplitter = docsStr.split(", ");
            		List<String> docs = new ArrayList<>();
            		for(String d: docSplitter) {
            			docs.add(d);
            		}
            		Globals.invertedIndex.put(token, docs);
            	} else if(separatorCount == 1) {
            		//tfMap
            		String[] urlSplitter = line.split(", ");
            		String url = "";
            		int ind = 0;
            		if(!urlSplitter[0].isEmpty()) {
            			ind = urlSplitter[0].lastIndexOf(":");
            			url = urlSplitter[0].substring(0, ind);
            		}
            		if(url.isEmpty())
            			continue;
            		//int firstColonIndex = line.indexOf(":");
            		String mapStr = line.substring(ind + 1);
            		String[] termCountSplitter = mapStr.split(", ");
            		Map<String, Double> fMap = new HashMap<>();
            		for(String tc: termCountSplitter) {
            			String[] splitKeyVal = tc.split("=");
            			String token = splitKeyVal[0];
            			Double count = Double.parseDouble(splitKeyVal[1]);
            			fMap.put(token, count);
            		}
            		Globals.tfMap.put(url, fMap);
            	} else {
            		//maxTfMap
            		String[] urlSplitter = line.split(", ");
            		String url = "";
            		int ind = 0;
            		if(!urlSplitter[0].isEmpty()) {
            			ind = urlSplitter[0].lastIndexOf(":");
            			url = urlSplitter[0].substring(0, ind);
            		}
            		if(url.isEmpty())
            			continue;
            		//int firstColonIndex = line.indexOf(":");
            		String val = line.substring(ind + 1);
            		Double v = Double.parseDouble(val);
            		Globals.maxTfMap.put(url, v);
            	}
            }
			
			reader.close();
			System.out.println("Successfully loaded inverted index!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeInvertedIndexTfMapAndMaxFreqMapToFile() {
		String str = getMapsAsString();
	    BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("invertedIndex.txt"));
			writer.write(str); 
			writer.close();
			System.out.println("Written everything to file");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	} 

	private static String getMapsAsString() {
		StringBuilder sb = new StringBuilder();
		for(String term: Globals.invertedIndex.keySet()) {
			sb.append(term).append(":");
			List<String> docs = Globals.invertedIndex.get(term);
			for(String doc: docs)
				sb.append(doc).append(", ");
			sb.deleteCharAt(sb.length()-2);
			String s = sb.toString().trim();
			sb = new StringBuilder(s);
			sb.append("\n");
		}
		
		sb.append("-----\n");
		
		for(String term: Globals.tfMap.keySet()) {
			sb.append(term).append(":");
			Map<String, Double> fMap = Globals.tfMap.get(term);
			for(String t: fMap.keySet())
				sb.append(t).append("=").append(fMap.get(t)).append(", ");
			sb.deleteCharAt(sb.length()-2);
			String s = sb.toString().trim();
			sb = new StringBuilder(s);
			sb.append("\n");
		}
		
		sb.append("-----\n");

		for(String url: Globals.maxTfMap.keySet()) {
			sb.append(url).append(":");
			sb.append(Globals.maxTfMap.get(url));
			sb.append("\n");
		}

		return sb.toString();
	}
}
