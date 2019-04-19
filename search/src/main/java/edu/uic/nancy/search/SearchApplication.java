package edu.uic.nancy.search;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import edu.uic.nancy.crawler.Crawler;


@SpringBootApplication
public class SearchApplication {
	
	public static void main(String[] args) {
		Crawler crawler = new Crawler();
		SpringApplication.run(SearchApplication.class, args);
	}
	
}

