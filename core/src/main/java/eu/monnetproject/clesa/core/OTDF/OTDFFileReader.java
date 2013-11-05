package eu.monnetproject.clesa.core.OTDF;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


/**
 *  
 * @author kasooja 
 */
public class OTDFFileReader {
	private Properties featureFile =  new Properties();	
	private Set<String> featureNames;
	private FileInputStream fileInputStream;
	private InputStreamReader inputStreamReader;
	private String filePath; 
	/*
	 * return true if read correctly
	 */
	public boolean readFile(OTDFFile file) {
		if(isOTDFFile(file.getAbsolutePath()))	
			return readFile(file.getAbsolutePath());
		return false;
	}

	private boolean isOTDFFile(String OTDFFilePath) {
		if(OTDFFilePath.endsWith(".OTDF"))
			return true;
		System.err.println("File is not OTDF file");		
		return false;	
	}


	/*
	 * return true if read correctly
	 */
	public boolean readFile(String OTDFFilePath) {
		if(isOTDFFile(OTDFFilePath)){
			try {
				filePath = OTDFFilePath;
				fileInputStream = new FileInputStream(OTDFFilePath);
				inputStreamReader = new InputStreamReader(fileInputStream, "UTF8");				
				featureFile.load(inputStreamReader);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
		return true;		
	}

	/*
	 * Gives the value for a specific feature 
	 */
	public String getFeatureValue(String featureName) {
		if(featureFile.containsKey(featureName)) 
			return featureFile.getProperty(featureName);		
		return null;
	}

	/*
	 * List all existing features
	 */
	public Set<String> getAllFeatureNames(){
		if(featureNames==null){ 
			featureNames = new HashSet<String>();
			for(Object featureName : featureFile.keySet()) 
				featureNames.add((String) featureName);			
		}
		return featureNames;
	}
	
	public Set<StringFeature> getAllStringFeatures(){
		Set<String> featureNames = getAllFeatureNames();
		Set<StringFeature> set = new HashSet<StringFeature>();
		for(String featureName : featureNames)
			set.add(new StringFeature(featureName, featureFile.getProperty(featureName)));
		return set;		
	}
	

	public void reset(){
		featureFile =  new Properties();	
		featureNames = null;
	}
	
	public OTDFFile getOTDFFile() {
		OTDFFile otdf = new OTDFFile(filePath);
		for(Object featureNameObject : featureFile.keySet()) {
			String featureName = (String) featureNameObject;
			String featureValue = featureFile.getProperty(featureName);
			otdf.addFeature(new StringFeature(featureName, featureValue));
		}
		return otdf;
	}
	
	public void close() {
		try {
			fileInputStream.close();
			inputStreamReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
