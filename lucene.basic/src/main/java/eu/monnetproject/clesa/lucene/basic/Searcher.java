package eu.monnetproject.clesa.lucene.basic;


import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;


/**
 *  
 * @author kasooja 
 */

public class Searcher {
	private final static int maxClauseCount = 6144;
	private IndexReader reader;
	private IndexSearcher searcher;

	public Searcher(String indexPath){
		BooleanQuery.setMaxClauseCount(maxClauseCount);		
		setIndexReader(getIndex(indexPath));
		setIndexSearcher();		
	}

	public Searcher(String indexPath, boolean onRAM){
		BooleanQuery.setMaxClauseCount(maxClauseCount);
		setIndexReader(getIndex(indexPath, onRAM));
		setIndexSearcher();		
	}


	public Searcher(IndexReader reader){
		BooleanQuery.setMaxClauseCount(maxClauseCount);		
		setIndexReader(reader);
		setIndexSearcher();		
	}

	private void setIndexReader(IndexReader reader) {
		this.reader = reader;
	}

	private void setIndexReader(Directory indexDir) {
		try {
			this.reader = IndexReader.open(indexDir);
			indexDir.close();			
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private void setIndexSearcher() {
		this.searcher = new IndexSearcher(this.reader);			
	}

	public boolean closeIndex() {		
		try {
			searcher.close();
			reader.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private Directory getIndex(String indexPath) {
		Directory index = null;
		try {
			Directory dir = new SimpleFSDirectory(new File(indexPath + 
					System.getProperty("file.separator")));
			index = new RAMDirectory(dir);
			dir.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return index;
	}

	private Directory getIndex(String indexPath, boolean onRAM) {
		Directory index = null;
		try {
			Directory dir = new SimpleFSDirectory(new File(indexPath + 
					System.getProperty("file.separator")));
			if(onRAM){
				index = new RAMDirectory(dir);
				dir.close();			
				return index;
			}
			else {				
				return dir;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return index;
	}

	private TopScoreDocCollector search(Query query, int lucHits) {
		TopScoreDocCollector collector = TopScoreDocCollector.create(lucHits, true);
		try {
			searcher.search(query, collector);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return collector;				
	}

	public TopScoreDocCollector termQuerySearch(String queryString, String fieldName, int lucHits) {		
		TermQuery query = new TermQuery(new Term(fieldName, queryString));
		return search(query, lucHits);
	}

	private static void escapeStrings(String[] queryStrings) {
		for(int i=0; i<queryStrings.length; i++) {
			queryStrings[i] = QueryParser.escape(queryStrings[i]);
		}
	}

	public TopScoreDocCollector multiFieldTermSearch(String[] queryStrings, String[] fields, BooleanClause.Occur[] flags, int lucHits){
		BooleanQuery booleanQuery = new BooleanQuery();
		for(int i=0; i<queryStrings.length ; i++) {
			TermQuery query = new TermQuery(new Term(fields[i], queryStrings[i]));			
			booleanQuery.add(query, flags[i]);			
		}
		return search(booleanQuery, lucHits);
	}

	public TopScoreDocCollector multiFieldSearch(String[] queryStrings, String[] fields, BooleanClause.Occur[] flags, Analyzer analyzer, int lucHits){		
		escapeStrings(queryStrings);
		Query query = null;
		try {
			query = MultiFieldQueryParser.parse(Version.LUCENE_36, queryStrings, fields, flags, analyzer);			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return search(query, lucHits);
	}

	public Document getDocumentWithDocID(int docID) {
		try {
			return searcher.doc(docID);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public TopScoreDocCollector search(String queryString, int lucHits, String fieldName, Analyzer analyzer) {
		queryString = QueryParser.escape(queryString.trim());		
		if(queryString.equalsIgnoreCase("")) 
			return null;
		Query query = null;
		try {
			QueryParser queryParser = new QueryParser(Version.LUCENE_36, fieldName, analyzer);			
			query = queryParser.parse(queryString);
		} catch (ParseException e) {
			e.printStackTrace();
		}						
		return search(query, lucHits);	
	}

}
