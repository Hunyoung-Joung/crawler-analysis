package com.young.homework;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.uci.ics.crawler4j.crawler.Page; 
import edu.uci.ics.crawler4j.crawler.WebCrawler; 
import edu.uci.ics.crawler4j.url.WebURL; 
 
/**
 * 
 * @author jounghunyoung@gmail.com
 *
 */
public class HtmlCrawler extends WebCrawler {
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(HtmlCrawler.class.getSimpleName());
	// Ignored pattern
    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(
    		".*(\\.(css|js|bmp|gif|jpe?g"
    				+ "|png|tiff?|mid|mp2|mp3|mp4"
    				+ "|wav|avi|mov|mpeg|ram|m4v|pdf"
    				+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$"
    			);
    // Auto increase follow to lower url
    private final AtomicInteger numSeenImages;

    /**
     * Creates a new crawler instance.
     *
     * @param numSeenImages
     */
    public HtmlCrawler (AtomicInteger numSeenImages) {
    	logger.info("## Construct? ", "BEGIN");
        this.numSeenImages = numSeenImages;
    }


    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        // Ignore the patterns and non article
        if ((IMAGE_EXTENSIONS.matcher(href).matches()) || (!href.startsWith("http://www.hani.co.kr/arti/"))) {
            numSeenImages.incrementAndGet();
            return false;
        }
        // Only article
        return href.startsWith("http://www.hani.co.kr/arti/");
        
    }

    @Override
    public void visit(Page page) {

        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();

        logger.debug("## URL? "+ url);
        logger.debug("## Docid{}? "+docid);
        logger.debug("## Domain{}? "+domain);
        logger.debug("## Sub-domain{}? "+ subDomain);
        logger.debug("## Path{}? "+path);
        logger.debug("## Parent page{}? "+parentUrl);
        logger.debug("## Anchor text{}? "+ anchor);

        Header[] responseHeaders = page.getFetchResponseHeaders();
        if (responseHeaders != null) {
            logger.debug("## Response headers:");
            for (Header header : responseHeaders) {
                logger.debug("## \t{}: {}"+header.getName()+", "+ header.getValue());
            }
        }
        // Parse
        try {
			if (!this.getArticles(url).isEmpty()) {
		    	String articleTitle = this.getArticles(url).keySet().iterator().next();
		    	String articleContents = this.getArticles(url).values().iterator().next();
		    	String category = url.replaceAll("http://www.hani.co.kr/arti/", "").split("/")[0];
		    	
		    	HtmlCrawlerCtrl.articleList.addAll(Arrays.asList(Arrays.asList(category+"||"+url+"||"+articleTitle+"||"+articleContents)));
				logger.info("## Article size? "+ HtmlCrawlerCtrl.articleList.size());
			}
		} catch (IOException e) {
			logger.error("## ", e);
		}
        // Store
        try {
			this.writeToFile();
		} catch (IOException e) {
			logger.error("## ", e);
		} 
    }
    
    /**
     * Get title and contents
     * 
     * @param url
     * @return
     * @throws IOException
     */
    private Map<String, String> getArticles(String url) throws IOException {
    	Map<String, String> tempMap = new HashMap<String, String>();
        Document document = document = document = Jsoup.connect(url).get();
        if (!document.select("span").select("[class=\"title\"]").isEmpty()) {
            if (!document.select("div").select("[itemprop=\"articleBody\"]").isEmpty()) {
            	tempMap.put(document.select("span").select("[class=\"title\"]").text(), document.select("div").select("[itemprop=\"articleBody\"]").text());
            }
        }
        return tempMap;
    }
    
    /**
     * Make crawl result file
     * 
     * @throws IOException
     */
    private void writeToFile() throws IOException {
    	BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(HtmlCrawlerCtrl.CRAWL_RESULT), "utf-8"));

        HtmlCrawlerCtrl.articleList.forEach(innerList -> {
            try {
            	fileWriter.write(String.join("||", innerList));
            	fileWriter.write("\n");
            } catch (IOException e) {
            	logger.error("## ", e);
            }
        });
        fileWriter.flush();
        fileWriter.close();
    }
}