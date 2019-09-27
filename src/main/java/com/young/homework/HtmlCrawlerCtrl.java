package com.young.homework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * 
 * @author jounghunyoung@gmail.com
 *
 */
public class HtmlCrawlerCtrl {
	// Article list
	public static List<List<String>> articleList = new ArrayList<List<String>>();
	// Keyword map
	public static Map<String, Integer> keyWordMap = new HashMap<String, Integer>();
	// Web crawl result
	public static final String CRAWL_RESULT = "./storage/crawl_result.txt";
	// Keyword analyzing result
	public static final String KEYWORD_RESULT = "./storage/keyword_result.csv";

    public static void main(String ... args) throws Exception {

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder("./storage/");
        config.setPolitenessDelay(2000);
        config.setMaxDepthOfCrawling(3);
        config.setMaxPagesToFetch(1000);
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(false);
        
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // Target URL
        controller.addSeed("http://www.hani.co.kr/arti/");
        // Worker thread
        int numberOfCrawlers = 50;
        // Auto increase follow to lower url
        AtomicInteger numSeenImages = new AtomicInteger();
        // The factory which creates instances of crawlers.
        CrawlController.WebCrawlerFactory<HtmlCrawler> factory = () -> new HtmlCrawler(numSeenImages);
        // Thread begin
        controller.start(factory, numberOfCrawlers);
        
        // Analyzing
        MorphologicalAnalysis morphologicalAnalysis = new MorphologicalAnalysis();
        try {
			morphologicalAnalysis.analyzing();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

}