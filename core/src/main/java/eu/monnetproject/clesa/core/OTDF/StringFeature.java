package eu.monnetproject.clesa.core.OTDF;


/**
 *  
 * @author kasooja 
 */

public class StringFeature {

	private String featureName;
	private String featureValue;
	
	public StringFeature(String featureName, String featureValue) {
		this.featureName = featureName;
		this.featureValue = featureValue;
	}
	
	public String getFeatureValue() {
		return featureValue;
	}
	
	public String getFeatureName(){
		return featureName;
	}	
		
}
