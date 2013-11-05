package eu.monnetproject.clesa.core.OTDF;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class OTDFTest {

	public OTDFTest(){
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}	


	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}


	/**
	 * Test OTDF File Reading And Writing
	 */
	@Test
	public void testOTDFFileCreation() {
		String testResourcesPath = "src/test/resources";		
		
		OTDFFile file = new OTDFFile(testResourcesPath + File.separator + "sample" + ".OTDF");		
		file.addFeature(new StringFeature("feature1", "feature1Value"));
		file.addFeature(new StringFeature("feature2", "feature2Value"));
		assertEquals("File Name is different from what returned by OTDFFile", "sample.OTDF", file.getName());

		boolean fileWritten = OTDFFileWriter.writeFile(file);
		assertEquals("File not written properly", true, fileWritten);
		
		OTDFFileReader reader = new OTDFFileReader();
		boolean fileRead = reader.readFile(file);
		assertEquals("Error in reading file" , true, fileRead);
		
		assertEquals("Reader gives different feature values", "feature1Value", reader.getFeatureValue("feature1"));
		assertEquals("Reader gives different feature values", "feature2Value", reader.getFeatureValue("feature2")); 
			
		Set<String> allFeatureNames = reader.getAllFeatureNames();	
		assertEquals("Reader gives different feature names", true, allFeatureNames.contains("feature1") && allFeatureNames.contains("feature2"));		
	}


}
