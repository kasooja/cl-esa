package eu.monnetproject.clesa.ds.clesa;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;

import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.core.tokenizer.Tokenizer;
import eu.monnetproject.clesa.core.utils.Pair;
import eu.monnetproject.clesa.core.utils.TextNormalizer;
import eu.monnetproject.clesa.lucene.basic.AnalyzerFactory;
import eu.monnetproject.clesa.lucene.basic.Searcher;
import gnu.trove.map.hash.TIntDoubleHashMap;


/**
 *  
 * @author kasooja 
 */

public class CLESATrove {
	public class Barrier {
		/** Number of objects being waited on */
		private int counter;
		/** Constructor for Barrier
		 * 
		 * @param n Number of objects to wait on
		 */
		public Barrier(int n) {
			counter = n;
		}
		/** Wait for objects to complete */
		public synchronized void barrierWait() {
			while(counter > 0) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}
		}
		/** Object just completed */
		public synchronized void barrierPost() {
			counter--;
			if(counter == 0) {
				notifyAll();
			}
		}
		public boolean isFinished() {
			return counter ==0 ? true : false;
		}
	}

	private static Properties config = new Properties();
	private String multiLingualIndexPathToRead; 
	private int lucHits = 1000;
//	private int noOfTopicsToBeCompared = 1000;
	private Searcher searcher;
	private Map<Language, Tokenizer> langTokenizerMap = new HashMap<Language, Tokenizer>();

	public CLESATrove() {
		loadConfig("load/eu.monnetproject.clesa.CLESA.properties");
		setConfig();
	}

	public CLESATrove(String configFilePath) {
		loadConfig(configFilePath);
		setConfig();
	}	


	public CLESATrove(Properties config) {
		CLESATrove.config = config;
		setConfig();
	}	

	private void setConfig(){
		multiLingualIndexPathToRead = config.getProperty("multiLingualIndexPathToRead");
	//	noOfTopicsToBeCompared = Integer.parseInt(config.getProperty("noOfTopicsToBeCompared"));
		lucHits = Integer.parseInt(config.getProperty("lucHits"));
		boolean onRAM = false;
		searcher = new Searcher(multiLingualIndexPathToRead, onRAM);
	}	


	private void loadConfig(String configFilePath){
		try {
			config.load(new FileInputStream(configFilePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}	

	public void close(){
		searcher.closeIndex();
	}

	private String normalize(String text, Language language){	
		text = text.toLowerCase();
		text = TextNormalizer.convertToUnicode(text);		
		if(!langTokenizerMap.containsKey(language))
			langTokenizerMap.put(language, TextNormalizer.getTokenizer(language));
		text = TextNormalizer.joinTokens(langTokenizerMap.get(language).tokenize(text));
		text = TextNormalizer.removePunctuations(text);
		text = text.replaceAll("\n", " ").trim();		
		text = TextNormalizer.deAccent(text);
		return text;
	}

	public double score(Pair<String, Language> pair1, Pair<String, Language> pair2) {
		String phrase1 = normalize(pair1.getFirst(), pair1.getSecond());
		String phrase2 = normalize(pair2.getFirst(), pair2.getSecond());
		pair1 = new Pair<String, Language>(phrase1, pair1.getSecond());
		pair2 = new Pair<String, Language>(phrase2, pair2.getSecond());		
		Map<String, TIntDoubleHashMap> vectorMap = new HashMap<String, TIntDoubleHashMap>();
		Barrier barrier = new Barrier(2);
		new TopicVectorThread("vector1", barrier, vectorMap, pair1, searcher).start();
		new TopicVectorThread("vector2", barrier, vectorMap, pair2, searcher).start();
		barrier.barrierWait();		
		TIntDoubleHashMap vector1 = vectorMap.get("vector1");
		TIntDoubleHashMap vector2 = vectorMap.get("vector2");				
		//VectorUtils<String> vecUtils = new VectorUtils<String>();
		//		vector1 = vecUtils.vectorWithPrincipalDimensions(vector1, noOfTopicsToBeCompared);
		//		vector2 = vecUtils.vectorWithPrincipalDimensions(vector2, noOfTopicsToBeCompared);		
		return cosineProduct(vector1, vector2);	
	}


	//	i = 0
	//			j = 0
	//			result = 0
	//			while i < length(vec1) && j < length(vec2):
	//			  if vec1[i].key == vec2[j].key:
	//			    result = result + vec1[i].value * vec2[j].value
	//			  else if vec1[i].key < vec2[j].key:
	//			    i = i + 1
	//			  else
	//			    j = j + 1


	public double cosineProduct(TIntDoubleHashMap map1, TIntDoubleHashMap map2){
		if(map1 == null || map2 == null)
			return 0.0;		
		double magnitudeVector1 = 0.0;
		double magnitudeVector2 = 0.0;
		double dotProduct = 0.0;					
		TIntDoubleHashMap iteratingMap = null;
		TIntDoubleHashMap secMap = null;		
		if(map1.size() >= map2.size()){
			iteratingMap = map1;
			secMap = map2;
		}
		else {
			iteratingMap = map2;
			secMap = map1;
		}		
		int[] secKeys = secMap.keys();
		int i = 0;		
		for(int unitVector : iteratingMap.keys()){
			Double score1 =  iteratingMap.get(unitVector);
			Double score2 = 0.0;
			if(secMap.containsKey(unitVector)){
				score2 = secMap.get(unitVector);
				dotProduct = dotProduct + score1*score2;
			}
			if(i < secKeys.length){
				score2 = secMap.get(secKeys[i++]);
				magnitudeVector2 = magnitudeVector2 + score2 * score2;			
			}		
			magnitudeVector1 = magnitudeVector1 + score1*score1;
		}

		if((magnitudeVector1 > 0.0)&&(magnitudeVector2 > 0.0))
			dotProduct = dotProduct /((Math.sqrt(magnitudeVector1) * Math.sqrt(magnitudeVector2)));
		else
			dotProduct = 0;

		return dotProduct;
	}


	//	public double scoreAgainstVector(Pair<String, Language> pair1, Vector<String> vector2) {
	//		String phrase1 = normalize(pair1.getFirst(), pair1.getSecond());
	//		pair1 = new Pair<String, Language>(phrase1, pair1.getSecond());
	//		Map<String, Vector<String>> vectorMap = new HashMap<String, Vector<String>>();
	//		Barrier barrier = new Barrier(1);
	//		new TopicVectorThread("vector1", barrier, vectorMap, pair1, searcher).start();
	//		barrier.barrierWait();		
	//		Vector<String> vector1 = vectorMap.get("vector1");
	//		VectorUtils<String> vecUtils = new VectorUtils<String>();
	//		//vector1 = vecUtils.vectorWithPrincipalDimensions(vector1, noOfTopicsToBeCompared);
	//		return vecUtils.cosineProduct(vector1, vector2);	
	//	}

	//	public Vector<String> getVector(Pair<String, Language> pair) {
	//		String phrase = normalize(pair.getFirst(), pair.getSecond());
	//		pair = new Pair<String, Language>(phrase, pair.getSecond());
	//			
	//		Map<String, Vector<String>> vectorMap = new HashMap<String, Vector<String>>();		
	//		Barrier barrier = new Barrier(1);
	//		new TopicVectorThread("vector", barrier, vectorMap, pair, searcher).start();
	//		barrier.barrierWait();		
	//		Vector<String> vector = vectorMap.get("vector");
	//		return vector;
	//	}

	public TIntDoubleHashMap getVector(Pair<String, Language> pair) {
		//THashMap<K, V>
		String phrase = normalize(pair.getFirst(), pair.getSecond());
		pair = new Pair<String, Language>(phrase, pair.getSecond());		
		Map<String, TIntDoubleHashMap> vectorMap = new HashMap<String, TIntDoubleHashMap>();		
		Barrier barrier = new Barrier(1);
		new TopicVectorThread("vector", barrier, vectorMap, pair, searcher).start();
		barrier.barrierWait();		
		return vectorMap.get("vector");
	}



	private class TopicVectorThread extends Thread {
		private Barrier barrier;
		//public Map<String, Vector<String>> vectorMap;
		public Map<String, TIntDoubleHashMap> vectorMapT;


		private Searcher searcher;
		private String threadName;
		private Pair<String, Language> pair;

		public TopicVectorThread(String threadName, Barrier barrier, Map<String, TIntDoubleHashMap> vectorMapT, Pair<String, Language> pair, Searcher searcher){
			super(threadName);		
			this.threadName = threadName;
			this.barrier = barrier;
			this.vectorMapT = vectorMapT;
			this.searcher = searcher;
			this.pair = pair;
		}

		public void run() {
			getVector();
			if(barrier!=null) 
				barrier.barrierPost();
		}

		public void getVector() {
			//Map<String, Double> uriWeightMap = new HashMap<String, Double>();
			//THashMap<Integer, Double> vecMapT = new THashMap<Integer, Double>();
			TIntDoubleHashMap vecMapT = new TIntDoubleHashMap();	

			String fieldName = pair.getSecond().getIso639_1() + "TopicContent";
			TopScoreDocCollector docsCollector = searcher.search(pair.getFirst(), lucHits, fieldName, AnalyzerFactory.getAnalyzer(pair.getSecond()));			
			if(docsCollector == null) {
				vectorMapT.put(threadName, null); 
				return;				
			}	
			ScoreDoc[] scoreDocs = docsCollector.topDocs().scoreDocs;
			double score = 0.0;
			for(int i=0;i<scoreDocs.length;++i) {
				int docID = scoreDocs[i].doc;
				score = scoreDocs[i].score;
				//org.apache.lucene.document.Document document = searcher.getDocumentWithDocID(docID);
				//String uri = document.get("URI_EN");
				vecMapT.put(docID, score);
				//uriWeightMap.put(uri, score);					
			}		
			vectorMapT.put(threadName, vecMapT);
		}
	}	

	public static void main(String[] args) {
		String text1 = args[0];
		String text2 = args[1];
		text1 = "computer";
		text2 = "researcher";
		Pair<String, Language> pair1 = new Pair<String, Language>(text1, Language.ENGLISH);
		Pair<String, Language> pair2 = new Pair<String, Language>(text2, Language.ENGLISH);
		CLESATrove clesa = new CLESATrove();
		double score = clesa.score(pair1, pair2);
		System.out.println(score);		
	}

}