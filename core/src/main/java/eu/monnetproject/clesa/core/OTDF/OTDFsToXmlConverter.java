package eu.monnetproject.clesa.core.OTDF;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


/**
 *  
 * @author kasooja 
 */

public class OTDFsToXmlConverter {
	
	private static Properties config = new Properties();
	private String xmlToWrite;
	private File OTDFsDirToRead;
	private OTDFXml xml;
	
	public OTDFsToXmlConverter(){
		loadConfig();
	}	
	
	private void loadConfig() {
		try {
			config.load(new FileInputStream("load/edu.our.core.OTDF.OTDFsToXmlConverter.properties"));
			OTDFsDirToRead = new File(config.getProperty("OTDFsDirToRead"));		
			xmlToWrite = config.getProperty("xmlToWrite");
			xml = new OTDFXml(xmlToWrite);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}	

	public void convert() {
		File[] files = OTDFsDirToRead.listFiles();
		OTDFFileReader reader = new OTDFFileReader();
		int i = 0;
		for(File file : files) {			
			boolean readFile = reader.readFile(file.getAbsolutePath());
			if(readFile) {
				System.out.println(++i);
				OTDFFile otdfFile = reader.getOTDFFile();
				xml.addOTDF(otdfFile);				
				reader.close();
				reader.reset();
			}			
		}
		System.out.println("Total Files " + files.length);
		System.out.println("Total OTDF Files Read " + i);		
		xml.close();		
	}

	public static void main(String[] args) {
		OTDFsToXmlConverter converter = new OTDFsToXmlConverter();
		converter.convert();		
	}

}
