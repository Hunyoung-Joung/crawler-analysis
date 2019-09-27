package com.young.homework;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jounghunyoung@gmail.com
 *
 */
public class MorphologicalAnalysis {
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(MorphologicalAnalysis.class.getSimpleName());

	public void analyzing() throws IOException {
    	BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(HtmlCrawlerCtrl.CRAWL_RESULT));
			String tokenStr = "";
			while ((tokenStr = reader.readLine()) != null) {
	
		        Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
		        KomoranResult analyzeResultList = komoran.analyze(tokenStr);
		        List<Token> tokenList = analyzeResultList.getTokenList();
		        Token previousToken = null;
		        for (int i=0; i<tokenList.size(); i++) {
		        	Token token = tokenList.get(i);
		        	if (i != 0) {
		        		previousToken = tokenList.get(i-1);
		        	}
		    
		        	if ((token.getPos().equals("JKS")) || (token.getPos().equals("JKO"))) {
		        		if (null == HtmlCrawlerCtrl.keyWordMap.get(previousToken.getMorph())) {
		        			HtmlCrawlerCtrl.keyWordMap.put(previousToken.getMorph(), 1);
		        		} else {
		        			HtmlCrawlerCtrl.keyWordMap.put(previousToken.getMorph(), HtmlCrawlerCtrl.keyWordMap.get(previousToken.getMorph())+1);
		        		}
		        		logger.info("## Keywork? "+previousToken.getMorph()+", "+HtmlCrawlerCtrl.keyWordMap.get(previousToken.getMorph()));
		        	}
		        }				
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.writeToFile();
    }
	
    /**
     * Make crawl result file
     * 
     * @throws IOException
     */
    private void writeToFile() throws IOException {
    	BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(HtmlCrawlerCtrl.KEYWORD_RESULT), "utf-8"));
    	
    	for (Iterator<String> iter = HtmlCrawlerCtrl.keyWordMap.keySet().iterator(); iter.hasNext();) {
    		String key = iter.next().toString();
    		int val = HtmlCrawlerCtrl.keyWordMap.get(key);
        	fileWriter.write(String.join("	,", key));
        	fileWriter.write("	,");
        	fileWriter.write(String.valueOf(val));
        	fileWriter.write("\n");
    	}
        fileWriter.flush();
        fileWriter.close();
    }
}