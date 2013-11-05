package eu.monnetproject.clesa.core.OTDF;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;


/**
 *  
 * @author kasooja 
 */

public class OTDFXml {

	private String xmlFilePath;
	private PrintWriter outFile;
	private int id = 0;

	public OTDFXml(String xmlFilePath){
		this.xmlFilePath = xmlFilePath;		
		initiateXML();
	}

	private void initiateXML() {
		File file = new File(xmlFilePath);
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));			
			outFile = new PrintWriter(out, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		outFile.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //writes to file		
		outFile.println("<OTDFXml>\n"); 			
	}

	public void addOTDF(OTDFFile file) {
		String invertedComma = "\"";
		outFile.println("\t<OTDFFile \t"+ "id=" + invertedComma  + (++id ) + invertedComma + ">" + "\n"); 						
		List<StringFeature> features = file.getFeatures();
		for(StringFeature feature : features) 
			outFile.println("\t\t" + "<" + feature.getFeatureName() + ">" + feature.getFeatureValue() + "</" + feature.getFeatureName() + ">" + "\n");		
		outFile.println("\t</OTDFFile>" + "\n"); 					
	}

	public void close(){
		outFile.println("</OTDFXml>"); 	
		outFile.close();
	}	

	public static void main(String argv[]) {
		OTDFFile file = new OTDFFile("test.OTDF");
		file.addFeature(new StringFeature("one", "1.0"));
		file.addFeature(new StringFeature("two", "2.0"));
		file.addFeature(new StringFeature("three", "3.0"));
		file.addFeature(new StringFeature("four", "4.0"));
		OTDFXml xml = new OTDFXml("src/test/resources/test.xml");
		xml.addOTDF(file);
		xml.close();

	}
}