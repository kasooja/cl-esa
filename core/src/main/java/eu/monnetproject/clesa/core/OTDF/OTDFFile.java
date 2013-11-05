package eu.monnetproject.clesa.core.OTDF;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 *  
 * @author kasooja 
 */
public class OTDFFile {

	private List<StringFeature> features;	
	private String name;
	private String absoluteFilePath; 
	private Map<String, String> featureMap;

	public OTDFFile(String filePath){
		if(isOTDFFile(filePath)){ 
			this.absoluteFilePath = filePath;
			int lastIndexOfFileSeparator = this.absoluteFilePath.lastIndexOf(File.separator);
			this.name = absoluteFilePath.substring(lastIndexOfFileSeparator+1);					
		}
	}			

	public OTDFFile(){
	}			

	public void setPath(String filePath) {
		if(isOTDFFile(filePath)){ 
			this.absoluteFilePath = filePath;
			int lastIndexOfFileSeparator = this.absoluteFilePath.lastIndexOf(File.separator);
			this.name = absoluteFilePath.substring(lastIndexOfFileSeparator+1);					
		}
	}

	private boolean isOTDFFile(String OTDFFilePath) {
		if(OTDFFilePath.endsWith(".OTDF"))
			return true;
		System.err.println("File not found or File is not OTDF file");		
		return false;	
	}

	public String getFeatureValue(String featureName){
		return featureMap.get(featureName);
	}

	/*
	 * adds new features to the feature list
	 */
	public void addFeature(StringFeature feature) {		
		if(features==null || featureMap == null){ 
			features = new ArrayList<StringFeature>();
			featureMap = new HashMap<String, String>();
		}		
		features.add(feature);
		featureMap.put(feature.getFeatureName(), feature.getFeatureValue());
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getAbsolutePath() {
		return absoluteFilePath;
	}

	public List<StringFeature> getFeatures(){
		return features;
	}

	public void setFeatures(List<StringFeature> features){
		this.features = features;
	}

	public void reset() {
		features = null;	
		absoluteFilePath = null;
		name = null;
		featureMap = null;

	}	

}
