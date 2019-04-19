package edu.uic.nancy.search.models;

public class SearchResult {

	private String title;
	private String excerpt;
	private String url;
	
	public SearchResult(String title, String excerpt, String url) {
		super();
		this.title = title;
		this.excerpt = excerpt;
		this.url = url;
	}

	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getExcerpt() {
		return excerpt;
	}
	public void setExcerpt(String excerpt) {
		this.excerpt = excerpt;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}
	
}
