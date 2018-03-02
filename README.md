CLESA (Cross Lingual Explicit Semantic Analysis)
=====
This is an IR based implementation of CLESA using Lucene library. 

It can be used for finding textual similarity across the language e.g. it can be applied for context disambiguation 
in machine translation (http://oa.upm.es/14474/1/07.UsingCLESAforOntologyTranslation.pdf).
It takes two texts (with lang codes) as input and returns a score between 0 and 1 showing the semantic relatedness between the texts.

This implementation requires a Lucene index having atleast following two fields signifying the topic and the topic content.
The actual field names can be configured via config file. 

Indexers for Wikipedia articles (using articles.xml) and Wikipedia abstracts (using DBpedia for abstracts) are also provided.
The field names used for these indices are:                                          
topic field name : "URI_EN" : e.g. http://dbpedia.org/resource/Asia                                         
topic content field name : Language Code + "TopicContent" : e.g. enTopicContent or esTopicContent as the field names 

Here, "URI_EN" signifies a unique topic or wikipedia concept by the English URI and "TopicContent" points to the 
content described in the Wikipedia article or abstract.

You can try running CLESA by using the sample indices (containing Wikipedia Abstracts for English, Spanish, German and Dutch), provided 
in the src/test/resources folder of ds.clesa module. There are some test codes in that module.


Considering an example run over English and French multilingual index. Download the clesa project. 
To create your own multilingual indices easily with the DBpedia triples based wiki dumps (with such DBpedia data, the project code would automatically utilize the English URIs as the pivot information to unify English and other language contents), you can follow the below steps: 

1. Find the relevant DBpedia triples files, e.g. download short or extended abstracts NT (n-triples) files for both English and French from http://wiki.dbpedia.org/Downloads2015-04.

2. Goto cl-esa.processor.processor.wiki.abstracts. 

3. Change the config settings in AbstractsOTDFProcessor.properties file (here at https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/load/eu.monnetproject.clesa.processor.wiki.abstracts.AbstractsOTDFProcessor.properties), e.g. for French, DBpediaNTFilePathToRead=path to the downloaded french nt file, AbstractLanguageISOCode=fr, OTDFXmlToWrite=xml file path to be written. 
OTDF xml is an xml representation of the data into a format which the further code would understand. 

4. Then run AbstractsOTDFProcessor.java (https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/src/main/java/eu/monnetproject/clesa/processor/wiki/abstracts/AbstractsOTDFProcessor.java). This would create the OTDF xmls. Follow the steps 3 and 4 for both French and English. 

5. Change the config settings in AbstractsOTDFIndexer.properties file (here at https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/load/eu.monnetproject.clesa.processor.wiki.abstracts.AbstractsOTDFIndexer.properties), e.g. for French, indexDirPathToWrite=dir where the index would be written, LanguageISOCodeForIndexer=fr, OTDFXmlToRead=give the path of the french OTDF xml file created in the previous step. 

6. Then run AbstractsOTDFIndexer.java (https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/src/main/java/eu/monnetproject/clesa/processor/wiki/abstracts/AbstractsOTDFIndexer.java). This would create a monolingual index following the settings in the previous step. Follow the steps 5 and 6 for both French and English. 
So, now you have monolingual indices for both English and French. Next steps would be to utilize these to build multilingual indices. 

7. Change the config settings in MultiLingualAbstractsOTDFProcessor.properties file (here at https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/load/eu.monnetproject.clesa.processor.wiki.abstracts.MultiLingualAbstractsOTDFProcessor.properties). 
Do it first for English as it would be used as the pivot e.g. for English, englishOTDFIndexDirPathToRead=give the index path created in the previous step, abstractLanguageISOCodeThisTime=en, multiLingualOTDFXmlToWrite=give a path of a new OTDF xml to be written. 
However, the next time when you update the settings for French, you would need to provide the values for these parameters as well: otherLanguageOTDFIndexDirPathToRead=path for the French index created in the previous step 6, and multiLingualOTDFXmlToRead=would be the multilingual OTDF file created in step 7 starting with English pivot and multiLingualOTDFXmlToWrite=would be the path to a new OTDF xml file. Essentially, you keep updating the multiLingualOTDFXmlToWrite and multiLingualOTDFXmlToRead every time you put a new language information in the OTDF. 

8. Then run MultiLingualAbstractsOTDFProcessor.java (https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/src/main/java/eu/monnetproject/clesa/processor/wiki/abstracts/MultiLingualAbstractsOTDFProcessor.java). This would create a multilingual OTDF xml file in a series of steps following the languages you want to index. Run steps 7 and 8 both for English (first, pivot lang) and French.

As you have now already created a multilingual xml file containing the wiki information in the last step, we can proceed with creating a multilingual index using this file. This file contains multiple documents, where every document contains a title, URI (English DBpedia URI as pivot), English content, French content (and other languages if followed in the previous steps). 

9. Before going ahead with creating the multilingual indices, we can put some constraints on the documents, e.g. how many documents we want in our index, or how many words a document (both English and French or any other language content) should contain. 
Change the config settings in MinNoOfWordsInAllFilter.properties file (here at https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/load/eu.monnetproject.clesa.processor.wiki.abstracts.MinNoOfWordsInAllFilter.properties). minNoOfWordsInAll= minimum no. of words a document to contain, multiLingualOTDFXmlToRead=multilingual OTDF xml file created in the previous step 8, multiLingualOTDFXmlToWrite=new filtered down multilingual OTDF file to be written, maxHowManyDocs=how many docs you want in the final index, what languages you want in the index, use semicolon to add more languages. 

10. Run MinNoOfWordsInAllFilter.java (here at https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/src/main/java/eu/monnetproject/clesa/processor/wiki/abstracts/MinNoOfWordsInAllFilter.java). This would create a filtered down multilingual OTDF xml file. 

Now go ahead and create the multilingual index using this OTDF xml file. 

11. Change the config settings in MultiLingualAbstractsOTDFIndexer.properties file (here at https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/load/eu.monnetproject.clesa.processor.wiki.abstracts.MultiLingualAbstractsOTDFIndexer.properties). 
indexDirPathToWrite = folder path to write the index in, OTDFXmlFileToRead=path to the filtered multilingual OTDF xml created in the previous step 11, languages=languages to index, use semicolon as separator. 


Thanks to Zansouyé https://github.com/zansouye01 ;) for making me add the above steps to create the multilingual index.





