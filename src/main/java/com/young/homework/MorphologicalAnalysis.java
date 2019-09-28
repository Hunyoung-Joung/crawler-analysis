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
	
	public static void main(String args[]) {
        // Analyzing
        MorphologicalAnalysis morphologicalAnalysis = new MorphologicalAnalysis();
        try {
			morphologicalAnalysis.analyzing();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void analyzing() throws IOException {
    	BufferedReader reader;
    	BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./storage/noun_result.csv"), "utf-8"));
		try {
			reader = new BufferedReader(new FileReader(HtmlCrawlerCtrl.CRAWL_RESULT));
			String tokenStr = "";
			int cnt =0;
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
		        	
		        	if ((token.getPos().equals("NNG")) || (token.getPos().equals("NNP")) || (token.getPos().equals("NNB")) || (token.getPos().equals("NNB"))) {
		        		if (token.getMorph().length() > 1) {
			        		System.out.format("%s\n", token.getMorph());

			            	fileWriter.write(String.valueOf(token.getMorph()));
			            	fileWriter.write("\n");
		        		}
		        	}
		        	if ((token.getPos().equals("JKS")) || (token.getPos().equals("JKO"))) {
		        		if (previousToken.getMorph().length() > 1) {
			        		if (null == HtmlCrawlerCtrl.keyWordMap.get(previousToken.getMorph())) {
			        			HtmlCrawlerCtrl.keyWordMap.put(previousToken.getMorph(), 1);
			        		} else {
			        			HtmlCrawlerCtrl.keyWordMap.put(previousToken.getMorph(), HtmlCrawlerCtrl.keyWordMap.get(previousToken.getMorph())+1);
			        		}
		        		}
		        	}
		        }
		        cnt++;
//        		logger.info(cnt +" ## Keyword? "+previousToken.getMorph()+", "+HtmlCrawlerCtrl.keyWordMap.get(previousToken.getMorph()));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	        fileWriter.flush();
	        fileWriter.close();
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