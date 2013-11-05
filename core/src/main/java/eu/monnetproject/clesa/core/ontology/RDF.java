package eu.monnetproject.clesa.core.ontology;



public class RDF {
	private String sub;
	private String prop;
	private String obj;
	
	public RDF(){
	}	
	
	public RDF(String sub, String prop, String obj) {		
		this.sub = sub;
		this.prop = prop;		
		this.obj = obj;
	}		

	public String getSub() {
		return sub;
	}

	public String getProp() {
		return prop;
	}
	
	public String getObj() {
		return obj;
	}

	public String toString(){
		return sub + " " +  prop + " " + obj;		
	}
	
}