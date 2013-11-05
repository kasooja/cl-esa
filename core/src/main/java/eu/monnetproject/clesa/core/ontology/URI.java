package eu.monnetproject.clesa.core.ontology;

public class URI {
	private String uri = null;
	private String label = null;
	
	/**
	 * @param uri
	 * @param label
	 */
	public URI(String uri, String label){
		this.uri = uri;
		this.label = label;
	}
	
	public String  getValue(){
		return this.uri;
	}
	
	public String getLabel(){
		return this.label;

	}
}