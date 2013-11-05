package eu.monnetproject.clesa.core.OTDF;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import eu.monnetproject.clesa.core.utils.BasicFileTools;



/**
 *  
 * @author kasooja 
 */

public class OTDFXmlReader {

	private String xmlPath;
	private BufferedReader reader;

	public OTDFXmlReader(String OTDFXmlFilePath) {
		this.xmlPath = OTDFXmlFilePath;
		this.reader = BasicFileTools.getBufferedReaderFile(xmlPath);
	}

	public Iterator<OTDFFile> getIterator() throws IOException {
		return new OTDFFileIterator(reader);
	}

	public boolean close() {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private class OTDFFileIterator implements Iterator<OTDFFile>{		

		private OTDFFile file = null;
		private BufferedReader reader;
	
		public OTDFFileIterator(BufferedReader reader) throws IOException{
			this.reader = reader;			
			toNext();
		}

		private void toNext() throws IOException{
				tryNext();
		}

		private void tryNext() throws IOException {
			String line = null;
			boolean breakOuterLoop = false;
			while((line=reader.readLine())!=null) {	
				if(breakOuterLoop) 
					break;							
				if(line.contains("<OTDFFile")){
					OTDFFile file = new OTDFFile();
					while((line=reader.readLine())!=null) {
						if(!line.contains("</OTDFFile>")){
							if(!line.replace("\\s+", "").trim().equals("")){
								line = line.replace("\\s+", "").trim();
								String featureName = line.substring(line.indexOf("<")+1, line.indexOf(">")).trim();					
								String featureValue = line.substring(line.indexOf(">")+1, line.lastIndexOf("<")).trim();
								file.addFeature(new StringFeature(featureName, featureValue));
							}
						} else {
							this.file = file;	
							breakOuterLoop = true;
							break;							
						}
					}
				}
			}
			if(!breakOuterLoop)
				this.file = null;
		}

		
		public boolean hasNext() {
			return file != null;
		}

		public OTDFFile next() {
			if(file != null) {
				OTDFFile currentFile = file;
				try {
					toNext();				
				} catch(Exception x) {
					throw new RuntimeException(x);
				}
				return currentFile;
			} else {
				throw new NoSuchElementException();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public static void main(String[] args) {
		OTDFXmlReader reader = new OTDFXmlReader("src/test/resources/test.xml");
		Iterator<OTDFFile> iterator = null;		
		try {
			iterator = reader.getIterator();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(iterator!=null) {
			while(iterator.hasNext()) {
				OTDFFile file = iterator.next();
				List<StringFeature> features = file.getFeatures();
				for(StringFeature feature : features) 
					System.out.println(feature.getFeatureName() + "  " + feature.getFeatureValue());

			}

		}

	}

}



