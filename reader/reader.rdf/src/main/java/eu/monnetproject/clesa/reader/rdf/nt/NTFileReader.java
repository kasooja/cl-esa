package eu.monnetproject.clesa.reader.rdf.nt;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.monnetproject.clesa.core.ontology.RDF;
import eu.monnetproject.clesa.core.utils.BasicFileTools;



/**
 *  
 * @author kasooja 
 */

public class NTFileReader {

	private String ntFilePath = null;	
	private final String STRANGE_SEQ = "|||__|||";

	public NTFileReader(String ntFilePath){
		if(isNTFile(ntFilePath)) 		
			this.ntFilePath = ntFilePath;
	}

	private boolean isNTFile(String ntFilePath) {
		if(ntFilePath.endsWith(".nt"))
			return true;
		System.err.println("File not found or File is not .nt file");		
		return false;	
	}

	public Iterator<RDF> getRDFIter() throws IOException{
		return new NTFileRDFIter(BasicFileTools.getBufferedReaderFile(ntFilePath));
	}

	private class NTFileRDFIter implements Iterator<RDF> {

		private BufferedReader reader;
		private RDF rdf = null;

		public NTFileRDFIter(BufferedReader reader) throws IOException {
			this.reader = reader;
			toNext();
		}

		public boolean hasNext() {
			return rdf != null;
		}

		private void toNext() throws IOException {
			tryNext();
			if(rdf!=null)
				if(rdf.getSub()!=null && rdf.getProp()!=null && rdf.getObj()!=null){
					while(rdf.getSub().equals(STRANGE_SEQ) || rdf.getProp().equals(STRANGE_SEQ) || rdf.getObj().equals(STRANGE_SEQ)){
						tryNext();
						if(rdf==null)
							break;
					}
				} else 
					tryNext();
		}

		private void tryNext() throws IOException {	
			String line = reader.readLine();	
			if(line!=null){
				String htmlTagRegEx = "<[^<]+?>";
				String textExcludingHtmlTagRegEx = "(?<=^|>)[^><]+?(?=<|$)";
				Pattern tagPattern = Pattern.compile(htmlTagRegEx);
				Pattern textPattern = Pattern.compile(textExcludingHtmlTagRegEx);
				Matcher tagMatcher = tagPattern.matcher(line);
				List<String> subPropObj = new ArrayList<String>(); 
				String textMatched;
				while (tagMatcher.find()){
					textMatched = tagMatcher.group().trim();
					if(!textMatched.equals("") && !textMatched.equals(" ")) 
						subPropObj.add(textMatched);					
				}
				switch(subPropObj.size()) {
				case 2:
					Matcher textMatcher = textPattern.matcher(line);	
					while(textMatcher.find()) {
						textMatched = textMatcher.group().trim();
						if(!textMatched.equals("") && !textMatched.equals(" ") && textMatched != null) {
							subPropObj.add(textMatched);
							break;
						}
					}
				case 3:
					break;
				default:					
					subPropObj.add(STRANGE_SEQ);
					subPropObj.add(STRANGE_SEQ);
					subPropObj.add(STRANGE_SEQ);			 
				}	
				
				if(subPropObj.size()==2)
					subPropObj.add(STRANGE_SEQ);											
				
				for(int j=0;j<3; j++) {
					if(subPropObj.get(j)==null) 
						subPropObj.set(j, STRANGE_SEQ);				
				}
				
				
				rdf  = new RDF(subPropObj.get(0), subPropObj.get(1), subPropObj.get(2));					
			} else {
				rdf = null;
			}
		}	

		public RDF next() {				
			if(rdf != null) {
				RDF currentRDF = rdf ;
				try {
					toNext();				
				} catch(Exception x) {
					throw new RuntimeException(x);
				}
				return currentRDF;
			} else {
				throw new NoSuchElementException();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}