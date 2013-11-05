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




