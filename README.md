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



To create your own multilingual indices easily with the DBpedia triples based wiki dumps, you can follow the below steps: 

Considering an example run over English and French multilingual index. Download the clesa project. 

1. Find the relevant DBpedia triples files, e.g. download short or extended abstracts NT (n-triples) files for both English and French from http://wiki.dbpedia.org/Downloads2015-04.

2. Goto cl-esa.processor.processor.wiki.abstracts. 

3. Change the config settings in AbstractsOTDFProcessor.properties file (here at https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/load/eu.monnetproject.clesa.processor.wiki.abstracts.AbstractsOTDFProcessor.properties), e.g. for French, DBpediaNTFilePathToRead=path to the downloaded french nt file, AbstractLanguageISOCode=fr, OTDFXmlToWrite=xml file path to be written. 
OTDF xml is an xml representation of the data into a format which the further code would understand. 

4. Then run AbstractsOTDFProcessor.java (https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/src/main/java/eu/monnetproject/clesa/processor/wiki/abstracts/AbstractsOTDFProcessor.java). This would create the OTDF xmls. Follow the steps 3 and 4 for both French and English. 

5. Change the config settings in AbstractsOTDFIndexer.properties file (here at https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/load/eu.monnetproject.clesa.processor.wiki.abstracts.AbstractsOTDFIndexer.properties), e.g. for French, indexDirPathToWrite=dir where the index would be written, LanguageISOCodeForIndexer=fr, OTDFXmlToRead=give the path of the french OTDF xml file created in the previous step. 

6. Then run AbstractsOTDFIndexer.java (https://github.com/kasooja/cl-esa/blob/master/processor/processor.wiki.abstracts/src/main/java/eu/monnetproject/clesa/processor/wiki/abstracts/AbstractsOTDFIndexer.java). This would create a monolingual index following the settings in the previous step. Follow the steps 5 and 6 for both French and English. 
So, now you have monolingual indices for both English and French. Next steps would be to utilize these to build multilingual indices. 






