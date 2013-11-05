package eu.monnetproject.clesa.processor.wiki.abstracts;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import eu.monnetproject.clesa.core.lang.Language;



/**
 *  
 * @author kasooja 
 */

public class AbstractOTDFLucDocCreator {

	private Document abstractOTDFLucDoc = new Document();	

	public enum Fields {
		//AbstractTitle, URI_EN, AbstractContent, Language
		Topic, URI_EN, TopicContent, LanguageISOCode;	
	}

	public void addUriField(String uri) {
		Field uriField = new Field(Fields.URI_EN.toString(), uri, Field.Store.YES, Field.Index.NOT_ANALYZED);
		abstractOTDFLucDoc.add(uriField);			
	}

	public void addTopicContentField(String topicContent) {		
		Field topicContentField = new Field(Fields.TopicContent.toString(), topicContent, Field.Store.YES, Field.Index.ANALYZED);		
		abstractOTDFLucDoc.add(topicContentField);					
	}

	public void addTopicField(String topic) {		
		Field topicField = new Field(Fields.Topic.toString(), topic, Field.Store.YES, Field.Index.NOT_ANALYZED);		
		abstractOTDFLucDoc.add(topicField);				
	}
	
	public void addLanguageField(Language language) {
		Field languageField = new Field(Fields.LanguageISOCode.toString(), language.getIso639_1(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		abstractOTDFLucDoc.add(languageField);			
	}

	public Document getLucDoc() {		
		return abstractOTDFLucDoc;
	}
	
	public void reset(){
		abstractOTDFLucDoc = new Document();	
	}
}